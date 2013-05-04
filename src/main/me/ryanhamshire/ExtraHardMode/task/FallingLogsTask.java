package me.ryanhamshire.ExtraHardMode.task;

<<<<<<< Updated upstream
=======
import me.ryanhamshire.ExtraHardMode.ExtraHardMode;
import me.ryanhamshire.ExtraHardMode.module.BlockModule;
import me.ryanhamshire.ExtraHardMode.module.DataStoreModule;
import org.apache.commons.lang.Validate;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

>>>>>>> Stashed changes
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
            //Clear the area below of leaves
            Block below = block;
            List<Block> looseLogs = new ArrayList<Block>();
            looseLogs.add(block);
            checkBelow : while (below.getY() > 0)
            {
                below = below.getRelative(BlockFace.DOWN);
                switch (below.getType())
                {
                    case AIR:
                    {
                        //ignore and go one down
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
                                looseLogs.add(below);
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
            for (Block looseLog : looseLogs)
            {
                blockModule.applyPhysics(looseLog, true);
            }
        }
    }
}