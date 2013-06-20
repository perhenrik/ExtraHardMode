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
import com.extrahardmode.module.EntityHelper;
import com.extrahardmode.service.ListenerModule;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;

/**
 * Changes to Skeletons include:
 *
 * Immunity to arrows ,
 *
 */
public class Skeletors extends ListenerModule
{
    private final ExtraHardMode plugin;
    private final RootConfig CFG;

    public Skeletors(ExtraHardMode plugin)
    {
        super(plugin);
        this.plugin = plugin;
        CFG = plugin.getModuleForClass(RootConfig.class);
    }


    /**
     * When an entity takes damage
     *
     * skeletons are immune to arrows
     *
     * @param event
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onEntityDamage(EntityDamageEvent event)
    {
        Entity entity = event.getEntity();
        EntityType entityType = entity.getType();
        World world = entity.getWorld();

        final int deflect = CFG.getInt(RootNode.SKELETONS_DEFLECT_ARROWS, world.getName());
        final int knockBackPercent = CFG.getInt(RootNode.SKELETONS_KNOCK_BACK_PERCENT, world.getName());


        // is this an entity damaged by entity event?
        EntityDamageByEntityEvent damageByEntityEvent = null;
        if (event instanceof EntityDamageByEntityEvent)
        {
            damageByEntityEvent = (EntityDamageByEntityEvent) event;
        }

        // FEATURE: arrows pass through skeletons
        if (entityType == EntityType.SKELETON && damageByEntityEvent != null && deflect > 0)
        {
            Entity damageSource = damageByEntityEvent.getDamager();
            //TODO EhmSkeletonDeflectArrowEvent
            // only arrows
            if (damageSource instanceof Arrow)
            {
                Arrow arrow = (Arrow) damageSource;

                // percent chance
                if (plugin.random(deflect))
                {

                    // cancel the damage
                    event.setCancelled(true);

                    // teleport the arrow a single block farther along its flight
                    // path
                    // note that .6 and 12 were the unexplained recommended values
                    // for speed and spread, reflectively, in the bukkit wiki
                    arrow.remove();
                    world.spawnArrow(arrow.getLocation().add((arrow.getVelocity().normalize()).multiply(2)), arrow.getVelocity(), 0.6f, 12.0f);
                }
            }
        }

        // FEATURE: skeletons can knock back
        if (knockBackPercent > 0)
        {
            if (damageByEntityEvent != null)
            {
                if (damageByEntityEvent.getDamager() instanceof Arrow)
                {
                    Arrow arrow = (Arrow) (damageByEntityEvent.getDamager());
                    if (arrow.getShooter() != null && arrow.getShooter() instanceof Skeleton)
                    {
                        if (plugin.random(knockBackPercent))
                        {
                            // cut damage in half
                            event.setDamage(event.getDamage() / 2);
                            //TODO EhmSkeletonKnockbackEvent
                            // knock back target with half the arrow's velocity
                            entity.setVelocity(arrow.getVelocity());
                        }
                    }
                }
            }
        }
    }

    /**
     * when an entity shoots a bow...
     *
     * skeletons shoot silverfish
     *
     * @param event - Event that occurred.
     */
    @EventHandler
    public void onShootProjectile(ProjectileLaunchEvent event)
    {
        Location location = event.getEntity().getLocation();
        World world = location.getWorld();
        EntityType entityType = event.getEntityType();

        final int silverfishShootPercent = CFG.getInt(RootNode.SKELETONS_RELEASE_SILVERFISH, world.getName());

        // FEATURE: skeletons sometimes release silverfish to attack their targets
        if (event.getEntity() != null && entityType == EntityType.ARROW)
        {
            Arrow arrow = (Arrow) event.getEntity();
            //TODO EhmSkeletonSilverfishEvent
            LivingEntity shooter = arrow.getShooter();
            if (shooter != null && shooter.getType() == EntityType.SKELETON && plugin.random(silverfishShootPercent))
            {
                Skeleton skeleton = (Skeleton) shooter;
                // cancel arrow fire
                event.setCancelled(true);

                // replace with silverfish, quarter velocity of arrow, wants to attack
                // same target as skeleton
                Creature silverFish = (Creature) skeleton.getWorld().spawnEntity(skeleton.getLocation().add(0, 1.5, 0), EntityType.SILVERFISH);
                silverFish.setVelocity(arrow.getVelocity().multiply(0.25));
                silverFish.setTarget(skeleton.getTarget());
                EntityHelper.markLootLess(plugin, silverFish); // this silverfish doesn't drop loot
            }
        }
    }
}
