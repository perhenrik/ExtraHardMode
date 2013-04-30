package me.ryanhamshire.ExtraHardMode.features;

import me.ryanhamshire.ExtraHardMode.ExtraHardMode;
import me.ryanhamshire.ExtraHardMode.config.RootConfig;
import me.ryanhamshire.ExtraHardMode.config.RootNode;
import me.ryanhamshire.ExtraHardMode.module.BlockModule;
import me.ryanhamshire.ExtraHardMode.module.DataStoreModule;
import me.ryanhamshire.ExtraHardMode.service.PermissionNode;
import me.ryanhamshire.ExtraHardMode.task.FallingLogsTask;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;

import java.util.List;
import java.util.UUID;

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
     * Temmporarily store data like logs that are supposed to fall
     */
    DataStoreModule dataStoreModule;

    public RealisticChopping (ExtraHardMode plugin)
    {
        this.plugin = plugin;
        CFG = plugin.getModuleForClass(RootConfig.class);
        blockModule = plugin.getModuleForClass(BlockModule.class);
        dataStoreModule = plugin.getModuleForClass(DataStoreModule.class);
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
        final boolean playerPerm = player != null ? player.hasPermission(PermissionNode.BYPASS.getNode())
                || player.getGameMode().equals(GameMode.CREATIVE) : true;

        // FEATURE: trees chop more naturally
        if (block.getType() == Material.LOG && betterTreeChoppingEnabled &&! playerPerm)
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
                    case AIR:case LOG:
                    {
                        break;
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
                                blockModule.applyPhysics(log, true);
                                //dataStoreModule.addLog(log.getLocation(), true);
                            }
                            //Only run that task once, it will cancel itself when all Blocks are used up
                            if (logs.length > 0 && !dataStoreModule.isTaskRunning(FallingLogsTask.class))
                            {
                                //plugin.getServer().getScheduler().runTaskLater(plugin, new FallingLogsTask(plugin, 1L), 2L);
                                //dataStoreModule.addRunningTask(FallingLogsTask.class, 1234567890);
                            }
                            break; //can air fall?
                        }
                        case LOG:
                            UUID id =  blockModule.applyPhysics(aboveLog);
                            break;
                        default: //we reached something that is not part of a tree or leaves
                            break loop;
                    }
                    aboveLog = aboveLog.getRelative(BlockFace.UP);
                }
            }
        }
    }

    /**
     * Called when an Entity forms a Block
     * - Damage Player when a FallingBlock hits him
     * @param event
     */
    @EventHandler
    public void whenBlockLands(EntityChangeBlockEvent event)
    {
        Entity entity = event.getEntity();
        Material to = event.getTo();

        //Only when Block has been marked to deal damage
        if (entity.getType().equals(EntityType.FALLING_BLOCK) && to.equals(Material.LOG) && entity.hasMetadata("key") && entity.getMetadata("key").get(0).asBoolean()/*dataStoreModule.isMarkedForProcessing(entity.getUniqueId())*/)
        {
            //dataStoreModule.rmFallLogById(entity.getUniqueId());
            List<Entity> entities =  entity.getNearbyEntities(1, 2, 1);
            for (Entity ent : entities)
            {
                if (ent instanceof LivingEntity)
                {
                    LivingEntity player = (LivingEntity) ent;
                    //Frighten the player
                    player.damage(6, entity);
                }
            }
        }
    }

    /**
     * When a Falling Block is destroyed
     * @param event
     */
    /*@EventHandler
    public void fallingBlockDestroyed (ItemSpawnEvent event)
    {
        Location loc = event.getLocation();
        Entity entity = event.getEntity();

        if (entity instanceof Item && ((Item)entity).getItemStack().getType().equals(Material.LOG))
        {
            Block newBlock = loc.getBlock().getRelative(BlockFace.DOWN);
            if (dataStoreModule.isBlockFallingAtLoc(loc))
            {
                if (blockModule.breaksFallingBlock(newBlock.getType()))
                    newBlock.breakNaturally();
                dataStoreModule.rmFallLogsByLoc(loc);
            }
        }
    } */
}
