package me.ryanhamshire.ExtraHardMode.features;


import me.ryanhamshire.ExtraHardMode.ExtraHardMode;
import me.ryanhamshire.ExtraHardMode.config.RootConfig;
import me.ryanhamshire.ExtraHardMode.config.RootNode;
import me.ryanhamshire.ExtraHardMode.config.messages.MessageConfig;
import me.ryanhamshire.ExtraHardMode.config.messages.MessageNode;
import me.ryanhamshire.ExtraHardMode.module.UtilityModule;
import me.ryanhamshire.ExtraHardMode.service.PermissionNode;
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
    ExtraHardMode plugin = null;
    RootConfig rootC = null;
    UtilityModule utils = null;
    MessageConfig messages;

    public HardenedStone (ExtraHardMode plugin)
    {
        this.plugin = plugin;
        rootC = plugin.getModuleForClass(RootConfig.class);
        utils = plugin.getModuleForClass(UtilityModule.class);
        messages = plugin.getModuleForClass(MessageConfig.class);
    }

    /**
     * When a player breaks stone
     * @param event
     */
    @EventHandler
    public void onBlockBreak (BlockBreakEvent event)
    {
        //CFG
        boolean hardStoneEnabled = rootC.getBoolean(RootNode.SUPER_HARD_STONE);
        boolean hardStonePhysix = rootC.getBoolean(RootNode.SUPER_HARD_STONE_PHYSICS);
        int stoneBlocksIron = rootC.getInt(RootNode.IRON_DURABILITY_PENALTY);
        int stoneBlocksDiamond = rootC.getInt(RootNode.DIAMOND_DURABILITY_PENALTY);

        Block block = event.getBlock();
        World world = block.getWorld();
        Player player = event.getPlayer();

        // FEATURE: stone breaks tools much more quickly
        if (hardStoneEnabled &! player.getGameMode().equals(GameMode.CREATIVE))
        {
            ItemStack inHandStack = player.getItemInHand();

            // if breaking stone with an item in hand and the player does NOT have the bypass permission
            //TODO Config for endstone
            if ((block.getType() == Material.STONE || block.getType() == Material.ENDER_STONE) && inHandStack != null)
            {
                // if not using an iron or diamond pickaxe, don't allow breakage and
                // explain to the player
                Material tool = inHandStack.getType();
                if (tool != Material.IRON_PICKAXE && tool != Material.DIAMOND_PICKAXE)
                {
                    utils.notifyPlayer(player, MessageNode.STONE_MINING_HELP, PermissionNode.SILENT_STONE_MINING_HELP);
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

                    int maxDurability = tool.getMaxDurability();
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
            for (BlockFace face : utils.blockFaces)
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
        //CFG
        boolean hardstoneEnabled = rootC.getBoolean(RootNode.SUPER_HARD_STONE);

        Player player = placeEvent.getPlayer();
        Block block = placeEvent.getBlock();
        World world = block.getWorld();

        if (hardstoneEnabled &! player.getGameMode().equals(GameMode.CREATIVE)
            && (block.getType().name().endsWith("ORE") || block.getType().name().endsWith("ORES")))
        {
            ArrayList<Block> adjacentBlocks = new ArrayList<Block>();
            for (BlockFace face : utils.blockFaces)
            {
                adjacentBlocks.add(block.getRelative(face));
            }

            for (Block adjacentBlock : adjacentBlocks)
            {
                if (adjacentBlock.getType() == Material.STONE)
                {
                    plugin.sendMessage(player, messages.getString(MessageNode.NO_PLACING_ORE_AGAINST_STONE));
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

        if (!rootC.getBoolean(RootNode.SUPER_HARD_STONE) || !rootC.getStringList(RootNode.WORLDS).contains(world.getName()))
            return;

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

    /**
     * When a piston pulls...
     * prevent players from circumventing hardened stone rules by
     * placing ore, then pulling the ore next to stone before breaking it
     * @param event - Event that occurred.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onBlockPistonRetract(BlockPistonRetractEvent event)
    {
        // we only care about sticky pistons
        if (!event.isSticky())
            return;

        Block block = event.getRetractLocation().getBlock();
        World world = block.getWorld();

        if (!rootC.getBoolean(RootNode.SUPER_HARD_STONE) || !rootC.getStringList(RootNode.WORLDS).contains(world.getName()))
            return;

        Material material = block.getType();
        if (material == Material.STONE || material.name().endsWith("_ORE"))
        {
            event.setCancelled(true);
            return;
        }
    }
}
