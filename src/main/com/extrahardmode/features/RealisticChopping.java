package com.extrahardmode.features;

import com.extrahardmode.ExtraHardMode;
import com.extrahardmode.config.RootConfig;
import com.extrahardmode.config.RootNode;
import com.extrahardmode.module.BlockModule;
import com.extrahardmode.module.DataStoreModule;
import com.extrahardmode.module.EntityModule;
import com.extrahardmode.service.PermissionNode;
import com.extrahardmode.task.FallingLogsTask;
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

/**
 * When chopping down trees the logs fall down and loose logs fall down on the side and can injure you
 */
public class RealisticChopping implements Listener
{
    /**
     * Plugin-Reference to get modules
     */
    ExtraHardMode plugin;
    /**
     * Config Instance
     */
    RootConfig CFG;
    /**
     * Stuff like FallingBlocks etc.
     */
    BlockModule blockModule;
    /**
     * Temporarily store data like logs that are supposed to fall
     */
    DataStoreModule dataStoreModule;
    /**
     * Stuff with Entities like MetaData
     */
    EntityModule entityModule;

    /**
     * Constructor
     * @param plugin
     */
    public RealisticChopping (ExtraHardMode plugin)
    {
        this.plugin = plugin;
        CFG = plugin.getModuleForClass(RootConfig.class);
        blockModule = plugin.getModuleForClass(BlockModule.class);
        dataStoreModule = plugin.getModuleForClass(DataStoreModule.class);
        entityModule = plugin.getModuleForClass(EntityModule.class);
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

        final boolean betterTreeChoppingEnabled = CFG.getBoolean(RootNode.BETTER_TREE_CHOPPING, world.getName());
        final boolean playerHasBypass = player != null ? player.hasPermission(PermissionNode.BYPASS.getNode())
                || player.getGameMode().equals(GameMode.CREATIVE) : true;

        // FEATURE: trees chop more naturally
        if (block.getType() == Material.LOG && betterTreeChoppingEnabled &&! playerHasBypass)
        {
            //Are there any leaves above the log? -> tree
            boolean isTree = false;
            checkers : for (int i = 1; i < 30; i++)
            {
                Material upType = block.getRelative(BlockFace.UP, i).getType();
                switch (upType)
                {
                    case LEAVES:
                    {
                        isTree = true;
                        break checkers;
                    }
                    case AIR: case LOG:
                    {
                        break; //skip to next iteration
                    }
                    default: //if something other than log/air this is most likely part of a building
                    {
                        break checkers;
                    }
                }
            }

            if (isTree)
            {
                Block aboveLog = block.getRelative(BlockFace.UP);
                loop : for (int limit = 0; limit < 30; limit++)
                {
                    switch (aboveLog.getType())
                    {
                        case AIR:
                        {
                            Block[] logs = blockModule.getBlocksInArea(aboveLog.getLocation(), 1, 4, Material.LOG);
                            for (Block log : logs)
                            {
                                //check 2 blocks down for logs to see if it it's a stem
                                if (log.getRelative(BlockFace.DOWN).getType() != Material.LOG && log.getRelative(BlockFace.DOWN, 2).getType() != Material.LOG)
                                    plugin.getServer().getScheduler().runTaskLater(plugin, new FallingLogsTask(plugin, log), plugin.getRandom().nextInt(50/*so they don't fall at once*/));
                            }
                            break; //can air fall?
                        }
                        case LOG:
                        {
                            blockModule.applyPhysics(aboveLog, false);
                            break;
                        }
                        default: //we reached something that is not part of a tree or leaves
                            break loop;
                    }
                    aboveLog = aboveLog.getRelative(BlockFace.UP);
                }
            }
        }
    }
}
