package me.ryanhamshire.ExtraHardMode.features.monsters;

import me.ryanhamshire.ExtraHardMode.ExtraHardMode;
import me.ryanhamshire.ExtraHardMode.config.ExplosionType;
import me.ryanhamshire.ExtraHardMode.config.RootConfig;
import me.ryanhamshire.ExtraHardMode.config.RootNode;
import me.ryanhamshire.ExtraHardMode.module.EntityModule;
import me.ryanhamshire.ExtraHardMode.service.PermissionNode;
import me.ryanhamshire.ExtraHardMode.task.CoolCreeperExplosion;
import me.ryanhamshire.ExtraHardMode.task.CreateExplosionTask;
import org.bukkit.Effect;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.potion.PotionEffectType;


public class BumBumBens implements Listener
{
    ExtraHardMode plugin = null;
    RootConfig CFG = null;
    EntityModule entityModule = null;

    public BumBumBens(ExtraHardMode plugin)
    {
        this.plugin = plugin;
        CFG = plugin.getModuleForClass(RootConfig.class);
        entityModule = plugin.getModuleForClass(EntityModule.class);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onEntitySpawn(CreatureSpawnEvent event)
    {

        LivingEntity entity = event.getEntity();
        EntityType entityType = entity.getType();
        World world = entity.getWorld();

        final int chargedSpawnPercent = CFG.getInt(RootNode.CHARGED_CREEPER_SPAWN_PERCENT, world.getName());

        // FEATURE: charged creeper spawns
        if (entityType == EntityType.CREEPER)
        {
            if (plugin.random(chargedSpawnPercent))
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

        final int creeperDropTNTPercent = CFG.getInt(RootNode.CREEPERS_DROP_TNT_ON_DEATH_PERCENT, world.getName());
        final int creeperDropTntMaxY = CFG.getInt(RootNode.CREEPERS_DROP_TNT_ON_DEATH_MAX_Y, world.getName());
        final boolean creeperSound = CFG.getBoolean(RootNode.SOUND_CREEPER_TNT, world.getName());

        // FEATURE: creepers may drop activated TNT when they die
        if (creeperDropTNTPercent > 0)
        {
            if (entity.getType() == EntityType.CREEPER && plugin.random(creeperDropTNTPercent)
                    && creeperDropTntMaxY > entity.getLocation().getBlockY())
            {
                world.spawnEntity(entity.getLocation(), EntityType.PRIMED_TNT);
                if (creeperSound)
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

        final boolean chargedExplodeOnHit = CFG.getBoolean(RootNode.CHARGED_CREEPERS_EXPLODE_ON_HIT, world.getName());
        final boolean flamingCreepersExplode = CFG.getBoolean(RootNode.FLAMING_CREEPERS_EXPLODE, world.getName());

        // FEATURE: charged creepers explode on hit
        if (chargedExplodeOnHit)
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
                        if (damageByEntityEvent.getDamager() instanceof Player)
                        {   //Normal Damage from a player
                            damager = (Player) damageByEntityEvent.getDamager();
                            if (damager != null && damager.hasPermission(PermissionNode.BYPASS_CREEPERS.getNode()))
                                return;
                        }
                        else if (damageByEntityEvent.getDamager() instanceof Projectile)
                        {   //Damaged by an arrow shot by a player
                            Projectile bullet = (Projectile) damageByEntityEvent.getDamager();
                            if (bullet.getShooter() instanceof Player)//otherwise skeli/dispenser etc.
                                damager = (Player) bullet.getShooter();
                            if (damager != null && damager.hasPermission(PermissionNode.BYPASS_CREEPERS.getNode()))
                                return;
                        }
                    }
                    if (creeper.getTarget() == null && damager == null)
                    {   //If not targetting a player this is an explosion we don't need. Trying to prevent unecessary world damage
                        return;
                    }
                    entityModule.markLootLess((LivingEntity) entity);
                    entity.remove();
                    new CreateExplosionTask(plugin, entity.getLocation(), ExplosionType.CREEPER_CHARGED, creeper).run(); // equal to a TNT blast
                }
            }
        }


        //FEATURE: a burning creeper will create a nice explosion + fireworks and will fly in the air
        //Will only trigger if creeper died from fire not from a sword with fireaspect or bow
        if (flamingCreepersExplode)
        {
            if (entityType.equals(EntityType.CREEPER))
            {
                if (!entityModule.hasFlagIgnore(entity))
                {
                    Creeper creeper = (Creeper) entity;
                    if ((event.getCause().equals(EntityDamageEvent.DamageCause.FIRE)
                            || event.getCause().equals(EntityDamageEvent.DamageCause.FIRE_TICK)
                            || event.getCause().equals(EntityDamageEvent.DamageCause.LAVA))
                            &&! creeper.hasPotionEffect(PotionEffectType.FIRE_RESISTANCE))
                    {
                        entityModule.flagIgnore(entity);
                        CoolCreeperExplosion bigBoom = new CoolCreeperExplosion(creeper, plugin);
                        bigBoom.run();
                    }
                }
            }
        }
    }

    @EventHandler
    public void onExplosion(EntityExplodeEvent event)
    {
        Entity entity = event.getEntity();

        // FEATURE: bigger creeper explosions (for more-frequent cave-ins)
        if (entity != null && entity instanceof Creeper &&! entityModule.hasFlagIgnore(entity)) //We create an Explosion event and need to prevent loops
        {
            event.setCancelled(true);
            entityModule.flagIgnore(entity);//Ignore this creeper in further calls to this method
            new CreateExplosionTask(plugin, entity.getLocation(), ExplosionType.CREEPER, (Creeper)entity).run();
        }
    }
}
