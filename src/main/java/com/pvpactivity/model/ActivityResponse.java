package com.pvpactivity.model;

import java.util.Collections;
import java.util.List;

public class ActivityResponse
{
    private List<WorldActivity> worlds;

    public List<WorldActivity> getWorlds()
    {
        return worlds == null ? Collections.emptyList() : worlds;
    }
}
