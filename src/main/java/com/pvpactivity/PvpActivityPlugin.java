package com.pvpactivity;

import com.google.inject.Provides;
import com.pvpactivity.model.CombatBracket;
import com.pvpactivity.model.HeartbeatRequest;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.UUID;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;

@PluginDescriptor(
    name = "PVP Activity",
    description = "Opt-in anonymous Wilderness activity finder for PKers",
    tags = {"pvp", "pking", "wilderness", "worlds"}
)
public class PvpActivityPlugin extends Plugin
{
    private static final long HEARTBEAT_INTERVAL_MS = 10_000L;

    @Inject
    private Client client;

    @Inject
    private ClientToolbar clientToolbar;

    @Inject
    private PvpActivityConfig config;

    @Inject
    private WildernessService wildernessService;

    @Inject
    private PvpActivityApiClient apiClient;

    private final String sessionId = UUID.randomUUID().toString();

    private PvpActivityPanel panel;
    private NavigationButton navButton;
    private long lastHeartbeatAt;
    private long lastActivityFetchAt;
    private volatile boolean serverConnected;

    @Override
    protected void startUp()
    {
        panel = new PvpActivityPanel();
        navButton = NavigationButton.builder()
            .tooltip("PVP Activity")
            .icon(createIcon())
            .priority(8)
            .panel(panel)
            .build();
        clientToolbar.addNavigation(navButton);
        lastHeartbeatAt = 0L;
        lastActivityFetchAt = 0L;
        refreshNow();
    }

    @Override
    protected void shutDown()
    {
        if (config.sharingEnabled())
        {
            apiClient.removeSession(sessionId);
        }

        clientToolbar.removeNavigation(navButton);
        panel = null;
        navButton = null;
        serverConnected = false;
    }

    @Subscribe
    public void onGameTick(GameTick event)
    {
        if (!config.sharingEnabled())
        {
            serverConnected = false;
            if (panel != null)
            {
                panel.updateLocalStatus(0, 0, false, false, false);
                panel.showServiceDisabled();
            }
            return;
        }

        if (!isLoggedIn())
        {
            serverConnected = false;
            if (panel != null)
            {
                panel.updateLocalStatus(0, 0, false, true, false);
                panel.showLoginRequired();
            }
            return;
        }

        final int world = client.getWorld();
        final int combatLevel = getCombatLevel();
        final boolean inWilderness = wildernessService.isInWilderness();
        final long now = System.currentTimeMillis();

        if (now - lastHeartbeatAt >= HEARTBEAT_INTERVAL_MS)
        {
            lastHeartbeatAt = now;
            sendHeartbeat(world, combatLevel, inWilderness, !serverConnected);
        }

        if (serverConnected && now - lastActivityFetchAt >= Math.max(1, config.refreshSeconds()) * 1_000L)
        {
            lastActivityFetchAt = now;
            fetchActivity(world, combatLevel, inWilderness);
        }

        if (panel != null)
        {
            panel.updateLocalStatus(world, combatLevel, inWilderness, true, serverConnected);
        }
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event)
    {
        if (event.getGameState() == GameState.LOGIN_SCREEN || event.getGameState() == GameState.HOPPING)
        {
            if (config.sharingEnabled())
            {
                apiClient.removeSession(sessionId);
            }
            serverConnected = false;
            lastHeartbeatAt = 0L;
            lastActivityFetchAt = 0L;

            if (panel != null && config.sharingEnabled())
            {
                panel.updateLocalStatus(0, 0, false, true, false);
                panel.showLoginRequired();
            }
        }
        else if (event.getGameState() == GameState.LOGGED_IN)
        {
            lastHeartbeatAt = 0L;
            lastActivityFetchAt = 0L;
            refreshNow();
        }
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event)
    {
        if (!PvpActivityConfig.GROUP.equals(event.getGroup()))
        {
            return;
        }

        if ("sharingEnabled".equals(event.getKey()) && !config.sharingEnabled())
        {
            apiClient.removeSession(sessionId);
            serverConnected = false;
        }

        lastHeartbeatAt = 0L;
        lastActivityFetchAt = 0L;
        refreshNow();
    }

    private void refreshNow()
    {
        if (panel == null)
        {
            return;
        }

        if (!config.sharingEnabled())
        {
            serverConnected = false;
            panel.updateLocalStatus(0, 0, false, false, false);
            panel.showServiceDisabled();
            return;
        }

        if (!isLoggedIn())
        {
            serverConnected = false;
            panel.updateLocalStatus(0, 0, false, true, false);
            panel.showLoginRequired();
            return;
        }

        final int world = client.getWorld();
        final int combatLevel = getCombatLevel();
        final boolean inWilderness = wildernessService.isInWilderness();

        panel.updateLocalStatus(world, combatLevel, inWilderness, true, false);
        panel.showConnecting();
        lastHeartbeatAt = System.currentTimeMillis();
        sendHeartbeat(world, combatLevel, inWilderness, true);
    }

    private void sendHeartbeat(int world, int combatLevel, boolean inWilderness, boolean fetchAfterSuccess)
    {
        HeartbeatRequest request = new HeartbeatRequest(
            sessionId,
            world,
            inWilderness,
            CombatBracket.fromLevel(combatLevel).getKey(),
            System.currentTimeMillis()
        );

        apiClient.sendHeartbeat(request, connected ->
        {
            serverConnected = connected;
            if (panel != null)
            {
                panel.updateLocalStatus(world, combatLevel, inWilderness, true, connected);
            }

            if (connected && fetchAfterSuccess)
            {
                lastActivityFetchAt = System.currentTimeMillis();
                fetchActivity(world, combatLevel, inWilderness);
            }
        });
    }

    private void fetchActivity(int world, int combatLevel, boolean inWilderness)
    {
        apiClient.fetchActivity(sessionId, worlds ->
        {
            serverConnected = true;
            if (panel != null)
            {
                panel.updateActivity(worlds);
                panel.updateLocalStatus(world, combatLevel, inWilderness, true, true);
            }
        }, () ->
        {
            serverConnected = false;
            if (panel != null)
            {
                panel.updateLocalStatus(world, combatLevel, inWilderness, true, false);
            }
        });
    }

    private boolean isLoggedIn()
    {
        return client.getGameState() == GameState.LOGGED_IN && client.getLocalPlayer() != null && client.getWorld() > 0;
    }

    private int getCombatLevel()
    {
        Player localPlayer = client.getLocalPlayer();
        return localPlayer == null ? 0 : localPlayer.getCombatLevel();
    }

    @Provides
    PvpActivityConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(PvpActivityConfig.class);
    }

    private BufferedImage createIcon()
    {
        BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        try
        {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(190, 45, 45));
            g.fillOval(2, 2, 12, 12);
            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(2f));
            g.drawLine(5, 8, 11, 8);
            g.drawLine(8, 5, 8, 11);
        }
        finally
        {
            g.dispose();
        }
        return image;
    }
}
