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

package com.extrahardmode.features.monsters.skeletors;


import com.extrahardmode.ExtraHardMode;
import com.extrahardmode.config.RootConfig;
import com.extrahardmode.events.EhmSkeletonDeflectEvent;
import com.extrahardmode.events.EhmSkeletonKnockbackEvent;
import com.extrahardmode.events.EhmSkeletonShootSilverfishEvent;
import com.extrahardmode.module.EntityHelper;
import com.extrahardmode.service.ListenerModule;
import com.extrahardmode.service.OurRandom;
import com.extrahardmode.task.SlowKillTask;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Changes to Skeletons include:
 * <p/>
 * Immunity to arrows
 */
public class Skeletors extends ListenerModule
{
    /**
     * Plugin
     */
    private final ExtraHardMode plugin;
    /**
     * Configuration
     */
    private final RootConfig CFG;
    /**
     * All our custom Skeletons, 0 is the default skeleton. all other skeletons are identified by the id of their PotionEffectType
     */
    private List<CustomSkeleton> customSkeletonsTypes = new ArrayList<CustomSkeleton>();


    /**
     * A constructor
     *
     * @param plugin owning plugin
     */
    public Skeletors(ExtraHardMode plugin)
    {
        super(plugin);
        this.plugin = plugin;
        CFG = plugin.getModuleForClass(RootConfig.class);
        //TODO
        Minion silverfishMinion = new Minion(OnDamage.NOTHING, EntityType.SILVERFISH, 0, 5, 20);
        Minion slimeMinion = new Minion(OnDamage.SLOW, EntityType.SLIME, 40, 5, 20);
        Minion magmaMinion = new Minion(OnDamage.BLIND, EntityType.MAGMA_CUBE, 40, 5, 10);
        customSkeletonsTypes.add(new CustomSkeleton("silverfish-annoyer", null, silverfishMinion, 15, true, 100, 15));
        customSkeletonsTypes.add(new CustomSkeleton("slime-dizzyness", PotionEffectType.HUNGER, slimeMinion, 100, true, 100, 15));
        customSkeletonsTypes.add(new CustomSkeleton("magmacube-blind", PotionEffectType.FAST_DIGGING, magmaMinion, 100, true, 100, 15));
    }


    /**
     * Arrows pass through skeletons
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onSkeletonHitByArrow(EntityDamageByEntityEvent event)
    {
        // FEATURE: arrows pass through skeletons
        if (event.getEntityType() == EntityType.SKELETON)
        {
            Skeleton skeli = (Skeleton) event.getEntity();
            World world = skeli.getWorld();

            //final int deflect = CFG.getInt(RootNode.SKELETONS_DEFLECT_ARROWS, world.getName());
            //final int knockBackPercent = CFG.getInt(RootNode.SKELETONS_KNOCK_BACK_PERCENT, world.getName());

            Entity damageSource = event.getDamager();
            CustomSkeleton type = CustomSkeleton.getCustom(skeli, plugin, customSkeletonsTypes);

            // only arrows
            if (damageSource instanceof Arrow)
            {
                if (type.getArrowsReflectPerc() > 0)
                {
                    Arrow arrow = (Arrow) damageSource;

                    Player player = arrow.getShooter() instanceof Player ? (Player) arrow.getShooter() : null;
                    if (player != null)
                    {
                        EhmSkeletonDeflectEvent skeliEvent = new EhmSkeletonDeflectEvent(player, skeli, type.getArrowsReflectPerc(), type, !type.isArrowsPassThrough());
                        plugin.getServer().getPluginManager().callEvent(skeliEvent);

                        // percent chance
                        if (!skeliEvent.isCancelled())
                        {
                            // cancel the damage
                            event.setCancelled(true);

                            // teleport the arrow a single block farther along its flight path
                            // note that .6 and 12 were the unexplained recommended values for speed and spread, reflectively, in the bukkit wiki
                            arrow.remove();
                            world.spawnArrow(arrow.getLocation().add((arrow.getVelocity().normalize()).multiply(2)), arrow.getVelocity(), 0.6f, 12.0f);
                        }
                    }
                }
            }
        }
    }


    /**
     * Skeletons sometimes hoot knockback arrows
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerHitByArrow(EntityDamageByEntityEvent event)
    {
        //Knockback player to some percentage
        if (event.getDamager() instanceof Arrow && event.getEntity() instanceof Player)
        {
            Arrow arrow = (Arrow) event.getDamager();
            if (arrow.getShooter() instanceof Skeleton)
            {
                CustomSkeleton type = CustomSkeleton.getCustom(arrow.getShooter(), plugin, customSkeletonsTypes);
                // FEATURE: skeletons can knock back
                if (type.getKnockbackPercent() > 0)
                {
                    if (plugin.random(type.getKnockbackPercent()))
                    {
                        // Knockback is enough, reduce dmg
                        event.setDamage(event.getDamage() / 2.0);
                        // knock back target with half the arrow's velocity
                        Vector knockback = arrow.getVelocity().multiply(0.5D);

                        EhmSkeletonKnockbackEvent knockbackEvent = new EhmSkeletonKnockbackEvent(event.getEntity(), (Skeleton) arrow.getShooter(), knockback, type.getKnockbackPercent());
                        if (!knockbackEvent.isCancelled())
                            knockbackEvent.getEntity().setVelocity(knockbackEvent.getVelocity());
                    }
                }
            }
        }
    }


    /**
     * Some Minions can apply PotionEffects to the player when they attack
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerHurtByMinion(EntityDamageByEntityEvent event)
    {
        // Apply effect when player hurt by minion
        if (event.getDamager() instanceof LivingEntity && Minion.isMinion((LivingEntity) event.getDamager()) && event.getEntity() instanceof Player)
        {
            Player player = (Player) event.getEntity();
            Minion matched = null;
            //Search for the Skeli that spawns this type of Minion
            for (CustomSkeleton skeli : customSkeletonsTypes)
            {
                if (skeli.getMinionType().getMinionType() == event.getDamager().getType())
                    matched = skeli.getMinionType();
            }

            if (matched != null)
            {
                switch (matched.getDamagePlayer())
                {
                    case SLOW:
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, matched.getEffectDuration(), 4));
                        break;
                    case BLIND:
                        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, matched.getEffectDuration(), 4));
                        break;
                    case NOTHING:
                    case EXPLODE:
                        break;
                    default:
                        throw new EnumConstantNotPresentException(OnDamage.class, "You added a new Action in OnDamage but didn't implement it!");
                }
            }
        }
    }


    /**
     * Minions can't be damaged by their summoners
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onMinionDamageBySummoner(EntityDamageByEntityEvent event)
    {
        //Block Summoners from damaging their minions
        if (event.getEntity() instanceof LivingEntity && Minion.isMinion((LivingEntity) event.getEntity()) && event.getDamager() instanceof Arrow && ((Arrow) event.getDamager()).getShooter() instanceof Skeleton)
        {
            event.setCancelled(true);
            Arrow arrow = (Arrow) event.getDamager();
            arrow.remove();
            Location loc = event.getDamager().getLocation();
            loc.getWorld().spawnArrow(arrow.getLocation().add((arrow.getVelocity().normalize()).multiply(4)), event.getDamager().getVelocity(), 2.0F, 0.0F);
        }
    }


    /**
     * when an entity shoots a bow...
     * <p/>
     * skeletons may summon minions
     *
     * @param event - Event that occurred.
     */
    @EventHandler
    public void onShootProjectile(ProjectileLaunchEvent event)
    {
        Location location = event.getEntity().getLocation();
        World world = location.getWorld();
        EntityType entityType = event.getEntityType();

        //final int silverfishShootPercent = CFG.getInt(RootNode.SKELETONS_RELEASE_SILVERFISH, world.getName());

        // FEATURE: skeletons sometimes release silverfish to attack their targets
        if (event.getEntity() != null && entityType == EntityType.ARROW)
        {
            Arrow arrow = (Arrow) event.getEntity();
            LivingEntity shooter = arrow.getShooter();
            if (shooter instanceof Skeleton)
            {
                Skeleton skeleton = (Skeleton) shooter;
                CustomSkeleton customSkeleton = CustomSkeleton.getCustom(skeleton, plugin, customSkeletonsTypes);
                int currentSpawnLimit = customSkeleton.getMinionType().getCurrentSpawnLimit(),
                        totalSpawnLimit = customSkeleton.getMinionType().getTotalSpawnLimit();
                if (skeleton.getTarget() instanceof Player && OurRandom.percentChance(customSkeleton.getReleaseMinionPercent())
                        && (currentSpawnLimit < 0 || currentSpawnLimit > CustomSkeleton.getSpawnedMinions(skeleton, plugin).size()) //Prevent tons of Minions
                        && (totalSpawnLimit < 0 || totalSpawnLimit > CustomSkeleton.getTotalSummoned(skeleton, plugin)))
                {
                    // Cancel and replace arrow with a summoned minion
                    event.setCancelled(true);
                    final Player player = (Player) skeleton.getTarget();

                    // replace with silverfish, quarter velocity of arrow, wants to attack same target as skeleton
                    LivingEntity minion = (LivingEntity) skeleton.getWorld().spawnEntity(skeleton.getLocation().add(0.0, 1.5, 0.0), customSkeleton.getMinionType().getMinionType());
                    minion.setVelocity(arrow.getVelocity().multiply(0.25));
                    if (minion instanceof Creature)
                        ((Creature) minion).setTarget(skeleton.getTarget());
                    if (minion instanceof Slime) //Magmacubes extend Slime
                        ((Slime) minion).setSize(2);
                    if (minion instanceof MagmaCube) //ignore so they dont explode
                        EntityHelper.flagIgnore(plugin, minion);
                    EntityHelper.markLootLess(plugin, minion); // the minion doesn't drop loot
                    Minion.setMinion(plugin, minion);
                    Minion.setParent(skeleton, minion, plugin);
                    CustomSkeleton.addMinion(skeleton, minion, plugin);

                    EhmSkeletonShootSilverfishEvent shootSilverfishEvent = new EhmSkeletonShootSilverfishEvent(player, skeleton, minion, customSkeleton.getReleaseMinionPercent(), customSkeleton);
                    plugin.getServer().getPluginManager().callEvent(shootSilverfishEvent);

                    if (shootSilverfishEvent.isCancelled()) //Undo
                    {
                        event.setCancelled(false);
                        minion.remove();
                    }
                }
            }
        }
    }


    /**
     * Remove minions if set when a skeleton dies
     *
     * @param event event that occured
     */
    @EventHandler
    public void onSkeletonDeath(EntityDeathEvent event)
    {
        LivingEntity entity = event.getEntity();
        if (entity instanceof Skeleton && !customSkeletonsTypes.isEmpty())
        {
            CustomSkeleton customSkeleton = CustomSkeleton.getCustom(entity, plugin, customSkeletonsTypes);
            if (customSkeleton.willRemoveMinions())
            {
                List<UUID> minionIds = CustomSkeleton.getSpawnedMinions(entity, plugin);
                //Only look for the type of minion we spawned, e.g. only Silverfish
                for (Entity worldEntity : entity.getWorld().getEntitiesByClass(customSkeleton.getMinionType().getMinionType().getEntityClass()))
                    for (UUID id : minionIds)
                        if (worldEntity.getUniqueId() == id)
                        {
                            ((LivingEntity) worldEntity).addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 1));
                            worldEntity.setFireTicks(Integer.MAX_VALUE);
                            new SlowKillTask((LivingEntity) worldEntity, plugin);
                        }
            }
        }
    }


    @EventHandler
    public void onMinionDeath(EntityDeathEvent event)
    {
        LivingEntity entity = event.getEntity();
        if (Minion.isMinion(entity))
        {
            UUID parent = Minion.getParent(entity, plugin);
            //Try to find the parent by id
            for (LivingEntity worldEntity : entity.getWorld().getLivingEntities())
            {
                if (worldEntity.getUniqueId() == parent)
                    CustomSkeleton.removeMinion(entity.getUniqueId(), worldEntity);
            }
        }
    }


    /**
     * Block slimes that are minions from splitting into small slimes as those don't damage you
     *
     * @param event event that occured
     */
    @EventHandler
    public void onSlimeSplit(SlimeSplitEvent event)
    {
        if (Minion.isMinion(event.getEntity()))
            event.setCancelled(true);
    }


    @EventHandler
    public void onSkeletonSpawn(CreatureSpawnEvent event)
    {
        if (event.getEntity() instanceof Skeleton)
        {
            //TODO weighted random
            int type = OurRandom.nextInt(customSkeletonsTypes.size());
            CustomSkeleton.setCustom(event.getEntity(), plugin, customSkeletonsTypes.get(type));
        }
    }


    @EventHandler
    public void onSilverfishSpawn(CreatureSpawnEvent event)
    {
        if (event.getEntityType() == EntityType.SILVERFISH)
            event.getEntity().addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, Integer.MAX_VALUE, 1, false));
    }


    @EventHandler
    public void onPiggieSpawn(CreatureSpawnEvent event)
    {
        if (event.getEntityType() == EntityType.SKELETON)
            event.getEntity().getEquipment().setItemInHand(new ItemStack(Material.STONE_SWORD));
    }
}