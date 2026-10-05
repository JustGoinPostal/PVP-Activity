package com.pvpactivity;

import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Varbits;

public class WildernessService
{
    private final Client client;

    @Inject
    WildernessService(Client client)
    {
        this.client = client;
    }

    public boolean isInWilderness()
    {
        // RuneLite exposes the Wilderness level varbit. A positive value means the
        // local player is currently in a Wilderness-enabled area.
        return client.getVarbitValue(Varbits.IN_WILDERNESS) == 1;
    }
}
