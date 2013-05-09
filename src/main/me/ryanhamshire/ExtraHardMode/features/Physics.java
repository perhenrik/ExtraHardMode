package me.ryanhamshire.ExtraHardMode.features;

import me.ryanhamshire.ExtraHardMode.ExtraHardMode;
import me.ryanhamshire.ExtraHardMode.config.RootConfig;
import me.ryanhamshire.ExtraHardMode.config.RootNode;
import me.ryanhamshire.ExtraHardMode.module.BlockModule;
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
import org.bukkit.event.block.BlockPlaceEvent;

public class Physics implements Listener
{
    ExtraHardMode plugin = null;
    RootConfig CFG = null;
    BlockModule blockModule = null;

    public Physics (ExtraHardMode plugin)
    {
        this.plugin = plugin;
        CFG = plugin.getModuleForClass(RootConfig.class);
        blockModule = plugin.getModuleForClass(BlockModule.class);
    }
    /**
     * When a player places a block...
     *
     * @param placeEvent - Event that occurred
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onBlockPlace(BlockPlaceEvent placeEvent)
    {
        Player player = placeEvent.getPlayer();
        Block block = placeEvent.getBlock();
        World world = block.getWorld();

        final boolean physixEnabled = CFG.getBoolean(RootNode.MORE_FALLING_BLOCKS_ENABLE, world.getName())
                                      &&! player.hasPermission(PermissionNode.BYPASS.getNode())
                                      &&! player.getGameMode().equals(GameMode.CREATIVE);

        if (physixEnabled)
        {
            blockModule.physicsCheck(block, 0, true);
        }
    }

    /**
     * When a player breaks a block...
     *
     * @param breakEvent - Event that occurred.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent breakEvent)
    {
        Block block = breakEvent.getBlock();
        World world = block.getWorld();
        Player player = breakEvent.getPlayer();

        final boolean moreFallingBlocksEnabled = CFG.getBoolean(RootNode.MORE_FALLING_BLOCKS_ENABLE, world.getName());
        final int netherRackFirePercent = CFG.getInt(RootNode.BROKEN_NETHERRACK_CATCHES_FIRE_PERCENT, world.getName());
        final boolean playerHasBypass = player != null ? player.hasPermission(PermissionNode.BYPASS.getNode())
                                   || player.getGameMode().equals(GameMode.CREATIVE) : true;



        // FEATURE: more falling blocks
        if (moreFallingBlocksEnabled &&! playerHasBypass)
        {
            blockModule.physicsCheck(block, 0, true);
        }

        // FEATURE: breaking netherrack may start a fire
        if (netherRackFirePercent > 0 && block.getType() == Material.NETHERRACK &&! playerHasBypass)
        {
            Block underBlock = block.getRelative(BlockFace.DOWN);
            if (underBlock.getType() == Material.NETHERRACK && plugin.random(netherRackFirePercent))
            {
                breakEvent.setCancelled(true);
                block.setType(Material.FIRE);
            }
        }
    }
}
