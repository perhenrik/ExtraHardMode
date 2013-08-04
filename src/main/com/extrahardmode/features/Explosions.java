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
import com.extrahardmode.service.Feature;
import com.extrahardmode.service.ListenerModule;
import com.extrahardmode.task.CreateExplosionTask;
import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.*;

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
     * Handles all of EHM's custom explosions, this includes bigger random tnt explosions , bigger ghast explosion ,
     * turn stone into cobble in hardened stone mode ,
     *
     * @param event
     *         - Event that occurred.
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


    /**
     * Gets called just when an ItemStack is about to be crafted Sets the amount in the result slot to the appropriate
     * number
     */
    @EventHandler
    public void beforeCraft(PrepareItemCraftEvent event)
    {
        Inventory inv = event.getInventory();

        if (inv != null && inv.getHolder() != null)
        {
            InventoryHolder human = inv.getHolder();
            if (human instanceof Player)
            {
                Player player = (Player) human;
                World world = player.getWorld();

                final int multiplier = CFG.getInt(RootNode.MORE_TNT_NUMBER, world.getName());

                switch (multiplier)
                {
                    case 0:
                        break;
                    case 1:
                        break;
                    default:
                        if (event.getRecipe().getResult().getType().equals(Material.TNT))
                        {
                            //TODO LOW EhmMoreTntEvent
                            //Recipe in CraftingGrid
                            ShapedRecipe craftRecipe = (ShapedRecipe) event.getRecipe();
                            CraftingInventory craftInv = event.getInventory();

                            //The vanilla tnt recipe
                            ShapedRecipe vanillaTnt = new ShapedRecipe(new ItemStack(Material.TNT)).shape("gsg", "sgs", "gsg").setIngredient('g', Material.SULPHUR).setIngredient('s', Material.SAND);

                            //Multiply the amount of tnt in enabled worlds
                            if (utils.isSameRecipe(craftRecipe, vanillaTnt))
                            {
                                craftInv.setResult(new ItemStack(Material.TNT, multiplier));
                            }
                        }
                        break;
                }
            }
        }
    }


    /**
     * when a player crafts something... MoreTnt: Contains the logic for modifying the amount of tnt crafted on a per
     * world basis.
     *
     * @param event
     *         - Event that occurred.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onItemCrafted(CraftItemEvent event)
    {
        InventoryHolder human = event.getInventory().getHolder();

        if (human instanceof Player)
        {
            Player player = (Player) human;

            World world = player.getWorld();

            final int multiplier = CFG.getInt(RootNode.MORE_TNT_NUMBER, world.getName());
            final boolean playerBypasses = playerModule.playerBypasses(player, Feature.EXPLOSIONS);

            //Are we crafting tnt and is more tnt enabled, from BeforeCraftEvent
            if (event.getRecipe().getResult().equals(new ItemStack(Material.TNT, multiplier)) && !playerBypasses)
            {
                switch (multiplier)
                {
                    case 0:
                        event.setCancelled(true); //Feature disable tnt crafting
                        break;
                    default:
                        Validate.isTrue(multiplier > 0, "Multiplier for tnt can't be negative");
                        PlayerInventory inv = player.getInventory();
                        //ShiftClick only causes this event to be called once
                        if (event.isShiftClick())
                        {
                            int amountBefore = PlayerModule.countInvItem(inv, Material.TNT);
                            //Add the missing tnt 1 tick later, we count what has been added by shiftclicking and multiply it
                            UtilityModule.addExtraItemsLater task = new UtilityModule.addExtraItemsLater(inv, amountBefore, Material.TNT, multiplier - 1);
                            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, task, 1L);
                        }
                        break;
                }
            }
        }
    }
}