package com.pvpactivity.model;

public enum CombatBracket
{
    LEVEL_3_50("3-50", 3, 50),
    LEVEL_51_70("51-70", 51, 70),
    LEVEL_71_90("71-90", 71, 90),
    LEVEL_91_110("91-110", 91, 110),
    LEVEL_111_126("111-126", 111, 126),
    UNKNOWN("unknown", 0, 0);

    private final String key;
    private final int minimum;
    private final int maximum;

    CombatBracket(String key, int minimum, int maximum)
    {
        this.key = key;
        this.minimum = minimum;
        this.maximum = maximum;
    }

    public String getKey()
    {
        return key;
    }

    public static CombatBracket fromLevel(int level)
    {
        for (CombatBracket bracket : values())
        {
            if (level >= bracket.minimum && level <= bracket.maximum)
            {
                return bracket;
            }
        }
        return UNKNOWN;
    }
}
