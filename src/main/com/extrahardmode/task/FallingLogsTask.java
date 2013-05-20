package com.extrahardmode.task;


import com.extrahardmode.ExtraHardMode;
import com.extrahardmode.module.BlockModule;
import com.extrahardmode.module.DataStoreModule;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.ArrayList;
import java.util.List;
/**
 * Gradually let's Logs which have been marked as loose fall down.
 */
public class FallingLogsTask implements Runnable
{
    /**
     * Reference to the plugin using this class
     */
    private final ExtraHardMode plugin;
    /**
     * Where our "loose" Logs are stored
     */
    private final DataStoreModule dataStoreModule;
    /**
     * BlockModule to spawn FallingBlocks
     */
    private final BlockModule blockModule;
    /**
     * Block to apply physics to
     */
    private final Block block;

    /**
     * Constructor
     * @param plugin reference to the plugin
     * @param block to apply physics to
     */

    public FallingLogsTask (ExtraHardMode plugin, Block block)
    {
        Validate.notNull(block, "Block can't be null");
        Validate.notNull(plugin, "Plugin can't be null");

        this.block = block;
        this.plugin = plugin;
        dataStoreModule = plugin.getModuleForClass(DataStoreModule.class);
        blockModule = plugin.getModuleForClass(BlockModule.class);
    }
    @Override
    public void run ()
    {
        if (block != null)
        {
            /* Prevent wooden structures near trees from being affected*/
            if (blockModule.getBlocksInArea(block.getLocation(), 2, 1, Material.LEAVES).length > 4)
            {
                //Clear the area below of leaves
                Block below = block;
                List<Block> looseLogs = new ArrayList<Block>();
                List<Block> tempBlocks = new ArrayList<Block>();
                looseLogs.add(block);
                checkBelow : for (int i = 0; below.getY() > 0; i++)
                {
                    below = below.getRelative(BlockFace.DOWN);
                    switch (below.getType())
                    {
                        case AIR:
                        {
                            //go one down
                            //All blocks above this can fall now that there is an air block
                            looseLogs.addAll(tempBlocks);
                            tempBlocks.clear();
                            break;
                        }
                        case LEAVES:
                        {
                            below.breakNaturally();
                            break;
                        }
                        case LOG:
                        {
                            //Prevent Logs on adjacent sides (Jungle Tree) from turning to FallingBlocks and some of them turning into items
                            switch (below.getRelative(BlockFace.DOWN).getType())
                            {
                                case AIR:case LEAVES:
                                    tempBlocks.add(below);
                            }
                            break;
                        }
                        default: //we hit the block where the FallingBlock will land
                        {
                            if (blockModule.breaksFallingBlock(below.getType()))
                            {
                                below.breakNaturally();
                            }
                            else
                            {
                                break checkBelow;
                            }
                        }
                    }
                }

                for (int i = 0; i < looseLogs.size(); i++)
                {
                    final Block looseLog = looseLogs.get(i);
                    plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
                        @Override
                        public void run() {
                            blockModule.applyPhysics(looseLog, true);
                        }
                    }, i /*delay to prevent FallingBlock collision*/);

                }
            }
        }
    }
}