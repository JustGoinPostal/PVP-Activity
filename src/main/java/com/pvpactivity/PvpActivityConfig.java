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
        name = "Enable service & share activity",
        description = "Opt in to the third-party PVP Activity service. When enabled, the plugin connects to the configured API and sends an anonymous session ID, world, Wilderness status and combat-level bracket. Your IP address is necessarily visible to the third-party server while connected.",
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
        return "http://16.59.193.155:8080";
    }

    @ConfigItem(
        keyName = "refreshSeconds",
        name = "Refresh seconds",
        description = "How often to refresh world activity while the service is enabled",
        position = 2
    )
    default int refreshSeconds()
    {
        return 10;
    }
}
