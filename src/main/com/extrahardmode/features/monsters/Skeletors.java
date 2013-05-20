package com.extrahardmode.features.monsters;

import com.extrahardmode.ExtraHardMode;
import com.extrahardmode.config.RootConfig;
import com.extrahardmode.config.RootNode;
import com.extrahardmode.module.EntityModule;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;

/**
 * Created with IntelliJ IDEA.
 * User: max
 * Date: 3/15/13
 * Time: 1:19 AM
 * To change this template use File | Settings | File Templates.
 */
public class Skeletors implements Listener
{
    ExtraHardMode plugin;
    RootConfig CFG;
    EntityModule entityModule;

    public Skeletors(ExtraHardMode plugin)
    {
        this.plugin = plugin;
        CFG = plugin.getModuleForClass(RootConfig.class);
        entityModule = plugin.getModuleForClass(EntityModule.class);
    }


    /**
     * When an entity takes damage
     * skeletons are immune to arrows
     * @param event
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onEntityDamage(EntityDamageEvent event)
    {
        Entity entity = event.getEntity();
        EntityType entityType = entity.getType();
        World world = entity.getWorld();

        final int deflect = CFG.getInt(RootNode.SKELETONS_DEFLECT_ARROWS, world.getName());
        final int knockBackPercent = CFG.getInt(RootNode.SKELETONS_KNOCK_BACK_PERCENT, world.getName());


        // is this an entity damaged by entity event?
        EntityDamageByEntityEvent damageByEntityEvent = null;
        if (event instanceof EntityDamageByEntityEvent)
        {
            damageByEntityEvent = (EntityDamageByEntityEvent) event;
        }

        // FEATURE: arrows pass through skeletons
        if (entityType == EntityType.SKELETON && damageByEntityEvent != null && deflect > 0)
        {
            Entity damageSource = damageByEntityEvent.getDamager();

            // only arrows
            if (damageSource instanceof Arrow)
            {
                Arrow arrow = (Arrow) damageSource;

                // percent chance
                if (plugin.random(deflect))
                {

                    // cancel the damage
                    event.setCancelled(true);

                    // teleport the arrow a single block farther along its flight
                    // path
                    // note that .6 and 12 were the unexplained recommended values
                    // for speed and spread, reflectively, in the bukkit wiki
                    arrow.remove();
                    world.spawnArrow(arrow.getLocation().add((arrow.getVelocity().normalize()).multiply(2)), arrow.getVelocity(), .6f, 12f);
                }
            }
        }

        // FEATURE: skeletons can knock back
        if (knockBackPercent > 0)
        {
            if (damageByEntityEvent != null)
            {
                if (damageByEntityEvent.getDamager() instanceof Arrow)
                {
                    Arrow arrow = (Arrow) (damageByEntityEvent.getDamager());
                    if (arrow.getShooter() != null && arrow.getShooter() instanceof Skeleton)
                    {
                        if (plugin.random(knockBackPercent))
                        {
                            // cut damage in half
                            event.setDamage(event.getDamage() / 2);

                            // knock back target with half the arrow's velocity
                            entity.setVelocity(arrow.getVelocity());
                        }
                    }
                }
            }
        }
    }

    /**
     * when an entity shoots a bow...
     * Skeletors: Knockback-arrows, silverfish
     *
     * @param event - Event that occurred.
     */
    @EventHandler
    public void onShootProjectile(ProjectileLaunchEvent event)
    {
        Location location = event.getEntity().getLocation();
        World world = location.getWorld();
        EntityType entityType = event.getEntityType();

        final int silverfishShootPercent = CFG.getInt(RootNode.SKELETONS_RELEASE_SILVERFISH, world.getName());

        // FEATURE: skeletons sometimes release silverfish to attack their targets
        if (event.getEntity() != null && entityType == EntityType.ARROW)
        {
            Arrow arrow = (Arrow) event.getEntity();

            LivingEntity shooter = arrow.getShooter();
            if (shooter != null && shooter.getType() == EntityType.SKELETON && plugin.random(silverfishShootPercent))
            {
                Skeleton skeleton = (Skeleton) shooter;
                EntityModule module = plugin.getModuleForClass(EntityModule.class);
                // cancel arrow fire
                event.setCancelled(true);

                // replace with silverfish, quarter velocity of arrow, wants to attack
                // same target as skeleton
                Creature silverFish = (Creature) skeleton.getWorld().spawnEntity(skeleton.getLocation().add(0, 1.5, 0), EntityType.SILVERFISH);
                silverFish.setVelocity(arrow.getVelocity().multiply(.25));
                silverFish.setTarget(skeleton.getTarget());
                module.markLootLess(silverFish); // this silverfish doesn't drop loot
            }
        }
    }
}
