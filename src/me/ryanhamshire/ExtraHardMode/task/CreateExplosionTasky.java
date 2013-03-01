/*
    ExtraHardMode Server Plugin for Minecraft
    Copyright (C) 2012 Ryan Hamshire

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


package me.ryanhamshire.ExtraHardMode.task;

import me.ryanhamshire.ExtraHardMode.config.explosions.ExplosionType;
import org.bukkit.Location;

/**
 * Creates Explosions. The type determines the power, if there should be fire and the blockDmg. The size of the explosion
 * is determined by the y-level. There are basically 2 settings for every explosion, below and above the specified y-level.
 */
public class CreateExplosionTasky implements Runnable
{
    /**
     * Location of explosion.
     */
    private Location location;
    /**
     * Type that holds information about size and things like blockDmg and Fire
     */
    private ExplosionType type;

    /**
     * Constructor.
     *
     * @param location - Location to make explosion occur.
     * @param type Type that determines size and possible blockdamage or fire of explosion.
     */
    public CreateExplosionTasky(Location location, ExplosionType type)
    {
        this.location = location;
        this.type = type;
    }

    @Override
    public void run()
    {
        double currentY = location.getY();

        final int yLevel = type.getYLevel();
        final int powerBelowY = type.getPowerBelowY();
        final int powerAboveY = type.getPowerAboveY();
        final boolean fireBelowY = type.getIsFireBelowY();
        final boolean fireAboveY = type.getIsFireAboveY();
        final boolean allowBlockDamageBelowY = type.getAllowBlockDamageBelowY();
        final boolean allowBlockDamageAboveY = type.getAllowBlockDamageAboveY();
        //TODO FINALIZE
        //Below Y
        if (currentY < yLevel)
        {
            location.getWorld().createExplosion(location.getX(), location.getY(), location.getZ(), powerBelowY, fireBelowY, allowBlockDamageBelowY);
        }
        else if (currentY >= yLevel)
        {
            location.getWorld().createExplosion(location.getX(), location.getY(), location.getZ(), powerAboveY, fireAboveY, allowBlockDamageAboveY);
        }

    }
}
