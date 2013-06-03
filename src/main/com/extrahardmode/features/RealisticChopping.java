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
import com.extrahardmode.module.BlockModule;
import com.extrahardmode.module.DataStoreModule;
import com.extrahardmode.module.EntityModule;
import com.extrahardmode.module.PlayerModule;
import com.extrahardmode.task.FallingLogsTask;
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
     * Temporarily store data
     */
    DataStoreModule dataStoreModule;
    /**
     * Stuff with Entities like MetaData
     */
    EntityModule entityModule;
    /**
     * Permissions etc.
     */
    PlayerModule playerModule;

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
        playerModule = plugin.getModuleForClass(PlayerModule.class);
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
        final boolean playerHasBypass = playerModule.playerBypasses(player, Feature.REALISTIC_CHOPPING);

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
                            Block[] logs = blockModule.getBlocksInArea(aboveLog.getLocation(), 1, 5, Material.LOG);
                            for (Block log : logs)
                            {
                                //TODO EhmRealisticChoppingLooseLogEvent
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
