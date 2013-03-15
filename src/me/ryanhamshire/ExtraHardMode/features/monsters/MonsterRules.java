package me.ryanhamshire.ExtraHardMode.features.monsters;

import me.ryanhamshire.ExtraHardMode.ExtraHardMode;
import me.ryanhamshire.ExtraHardMode.config.RootConfig;
import me.ryanhamshire.ExtraHardMode.config.RootNode;
import me.ryanhamshire.ExtraHardMode.config.messages.MessageConfig;
import me.ryanhamshire.ExtraHardMode.module.EntityModule;
import me.ryanhamshire.ExtraHardMode.module.UtilityModule;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;

public class MonsterRules implements Listener
{
    ExtraHardMode plugin = null;
    RootConfig rootC = null;
    MessageConfig messages;
    UtilityModule utils = null;
    EntityModule entityModule = null;

    public MonsterRules(ExtraHardMode plugin)
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
        final int maxY = rootC.getInt(RootNode.MONSTER_SPAWNS_IN_LIGHT_MAX_Y);
        final int multiplier = rootC.getInt(RootNode.MORE_MONSTERS_MULTIPLIER);

        Location location = event.getLocation();
        World world = location.getWorld();
        if (!rootC.getStringList(RootNode.WORLDS).contains(world.getName()))
        {
            return;
        }

        LivingEntity entity = event.getEntity();
        EntityType entityType = entity.getType();

        CreatureSpawnEvent.SpawnReason reason = event.getSpawnReason();

        //We don't know how to handle ghosts. (Mo Creatures)
        if (!entityType.equals(EntityType.UNKNOWN) && reason != CreatureSpawnEvent.SpawnReason.CUSTOM)
        {
            // FEATURE: extra monster spawns underground
            if (maxY > 0)
            {
                if (world.getEnvironment() == World.Environment.NORMAL && event.getLocation() != null
                        && event.getLocation().getBlockY() < maxY && entityType != null && entity instanceof Monster)
                {
                    if (!entityType.equals(EntityType.SILVERFISH)) //no multiple silverfish per block
                    {
                        for (int i = 1; i < multiplier; i++)
                        {
                            Entity newEntity = world.spawnEntity(event.getLocation(), entityType);
                            if (entityModule.isLootLess(entity))
                            {
                                entityModule.markLootLess((LivingEntity) newEntity);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * when an entity targets something (as in to attack it)...
     *
     * @param event - Event that occurred.
     */
    @EventHandler
    public void onEntityTarget(EntityTargetEvent event)
    {
        Entity entity = event.getEntity();
        World world = entity.getWorld();
        if (!rootC.getStringList(RootNode.WORLDS).contains(world.getName()))
            return;

        // FEATURE: a monster which gains a target breaks out of any webbing it
        // might have been trapped within
        if (entity instanceof Monster)
        {
            entityModule.clearWebbing(entity);
        }
    }

    /**
     * when an entity is damaged
     * handles
     *
     * @param event - Event that occurred.
     */
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

        // FEATURE: don't allow explosions to destroy items on the ground
        // REASONS: charged creepers explode twice, enhanced TNT explodes 5 times
        if (entityType == EntityType.DROPPED_ITEM)
        {
            event.setCancelled(true);
        }

        // FEATURE: monsters trapped in webbing break out of the webbing when hit
        if (entity instanceof Monster)
        {
            entityModule.clearWebbing(entity);
        }
    }
}
