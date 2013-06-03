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
import com.extrahardmode.config.messages.MessageNode;
import com.extrahardmode.events.EhmHardenedStoneEvent;
import com.extrahardmode.module.BlockModule;
import com.extrahardmode.module.MessagingModule;
import com.extrahardmode.module.UtilityModule;
import com.extrahardmode.service.PermissionNode;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Hardened Stone is there to make branchmining harder/impossible
 *
 * Only Iron/Diamond Picks can break stone ,
 * Tools break faster when breaking stone ,
 * Breaking ore causes surounding stone to fall ,
 * Various Fixes to prevent working around the hardened stone
 */
public class HardenedStone implements Listener
{
    ExtraHardMode plugin;
    RootConfig CFG;
    UtilityModule utils;
    MessagingModule messenger;
    BlockModule blockModule;

    public HardenedStone (ExtraHardMode plugin)
    {
        this.plugin = plugin;
        CFG = plugin.getModuleForClass(RootConfig.class);
        utils = plugin.getModuleForClass(UtilityModule.class);
        messenger = plugin.getModuleForClass(MessagingModule.class);
        blockModule = plugin.getModuleForClass(BlockModule.class);
    }

    /**
     * When a player breaks stone
     * @param event
     */
    @EventHandler
    public void onBlockBreak (BlockBreakEvent event)
    {
        Block block = event.getBlock();
        World world = block.getWorld();
        Player player = event.getPlayer();

        final boolean hardStoneEnabled = CFG.getBoolean(RootNode.SUPER_HARD_STONE, world.getName());
        final boolean hardStonePhysix = CFG.getBoolean(RootNode.SUPER_HARD_STONE_PHYSICS, world.getName());
        final int stoneBlocksIron = CFG.getInt(RootNode.IRON_DURABILITY_PENALTY, world.getName());
        final int stoneBlocksDiamond = CFG.getInt(RootNode.DIAMOND_DURABILITY_PENALTY, world.getName());

        // FEATURE: stone breaks tools much quicker
        if (hardStoneEnabled && block.getType() == Material.STONE &&! player.getGameMode().equals(GameMode.CREATIVE))
        {
            ItemStack inHandStack = player.getItemInHand();

            if (inHandStack != null)
            {
                EhmHardenedStoneEvent hardEvent = new EhmHardenedStoneEvent(player, inHandStack, 0);

                switch (inHandStack.getType())
                {
                    case IRON_PICKAXE:
                        hardEvent.setNumOfBlocks(stoneBlocksIron);
                        break;
                    case DIAMOND_PICKAXE:
                        hardEvent.setNumOfBlocks(stoneBlocksDiamond);
                        break;
                }

                /* Broadcast an Event for other Plugins to change if the tool can break stone and the amount of blocks */
                plugin.getServer().getPluginManager().callEvent(hardEvent);

                if (hardEvent.getNumOfBlocks() == 0)
                {
                    messenger.notifyPlayer(player, MessageNode.STONE_MINING_HELP, PermissionNode.SILENT_STONE_MINING_HELP);
                    event.setCancelled(true);
                    return;
                }

                // otherwise, drastically reduce tool durability when breaking stone
                else if (hardEvent.getNumOfBlocks() > 0)
                {

                    int maxDurability = inHandStack.getType().getMaxDurability();
                    int damagePerBlock = maxDurability / hardEvent.getNumOfBlocks();

                    if (maxDurability > 0 && damagePerBlock > 0)
                    {
                        inHandStack.setDurability((short) (inHandStack.getDurability() + damagePerBlock));

                        // For cases where a remainder causes the tool to be viable for an extra use, eat up the remainder of thet durability
                        if ( maxDurability - inHandStack.getDurability() < inHandStack.getType().getMaxDurability() / damagePerBlock)
                            inHandStack.setDurability((short) maxDurability);
                    }
                }
            }
        }

        // when ore is broken, it softens adjacent stone important to ensure players can reach the ore they break
        if (hardStonePhysix && ((block.getType().name().endsWith("ORE") || block.getType().name().endsWith("ORES"))))
        {
            //TODO HIGH EhmOrePhysicsEvent
            for (BlockFace face : blockModule.getTouchingFaces())
            {
                Block adjacentBlock = block.getRelative(face);
                if (adjacentBlock.getType() == Material.STONE)
                    adjacentBlock.setType(Material.COBBLESTONE);
            }
        }
    }

    /**
     * FIX: prevent players from placing ore as an exploit to work around the hardened stone rule
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onBlockPlace(BlockPlaceEvent placeEvent)
    {
        Player player = placeEvent.getPlayer();
        Block block = placeEvent.getBlock();
        World world = block.getWorld();

        final boolean hardstoneEnabled = CFG.getBoolean(RootNode.SUPER_HARD_STONE, world.getName())
                                         &&! player.getGameMode().equals(GameMode.CREATIVE);

        //TODO EhmBlockOrePlacementEvent
        if (hardstoneEnabled && (block.getType().name().endsWith("ORE") || block.getType().name().endsWith("ORES")))
        {
            ArrayList<Block> adjacentBlocks = new ArrayList<Block>();
            for (BlockFace face : blockModule.getTouchingFaces())
            {
                adjacentBlocks.add(block.getRelative(face));
            }

            for (Block adjacentBlock : adjacentBlocks)
            {
                if (adjacentBlock.getType() == Material.STONE)
                {
                    messenger.sendMessage(player, MessageNode.NO_PLACING_ORE_AGAINST_STONE);
                    placeEvent.setCancelled(true);
                    return;
                }
            }
        }
    }

    /**
     * When a piston extends
     * prevent players from circumventing hardened stone rules by
     * placing ore, then pushing the ore next to stone before breaking it
     *
     * @param event - Event that occurred
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onBlockPistonExtend(BlockPistonExtendEvent event)
    {
        List<Block> blocks = event.getBlocks();
        World world = event.getBlock().getWorld();

        final boolean superHardStone = CFG.getBoolean(RootNode.SUPER_HARD_STONE, world.getName());

        //TODO EhmBlockOrePlacementEvent
        if (superHardStone)
        {
            // which blocks are being pushed?
            for (Block block : blocks)
            {
                // if any are ore or stone, don't push
                Material material = block.getType();
                if (material == Material.STONE || material.name().endsWith("_ORE"))
                {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    /**
     * When a piston pulls...
     * prevent players from circumventing hardened stone rules by
     * placing ore, then pulling the ore next to stone before breaking it
     * @param event - Event that occurred.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onBlockPistonRetract(BlockPistonRetractEvent event)
    {
        Block block = event.getRetractLocation().getBlock();
        World world = block.getWorld();

        final boolean hardStoneEnabled = CFG.getBoolean(RootNode.SUPER_HARD_STONE, world.getName());

        //TODO EhmBlockOrePlacementEvent
        // we only care about sticky pistons
        if (event.isSticky() && hardStoneEnabled)
        {
            Material material = block.getType();
            if (material == Material.STONE || material.name().endsWith("_ORE"))
            {
                event.setCancelled(true);
                return;
            }
        }
    }
}
