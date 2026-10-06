package com.pvpactivity.model;

import java.util.Collections;
import java.util.List;

public class ActivityResponse
{
    private long generatedAt;
    private int activeSessions;
    private int activeWildernessSessions;
    private List<WorldActivity> worlds;

    public long getGeneratedAt()
    {
        return generatedAt;
    }

    public int getActiveSessions()
    {
        return activeSessions;
    }

    public int getActiveWildernessSessions()
    {
        return activeWildernessSessions;
    }

    public List<WorldActivity> getWorlds()
    {
        return worlds == null ? Collections.emptyList() : worlds;
    }
}
