/*
 * This file is part of
 * ExtraHardMode Server Plugin for Minecraft
 *
 * Copyright (C) 2012 Ryan Hamshire
 * Copyright (C) 2013 Diemex
 *
 * ExtraHardMode is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ExtraHardMode is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero Public License
 * along with ExtraHardMode.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.extrahardmode.features.monsters;


import com.extrahardmode.ExtraHardMode;
import com.extrahardmode.config.RootConfig;
import com.extrahardmode.config.RootNode;
import com.extrahardmode.module.PlayerModule;
import com.extrahardmode.service.Feature;
import com.extrahardmode.service.ListenerModule;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * All changes to Ghasts including:
 * <p/>
 * Increase loot for Ghasts drastically Ghasts don't take damage from arrows
 */
public class Ghasts extends ListenerModule
{
    private final ExtraHardMode plugin;

    private final RootConfig CFG;

    private final PlayerModule playerModule;


    public Ghasts(ExtraHardMode plugin)
    {
        super(plugin);
        this.plugin = plugin;
        CFG = plugin.getModuleForClass(RootConfig.class);
        playerModule = plugin.getModuleForClass(PlayerModule.class);
    }


    /**
     * When an Entity dies
     * <p/>
     * Increase loot for Ghasts drastically
     */
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event)
    {
        LivingEntity entity = event.getEntity();
        World world = entity.getWorld();

        final boolean ghastDeflectArrows = CFG.getInt(RootNode.GHASTS_DEFLECT_ARROWS, world.getName()) > 0;
        final int ghastExpMupliplier = CFG.getInt(RootNode.GHASTS_EXP_MULTIPLIER, world.getName());
        final int ghastDropsMultiplier = CFG.getInt(RootNode.GHASTS_DROPS_MULTIPLIER, world.getName());

        // FEATURE: ghasts deflect arrows and drop extra loot and exp
        if (ghastDeflectArrows)
        {
            if (entity instanceof Ghast)
            {
                event.setDroppedExp(event.getDroppedExp() * ghastExpMupliplier);
                List<ItemStack> itemDrops = event.getDrops();
                for (ItemStack itemDrop : itemDrops)
                {
                    itemDrop.setAmount(itemDrop.getAmount() * ghastDropsMultiplier);
                }
            }
        }
    }


    /**
     * When an Entity takes damage
     * <p/>
     * Ghasts don't take damage from arrows
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onEntityDamage(EntityDamageEvent event)
    {
        Entity entity = event.getEntity();
        World world = entity.getWorld();

        final int arrowDamagePercent = CFG.getInt(RootNode.GHASTS_DEFLECT_ARROWS, world.getName());

        EntityDamageByEntityEvent damageByEntityEvent = null;
        if (event instanceof EntityDamageByEntityEvent)
        {
            damageByEntityEvent = (EntityDamageByEntityEvent) event;
        }

        // FEATURE: ghasts deflect arrows and drop extra loot
        if (arrowDamagePercent < 100)
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
                        if (!playerModule.playerBypasses(player, Feature.MONSTER_GHASTS))
                            event.setDamage(event.getDamage() * arrowDamagePercent / 100.0);
                    }
                }
            }
        }
    }
}
