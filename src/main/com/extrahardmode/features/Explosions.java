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
import com.extrahardmode.config.ExplosionType;
import com.extrahardmode.config.RootConfig;
import com.extrahardmode.config.RootNode;
import com.extrahardmode.config.messages.MessageConfig;
import com.extrahardmode.module.PlayerModule;
import com.extrahardmode.module.UtilityModule;
import com.extrahardmode.service.ListenerModule;
import com.extrahardmode.task.CreateExplosionTask;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityExplodeEvent;

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
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onExplosion(EntityExplodeEvent event)
    {
        World world = event.getLocation().getWorld();
        Entity entity = event.getEntity();

        final boolean customTntExplosion = CFG.getBoolean(RootNode.EXPLOSIONS_TNT_ENABLE, world.getName());
        final boolean customGhastExplosion = CFG.getBoolean(RootNode.EXPLOSIONS_GHASTS_ENABLE, world.getName());
        final boolean multipleExplosions = CFG.getBoolean(RootNode.BETTER_TNT, world.getName());
        final boolean turnStoneToCobble = CFG.getBoolean(RootNode.EXPLOSIONS_TURN_STONE_TO_COBLE, world.getName());

        // FEATURE: bigger TNT booms, all explosions have 100% block yield
        if (customTntExplosion)
        {
            if (entity != null && (entity.getType() == EntityType.CREEPER || entity.getType() == EntityType.PRIMED_TNT))
                event.setYield(1); //so people have enough blocks to fill creeper holes and because TNT explodes multiple times

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
            }
        }

        // FEATURE: in hardened stone mode, TNT only softens stone to cobble
        if (turnStoneToCobble)
        {
            List<Block> blocks = event.blockList();
            Iterator<Block> iter = blocks.iterator();
            //TODO LOW EhmSoftenStoneEvent
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
}