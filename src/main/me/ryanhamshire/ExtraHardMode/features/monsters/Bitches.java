package me.ryanhamshire.ExtraHardMode.features.monsters;

import me.ryanhamshire.ExtraHardMode.ExtraHardMode;
import me.ryanhamshire.ExtraHardMode.config.ExplosionType;
import me.ryanhamshire.ExtraHardMode.config.RootConfig;
import me.ryanhamshire.ExtraHardMode.config.RootNode;
import me.ryanhamshire.ExtraHardMode.module.EntityModule;
import me.ryanhamshire.ExtraHardMode.task.CreateExplosionTask;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.PotionSplashEvent;

/**
 * Created with IntelliJ IDEA.
 * User: max
 * Date: 3/15/13
 * Time: 2:03 AM
 * To change this template use File | Settings | File Templates.
 */
public class Bitches implements Listener
{
    ExtraHardMode plugin;
    RootConfig CFG;
    EntityModule entityModule;

    public Bitches (ExtraHardMode plugin)
    {
        this.plugin = plugin;
        CFG = plugin.getModuleForClass(RootConfig.class);
        entityModule = plugin.getModuleForClass(EntityModule.class);
    }

    /**
     * When an Entity spawns:
     * Spawn a Witch above ground sometimes instead of a Zombie
     * @param event
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onEntitySpawn(CreatureSpawnEvent event)
    {
        Location location = event.getLocation();
        World world = location.getWorld();
        LivingEntity entity = event.getEntity();
        EntityType entityType = entity.getType();

        final int witchSpawnPercent = CFG.getInt(RootNode.BONUS_WITCH_SPAWN_PERCENT, world.getName());

        // FEATURE: more witches above ground (on grass)
        if (entityType == EntityType.ZOMBIE && world.getEnvironment() == World.Environment.NORMAL
        && entity.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == Material.GRASS)
        {
            if (plugin.random(witchSpawnPercent))
            {
                event.setCancelled(true);
                entityModule.spawn(location, EntityType.WITCH);
            }
        }
    }

    /**
     * When a potion breaks
     * atm this is used for witches. When they throw a potion we sometimes spawn explosions or monsters
     *
     * @param event - Event that occurred.
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onPotionSplash(PotionSplashEvent event)
    {
        ThrownPotion potion = event.getPotion();
        Location location = potion.getLocation();
        World world = location.getWorld();

        final boolean additionalAttacks = CFG.getBoolean(RootNode.WITCHES_ADDITIONAL_ATTACKS, world.getName());

        // FEATURE: enhanced witches. they throw wolf spawner and teleport potions as well as poison potions
        LivingEntity shooter = potion.getShooter();
        if (additionalAttacks && shooter != null && shooter.getType() == EntityType.WITCH)
        {
            Witch witch = (Witch) shooter;

            int random = plugin.getRandom().nextInt(100);

            boolean makeExplosion = false;

            // 30% summon zombie
            if (random < 30)
            {
                event.setCancelled(true);

                boolean zombieNearby = false;
                for (Entity entity : location.getChunk().getEntities())
                {
                    if (entity.getType() == EntityType.ZOMBIE)
                    {
                        Zombie zombie = (Zombie) entity;
                        if (zombie.isVillager() && zombie.isBaby())
                        {
                            zombieNearby = true;
                            break;
                        }
                    }
                }

                if (!zombieNearby)
                {
                    Zombie zombie = (Zombie) entityModule.spawn(location, EntityType.ZOMBIE);
                    zombie.setVillager(true);
                    zombie.setBaby(true);
                    if (zombie.getTarget() != null)
                    {
                        zombie.setTarget(witch.getTarget());
                    }

                    entityModule.markLootLess(zombie);
                }
                else
                {
                    makeExplosion = true;
                }
            }
            else if (random < 60)
            {
                // 30% teleport
                event.setCancelled(true);
                witch.teleport(location);
            }
            else if (random < 90)
            {
                // 30% explosion
                event.setCancelled(true);
                makeExplosion = true;
            }
            else
            {
                // otherwise poison potion (selective target)
                for (LivingEntity target : event.getAffectedEntities())
                {
                    if (target.getType() != EntityType.PLAYER)
                    {
                        event.setIntensity(target, 0);
                    }
                }
            }

            // if explosive potion, direct damage to players in the area
            if (makeExplosion)
            {
                // explosion just for show, no damage
                new CreateExplosionTask(plugin, location, ExplosionType.EFFECT).run();

                for (LivingEntity target : event.getAffectedEntities())
                {
                    if (target.getType() == EntityType.PLAYER)
                    {
                        target.damage(3);
                    }
                }
            }
        }

    }

}
