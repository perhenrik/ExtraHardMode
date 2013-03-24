package me.ryanhamshire.ExtraHardMode.config;

/**
 * Holds all the information about Explosions, like size , optional fire and blockdamage
 */
public enum ExplosionType
{ /**yLevel, powerBelow, powerAbove, fireBelow, fireAbove, allowBlockDamageBelow, allowBlockDamageAbove**/

    /**
     * From BetterTnt: Stronger tnt
     */
    TNT
            (6, false, true),
    /**
     * When a magmacube explodes into blazeform
     */
    MAGMACUBE_FIRE
            (2, true, true),
    /**
     * Just more powerful creepers
     */
    CREEPER
            (3, false, true),
    /**
     * Even more powerful charged creepers
     */
    CREEPER_CHARGED
            (4, false, true),
    /**
     * GhastFireball, netherrack is very soft, so this makes a big hole
     */
    GHAST_FIREBALL
            (3, true, true),
    /**
     * When a blaze explodes in the overworld
     */
    BLAZE
            (4, true, true),
    /**
     * Only visual Explosions, the Explosion effect without any damage
     */
    EFFECT
            (0, false, false)
    ;

    private int power;
    private boolean fire;
    private boolean allowBlockDamage;

    ExplosionType(int power, boolean fire, boolean allowBlockDamage)
    {
        this.power = power;
        this.fire = fire;
        this.allowBlockDamage = allowBlockDamage;
    }

    public int getPower()
    {
        return power;
    }

    public boolean isFire()
    {
        return fire;
    }

    public boolean allowBlockDmg()
    {
        return allowBlockDamage;
    }
}