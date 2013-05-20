package com.extrahardmode.features.monsters;

import com.extrahardmode.ExtraHardMode;
import com.extrahardmode.config.RootConfig;
import com.extrahardmode.config.RootNode;
import com.extrahardmode.service.PermissionNode;
import com.extrahardmode.task.RespawnZombieTask;
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
    RootConfig CFG;

    public Zombies (ExtraHardMode plugin)
    {
        this.plugin = plugin;
        CFG = plugin.getModuleForClass(RootConfig.class);
    }

    /**
     * When a zombie dies sometimes reanimate the zombie
     * @param event
     */
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event)
    {
        LivingEntity entity = event.getEntity();
        World world = entity.getWorld();

        final int zombiesReanimatePercent = CFG.getInt(RootNode.ZOMBIES_REANIMATE_PERCENT, world.getName());

        // FEATURE: zombies may reanimate if not on fire when they die
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
                    int respawnSeconds = plugin.getRandom().nextInt(6) + 3; // 3-8 seconds
                    plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, task, 20L * respawnSeconds); // /20L
                    // ~ 1 second
                }
            }
        }
    }

    /**
     * When a player is damaged by a zombie
     * @param event
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onEntityDamage(EntityDamageEvent event)
    {
        Entity entity = event.getEntity();
        World world = null;
        if (entity != null) world = entity.getWorld();
        Player player = null;
        if (entity instanceof Player)
        {
            player = (Player) entity;
        }

        final boolean zombiesSlowPlayers = CFG.getBoolean(RootNode.ZOMBIES_DEBILITATE_PLAYERS, world.getName());
        final boolean playerHasBypass = player != null ? player.hasPermission(PermissionNode.BYPASS.getNode()) : false;

        // is this an entity damaged by entity event?
        EntityDamageByEntityEvent damageByEntityEvent = null;
        if (event instanceof EntityDamageByEntityEvent)
        {
            damageByEntityEvent = (EntityDamageByEntityEvent) event;
        }

        // FEATURE: zombies can apply a debilitating effect
        if (zombiesSlowPlayers && player != null &&! playerHasBypass)
        {
            if (damageByEntityEvent != null && damageByEntityEvent.getDamager() instanceof Zombie)
            {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * 10, 3));
            }
        }
    }
}
