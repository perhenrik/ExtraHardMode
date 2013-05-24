package com.extrahardmode.features;


import com.extrahardmode.ExtraHardMode;
import com.extrahardmode.config.RootConfig;
import com.extrahardmode.config.RootNode;
import com.extrahardmode.config.messages.MessageNode;
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

        // FEATURE: stone breaks tools much more quickly
        if (hardStoneEnabled &&! player.getGameMode().equals(GameMode.CREATIVE))
        {
            ItemStack inHandStack = player.getItemInHand();

            // if breaking stone with an item in hand and the player does NOT have the bypass permission
            //TODO Config for endstone
            if ((block.getType() == Material.STONE || block.getType() == Material.ENDER_STONE) && inHandStack != null)
            {
                // if not using an iron or diamond pickaxe, don't allow breakage and explain to the player
                Material tool = inHandStack.getType();
                if (tool != Material.IRON_PICKAXE && tool != Material.DIAMOND_PICKAXE)
                {
                    messenger.notifyPlayer(player, MessageNode.STONE_MINING_HELP, PermissionNode.SILENT_STONE_MINING_HELP);
                    event.setCancelled(true);
                    return;
                }

                // otherwise, drastically reduce tool durability when breaking stone
                else
                {
                    int amount;

                    if (tool == Material.IRON_PICKAXE)
                        amount = stoneBlocksIron;
                    else
                        amount = stoneBlocksDiamond;

                    int maxDurability = 0;
                    if (amount != 0)
                        maxDurability = tool.getMaxDurability();
                    else return;
                    int damagePerBlock = maxDurability / amount;

                    inHandStack.setDurability((short) (inHandStack.getDurability() + damagePerBlock));

                    // For cases where a remainder causes the tool to be viable for an extra use,
                    //   eat up the remainder of thet durability
                    if ( maxDurability - inHandStack.getDurability() < tool.getMaxDurability() / amount )
                        inHandStack.setDurability((short) maxDurability);
                }
            }
        }

        // when ore is broken, it softens adjacent stone important to ensure players can reach the ore they break
        if (hardStonePhysix && ((block.getType().name().endsWith("ORE") || block.getType().name().endsWith("ORES"))))
        {
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
