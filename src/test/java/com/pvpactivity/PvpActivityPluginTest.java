package com.pvpactivity;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class PvpActivityPluginTest
{
    public static void main(String[] args) throws Exception
    {
        ExternalPluginManager.loadBuiltin(PvpActivityPlugin.class);
        RuneLite.main(args);
    }
}
