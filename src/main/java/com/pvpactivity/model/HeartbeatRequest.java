package com.pvpactivity.model;

public class HeartbeatRequest
{
    private final String sessionId;
    private final int world;
    private final boolean inWilderness;
    private final String combatBracket;
    private final long timestamp;

    public HeartbeatRequest(String sessionId, int world, boolean inWilderness, String combatBracket, long timestamp)
    {
        this.sessionId = sessionId;
        this.world = world;
        this.inWilderness = inWilderness;
        this.combatBracket = combatBracket;
        this.timestamp = timestamp;
    }

    public String getSessionId()
    {
        return sessionId;
    }

    public int getWorld()
    {
        return world;
    }

    public boolean isInWilderness()
    {
        return inWilderness;
    }

    public String getCombatBracket()
    {
        return combatBracket;
    }

    public long getTimestamp()
    {
        return timestamp;
    }
}
