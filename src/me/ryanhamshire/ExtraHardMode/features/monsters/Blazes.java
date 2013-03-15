package me.ryanhamshire.ExtraHardMode.features.monsters;


import me.ryanhamshire.ExtraHardMode.ExtraHardMode;
import me.ryanhamshire.ExtraHardMode.config.RootConfig;
import me.ryanhamshire.ExtraHardMode.config.RootNode;
import me.ryanhamshire.ExtraHardMode.config.messages.MessageConfig;
import me.ryanhamshire.ExtraHardMode.module.EntityModule;
import me.ryanhamshire.ExtraHardMode.module.UtilityModule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class Blazes implements Listener
{
    ExtraHardMode plugin = null;
    RootConfig rootC = null;
    MessageConfig messages;
    UtilityModule utils = null;
    EntityModule entityModule = null;

    public Blazes (ExtraHardMode plugin)
    {
        this.plugin = plugin;
        rootC = plugin.getModuleForClass(RootConfig.class);
        messages = plugin.getModuleForClass(MessageConfig.class);
        utils = plugin.getModuleForClass(UtilityModule.class);
        entityModule = plugin.getModuleForClass(EntityModule.class);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onEntitySpawn(CreatureSpawnEvent event)
    {
        Location location = event.getLocation();
        World world = location.getWorld();
        if (!rootC.getStringList(RootNode.WORLDS).contains(world.getName()))
            return;

        LivingEntity entity = event.getEntity();
        EntityType entityType = entity.getType();

        // FEATURE: more blazes in nether
        if (entityType == EntityType.PIG_ZOMBIE)
        {
            if (plugin.random(rootC.getInt(RootNode.BONUS_NETHER_BLAZE_SPAWN_PERCENT)))
            {
                event.setCancelled(true);
                entityType = EntityType.BLAZE;

                // FEATURE: magma cubes spawn with blazes
                if (plugin.random(rootC.getInt(RootNode.BONUS_NETHER_BLAZE_SPAWN_PERCENT)))
                {
                    MagmaCube cube = (MagmaCube) (world.spawnEntity(location, EntityType.MAGMA_CUBE));
                    cube.setSize(1);
                }
                world.spawnEntity(location, entityType);
            }
        }

        // FEATURE: blazes near bedrock
        if (entityType == EntityType.SKELETON && world.getEnvironment() == World.Environment.NORMAL && location.getBlockY() < 20)
        {
            if (plugin.random(rootC.getInt(RootNode.NEAR_BEDROCK_BLAZE_SPAWN_PERCENT)))
            {
                event.setCancelled(true);
                entityType = EntityType.BLAZE;
                world.spawnEntity(location, entityType);
            }
        }
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

        // FEATURE: nether blazes drop extra loot (glowstone and gunpowder)
        if (rootC.getBoolean(RootNode.BLAZES_DROP_BONUS_LOOT) && entity instanceof Blaze)
        {
            if (world.getEnvironment() == World.Environment.NETHER)
            {
                // 50% chance of each
                if (plugin.getRandom().nextInt(2) == 0)
                {
                    event.getDrops().add(new ItemStack(Material.SULPHUR, 2));
                }
                else
                {
                    event.getDrops().add(new ItemStack(Material.GLOWSTONE_DUST, 2));
                }
            }
            else // no drops in the normal world (restricting blaze rods to the
            // nether)
            {
                event.getDrops().clear();
            }
        }

        // FEATURE: blazes explode on death in normal world
        if (rootC.getBoolean(RootNode.BLAZES_EXPLODE_ON_DEATH) && entity instanceof Blaze && world.getEnvironment() == World.Environment.NORMAL
                && !rootC.getBoolean(RootNode.DISABLE_EXPLOSIONS))
        {
            // create explosion
            world.createExplosion(entity.getLocation(), 2F, true); // equal to a
            // TNT blast,
            // sets fires

            // fire a fireball straight up in normal worlds
            Fireball fireball = (Fireball) world.spawnEntity(entity.getLocation(), EntityType.FIREBALL);
            fireball.setDirection(new Vector(0, 10, 0));
            fireball.setYield(1);
        }

        // FEATURE: nether blazes drop extra loot (glowstone and gunpowder)
        if (rootC.getBoolean(RootNode.BLAZES_DROP_BONUS_LOOT) && entity instanceof Blaze)
        {
            if (world.getEnvironment() == World.Environment.NETHER)
            {
                // 50% chance of each
                if (plugin.getRandom().nextInt(2) == 0)
                {
                    event.getDrops().add(new ItemStack(Material.SULPHUR, 2));
                }
                else
                {
                    event.getDrops().add(new ItemStack(Material.GLOWSTONE_DUST, 2));
                }
            }
            else // no drops in the normal world (restricting blaze rods to the
            // nether)
            {
                event.getDrops().clear();
            }
        }

        // FEATURE: nether blazes may multiply on death
        final int blazeSplitPercent = rootC.getInt(RootNode.NETHER_BLAZES_SPLIT_ON_DEATH_PERCENT);
        if (blazeSplitPercent > 0 && world.getEnvironment() == World.Environment.NETHER && entity instanceof Blaze)
        {
            if (plugin.random(blazeSplitPercent))
            {
                Entity firstNewBlaze = world.spawnEntity(entity.getLocation(), EntityType.BLAZE);
                firstNewBlaze.setVelocity(new Vector(1, 0, 1));

                Entity secondNewBlaze = world.spawnEntity(entity.getLocation(), EntityType.BLAZE);
                secondNewBlaze.setVelocity(new Vector(-1, 0, -1));

                // if this blaze was marked lootless, mark the new blazes the same
                if (entityModule.isLootLess(entity))
                {
                    entityModule.markLootLess((LivingEntity) firstNewBlaze);
                    entityModule.markLootLess((LivingEntity) secondNewBlaze);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onEntityDamage(EntityDamageEvent event)
    {
        Entity entity = event.getEntity();
        EntityType entityType = entity.getType();
        World world = entity.getWorld();

        if (!rootC.getStringList(RootNode.WORLDS).contains(world.getName()))
            return;

        // FEATURE: magma cubes become blazes when they take damage
        if (entityType == EntityType.MAGMA_CUBE && rootC.getBoolean(RootNode.MAGMA_CUBES_BECOME_BLAZES_ON_DAMAGE) && !entity.isDead() && !rootC.getBoolean(RootNode.DISABLE_EXPLOSIONS))
        {
            entity.remove(); // remove magma cube
            entity.getWorld().spawnEntity(entity.getLocation().add(0, 2, 0), EntityType.BLAZE); // replace with blaze
            entity.getWorld().createExplosion(entity.getLocation(), 2F, true); // fiery explosion for effect
        }

        // FEATURE: blazes drop fire on hit, also in nether
        if (rootC.getBoolean(RootNode.BLAZES_DROP_FIRE_ON_DAMAGE))
        {
            if (entityType == EntityType.BLAZE)
            {
                Blaze blaze = (Blaze) entity;

                if (blaze.getHealth() > blaze.getMaxHealth() / 2)
                {

                    Block block = entity.getLocation().getBlock();

                    Block underBlock = block.getRelative(BlockFace.DOWN);
                    while (underBlock.getType() == Material.AIR)
                        underBlock = underBlock.getRelative(BlockFace.DOWN);

                    block = underBlock.getRelative(BlockFace.UP);
                    if (block.getType() == Material.AIR && underBlock.getType() != Material.AIR && !underBlock.isLiquid() && underBlock.getY() > 0)
                    {
                        block.setType(Material.FIRE);
                    }
                }
            }
        }

    }
}
