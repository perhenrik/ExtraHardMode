package me.ryanhamshire.ExtraHardMode.config.explosions;

import me.ryanhamshire.ExtraHardMode.service.ConfigNode;
import org.bukkit.Location;

/**
 * Holds all the information about Explosions, this class should be used to create Explosions and not to access
 * configvalues of explosions
 */
public enum ExplosionType implements ConfigNode
{   /**yLevel, powerBelow, powerAbove, fireBelow, fireAbove, allowBlockDamageBelow, allowBlockDamageAbove**/

    /**
     * From BetterTnt: Stronger tnt
     */
    TNT
            (60, 6, false, true, 4, false, false),
    /**
     * When a magmacube explodes into blazeform
     */
    MAGMACUBE_FIRE
            (0, 2, true, true, 2, true, true),
    /**
     * Just more powerful creepers
     */
    CREEPER
            (60, 3, false, true, 5, false, false),
    /**
     * Even more powerful charged creepers
     */
    CREEPER_CHARGED
            (60, 4, false, true, 6, false, false),
    /**
     * GhastFireball, netherrack is very soft, so this makes a big hole
     */
    GHAST_FIREBALL
            (60, 3, true, true, 3, true, true),
    /**
     * When a blaze explodes in the overworld
     */
    BLAZE
            (60, 4, true, true, 4, true, false);

    private int yLevel;
    private int powerBelowY;
    private int powerAboveY;
    private boolean fireBelowY;
    private boolean fireAboveY;
    private boolean allowBlockDamageBelowY;
    private boolean allowBlockDamageAboveY;

    ExplosionType(int yLevel, int powerBelowY, boolean fireBelowY,boolean allowBlockDamageBelowY,  int powerAboveY, boolean fireAboveY, boolean allowBlockDamageAboveY)
    {
        this.yLevel = yLevel;
        this.powerBelowY = powerBelowY;
        this.powerAboveY = powerAboveY;
        this.fireBelowY = fireBelowY;
        this.fireAboveY = fireAboveY;
        this.allowBlockDamageBelowY = allowBlockDamageBelowY;
        this.allowBlockDamageAboveY = allowBlockDamageAboveY;
    }

    /**
     * Creates the explosion with the given type
     */
    public void createExplosion(Location location)
    {
        double currentY = location.getY();
        if (currentY < yLevel)
        {
            location.getWorld().createExplosion(location.getX(), location.getY(), location.getZ(), powerBelowY, fireBelowY, allowBlockDamageBelowY);
        }
        else if (currentY >= yLevel)
        {
            location.getWorld().createExplosion(location.getX(), location.getY(), location.getZ(), powerAboveY, fireAboveY, allowBlockDamageAboveY);
        }
    }

    @Override
    public String getPath()
    {
        return null;
    }

    @Override
    public VarType getVarType()
    {
        return null;
    }

    @Override
    public Object getDefaultValue()
    {
        return null;
    }

    public int getYLevel()
    {
        return yLevel;
    }

    public void setyLevel(int yLevel)
    {
        this.yLevel = yLevel;
    }

    public int getPowerBelowY()
    {
        return powerBelowY;
    }

    public void setPowerBelowY(int powerBelowY)
    {
        this.powerBelowY = powerBelowY;
    }

    public int getPowerAboveY()
    {
        return powerAboveY;
    }

    public void setPowerAboveY(int powerAboveY)
    {
        this.powerAboveY = powerAboveY;
    }

    public boolean getIsFireBelowY()
    {
        return  fireBelowY;
    }

    public void setFireBelowY(boolean fireBelowY)
    {
        this.fireBelowY = fireBelowY;
    }

    public boolean getIsFireAboveY()
    {
        return fireAboveY;
    }

    public void setFireAboveY(boolean fireAboveY)
    {
        this.fireAboveY = fireAboveY;
    }

    public boolean getAllowBlockDamageBelowY()
    {
        return allowBlockDamageBelowY;
    }

    public void setAllowBlockDamageBelowY(boolean allowBlockDamageBelowY)
    {
        this.allowBlockDamageBelowY = allowBlockDamageBelowY;
    }

    public boolean getAllowBlockDamageAboveY()
    {
        return allowBlockDamageAboveY;
    }

    public void setAllowBlockDamageAboveY(boolean allowBlockDamageAboveY)
    {
        this.allowBlockDamageAboveY = allowBlockDamageAboveY;
    }
}