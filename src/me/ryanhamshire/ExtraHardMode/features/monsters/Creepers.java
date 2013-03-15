package me.ryanhamshire.ExtraHardMode.features.monsters;

import me.ryanhamshire.ExtraHardMode.ExtraHardMode;
import me.ryanhamshire.ExtraHardMode.config.RootConfig;
import me.ryanhamshire.ExtraHardMode.config.RootNode;
import me.ryanhamshire.ExtraHardMode.module.EntityModule;
import me.ryanhamshire.ExtraHardMode.service.PermissionNode;
import me.ryanhamshire.ExtraHardMode.task.CoolCreeperExplosion;
import org.bukkit.Effect;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;


public class Creepers implements Listener
{
    ExtraHardMode plugin = null;
    RootConfig rootC = null;
    EntityModule entityModule = null;

    public Creepers(ExtraHardMode plugin)
    {
        this.plugin = plugin;
        rootC = plugin.getModuleForClass(RootConfig.class);
        entityModule = plugin.getModuleForClass(EntityModule.class);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onEntitySpawn(CreatureSpawnEvent event)
    {

        LivingEntity entity = event.getEntity();

        EntityType entityType = entity.getType();

        CreatureSpawnEvent.SpawnReason reason = event.getSpawnReason();

        // FEATURE: charged creeper spawns
        if (entityType == EntityType.CREEPER)
        {
            if (plugin.random(rootC.getInt(RootNode.CHARGED_CREEPER_SPAWN_PERCENT)))
            {
                ((Creeper) entity).setPowered(true);
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event)
    {
        LivingEntity entity = event.getEntity();
        World world = entity.getWorld();

        // FEATURE: creepers may drop activated TNT when they die
        final int creeperDropTNTPercent = rootC.getInt(RootNode.CREEPERS_DROP_TNT_ON_DEATH_PERCENT);
        final int creeperDropTntMaxY = rootC.getInt(RootNode.CREEPERS_DROP_TNT_ON_DEATH_MAX_Y);
        if (creeperDropTNTPercent > 0)
        {
            if (entity.getType() == EntityType.CREEPER && plugin.random(creeperDropTNTPercent)
                    && creeperDropTntMaxY > entity.getLocation().getBlockY())
            {
                world.spawnEntity(entity.getLocation(), EntityType.PRIMED_TNT);
                if (rootC.getBoolean(RootNode.SOUND_CREEPER_TNT))
                    world.playEffect(entity.getLocation(), Effect.GHAST_SHRIEK, 1, 35);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onEntityDamage(EntityDamageEvent event)
    {
        Entity entity = event.getEntity();
        EntityType entityType = entity.getType();
        World world = entity.getWorld();

        // is this an entity damaged by entity event?
        EntityDamageByEntityEvent damageByEntityEvent = null;
        if (event instanceof EntityDamageByEntityEvent)
        {
            damageByEntityEvent = (EntityDamageByEntityEvent) event;
        }

        // FEATURE: charged creepers explode on hit
        if (rootC.getBoolean(RootNode.CHARGED_CREEPERS_EXPLODE_ON_HIT) && !rootC.getBoolean(RootNode.DISABLE_EXPLOSIONS))
        {
            if (entityType == EntityType.CREEPER && !entity.isDead())
            {
                Creeper creeper = (Creeper) entity;
                if (creeper.isPowered())
                {
                    Player damager = null;
                    //Always explode when damaged by a player
                    if (damageByEntityEvent != null)
                    {
                        if (damageByEntityEvent.getDamager() != null && damageByEntityEvent.getDamager() instanceof Player)
                        {   //Normal Damage from a player
                            damager = (Player) damageByEntityEvent.getDamager();
                            if (damager != null && damager.hasPermission(PermissionNode.BYPASS_CREEPERS.getNode()))
                                return;
                        }
                        else if (damageByEntityEvent.getDamager() != null && damageByEntityEvent.getDamager() instanceof Arrow)
                        {   //Damaged by an arrow shot by a player
                            Arrow arrow = (Arrow) damageByEntityEvent.getDamager();
                            damager = (Player) arrow.getShooter();
                            if (damager != null && damager.hasPermission(PermissionNode.BYPASS_CREEPERS.getNode()))
                                return;
                        }
                    }
                    if (event != null && creeper.getTarget() == null && damager == null)
                    {   //If not targetting a player this is an explosion we don't need. Trying to prevent unecessary world damage
                        return;
                    }
                    entityModule.markLootLess((LivingEntity) entity);
                    entity.remove();
                    world.createExplosion(entity.getLocation(), 4F); // equal to a TNT blast
                }
            }
        }


        //FEATURE: a burning creeper will create a nice explosion + fireworks and will fly in the air
        //Will only trigger if creeper died from fire not from a sword with fireaspect or bow
        if (rootC.getBoolean(RootNode.FLAMING_CREEPERS_EXPLODE))
        {
            if (event != null && entity != null && entityType.equals(EntityType.CREEPER))
            {
                if (!entityModule.hasFlagIgnore(entity))
                {
                    if (event.getCause().equals(EntityDamageEvent.DamageCause.FIRE)
                            || event.getCause().equals(EntityDamageEvent.DamageCause.FIRE_TICK)
                            || event.getCause().equals(EntityDamageEvent.DamageCause.LAVA))
                    {
                        Creeper creeper = (Creeper) entity;
                        entityModule.flagIgnore(entity);
                        CoolCreeperExplosion bigBoom = new CoolCreeperExplosion(creeper, plugin);
                        bigBoom.run();
                    }
                }
            }
        }
    }
}
