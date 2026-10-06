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
    tags = {"pvp", "pking", "wilderness", "worlds"},
    warning = "This plugin connects to a third-party server not controlled or verified by RuneLite. If sharing is enabled, it sends an anonymous session ID, your current world, Wilderness status and combat-level bracket. Your IP address is visible to the server as part of the network connection."
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
                panel.setServerConnected(false);
                panel.setLocalStatus(client.getWorld(), getCombatLevel(), wildernessService.isInWilderness(), false);
            }
            return;
        }

        long now = System.currentTimeMillis();
        boolean inWilderness = wildernessService.isInWilderness();
        int combatLevel = getCombatLevel();

        if (now - lastHeartbeatAt >= HEARTBEAT_INTERVAL_MS)
        {
            lastHeartbeatAt = now;
            HeartbeatRequest request = new HeartbeatRequest(
                sessionId,
                client.getWorld(),
                inWilderness,
                CombatBracket.fromCombatLevel(combatLevel),
                now
            );

            apiClient.sendHeartbeat(request, connected ->
            {
                serverConnected = connected;
                if (panel != null)
                {
                    panel.setServerConnected(connected);
                }
            });
        }

        if (now - lastActivityFetchAt >= config.refreshSeconds() * 1_000L)
        {
            lastActivityFetchAt = now;
            apiClient.fetchActivity(activity ->
            {
                if (panel != null)
                {
                    panel.setActivity(activity);
                }
            }, connected ->
            {
                serverConnected = connected;
                if (panel != null)
                {
                    panel.setServerConnected(connected);
                }
            });
        }

        if (panel != null)
        {
            panel.setLocalStatus(client.getWorld(), combatLevel, inWilderness, true);
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
        }
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event)
    {
        if (!PvpActivityConfig.GROUP.equals(event.getGroup()))
        {
            return;
        }

        if ("sharingEnabled".equals(event.getKey()))
        {
            if (!config.sharingEnabled())
            {
                apiClient.removeSession(sessionId);
                serverConnected = false;
            }
            lastHeartbeatAt = 0L;
            lastActivityFetchAt = 0L;
            refreshNow();
        }
    }

    private void refreshNow()
    {
        if (panel == null)
        {
            return;
        }

        panel.setLocalStatus(client.getWorld(), getCombatLevel(), wildernessService.isInWilderness(), config.sharingEnabled());
        panel.setServerConnected(serverConnected);

        if (config.sharingEnabled())
        {
            apiClient.fetchActivity(panel::setActivity, connected ->
            {
                serverConnected = connected;
                if (panel != null)
                {
                    panel.setServerConnected(connected);
                }
            });
        }
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
