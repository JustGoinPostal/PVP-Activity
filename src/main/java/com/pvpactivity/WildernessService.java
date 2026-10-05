package com.pvpactivity;

import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.gameval.VarbitID;

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
        return client.getVarbitValue(VarbitID.INSIDE_WILDERNESS) == 1;
    }
}
