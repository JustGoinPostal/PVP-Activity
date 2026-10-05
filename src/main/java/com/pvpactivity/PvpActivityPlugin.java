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

        if (navButton != null)
        {
            clientToolbar.removeNavigation(navButton);
        }

        panel = null;
        navButton = null;
        serverConnected = false;
    }

    @Subscribe
    public void onGameTick(GameTick event)
    {
        refreshNow();
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event)
    {
        if (event.getGameState() != GameState.LOGGED_IN)
        {
            if (config.sharingEnabled())
            {
                apiClient.removeSession(sessionId);
            }
            serverConnected = false;
            updatePanel(0, 0, false);
        }
        else
        {
            lastHeartbeatAt = 0L;
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
            if (panel != null)
            {
                panel.showServiceDisabled();
            }
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

        if (client.getGameState() != GameState.LOGGED_IN)
        {
            updatePanel(0, 0, false);
            return;
        }

        Player localPlayer = client.getLocalPlayer();
        if (localPlayer == null)
        {
            updatePanel(client.getWorld(), 0, false);
            return;
        }

        final int world = client.getWorld();
        final int combat = localPlayer.getCombatLevel();
        final boolean inWilderness = wildernessService.isInWilderness();
        updatePanel(world, combat, inWilderness);

        if (!config.sharingEnabled())
        {
            serverConnected = false;
            panel.showServiceDisabled();
            updatePanel(world, combat, inWilderness);
            return;
        }

        long now = System.currentTimeMillis();

        if (now - lastHeartbeatAt >= HEARTBEAT_INTERVAL_MS)
        {
            lastHeartbeatAt = now;
            HeartbeatRequest heartbeat = new HeartbeatRequest(
                sessionId,
                world,
                inWilderness,
                CombatBracket.fromLevel(combat).getKey(),
                now
            );
            apiClient.sendHeartbeat(heartbeat, connected ->
            {
                serverConnected = connected;
                updatePanel(world, combat, inWilderness);
            });
        }

        long refreshMs = Math.max(3, Math.min(60, config.refreshSeconds())) * 1000L;
        if (now - lastActivityFetchAt >= refreshMs)
        {
            lastActivityFetchAt = now;
            apiClient.fetchActivity(
                worlds ->
                {
                    serverConnected = true;
                    if (panel != null)
                    {
                        panel.updateActivity(worlds);
                    }
                    updatePanel(world, combat, inWilderness);
                },
                () ->
                {
                    serverConnected = false;
                    updatePanel(world, combat, inWilderness);
                }
            );
        }
    }

    private void updatePanel(int world, int combat, boolean inWilderness)
    {
        PvpActivityPanel currentPanel = panel;
        if (currentPanel != null)
        {
            currentPanel.updateLocalStatus(world, combat, inWilderness, config.sharingEnabled(), serverConnected);
        }
    }

    private static BufferedImage createIcon()
    {
        BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        try
        {
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setColor(Color.WHITE);
            graphics.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            graphics.drawLine(3, 3, 13, 13);
            graphics.drawLine(13, 3, 3, 13);
            graphics.drawLine(2, 5, 5, 2);
            graphics.drawLine(11, 2, 14, 5);
        }
        finally
        {
            graphics.dispose();
        }
        return image;
    }

    @Provides
    PvpActivityConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(PvpActivityConfig.class);
    }
}
