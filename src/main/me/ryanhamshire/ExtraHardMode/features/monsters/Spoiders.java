package me.ryanhamshire.ExtraHardMode.features.monsters;

import me.ryanhamshire.ExtraHardMode.ExtraHardMode;
import me.ryanhamshire.ExtraHardMode.config.RootConfig;
import me.ryanhamshire.ExtraHardMode.config.RootNode;
import me.ryanhamshire.ExtraHardMode.task.WebCleanupTask;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Spider;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: max
 * Date: 3/15/13
 * Time: 1:21 AM
 * To change this template use File | Settings | File Templates.
 */
public class Spoiders implements Listener
{
    ExtraHardMode plugin;
    RootConfig CFG;

    public Spoiders(ExtraHardMode plugin)
    {
        this.plugin = plugin;
        CFG = plugin.getModuleForClass(RootConfig.class);
    }

    /**
     * More spiders in caves
     * @param event
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onEntitySpawn(CreatureSpawnEvent event)
    {
        Location location = event.getLocation();
        World world = location.getWorld();
        LivingEntity entity = event.getEntity();
        EntityType entityType = entity.getType();

        final int spiderBonusSpawnPercent = CFG.getInt(RootNode.BONUS_UNDERGROUND_SPIDER_SPAWN_PERCENT, world.getName());

        // FEATURE: more spiders underground
        if (entityType == EntityType.ZOMBIE && world.getEnvironment() == World.Environment.NORMAL && location.getBlockY() < world.getSeaLevel() - 5)
        {
            if (plugin.random(spiderBonusSpawnPercent))
            {
                event.setCancelled(true);
                entityType = EntityType.SPIDER;
                world.spawnEntity(location, entityType);
            }
        }
    }

    /**
     * drop web on death
     * @param event
     */
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event)
    {
        LivingEntity entity = event.getEntity();
        World world = entity.getWorld();

        final boolean spidersDropWebOnDeath= CFG.getBoolean(RootNode.SPIDERS_DROP_WEB_ON_DEATH, world.getName());

        // FEATURE: spiders drop web on death
        if (spidersDropWebOnDeath)
        {
            if (entity instanceof Spider)
            {
                // random web placement
                long serverTime = world.getFullTime();
                int random1 = (int) (serverTime + entity.getLocation().getBlockZ()) % 9;
                int random2 = (int) (serverTime + entity.getLocation().getBlockX()) % 9;

                Location[] locations = new Location[4];

                locations[0] = entity.getLocation().add(random1, 0, random2);
                locations[1] = entity.getLocation().add(-random2, 0, random1 / 2);
                locations[2] = entity.getLocation().add(-random1 / 2, 0, -random2);
                locations[3] = entity.getLocation().add(random1 / 2, 0, -random2 / 2);

                List<Block> changedBlocks = new ArrayList<Block>();
                for (Location location : locations)
                {
                    Block block = location.getBlock();

                    // don't replace anything solid with web
                    if (block.getType() != Material.AIR)
                        continue;

                    // only place web on the ground, not hanging up in the air
                    do
                    {
                        block = block.getRelative(BlockFace.DOWN);
                    } while (block.getType() == Material.AIR);

                    // don't place web over fluids or stack webs
                    if (!block.isLiquid() && block.getType() != Material.WEB)
                    {
                        block = block.getRelative(BlockFace.UP);

                        // don't place next to cactus, because it will break the
                        // cactus
                        Block[] adjacentBlocks = new Block[]{block.getRelative(BlockFace.EAST), block.getRelative(BlockFace.WEST),
                                block.getRelative(BlockFace.NORTH), block.getRelative(BlockFace.SOUTH)};

                        boolean nextToCactus = false;
                        for (Block adjacentBlock : adjacentBlocks)
                        {
                            if (adjacentBlock.getType() == Material.CACTUS)
                            {
                                nextToCactus = true;
                                break;
                            }
                        }

                        if (!nextToCactus)
                        {
                            block.setType(Material.WEB);
                            changedBlocks.add(block);
                        }
                    }
                }

                // any webs placed above sea level will be automatically cleaned up
                // after a short time
                if (entity.getLocation().getBlockY() >= entity.getLocation().getWorld().getSeaLevel() - 5)
                {
                    WebCleanupTask task = new WebCleanupTask(changedBlocks);
                    plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, task, 20L * 30);
                }
            }
        }
    }
}
