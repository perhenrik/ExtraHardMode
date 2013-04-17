package me.ryanhamshire.ExtraHardMode.features.monsters;

import me.ryanhamshire.ExtraHardMode.ExtraHardMode;
import me.ryanhamshire.ExtraHardMode.config.RootConfig;
import me.ryanhamshire.ExtraHardMode.config.RootNode;
import me.ryanhamshire.ExtraHardMode.module.EntityModule;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.PigZombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Created with IntelliJ IDEA.
 * User: max
 * Date: 3/15/13
 * Time: 1:20 AM
 * To change this template use File | Settings | File Templates.
 */
public class Pigies implements Listener
{
    ExtraHardMode plugin;
    RootConfig CFG;

    public Pigies(ExtraHardMode plugin)
    {
        this.plugin = plugin;
        CFG = plugin.getModuleForClass(RootConfig.class);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event)
    {
        LivingEntity entity = event.getEntity();
        World world = entity.getWorld();

        final boolean pigWartFortress = CFG.getBoolean(RootNode.FORTRESS_PIGS_DROP_WART, world.getName());
        final int  pigWartDropEveryWherePercent = CFG.getInt(RootNode.NETHER_PIGS_DROP_WART, world.getName());

        // FEATURE: pig zombies drop nether wart when slain in nether fortresses
        if (pigWartFortress && world.getEnvironment().equals(World.Environment.NETHER) && entity instanceof PigZombie)
        {
            Block underBlock = entity.getLocation().getBlock().getRelative(BlockFace.DOWN);
            if (underBlock.getType() == Material.NETHER_BRICK)
            {
                event.getDrops().add(new ItemStack(Material.NETHER_STALK));
            }
        }

        // FEATURE: pig zombies sometimes drop nether wart when slain elsewhere
        else if (pigWartDropEveryWherePercent > 0 && world.getEnvironment().equals(World.Environment.NETHER) && entity instanceof PigZombie)
        {
            if (plugin.random(pigWartDropEveryWherePercent))
                event.getDrops().add(new ItemStack(Material.NETHER_STALK));
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onEntitySpawn(CreatureSpawnEvent event)
    {
        Location location = event.getLocation();
        World world = location.getWorld();
        LivingEntity entity = event.getEntity();

        final boolean pigsAlwaysAggro = CFG.getBoolean(RootNode.ALWAYS_ANGRY_PIG_ZOMBIES, world.getName());

        // FEATURE: always-angry pig zombies
        if (pigsAlwaysAggro)
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

        final boolean pigAlwaysAggro = CFG.getBoolean(RootNode.ALWAYS_ANGRY_PIG_ZOMBIES, world.getName());

        // FEATURE: always-angry pig zombies
        if (pigAlwaysAggro)
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

    /**
     * When a lightning strikes
     * spawn a pigmen
     */
    @EventHandler
    public void onLightingStrike(LightningStrikeEvent event)
    {
        LightningStrike strike = event.getLightning();
        EntityModule entities = plugin.getModuleForClass(EntityModule.class);

        Location loc = strike.getLocation();
        World world = loc.getWorld();

        final boolean spawnPigsOnLightning = CFG.getBoolean(RootNode.LIGHTNING_SPAWNS_PIGMEN, world.getName());

        if (spawnPigsOnLightning && entities.simpleIsLocSafeSpawn(loc))
        {
            int rdm = plugin.getRandom().nextInt(10);
            int amount = 1;
            switch (rdm)
            {
                case 0:case 1: //20%
                {
                    amount = 2;
                    break;
                }
                case 2:case 3://20%
                {
                    amount = 3;
                }
                default:
                {
                    amount = 1;
                }
            }
            for (int i = 0; i < amount; i++)
                world.spawn(loc, PigZombie.class);
        }
    }
}