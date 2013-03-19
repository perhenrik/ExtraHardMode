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

import me.ryanhamshire.ExtraHardMode.ExtraHardMode;
import me.ryanhamshire.ExtraHardMode.features.Explosions;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Creeper;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.ArrayList;

/**
 * Creates Explosions. The type determines the power, if there should be fire and the blockDmg. The size of the explosion
 * is determined by the y-level. There are basically 2 settings for every explosion, below and above the specified y-level.
 */
public class CreateExplosionTask implements Runnable
{
    /**
     * Plugin reference to get the server etc.
     */
    private ExtraHardMode plugin;
    /**
     * Location of explosion.
     */
    private Location location;
    /**
     * Type that holds information about size and things like blockDmg and Fire
     */
    private Explosions.Type type;
    /**
     * Instance of a creeper that should explode, only used for custom creeper explosions
     */
    private Creeper creeper;

    /**
     * Constructor.
     *
     * @param location - Location to make explosion occur.
     * @param type Type that determines size and possible blockdamage or fire of explosion.
     */
    public CreateExplosionTask(ExtraHardMode plugin, Location location, Explosions.Type type)
    {
        this.location = location;
        this.type = type;
        this.plugin = plugin;
    }

    /**
     * Constructor.
     *
     * @param location  - Location to make explosion occur.
     * @param type      - Type that determines size and possible blockdamage or fire of explosion.
     * @param creeper   - Reference to the creeper that just exploded
     */
    public CreateExplosionTask(ExtraHardMode plugin, Location location, Explosions.Type type, Creeper creeper)
    {
        this(plugin, location, type); //Call to standard constructor to save code
        this.creeper = creeper;
    }

    @Override
    public void run()
    {
        createExplosion(location, type);
    }

    /**
     * Creates a Explosion, can be different above/below a certain y-level
     */
    public void createExplosion(Location loc, Explosions.Type type)
    {
        if (validateLocationSafe(loc, type))
        {
            //Below Y - Level
            if (location.getY() < type.getYLevel())
            {
                location.getWorld().createExplosion(
                        location.getX(), location.getY(), location.getZ(),
                        type.getPowerBelowY(),
                        type.getIsFireBelowY(),
                        type.getAllowBlockDamageBelowY());
            }
            //Above Y - Level
            else if (location.getY() >= type.getYLevel())
            {
                location.getWorld().createExplosion(
                        location.getX(), location.getY(), location.getZ(),
                        type.getPowerAboveY(),
                        type.getIsFireAboveY(),
                        type.getAllowBlockDamageAboveY());
            }
        }
    }

    /**
     * Validate if the given Location is not protected by a protection plugin
     * @param loc The Location
     * @param type ExplosionType determining the size of the Explosion and size of the area to check
     */
    public boolean validateLocationSafe(Location loc, Explosions.Type type)
    {
        boolean isSafe = true;
        //Different ExplosionPowers below/above Y
        int boomSize = loc.getY() > type.getYLevel() ? type.getPowerAboveY() : type.getPowerBelowY();
        boomSize *= 2;
        ArrayList<Block> boundaries = getBlockList(loc, boomSize);

        switch (type)
        {
            case CREEPER: case CREEPER_CHARGED:
                if (creeper != null)
                {
                    EntityExplodeEvent suicide = new EntityExplodeEvent(creeper, loc, boundaries, 1);
                    plugin.getServer().getPluginManager().callEvent(suicide);
                    creeper.remove();
                    isSafe = !suicide.isCancelled();
                }
                break;
            default:
                //sssss
                break;
        }
        return isSafe;
    }

    /**
     * Check pillars in the 4 outer corners of a virtual cube around the explosion. If the protection plugin uses a cuboid
      selection system then there is no way that an explosion can reach into a protected area without atleast on corner touching it
     */
    private ArrayList<Block> getBlockList (Location loc, int boomSize)
    {
        //Doesn't aim to be accurate, just to prevent explosions on the edges of protected land
        ArrayList<Block> boundaries = new ArrayList<Block>();
        Location cubeLoc = loc.clone();

        for (int i = 0; i < 8; i++) //4 outer walls
        {
            //10(5*2) blocks per cornerPillar, no matter the size of the explosion. (x+n-1)/n == x/n but always rounds up
            for (int j = -boomSize; j < boomSize; j+=(boomSize+4)/5)
            {
                switch (i)
                {
                    case 0:
                        cubeLoc = new Location (loc.getWorld(), loc.getX() + boomSize, loc.getY() + j, loc.getZ() + boomSize);
                        break;
                    case 1:
                        cubeLoc = new Location (loc.getWorld(), loc.getX() - boomSize, loc.getY() + j, loc.getZ() + boomSize);
                        break;
                    case 2:
                        cubeLoc = new Location (loc.getWorld(), loc.getX() + boomSize, loc.getY() + j, loc.getZ() - boomSize);
                        break;
                    case 3:
                        cubeLoc = new Location (loc.getWorld(), loc.getX() - boomSize, loc.getY() + j, loc.getZ() - boomSize);
                        break;
                    case 4: //Locations in the middle inbetween the corners. Needed if explosion bigger than claim
                        cubeLoc = new Location (loc.getWorld(), loc.getX() - boomSize/2, loc.getY() + j, loc.getZ() - boomSize/2);
                        break;
                    case 5:
                        cubeLoc = new Location (loc.getWorld(), loc.getX() - boomSize/2, loc.getY() + j, loc.getZ() - boomSize/2);
                        break;
                    case 6:
                        cubeLoc = new Location (loc.getWorld(), loc.getX() - boomSize/2, loc.getY() + j, loc.getZ() - boomSize/2);
                        break;
                    case 7:
                        cubeLoc = new Location (loc.getWorld(), loc.getX() - boomSize/2, loc.getY() + j, loc.getZ() - boomSize/2);
                        break;
                }
                boundaries.add(cubeLoc.getBlock());
            }
        }
        return boundaries;
    }
}