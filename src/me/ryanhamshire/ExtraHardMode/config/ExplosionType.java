package me.ryanhamshire.ExtraHardMode.config;

import me.ryanhamshire.ExtraHardMode.service.ConfigNode;

/**
 * Holds all the information about Explosions
 */
public enum ExplosionType implements ConfigNode
{   /**yLevel, powerBelow, powerAbove, fireBelow, fireAbove, blockDamageBelow, blockDamageAbove**/

    /**
     * From BetterTnt: Stronger tnt
     */
    TNT
            (0, 6, 4, false, false, true, false),
    /**
     *
     */
    TNT_CREEPER
            (0, 4, 4, false, false, false, false),
    /**
     * When a magmacube explodes into blazeform
     */
    MAGMACUBE_FIRE
            (0, 2, 2, true, true, false, false),
    /**
     * Just more powerful creepers
     */
    CREEPER
            (0, 3, 3, false, false, false, false),
    /**
     * Even more powerful charged creepers
     */
    CREEPER_CHARGED
            (0, 4, 4, false, false, false, false),
    /**
     * GhastFireball, netherrack is very soft, so this makes a big hole
     */
    GHAST_FIREBALL
            (0, 4, 4, true, true, false, false),
    /**
     * When a blaze explodes in the overworld
     */
    BLAZE
            (0, 4, 4, true, true, false, false);

    private final int yLevel;
    private final int powerBelowY;
    private final int powerAboveY;
    private final boolean fireBelowY;
    private final boolean fireAboveY;
    private final boolean noBloDamageBelowY;
    private final boolean noBlockDamageAboveY;

    ExplosionType(int yLevel, int powerBelowY, int powerAboveY, boolean fireBelowY, boolean fireAboveY, boolean noBlockDamageBelowY, boolean noBlockDamageAboveY)
    {
        this.yLevel = yLevel;
        this.powerBelowY = powerBelowY;
        this.powerAboveY = powerAboveY;
        this.fireBelowY = fireBelowY;
        this.fireAboveY = fireAboveY;
        this.noBloDamageBelowY = noBlockDamageBelowY;
        this.noBlockDamageAboveY = noBlockDamageAboveY;
    }

    public int getYLevel()
    {
        return yLevel;
    }

    public int getPowerBelowY()
    {
        return powerBelowY;
    }

    public int getPowerAboveY()
    {
        return powerAboveY;
    }

    public boolean isFireBelowY()
    {
        return  fireBelowY;
    }

    public boolean isFireAboveY()
    {
        return fireAboveY;
    }

    public boolean isNoBlockDamageBelowY ()
    {
        return noBloDamageBelowY;
    }

    public boolean isNoBlockDamageAboveY()
    {
        return noBlockDamageAboveY;
    }

    @Override
    public String getPath()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public VarType getVarType()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Object getDefaultValue()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}