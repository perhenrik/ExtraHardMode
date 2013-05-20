package com.extrahardmode.config;

/**
 * Holds all the properties for the custom explosions, handles the configuration aswell
 */
public enum ExplosionType
{ /**yLevel, powerBelow, powerAbove, fireBelow, fireAbove, allowBlockDamageBelow, allowBlockDamageAbove**/

    /**
     * From BetterTnt: Stronger tnt
     */
    TNT
            (6, false, true, 6, false, true),
    /**
     * When a magmacube explodes into blazeform
     */
    MAGMACUBE_FIRE
            (2, true, true, 2, true, true),
    /**
     * Just more powerful creepers
     */
    CREEPER
            (3, false, true, 3, false, true),
    /**
     * Even more powerful charged creepers
     */
    CREEPER_CHARGED
            (4, false, true, 4, false, true),
    /**
     * GhastFireball, netherrack is very soft, so this makes a big hole
     */
    GHAST_FIREBALL
            (3, true, true, 3, true, true),
    /**
     * When a blaze explodes in the overworld
     */
    OVERWORLD_BLAZE
            (4, true, true, 4, true, true),
    /**
     * Only visual Explosions, the Explosion effect without any damage
     */
    EFFECT
            (0, false, false, 0, false, false)
    ;

    private int powerB;
    private boolean fireB;
    private boolean allowBlockDamageB;

    private int powerA;
    private boolean fireA;
    private boolean allowBlockDamageA;

    ExplosionType(int powerA, boolean fireA, boolean allowBlockDamageA, int powerB, boolean fireB, boolean allowBlockDamageB)
    {
        this.powerA = powerA;
        this.fireA = fireA;
        this.allowBlockDamageA = allowBlockDamageA;

        this.powerB = powerB;
        this.fireB = fireB;
        this.allowBlockDamageB = allowBlockDamageB;
    }

    /**
     * Get the power
     * @return power of the explosions
     */
    public int getPowerA()
    {
        return powerA;
    }

    /**
     * Get if explosion should set fire
     * @return if fire should be set
     */
    public boolean isFireA()
    {
        return fireA;
    }

    /**
     * Get if the explosion should damage the world
     * @return if explosion should damage the world
     */
    public boolean allowBlockDmgA()
    {
        return allowBlockDamageA;
    }

    /**
     * Get the power
     * @return power of the explosions
     */
    public int getPowerB()
    {
        return powerB;
    }

    /**
     * Get if explosion should set fire
     * @return if fire should be set
     */
    public boolean isFireB()
    {
        return fireB;
    }

    /**
     * Get if the explosion should damage the world
     * @return if explosion should damage the world
     */
    public boolean allowBlockDmgB()
    {
        return allowBlockDamageB;
    }
}