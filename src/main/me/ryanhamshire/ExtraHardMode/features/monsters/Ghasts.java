package me.ryanhamshire.ExtraHardMode.features.monsters;

import me.ryanhamshire.ExtraHardMode.ExtraHardMode;
import me.ryanhamshire.ExtraHardMode.config.RootConfig;
import me.ryanhamshire.ExtraHardMode.config.RootNode;
import me.ryanhamshire.ExtraHardMode.service.PermissionNode;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: max
 * Date: 3/15/13
 * Time: 1:20 AM
 * To change this template use File | Settings | File Templates.
 */
public class Ghasts implements Listener
{
    ExtraHardMode plugin;
    RootConfig CFG;

    public Ghasts (ExtraHardMode plugin)
    {
        this.plugin = plugin;
        CFG = plugin.getModuleForClass(RootConfig.class);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event)
    {
        LivingEntity entity = event.getEntity();
        World world = entity.getWorld();

        final boolean ghastDeflectArrows = CFG.getBoolean(RootNode.GHASTS_DEFLECT_ARROWS, world.getName());

        // FEATURE: ghasts deflect arrows and drop extra loot and exp
        if (ghastDeflectArrows)
        {
            if (entity instanceof Ghast)
            {
                event.setDroppedExp(event.getDroppedExp() * 10);
                List<ItemStack> itemDrops = event.getDrops();
                for (ItemStack itemDrop : itemDrops)
                {
                    itemDrop.setAmount(itemDrop.getAmount() * 10);
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

        final boolean ghastDeflectArrows = CFG.getBoolean(RootNode.GHASTS_DEFLECT_ARROWS, world.getName());

        EntityDamageByEntityEvent damageByEntityEvent = null;
        if (event instanceof EntityDamageByEntityEvent)
        {
            damageByEntityEvent = (EntityDamageByEntityEvent) event;
        }

        // FEATURE: ghasts deflect arrows and drop extra loot
        if (ghastDeflectArrows)
        {
            // only ghasts, and only if damaged by another entity (as opposed to
            // environmental damage)
            if (entity instanceof Ghast && event instanceof EntityDamageByEntityEvent)
            {
                Entity damageSource = damageByEntityEvent.getDamager();

                // only arrows
                if (damageSource instanceof Arrow)
                {
                    // who shot it?
                    Arrow arrow = (Arrow) damageSource;
                    if (arrow.getShooter() != null && arrow.getShooter() instanceof Player)
                    {
                        // check permissions when it's shot by a player
                        Player player = (Player) arrow.getShooter();
                        event.setCancelled(!player.hasPermission(PermissionNode.BYPASS.getNode()));
                    }
                    else
                    {
                        // otherwise always deflect
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }
}
