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
import com.extrahardmode.config.ExplosionType;
import com.extrahardmode.config.RootConfig;
import com.extrahardmode.config.RootNode;
import com.extrahardmode.config.messages.MessageConfig;
import com.extrahardmode.module.EntityHelper;
import com.extrahardmode.module.UtilityModule;
import com.extrahardmode.task.CreateExplosionTask;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

/**
 * Changes to Blazes including:
 * <p/>
 * spawn in the Nether everywhere ,
 * multiply on death ,
 * more loot ,
 * magmacubes explode on death and turn into Blazes
 * <p/>
 * spawn at lavalevel in the OverWorld ,
 * explode on death in the Overworld ,
 * no blazerods in the OverWorld
 */
public class Blazes implements Listener
{
    private ExtraHardMode plugin = null;
    private RootConfig CFG = null;
    private final MessageConfig messages;
    private UtilityModule utils = null;

    public Blazes (ExtraHardMode plugin)
    {
        this.plugin = plugin;
        CFG = plugin.getModuleForClass(RootConfig.class);
        messages = plugin.getModuleForClass(MessageConfig.class);
        utils = plugin.getModuleForClass(UtilityModule.class);
    }

    /**
     * When an Entity spawns,
     * <p/>
     * handles all the extra spawns for Blazes in the OverWorld and Nether
     *
     * @param event
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onEntitySpawn(CreatureSpawnEvent event)
    {
        Location location = event.getLocation();
        World world = location.getWorld();

        final int bonusNetherBlazeSpawnPercent = CFG.getInt(RootNode.BONUS_NETHER_BLAZE_SPAWN_PERCENT, world.getName());
        final int nearBedrockSpawnPercent = CFG.getInt(RootNode.NEAR_BEDROCK_BLAZE_SPAWN_PERCENT, world.getName());

        LivingEntity entity = event.getEntity();
        EntityType entityType = entity.getType();

        // FEATURE: more blazes in nether
        if (entityType == EntityType.PIG_ZOMBIE)
        {
            if (plugin.random(bonusNetherBlazeSpawnPercent))
            {
                event.setCancelled(true);
                entityType = EntityType.BLAZE;

                // FEATURE: magma cubes spawn with blazes
                if (plugin.random(bonusNetherBlazeSpawnPercent))
                {
                    MagmaCube cube = (MagmaCube) (EntityHelper.spawn(location, EntityType.MAGMA_CUBE));
                    cube.setSize(1);
                }
                EntityHelper.spawn(location, entityType);
                //TODO EhmBlazeSpawnEvent (Nether)
            }
        }

        // FEATURE: blazes near bedrock
        if (entityType == EntityType.SKELETON && world.getEnvironment() == World.Environment.NORMAL && location.getBlockY() < 20)
        {
            if (plugin.random(nearBedrockSpawnPercent))
            {
                event.setCancelled(true);
                entityType = EntityType.BLAZE;
                EntityHelper.spawn(location, entityType);
                //TODO EhmBlazeSpawnEvent (OverWorld)
            }
        }
    }

    /**
     * When a Blaze dies,
     *
     * exlode in OverWorld ,
     * multiply in the Nether
     *
     * @param event
     */
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event)
    {
        LivingEntity entity = event.getEntity();
        World world = entity.getWorld();

        final boolean bonusLoot = CFG.getBoolean(RootNode.BLAZES_DROP_BONUS_LOOT, world.getName());
        final boolean blazesExplodeOnDeath = CFG.getBoolean(RootNode.BLAZES_EXPLODE_ON_DEATH, world.getName());
        final int blazeSplitPercent = CFG.getInt(RootNode.NETHER_BLAZES_SPLIT_ON_DEATH_PERCENT, world.getName());

        // FEATURE: nether blazes drop extra loot (glowstone and gunpowder)
        if (bonusLoot && entity instanceof Blaze)
        {
            if (world.getEnvironment() == World.Environment.NETHER)
            {
                // 50% chance of each
                if (plugin.getRandom().nextInt(2) == 0)
                {
                    event.getDrops().add(new ItemStack(Material.SULPHUR, 2));
                }
                else
                {
                    event.getDrops().add(new ItemStack(Material.GLOWSTONE_DUST, 2));
                }
            }
            else // no drops in the normal world (restricting blaze rods to the
            // nether)
            {
                event.getDrops().clear();
            }
        }

        // FEATURE: blazes explode on death in normal world
        if (blazesExplodeOnDeath && entity instanceof Blaze && world.getEnvironment() == World.Environment.NORMAL)
        {
            // create explosion
            new CreateExplosionTask(plugin, entity.getLocation(), ExplosionType.OVERWORLD_BLAZE); // equal to a TNT blast, sets fires
            // fire a fireball straight up in normal worlds
            Fireball fireball = (Fireball) world.spawnEntity(entity.getLocation(), EntityType.FIREBALL);
            fireball.setDirection(new Vector(0, 10, 0));
            fireball.setYield(1.0F);
            //TODO EhmBlazeExplodeEvent
        }

        // FEATURE: nether blazes may multiply on death
        if (blazeSplitPercent > 0 && world.getEnvironment() == World.Environment.NETHER && entity instanceof Blaze)
        {
            if (plugin.random(blazeSplitPercent))
            {
                //TODO EhmBlazeSplitEvent
                Entity firstNewBlaze = EntityHelper.spawn(entity.getLocation(), EntityType.BLAZE);
                firstNewBlaze.setVelocity(new Vector(1, 0, 1));

                Entity secondNewBlaze = EntityHelper.spawn(entity.getLocation(), EntityType.BLAZE);
                secondNewBlaze.setVelocity(new Vector(-1, 0, -1));

                // if this blaze was marked lootless, mark the new blazes the same
                if (EntityHelper.isLootLess(entity))
                {
                    EntityHelper.markLootLess(plugin, (LivingEntity) firstNewBlaze);
                    EntityHelper.markLootLess(plugin, (LivingEntity) secondNewBlaze);
                }
            }
        }
    }

    /**
     * When an Entity takes damage
     *
     * Magmacubes turn into blazes ,
     * Blazes drop fire when hit ,
     *
     * @param event
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onEntityDamage(EntityDamageEvent event)
    {
        Entity entity = event.getEntity();
        EntityType entityType = entity.getType();
        World world = entity.getWorld();

        final boolean magmacubesBlazeOnDmg = CFG.getBoolean(RootNode.MAGMA_CUBES_BECOME_BLAZES_ON_DAMAGE, world.getName());
        final boolean blazeFireOnDmg = CFG.getBoolean(RootNode.BLAZES_DROP_FIRE_ON_DAMAGE, world.getName());

        // FEATURE: magma cubes become blazes when they take damage
        if (magmacubesBlazeOnDmg && entityType == EntityType.MAGMA_CUBE  && !entity.isDead())
        {
            entity.remove(); // remove magma cube
            EntityHelper.spawn(entity.getLocation().add(0.0, 2.0, 0.0), EntityType.BLAZE); // replace with blaze
            new CreateExplosionTask(plugin, entity.getLocation(), ExplosionType.MAGMACUBE_FIRE).run(); // fiery explosion for effect
            //TODO EhmMagmaCubeExplodeEvent
        }

        // FEATURE: blazes drop fire on hit, also in nether
        if (blazeFireOnDmg)
        {
            if (entityType == EntityType.BLAZE)
            {
                Blaze blaze = (Blaze) entity;

                if (blaze.getHealth() > blaze.getMaxHealth() / 2)
                {

                    Block block = entity.getLocation().getBlock();

                    Block underBlock = block.getRelative(BlockFace.DOWN);
                    for (int i = 0; i < 50; i++)
                    {
                        if (underBlock.getType() == Material.AIR)
                        {
                            underBlock = underBlock.getRelative(BlockFace.DOWN);
                        }
                        else break;
                    }
                    block = underBlock.getRelative(BlockFace.UP);
                    if (block.getType() == Material.AIR && underBlock.getType() != Material.AIR && !underBlock.isLiquid() && underBlock.getY() > 0)
                    {
                        block.setType(Material.FIRE);
                    }
                }
            }
        }

    }
}