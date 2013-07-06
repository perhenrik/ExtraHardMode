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
import com.extrahardmode.service.ListenerModule;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.HorseInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * @author Diemex
 */
public class Horses extends ListenerModule
{
    public Horses(ExtraHardMode plugin)
    {
        super(plugin);
    }


    /**
     * Block using of horse inventory in caves, prevent usage of horses as transportable chests
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onHorseInvClick(InventoryClickEvent event)
    {
        final Inventory inv = event.getInventory();

        //test
        short dur = event.getCurrentItem().getDurability();
        switch (event.getClick())
        {
            case RIGHT:
                event.getCurrentItem().setDurability(dur--);
                break;
            case LEFT:
                event.getCurrentItem().setDurability(dur++);
                break;
        }

        plugin.getServer().broadcastMessage("durability: " + event.getCurrentItem().getDurability());

        if (inv instanceof HorseInventory)
        {
            Inventory horseInv = event.getView().getTopInventory();
            int clickedSlot = event.getRawSlot();

            //TODO allow saddle and armor placement
            //if (event.getWhoClicked().getLocation().getBlockY() < 60)
            {
                //In a horse inventory the first two slots are saddle + armor, a mule only has a saddle + potentially a chest
                //Block usage of the chest in caves, but allow taking of the saddle
                if (horseInv.getSize() > 2 && ((clickedSlot < horseInv.getSize() && clickedSlot > 0) || event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY))
                {
                    event.setCancelled(true);
                }
            }
        }
    }


    /**
     * Greatly increase damage to horses
     */
    @EventHandler
    public void onHorseDamage(EntityDamageEvent event)
    {
        if (event.getEntity() instanceof Horse)
        {
            final LivingEntity horse = (LivingEntity) event.getEntity();
            final EntityDamageEvent.DamageCause cause = event.getCause();

            switch (cause)
            {
                case BLOCK_EXPLOSION:
                case ENTITY_EXPLOSION:
                    //TODO EhmPlayerEnvironmentalDamageEvent for each type
                    if (event.getDamage() > 2)
                        horse.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 20 * 15, 3));
                    break;
                case FALL:
                    horse.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) (20 * event.getDamage()), 4));
                    event.setDamage(event.getDamage() * 2.0);
                    break;
                case SUFFOCATION:
                    event.setDamage(event.getDamage() * 5.0);
                    break;
                case LAVA:
                    event.setDamage(event.getDamage() * 2.0);
                    break;
                case FIRE_TICK:
                    horse.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20, 1));
                    break;
            }
        }
    }

    /**
     * Make horse armor wear out
     */
    /**
     * Make saddle wear out
     */
    /**
     * Make horses require food
     */
}
