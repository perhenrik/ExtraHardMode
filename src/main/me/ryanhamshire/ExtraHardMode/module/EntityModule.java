/*
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
package me.ryanhamshire.ExtraHardMode.module;

import me.ryanhamshire.ExtraHardMode.ExtraHardMode;
import me.ryanhamshire.ExtraHardMode.config.RootNode;
import me.ryanhamshire.ExtraHardMode.service.EHMModule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.metadata.FixedMetadataValue;

/**
 * Module that contains logic dealing with entities.
 */
public class EntityModule extends EHMModule
{

    /**
     * Constructor.
     *
     * @param plugin - plugin instance.
     */
    public EntityModule(ExtraHardMode plugin)
    {
        super(plugin);
    }

    /**
     * Marks an entity so that the plugin can remember not to drop loot or
     * experience if it's killed.
     *
     * @param entity - Entity to modify.
     */
    public void markLootLess(LivingEntity entity)
    {
        entity.setMetadata("extrahard_environmentalDamage", new FixedMetadataValue(plugin, entity.getMaxHealth()));
    }

    /**
     * Tracks total environmental damage done to an entity
     *
     * @param entity - Entity to check.
     * @param damage - Amount of damage.
     */
    public void addEnvironmentalDamage(LivingEntity entity, int damage)
    {
        if (!entity.hasMetadata("extrahard_environmentalDamage"))
        {
            entity.setMetadata("extrahard_environmentalDamage", new FixedMetadataValue(plugin, damage));
        }
        else
        {
            int currentTotalDamage = entity.getMetadata("extrahard_environmentalDamage").get(0).asInt();
            entity.setMetadata("extrahard_environmentalDamage", new FixedMetadataValue(plugin, currentTotalDamage + damage));
        }
    }

    /**
     * Checks whether an entity should drop items when it dies
     *
     * @param entity - Entity to check.
     * @return True if the entity is lootable, else false.
     */
    public boolean isLootLess(LivingEntity entity)
    {
        if (entity instanceof Creature && entity.hasMetadata("extrahard_environmentalDamage"))
        {
            int totalDamage = entity.getMetadata("extrahard_environmentalDamage").get(0).asInt();
            // wither is exempt. he can't be farmed because creating him requires combining not-farmable components
            return !(entity instanceof Wither) && (totalDamage > entity.getMaxHealth() / 2);
        }

        return false;
    }

    /**
     * Clears any webbing which may be trapping this entity (assumes
     * two-block-tall entity)
     *
     * @param entity - Entity to help.
     */
    public void clearWebbing(Entity entity)
    {
        Block feetBlock = entity.getLocation().getBlock();
        Block headBlock = feetBlock.getRelative(BlockFace.UP);

        Block[] blocks = {feetBlock, headBlock};
        for (Block block : blocks)
        {
            if (block.getType() == Material.WEB)
            {
                block.setType(Material.AIR);
            }
        }
    }

    /**
     * Flag an entity to be ignored in further processing. E.g if an event could be called multiple times
     * @param entity
     */
    public void flagIgnore(Entity entity)
    {
        if (entity instanceof LivingEntity)
        {
            entity.setMetadata("extrahardmode.ignore.me", new FixedMetadataValue(plugin, true));
        }
    }

    /**
     * Check if an entity has been flagged to be ignored
     * @param entity
     * @return
     */
    public boolean hasFlagIgnore(Entity entity)
    {
        if (entity instanceof LivingEntity)
        {
            if (entity.hasMetadata("extrahardmode.ignore.me"))
            {
                return true;
            }
        }
        return false;
    }

    //TODO config block iron farms
    /**
     * Is the Monster farmable cattle, which drops something on death?
     */
    public boolean isCattle (Entity entity)
    {
        return     entity instanceof Cow
                || entity instanceof Chicken
                || entity instanceof Pig;
    }

    /**
     * Simple check if there is enough space for a monster to spawn
     * @param loc
     * @return
     */
    public boolean simpleIsLocSafeSpawn(Location loc)
    {
        //quickly check if 2 blocks above this is clear
        Block oneAbove = loc.getBlock();
        Block twoAbove = oneAbove.getRelative(BlockFace.UP, 1);
        return oneAbove.getType().equals(Material.AIR) && twoAbove.getType().equals(Material.AIR);
    }

    /**
     * Checks if Location is safe, if in air will return the a valid Block to spawn on or null
     * @return valid Block or null if no valid Block
     */
    public Location isLocSafeSpawn(Location location)
    {
        Block playerBlock = location.getBlock();

        // the playerBlock should always be air, but if the player stands on a slab he actually is in the slab, checking a few blocks under because player could have jumped etc..
        if (playerBlock.getType().equals(Material.AIR))
        {
            for (int i = 0; i <= 3; i++)
            {
                playerBlock = location.getBlock().getRelative(BlockFace.DOWN, 1);

                if (playerBlock.getType().equals(Material.AIR))
                {
                    location.subtract(0, 1, 0);
                    playerBlock = location.getBlock();
                    // the playerBlock is now the block where the monster
                    // should spawn on, next up: verify block
                }
                else
                {
                    break;
                }
            }
        }
        // no spawning on steps, stairs and transparent blocks
        if (playerBlock.getType().name().endsWith("STEP") || playerBlock.getType().name().endsWith("STAIRS")
                || playerBlock.getType().isTransparent() || !playerBlock.getType().isOccluding() || playerBlock.getType().equals(Material.AIR))
        {
            // don't spawn here
            return null;
        }

        return location;
    }

    @Override
    public void starting()
    {
    }

    @Override
    public void closing()
    {
    }

}
