package com.extrahardmode.module.temporaryblock;


import org.bukkit.Location;

public class TemporaryBlock
{
    Location loc;
    boolean isBroken;
    Object[] data;


    public TemporaryBlock(Location loc, Object... data)
    {
        this.loc = loc;
        this.data = data;
    }


    public Location getLoc()
    {
        return loc;
    }


    public boolean isBroken()
    {
        return isBroken;
    }


    public Object[] getData()
    {
        return data;
    }
}
