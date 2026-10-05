package com.pvpactivity.model;

import java.util.Collections;
import java.util.Map;

public class WorldActivity
{
    private final int world;
    private final int total;
    private final Map<String, Integer> brackets;

    public WorldActivity(int world, int total, Map<String, Integer> brackets)
    {
        this.world = world;
        this.total = total;
        this.brackets = brackets == null ? Collections.emptyMap() : brackets;
    }

    public int getWorld()
    {
        return world;
    }

    public int getTotal()
    {
        return total;
    }

    public Map<String, Integer> getBrackets()
    {
        return brackets;
    }
}
