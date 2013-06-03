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

package com.extrahardmode.features.monsters;

import com.extrahardmode.ExtraHardMode;
import com.extrahardmode.config.RootConfig;
import com.extrahardmode.config.RootNode;
import com.extrahardmode.config.messages.MessageConfig;
import com.extrahardmode.module.EntityModule;
import com.extrahardmode.module.UtilityModule;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;

public class MonsterRules implements Listener
{
    ExtraHardMode plugin = null;
    RootConfig CFG = null;
    MessageConfig messages;
    UtilityModule utils = null;
    EntityModule entityModule = null;

    public MonsterRules(ExtraHardMode plugin)
    {
        this.plugin = plugin;
        CFG = plugin.getModuleForClass(RootConfig.class);
        messages = plugin.getModuleForClass(MessageConfig.class);
        utils = plugin.getModuleForClass(UtilityModule.class);
        entityModule = plugin.getModuleForClass(EntityModule.class);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onEntitySpawn(CreatureSpawnEvent event)
    {
        Location location = event.getLocation();
        World world = location.getWorld();

        final int maxY = CFG.getInt(RootNode.MORE_MONSTERS_MAX_Y, world.getName());
        final int multiplier = CFG.getInt(RootNode.MORE_MONSTERS_MULTIPLIER, world.getName());

        LivingEntity entity = event.getEntity();
        EntityType entityType = entity.getType();

        CreatureSpawnEvent.SpawnReason reason = event.getSpawnReason();

        //We don't know how to handle ghosts. (Mo Creatures)
        if (!entityType.equals(EntityType.UNKNOWN) && reason != CreatureSpawnEvent.SpawnReason.CUSTOM)
        {
            // FEATURE: extra monster spawns underground
            if (maxY > 0)
            {
                if (world.getEnvironment() == World.Environment.NORMAL && event.getLocation() != null
                        && event.getLocation().getBlockY() < maxY && entityType != null && entity instanceof Monster)
                {
                    if (!entityType.equals(EntityType.SILVERFISH)) //no multiple silverfish per block
                    {
                        for (int i = 1; i < multiplier; i++)
                        {
                            Entity newEntity = entityModule.spawn(event.getLocation(), entityType);
                            if (entityModule.isLootLess(entity))
                            {
                                entityModule.markLootLess((LivingEntity) newEntity);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * when an entity targets something (as in to attack it)...
     *
     * @param event - Event that occurred.
     */
    @EventHandler
    public void onEntityTarget(EntityTargetEvent event)
    {
        Entity entity = event.getEntity();
        World world = entity.getWorld();

        //TODO ENABLED WORLDS

        // FEATURE: a monster which gains a target breaks out of any webbing it might have been trapped within
        if (entity instanceof Monster)
        {
            entityModule.clearWebbing(entity);
        }
    }

    /**
     * when an entity is damaged
     * handles
     *
     * @param event - Event that occurred.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onEntityDamage(EntityDamageEvent event)
    {
        Entity entity = event.getEntity();
        EntityType entityType = entity.getType();
        World world = entity.getWorld();

        //TODO WORLDS

        // FEATURE: don't allow explosions to destroy items on the ground
        // REASONS: charged creepers explode twice, enhanced TNT explodes 5 times
        if (entityType == EntityType.DROPPED_ITEM)
        {
            event.setCancelled(true);
        }

        // FEATURE: monsters trapped in webbing break out of the webbing when hit
        if (entity instanceof Monster)
        {
            entityModule.clearWebbing(entity);
        }
    }
}
