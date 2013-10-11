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
import com.extrahardmode.config.RootNode;
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

import java.util.*;

/**
 * Changes to Skeletons include:
 * <p/>
 * Immunity to arrows
 */
public class Skeletors extends ListenerModule
{
    /** Configuration */
    private RootConfig CFG;
    /** All our custom Skeletons, 0 is the default skeleton. all other skeletons are identified by the id of their PotionEffectType */
    private Map<String, List<CustomSkeleton>> customSkeletonsTypes = new HashMap<String, List<CustomSkeleton>>();


    /**
     * A constructor
     *
     * @param plugin owning plugin
     */
    public Skeletors(ExtraHardMode plugin)
    {
        super(plugin);
    }


    @Override
    public void starting()
    {
        super.starting();
        CFG = plugin.getModuleForClass(RootConfig.class);
        //Initlizes all CustomSkeletons for all the worlds that we are activated in.
        for (String world : CFG.getEnabledWorlds())
            initForWorld(world);
    }


    /** Arrows pass through skeletons */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onSkeletonHitByArrow(EntityDamageByEntityEvent event)
    {
        // FEATURE: arrows pass through skeletons
        if (event.getEntity() instanceof Skeleton && !getSkelisForWorld(event.getEntity().getWorld().getName()).isEmpty())
        {
            Skeleton skeli = (Skeleton) event.getEntity();
            World world = skeli.getWorld();

            Entity damageSource = event.getDamager();
            CustomSkeleton type = CustomSkeleton.getCustom(skeli, plugin, getSkelisForWorld(world.getName()));

            // only arrows
            if (damageSource instanceof Arrow && type != null)
            {
                Arrow arrow = (Arrow) damageSource;
                if (type.getArrowsReflectPerc() > 0)
                {
                    Player player = arrow.getShooter() instanceof Player ? (Player) arrow.getShooter() : null;
                    //We also fire the event if the arrow doesnt pass through, in that case its cancelled by default
                    EhmSkeletonDeflectEvent skeliEvent = new EhmSkeletonDeflectEvent(player, skeli, type.getArrowsReflectPerc(), type, !(type.isArrowsPassThrough() || player == null));
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


    /** Skeletons sometimes hoot knockback arrows */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerHitByArrow(EntityDamageByEntityEvent event)
    {
        //Knockback player to some percentage
        if (event.getDamager() instanceof Arrow && event.getEntity() instanceof Player && !getSkelisForWorld(event.getEntity().getWorld().getName()).isEmpty())
        {
            Arrow arrow = (Arrow) event.getDamager();
            if (arrow.getShooter() instanceof Skeleton)
            {
                CustomSkeleton type = CustomSkeleton.getCustom(arrow.getShooter(), plugin, getSkelisForWorld(arrow.getWorld().getName()));
                // FEATURE: skeletons can knock back
                if (type != null && type.getKnockbackPercent() > 0)
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


    /** Some Minions can apply PotionEffects to the player when they attack */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerHurtByMinion(EntityDamageByEntityEvent event)
    {
        // Apply effect when player hurt by minion
        if (event.getDamager() instanceof LivingEntity && Minion.isMinion((LivingEntity) event.getDamager()) && event.getEntity() instanceof Player)
        {
            Player player = (Player) event.getEntity();
            Minion matched = null;
            //Search for the Skeli that spawns this type of Minion
            for (CustomSkeleton skeli : getSkelisForWorld(event.getEntity().getWorld().getName()))
            {
                if (skeli.getMinionType() != null && skeli.getMinionType().getMinionType() == event.getDamager().getType())
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


    /** Minions can't be damaged by their summoners */
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

        // FEATURE: skeletons sometimes release silverfish to attack their targets
        if (event.getEntity() != null && event.getEntity() instanceof Arrow && !getSkelisForWorld(world.getName()).isEmpty())
        {
            Arrow arrow = (Arrow) event.getEntity();
            LivingEntity shooter = arrow.getShooter();
            if (shooter instanceof Skeleton)
            {
                Skeleton skeleton = (Skeleton) shooter;
                CustomSkeleton customSkeleton = CustomSkeleton.getCustom(skeleton, plugin, getSkelisForWorld(world.getName()));
                //Can be null if no skeletons are activated
                if (customSkeleton != null)
                {
                    //Some skeletons might not have a minion to spawn
                    if (customSkeleton.getMinionType() != null)
                    {
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
        if (entity instanceof Skeleton && !customSkeletonsTypes.isEmpty() && !getSkelisForWorld(entity.getWorld().getName()).isEmpty())
        {
            CustomSkeleton customSkeleton = CustomSkeleton.getCustom(entity, plugin, getSkelisForWorld(entity.getWorld().getName()));
            if (customSkeleton != null && customSkeleton.willRemoveMinions())
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
        World world = event.getLocation().getWorld();
        if (event.getEntity() instanceof Skeleton && !getSkelisForWorld(event.getEntity().getWorld().getName()).isEmpty() && world != null)
        {
            List<Integer> weights = new ArrayList<Integer>();
            for (CustomSkeleton skeli : getSkelisForWorld(world.getName()))
                weights.add(skeli.getSpawnWeight());
            int type = OurRandom.weightedRandom(weights.toArray(new Integer[weights.size()]));
            //Last index is our sword skeli
            if (getSkelisForWorld(world.getName()).get(type).getIdentifier().equals("ehm-skeli-swordguy"))
                event.getEntity().getEquipment().setItemInHand(new ItemStack(Material.STONE_HOE));
            CustomSkeleton.setCustom(event.getEntity(), plugin, getSkelisForWorld(world.getName()).get(type));
        }
    }


    @EventHandler
    public void onSilverfishSpawn(CreatureSpawnEvent event)
    {
        final boolean tempFix = CFG.getBoolean(RootNode.SILVERFISH_TEMP_POTION_EFFECT_FIX, event.getLocation().getWorld().getName());
        if (event.getEntityType() == EntityType.SILVERFISH && tempFix)
            event.getEntity().addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, Integer.MAX_VALUE, 1, false));
    }


    /**
     * Get the CustomSkeleton types that can be spawned in a particular world
     *
     * @param world get skeletons for this world
     *
     * @return List of Skeletons or an empty List if no skeletons are enabled in the world
     */
    private List<CustomSkeleton> getSkelisForWorld(String world)
    {
        List<CustomSkeleton> skelis = Collections.emptyList();
        if (CFG.isEnabledForAll() && !customSkeletonsTypes.containsKey(world))
            skelis = customSkeletonsTypes.get(CFG.getAllWorldString());
        else if (customSkeletonsTypes.containsKey(world))
            skelis = customSkeletonsTypes.get(world);
        return skelis;
    }


    /** Initializes the CustomSkeletons for a given world */
    private void initForWorld(String world)
    {
        List<CustomSkeleton> skeletons = new ArrayList<CustomSkeleton>(3);
        //It's important that all customskelis have the same format for the ENUM otherwise you be getting an Enum not found error
        {
            boolean enabled = CFG.getBoolean(RootNode.SKELI_GREY_ENABLE, world);
            if (enabled)
            {
                int spawnWeight = CFG.getInt(RootNode.SKELI_GREY_WEIGHT, world);
                int knockbackPercent = CFG.getInt(RootNode.SKELI_GREY_KNOCK_BACK_PERCENT, world);
                int deflectPercent = CFG.getInt(RootNode.SKELI_GREY_DEFLECT_ARROWS, world);

                int minionReleasePercent = CFG.getInt(RootNode.SKELI_GREY_RELEASE_PERCENT, world);
                int minionLimit = CFG.getInt(RootNode.SKELI_GREY_MINION_LIMIT, world);
                int minionTotalLimit = CFG.getInt(RootNode.SKELI_GREY_MINION_TOTAL_LIMIT, world);
                boolean minionDieWith = CFG.getBoolean(RootNode.SKELI_GREY_MINION_KILL_WITH, world);
                int minionLootPercentage = CFG.getInt(RootNode.SKELI_GREY_MINION_LOOT_PERCENTAGE, world);

                Minion myMinion = new Minion(OnDamage.NOTHING, EntityType.SILVERFISH, 0, minionLimit, minionTotalLimit, minionLootPercentage);
                CustomSkeleton skeleton = new CustomSkeleton("ehm-skeli-grey", null, myMinion, minionReleasePercent, minionDieWith, deflectPercent, knockbackPercent, spawnWeight);
                skeletons.add(skeleton);
            }
        }

        {
            boolean enabled = CFG.getBoolean(RootNode.SKELI_GREEN_ENABLE, world);
            if (enabled)
            {
                int spawnWeight = CFG.getInt(RootNode.SKELI_GREEN_WEIGHT, world);
                int knockbackPercent = CFG.getInt(RootNode.SKELI_GREEN_KNOCK_BACK_PERCENT, world);
                int deflectPercent = CFG.getInt(RootNode.SKELI_GREEN_DEFLECT_ARROWS, world);

                int minionReleasePercent = CFG.getInt(RootNode.SKELI_GREEN_RELEASE_PERCENT, world);
                int minionLimit = CFG.getInt(RootNode.SKELI_GREEN_MINION_LIMIT, world);
                int minionTotalLimit = CFG.getInt(RootNode.SKELI_GREEN_MINION_TOTAL_LIMIT, world);
                boolean minionDieWith = CFG.getBoolean(RootNode.SKELI_GREEN_MINION_KILL_WITH, world);
                int minionLootPercentage = CFG.getInt(RootNode.SKELI_GREEN_MINION_LOOT_PERCENTAGE, world);

                Minion myMinion = new Minion(OnDamage.SLOW, EntityType.SLIME, 40, minionLimit, minionTotalLimit, minionLootPercentage);
                CustomSkeleton skeleton = new CustomSkeleton("ehm-skeli-green", null, myMinion, minionReleasePercent, minionDieWith, deflectPercent, knockbackPercent, spawnWeight);
                skeletons.add(skeleton);
            }
        }

        {
            boolean enabled = CFG.getBoolean(RootNode.SKELI_RED_ENABLE, world);
            if (enabled)
            {
                int spawnWeight = CFG.getInt(RootNode.SKELI_RED_WEIGHT, world);
                int knockbackPercent = CFG.getInt(RootNode.SKELI_RED_KNOCK_BACK_PERCENT, world);
                int deflectPercent = CFG.getInt(RootNode.SKELI_RED_DEFLECT_ARROWS, world);

                int minionReleasePercent = CFG.getInt(RootNode.SKELI_RED_RELEASE_PERCENT, world);
                int minionLimit = CFG.getInt(RootNode.SKELI_RED_MINION_LIMIT, world);
                int minionTotalLimit = CFG.getInt(RootNode.SKELI_RED_MINION_TOTAL_LIMIT, world);
                boolean minionDieWith = CFG.getBoolean(RootNode.SKELI_RED_MINION_KILL_WITH, world);
                int minionLootPercentage = CFG.getInt(RootNode.SKELI_RED_MINION_LOOT_PERCENTAGE, world);

                Minion myMinion = new Minion(OnDamage.BLIND, EntityType.MAGMA_CUBE, 40, minionLimit, minionTotalLimit, minionLootPercentage);
                CustomSkeleton skeleton = new CustomSkeleton("ehm-skeli-red", null, myMinion, minionReleasePercent, minionDieWith, deflectPercent, knockbackPercent, spawnWeight);
                skeletons.add(skeleton);
            }
        }

        //mockup Sword Skeli
        {
            boolean enabled = CFG.getBoolean(RootNode.SKELI_SWORDGUY_ENABLE, world);
            if (enabled)
            {
                int spawnWeight = CFG.getInt(RootNode.SKELI_SWORDGUY_WEIGHT, world);
                int knockbackPercent = 0;
                int deflectPercent = CFG.getInt(RootNode.SKELI_SWORDGUY_DEFLECT_ARROWS, world);

                int minionReleasePercent = 0;
                boolean minionDieWith = false;

                CustomSkeleton skeleton = new CustomSkeleton("ehm-skeli-swordguy", null, null, minionReleasePercent, minionDieWith, deflectPercent, knockbackPercent, spawnWeight);
                skeletons.add(skeleton);
            }
        }

        customSkeletonsTypes.put(world, skeletons);
    }
}