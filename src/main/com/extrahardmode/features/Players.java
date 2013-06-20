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
import com.extrahardmode.config.RootConfig;
import com.extrahardmode.config.RootNode;
import com.extrahardmode.config.messages.MessageConfig;
import com.extrahardmode.module.DataStoreModule;
import com.extrahardmode.module.PlayerModule;
import com.extrahardmode.module.UtilityModule;
import com.extrahardmode.task.SetPlayerHealthAndFoodTask;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

/**
 * Playerchanges include
 *
 * less health/food on respawn ,
 * loss of some of their inventory ,
 * enhanced environmental damage ,
 * catching the player on fire if extinguishing fires by hand
 */
public class Players implements Listener
{
    private ExtraHardMode plugin = null;
    private RootConfig CFG = null;
    private final MessageConfig messages;
    private UtilityModule utils = null;
    private final PlayerModule playerModule;

    /**
     * Constructor
     *
     * @param plugin
     */
    public Players (ExtraHardMode plugin)
    {
        this.plugin = plugin;
        CFG = plugin.getModuleForClass(RootConfig.class);
        messages = plugin.getModuleForClass(MessageConfig.class);
        utils = plugin.getModuleForClass(UtilityModule.class);
        playerModule = plugin.getModuleForClass(PlayerModule.class);
    }

    /**
     * FEATURE: respawning players start without full health or food
     *
     * @param respawnEvent - Event that occurred.
     */
    @EventHandler(ignoreCancelled = true)
    public void onPlayerRespawn(PlayerRespawnEvent respawnEvent)
    {
        Player player = respawnEvent.getPlayer();
        World world = player.getWorld();

        final boolean playerBypasses = playerModule.playerBypasses(player, Feature.RESPAWN_FOOD_HEALTH);

        final int respawnHealth = playerBypasses ? player.getMaxHealth()
                                  : CFG.getInt(RootNode.PLAYER_RESPAWN_HEALTH, world.getName());
        final int respawnFood = playerBypasses ? player.getMaxHealth()
                                : CFG.getInt(RootNode.PLAYER_RESPAWN_FOOD_LEVEL, world.getName());

        if (respawnFood < player.getMaxHealth() || respawnHealth < player.getMaxHealth()) //maxHealth and maxFoodLevel are both 20, but there is no method for maxFoodLevel
        {
            //TODO HIGH EhmPlayerRespawnEvent
            SetPlayerHealthAndFoodTask task = new SetPlayerHealthAndFoodTask(player, respawnHealth, respawnFood);
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, task, 10L); // half-second delay
        }
        // FEATURE: players can't swim when they're carrying a lot of weight, reset the cached value
        DataStoreModule.PlayerData playerData = plugin.getModuleForClass(DataStoreModule.class).getPlayerData(player.getName());
        playerData.cachedWeightStatus = -1.0F;
    }

    /**
     * When a Player dies he looses a percentage of his inventory
     *
     * @param event
     */
    @EventHandler(ignoreCancelled = true)
    public void onEntityDeath(PlayerDeathEvent event)
    {
        Player player = event.getEntity();
        World world = player.getWorld();

        final int deathLossPercent = CFG.getInt(RootNode.PLAYER_DEATH_ITEM_STACKS_FORFEIT_PERCENT, world.getName());
        final boolean playerBypasses = playerModule.playerBypasses(player, Feature.DEATH_INV_LOSS);

        // FEATURE: some portion of player inventory is permanently lost on death
        if (!playerBypasses)
        {
            //TODO HIGH EhmPlayerInvetoryLossEvent after computation
            List<ItemStack> drops = event.getDrops();
            int numberOfStacksToRemove = (int) (drops.size() * (deathLossPercent / 100.0f));
            for (int i = 0; i < numberOfStacksToRemove && drops.size() > 0; i++)
            {
                int indexOfStackToRemove = plugin.getRandom().nextInt(drops.size());
                drops.remove(indexOfStackToRemove);
                //TODO HIGH tools percentage, damage etc.
            }
        }

    }

    /**
     * Environmental effects when player is damaged
     *
     * @param event
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)//so we know if the event got cancelled
    public void onEntityDamage(EntityDamageEvent event)
    {
        Entity entity = event.getEntity();
        World world = entity.getWorld();

        if (entity instanceof Player)
        {
            Player player = (Player) entity;
            final boolean enhancedEnvironmentalDmg = CFG.getBoolean(RootNode.ENHANCED_ENVIRONMENTAL_DAMAGE, world.getName());
            final boolean playerBypasses = playerModule.playerBypasses(player, Feature.ENVIRONMENTAL_EFFECTS);

            // FEATURE: extra damage and effects from environmental damage
            if (enhancedEnvironmentalDmg &&! playerBypasses)
            {
                EntityDamageEvent.DamageCause cause = event.getCause();

                switch (cause)
                {
                    case BLOCK_EXPLOSION:
                    case ENTITY_EXPLOSION:
                        //TODO EhmPlayerEnvironmentalDamageEvent for each type
                        if (event.getDamage() > 2)
                            player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 20 * 15, 3));
                        break;
                    case FALL:
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * event.getDamage(), 4));
                        event.setDamage(event.getDamage() * 2);
                        break;
                    case SUFFOCATION:
                        event.setDamage(event.getDamage() * 5);
                        break;
                    case LAVA:
                        event.setDamage(event.getDamage() * 2);
                        break;
                    case FIRE_TICK:
                        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20, 1));
                        break;
                }
            }
        }


    }

    /**
     * when a player interacts with the world
     *
     * @param event - Event that occurred.
     */
    @EventHandler(priority = EventPriority.LOWEST)
    void onPlayerInteract(PlayerInteractEvent event)
    {
        Player player = event.getPlayer();
        World world = event.getPlayer().getWorld();
        Block block = event.getClickedBlock();
        Action action = event.getAction();

        final boolean extinguishingFireIgnites = CFG.getBoolean(RootNode.EXTINGUISHING_FIRE_IGNITES_PLAYERS, world.getName());

        final boolean playerBypasses = playerModule.playerBypasses(player, Feature.DANGEROUS_FIRES);

        // FEATURE: putting out fire up close catches the player on fire
        if (extinguishingFireIgnites &&! playerBypasses && block != null && block.getType() != Material.AIR &&
                (action.equals(Action.LEFT_CLICK_BLOCK) || action.equals(Action.LEFT_CLICK_AIR)))
        {
            if (block.getRelative(event.getBlockFace()).getType() == Material.FIRE)
            {
                //TODO EhmPlayerExtinguishFireEvent
                player.setFireTicks(100); // 20L ~ 1 seconds; 100L ~ 5 seconds
            }
        }
    }
}
