package com.extrahardmode.module.temporaryblock;


import org.bukkit.Location;

public class LiteLocation
{
    public int x, y, z;


    @Override
    public boolean equals(Object obj)
    {
        return obj instanceof LiteLocation &&
                ((LiteLocation) obj).x == x &&
                ((LiteLocation) obj).y == y &&
                ((LiteLocation) obj).z == z;
    }


    @Override
    public int hashCode()
    {
        //Just a quick hashcode
        return x & y & z;
    }


    public static LiteLocation fromLocation(Location location)
    {
        LiteLocation liteLocation = new LiteLocation();

        liteLocation.x = location.getBlockX();
        liteLocation.y = location.getBlockY();
        liteLocation.z = location.getBlockZ();

        return liteLocation;
    }
}
