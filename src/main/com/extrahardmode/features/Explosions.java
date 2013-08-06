/*
 * This file is part of
 * ExtraHardMode Server Plugin for Minecraft
 *
 * Copyright (C) 2012 Ryan Hamshire
 * Copyright (C) 2013 Diemex
 *
 * ExtraHardMode is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ExtraHardMode is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero Public License
 * along with ExtraHardMode.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.extrahardmode.features;


import com.extrahardmode.ExtraHardMode;
import com.extrahardmode.compatibility.CompatHandler;
import com.extrahardmode.config.ExplosionType;
import com.extrahardmode.config.RootConfig;
import com.extrahardmode.config.RootNode;
import com.extrahardmode.config.messages.MessageConfig;
import com.extrahardmode.module.BlockModule;
import com.extrahardmode.module.PlayerModule;
import com.extrahardmode.module.UtilityModule;
import com.extrahardmode.service.Feature;
import com.extrahardmode.service.ListenerModule;
import com.extrahardmode.task.CreateExplosionTask;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Fireball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Various changes to Explosions including:
 */
public class Explosions extends ListenerModule
{
    private final ExtraHardMode plugin;

    private final RootConfig CFG;

    private final MessageConfig messages;

    private final UtilityModule utils;

    private final PlayerModule playerModule;

    private final String tag = "extrahardmode.explosion.fallingblock";


    /**
     * Your constructor of choice
     */
    public Explosions(ExtraHardMode plugin)
    {
        super(plugin);
        this.plugin = plugin;
        CFG = plugin.getModuleForClass(RootConfig.class);
        messages = plugin.getModuleForClass(MessageConfig.class);
        utils = plugin.getModuleForClass(UtilityModule.class);

        playerModule = plugin.getModuleForClass(PlayerModule.class);
    }


    /**
     * Handles all of EHM's custom explosions, this includes bigger random tnt explosions , bigger ghast explosion , turn stone into cobble in hardened stone mode ,
     *
     * @param event - Event that occurred.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onExplosion(EntityExplodeEvent event)
    {
        World world = event.getLocation().getWorld();
        Entity entity = event.getEntity();

        final boolean customTntExplosion = CFG.getBoolean(RootNode.EXPLOSIONS_TNT_ENABLE, world.getName());
        final boolean customGhastExplosion = CFG.getBoolean(RootNode.EXPLOSIONS_GHASTS_ENABLE, world.getName());
        final boolean multipleExplosions = CFG.getBoolean(RootNode.BETTER_TNT, world.getName());
        final boolean turnStoneToCobble = CFG.getBoolean(RootNode.EXPLOSIONS_TURN_STONE_TO_COBLE, world.getName());
        //cancel explosion if no worldDamage should be done
        final boolean tntWorldDamage = event.getLocation().getBlockY() > CFG.getInt(RootNode.EXPLOSIONS_Y, world.getName())
                ? CFG.getBoolean(RootNode.EXPLOSIONS_TNT_ABOVE_WORLD_GRIEF, world.getName())
                : CFG.getBoolean(RootNode.EXPLOSIONS_TNT_BELOW_WORLD_GRIEF, world.getName());

        if (entity != null && (entity.getType() == EntityType.CREEPER || entity.getType() == EntityType.PRIMED_TNT))
            event.setYield(1); //so people have enough blocks to fill creeper holes and because TNT explodes multiple times

        // FEATURE: bigger TNT booms, all explosions have 100% block yield
        if (customTntExplosion)
        {
            if (entity != null && entity.getType() == EntityType.PRIMED_TNT)
            {
                // create more explosions nearby
                long serverTime = world.getFullTime();
                int random1 = (int) (serverTime + entity.getLocation().getBlockZ()) % 8;
                int random2 = (int) (serverTime + entity.getLocation().getBlockX()) % 8;

                Location[] locations = new Location[]
                        {
                                entity.getLocation().add(random1, 1, random2),
                                entity.getLocation().add(-random2, 0, random1 / 2),
                                entity.getLocation().add(-random1 / 2, -1, -random2),
                                entity.getLocation().add(random1 / 2, 0, -random2 / 2)
                        };

                final int explosionsNum = multipleExplosions ? locations.length : 1;

                for (int i = 0; i < explosionsNum; i++)
                {
                    CreateExplosionTask task = new CreateExplosionTask(plugin, locations[i], ExplosionType.TNT);
                    plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, task, 3L * (i + 1));
                }

                // FEATURE: in hardened stone mode, TNT only softens stone to cobble
                if (turnStoneToCobble)
                {
                    List<Block> blocks = event.blockList();
                    Iterator<Block> iter = blocks.iterator();
                    while (iter.hasNext())
                    {
                        Block block = iter.next();
                        if (block.getType() == Material.STONE)
                        {
                            block.setType(Material.COBBLESTONE);
                            iter.remove();
                        }
                    }
                }

                //FEATURE: World damage based on the y-coordinate
                if (!tntWorldDamage)
                    event.setCancelled(true);
            }
        }

        // FEATURE: more powerful ghast fireballs
        if (entity != null && entity instanceof Fireball && customGhastExplosion)
        {
            Fireball fireball = (Fireball) entity;
            if (fireball.getShooter() != null && fireball.getShooter().getType() == EntityType.GHAST)
            {
                event.setCancelled(true);
                // same as vanilla TNT, plus fire
                new CreateExplosionTask(plugin, entity.getLocation(), ExplosionType.GHAST_FIREBALL).run();
            }
        }
    }


    /**
     * Provide Compat for block protection plugins
     *
     * @param event
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAnyExplosion(EntityExplodeEvent event)
    {
        //Remove Blocks that are in protected area
        Iterator<Block> iter= event.blockList().iterator();
        while (iter.hasNext())
        {
            Block block = iter.next();
            if (CompatHandler.isExplosionProtected(block.getLocation()))
            {
                iter.remove();
                //Restore old block
                //block.setType(block.getLocation().getBlock().getType());
            }
        }
    }


    /**
     * Apply Physics after explosion
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPhysicsApply(final EntityExplodeEvent event)
    {
        String worldName = event.getLocation().getWorld().getName();
        if (CFG.getBoolean(RootNode.EXPLOSIONS_FYLING_BLOCKS_ENABLE, worldName))
        {
            final int flyPercentage = CFG.getInt(RootNode.EXPLOSIONS_FLYING_BLOCKS_PERCENTAGE, worldName);
            final double upVel = CFG.getDouble(RootNode.EXPLOSIONS_FLYING_BLOCKS_UP_VEL, worldName);
            final double spreadVel = CFG.getDouble(RootNode.EXPLOSIONS_FLYING_BLOCKS_SPREAD_VEL, worldName);

            //if (event.getEntity() instanceof TNTPrimed)
            {
                final List<FallingBlock> fallingBlockList = new ArrayList<FallingBlock>();
                for (Block block : event.blockList())
                {
                    if (block.getType().isSolid())
                    {
                        //Only a few of the blocks fly as an effect
                        if (plugin.random(flyPercentage))
                        //if (block.getDrops().size() > 0)
                        {
                            FallingBlock fall = block.getLocation().getWorld().spawnFallingBlock(block.getLocation(), block.getType(), block.getData());
                            fall.setMetadata(tag, new FixedMetadataValue(plugin, block.getLocation())); //decide on the distance if block should be placed
                            //fall.setMetadata("drops", new FixedMetadataValue(plugin, block.getDrops()));
                            fall.setDropItem(false);
                            UtilityModule.moveUp(fall, upVel);
                            //block.setType(Material.AIR);
                            fallingBlockList.add(fall);
                        }
                    }
                }

                plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable()
                {
                    @Override
                    public void run()
                    {
                        for (FallingBlock fall : fallingBlockList)
                        {
                            UtilityModule.moveAway(fall, event.getLocation(), spreadVel);
                            //fall.removeMetadata(tag, plugin);
                            //fall.setMetadata("tag2", new FixedMetadataValue(plugin, true));
                        }
                    }
                }, 2L);
            }
        }
    }


    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true) //so we are last and if a block protection plugin cancelled the event we know it
    public void blockLand(EntityChangeBlockEvent event)
    {
        final int distance = (int) Math.pow(CFG.getInt(RootNode.EXPLOSIONS_FLYING_BLOCKS_AUTOREMOVE_RADIUS, event.getBlock().getWorld().getName()), 2);
        if (event.getEntity() instanceof FallingBlock)
        {
            Block block = event.getBlock();
            FallingBlock fallBaby = (FallingBlock) event.getEntity();
            if (fallBaby.hasMetadata(tag))
            {
                Object obj = fallBaby.getMetadata(tag).size() > 0 ? fallBaby.getMetadata(tag).get(0).value() : null;
                if (obj instanceof Location)
                {
                    Location loc = (Location) obj;
                    //Compare the distance to the original explosion, dont place block if the block landed far away (dont make landscape ugly)
                    if (event.getBlock().getLocation().distanceSquared(loc) > distance)
                    {
                        event.setCancelled(true);
                        fallBaby.remove();
                    }
                    //If close place the block as if the player broke it first: stone -> cobble, gras -> dirt etc.
                    else
                    {
                        Material type = BlockModule.getDroppedMaterial(fallBaby.getMaterial());
                        if (type.isBlock())
                            block.setType(type);
                        else //if block doesnt drop something that can be placed again... thin glass, redstone ore
                            block.setType(Material.AIR);
                        event.setCancelled(true);
                    }
                }
            }
        }
    }
}