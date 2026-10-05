package com.pvpactivity;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(PvpActivityConfig.GROUP)
public interface PvpActivityConfig extends Config
{
    String GROUP = "pvpactivity";

    @ConfigItem(
        keyName = "sharingEnabled",
        name = "Share PvP activity",
        description = "Opt in to the third-party PVP Activity service. When enabled, an anonymous session ID, world, Wilderness status and combat-level bracket are sent to the configured API. Your IP address is necessarily visible to the third-party server when connecting.",
        position = 0
    )
    default boolean sharingEnabled()
    {
        return false;
    }

    @ConfigItem(
        keyName = "apiUrl",
        name = "API URL",
        description = "PVP Activity aggregation API base URL",
        position = 1
    )
    default String apiUrl()
    {
        return "http://127.0.0.1:8080";
    }

    @ConfigItem(
        keyName = "refreshSeconds",
        name = "Refresh seconds",
        description = "How often to refresh world activity",
        position = 2
    )
    default int refreshSeconds()
    {
        return 10;
    }
}
