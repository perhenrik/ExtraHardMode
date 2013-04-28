package me.ryanhamshire.ExtraHardMode.task;

import me.ryanhamshire.ExtraHardMode.ExtraHardMode;
import me.ryanhamshire.ExtraHardMode.module.BlockModule;
import me.ryanhamshire.ExtraHardMode.module.DataStoreModule;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Gradually let's Logs which have been marked as loose fall down.
 */
public class FallingLogsTask //extends BukkitRunnable
{
    /**
     * Reference to the plugin using this class
     */
    //private final ExtraHardMode plugin;
    /**
     * Where our "loose" Logs are stored
     */
    //private final DataStoreModule dataStoreModule;
    /**
     * BlockModule to spawn FallingBlocks
     */
    //private final BlockModule blockModule;
    /**
     * Delay before next block falls
     */
    //private final long delay;

    /**
     * Constructor
     * @param plugin reference to the plugin
     * @param delay before the next block falls
     */
    /*
    public FallingLogsTask (ExtraHardMode plugin, long delay)
    {
        this.plugin = plugin;
        dataStoreModule = plugin.getModuleForClass(DataStoreModule.class);
        blockModule = plugin.getModuleForClass(BlockModule.class);
        this.delay = delay;
    }
    @Override
    public void run ()
    {
        Block rdmLog = dataStoreModule.getRdmLog();
        if (rdmLog != null)
        {
            dataStoreModule.rmLog(rdmLog);
            //Clear the area below of leaves
            Block below = rdmLog;
            List<Block> looseLogs = new ArrayList<Block>();
            looseLogs.add(rdmLog);
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
                UUID id = blockModule.applyPhysics(looseLog);
                //Save the Location so when the Block lands we know that we spawned it
                dataStoreModule.addFallLog(id, looseLog.getLocation());
            }
        }
        if (dataStoreModule.getRdmLog() != null) //More Logs
        {
            plugin.getServer().getScheduler().runTaskLater(plugin, new FallingLogsTask(plugin, delay), delay);
        }
        else //try to stop the task
        {
            dataStoreModule.rmRunningTask(FallingLogsTask.class);
            try
            {
                cancel();
            }
            catch (IllegalStateException ignored){}
        }
    }*/
}