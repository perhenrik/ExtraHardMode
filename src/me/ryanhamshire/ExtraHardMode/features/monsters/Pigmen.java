package me.ryanhamshire.ExtraHardMode.features.monsters;

import me.ryanhamshire.ExtraHardMode.ExtraHardMode;
import me.ryanhamshire.ExtraHardMode.config.RootConfig;
import me.ryanhamshire.ExtraHardMode.config.RootNode;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.PigZombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Created with IntelliJ IDEA.
 * User: max
 * Date: 3/15/13
 * Time: 1:20 AM
 * To change this template use File | Settings | File Templates.
 */
public class Pigmen implements Listener
{
    ExtraHardMode plugin;
    RootConfig rootC;

    public Pigmen (ExtraHardMode plugin)
    {
        this.plugin = plugin;
        rootC = plugin.getModuleForClass(RootConfig.class);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event)
    {
        LivingEntity entity = event.getEntity();
        World world = entity.getWorld();

        if (!rootC.getStringList(RootNode.WORLDS).contains(world.getName()))
        {
            return;
        }

        // FEATURE: pig zombies drop nether wart when slain in nether fortresses
        if (rootC.getBoolean(RootNode.FORTRESS_PIGS_DROP_WART) && world.getEnvironment().equals(World.Environment.NETHER) && entity instanceof PigZombie)
        {
            Block underBlock = entity.getLocation().getBlock().getRelative(BlockFace.DOWN);
            if (underBlock.getType() == Material.NETHER_BRICK)
            {
                event.getDrops().add(new ItemStack(Material.NETHER_STALK));
            }
        }

        // FEATURE: pig zombies sometimes drop nether wart when slain elsewhere
        else if (rootC.getInt(RootNode.NETHER_PIGS_DROP_WART) > 0 && world.getEnvironment().equals(World.Environment.NETHER) && entity instanceof PigZombie)
        {
            if (plugin.random(rootC.getInt(RootNode.NETHER_PIGS_DROP_WART)))
                event.getDrops().add(new ItemStack(Material.NETHER_STALK));
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onEntitySpawn(CreatureSpawnEvent event)
    {
        Location location = event.getLocation();
        World world = location.getWorld();
        if (!rootC.getStringList(RootNode.WORLDS).contains(world.getName()))
            return;

        LivingEntity entity = event.getEntity();

        // FEATURE: always-angry pig zombies
        if (rootC.getBoolean(RootNode.ALWAYS_ANGRY_PIG_ZOMBIES))
        {
            if (entity instanceof PigZombie)
            {
                PigZombie pigZombie = (PigZombie) entity;
                pigZombie.setAnger(Integer.MAX_VALUE);
            }
        }
    }
    /**
     * when a chunk loads...
     * Always angry pigzombies
     *
     * @param event - Event that occurred.
     */
    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event)
    {
        Chunk chunk = event.getChunk();
        World world = chunk.getWorld();

        if (!rootC.getStringList(RootNode.WORLDS).contains(world.getName()))
            return;

        // FEATURE: always-angry pig zombies
        if (rootC.getBoolean(RootNode.ALWAYS_ANGRY_PIG_ZOMBIES))
        {
            for (Entity entity : chunk.getEntities())
            {
                if (entity instanceof PigZombie)
                {
                    PigZombie pigZombie = (PigZombie) entity;
                    pigZombie.setAnger(Integer.MAX_VALUE);
                }
            }
        }
    }
}
