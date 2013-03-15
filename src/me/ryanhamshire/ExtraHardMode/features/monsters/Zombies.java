package me.ryanhamshire.ExtraHardMode.features.monsters;

import me.ryanhamshire.ExtraHardMode.ExtraHardMode;
import me.ryanhamshire.ExtraHardMode.config.RootConfig;
import me.ryanhamshire.ExtraHardMode.config.RootNode;
import me.ryanhamshire.ExtraHardMode.service.PermissionNode;
import me.ryanhamshire.ExtraHardMode.task.RespawnZombieTask;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Created with IntelliJ IDEA.
 * User: max
 * Date: 3/15/13
 * Time: 1:20 AM
 * To change this template use File | Settings | File Templates.
 */
public class Zombies implements Listener
{
    ExtraHardMode plugin;
    RootConfig rootC;

    public Zombies (ExtraHardMode plugin)
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

        // FEATURE: zombies may reanimate if not on fire when they die
        final int zombiesReanimatePercent = rootC.getInt(RootNode.ZOMBIES_REANIMATE_PERCENT);
        if (zombiesReanimatePercent > 0)
        {
            if (entity.getType() == EntityType.ZOMBIE)
            {
                Zombie zombie = (Zombie) entity;

                if (!zombie.isVillager() && entity.getFireTicks() < 1 && plugin.random(zombiesReanimatePercent))
                {
                    Player playerTarget = null;
                    Entity target = zombie.getTarget();
                    if (target instanceof Player)
                    {
                        playerTarget = (Player) target;
                    }

                    RespawnZombieTask task = new RespawnZombieTask(plugin, entity.getLocation(), playerTarget);
                    int respawnSeconds = plugin.getRandom().nextInt(6) + 3; // 3-8
                    // seconds
                    plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, task, 20L * respawnSeconds); // /20L
                    // ~
                    // 1
                    // second
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

        // is this an entity damaged by entity event?
        EntityDamageByEntityEvent damageByEntityEvent = null;
        if (event instanceof EntityDamageByEntityEvent)
        {
            damageByEntityEvent = (EntityDamageByEntityEvent) event;
        }

        // FEATURE: zombies can apply a debilitating effect
        if (rootC.getBoolean(RootNode.ZOMBIES_DEBILITATE_PLAYERS))
        {
            if (damageByEntityEvent != null && damageByEntityEvent.getDamager() instanceof Zombie)
            {
                if (entity instanceof Player)
                {
                    Player player = (Player) entity;
                    if (!player.hasPermission(PermissionNode.BYPASS.getNode()))
                    {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * 10, 3));
                    }
                }
            }
        }
    }
}
