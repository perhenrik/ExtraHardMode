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
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;

/**
 * @author Diemex
 */
public class Horses extends ListenerModule
{
    public Horses(ExtraHardMode plugin)
    {
        super(plugin);
    }


    @EventHandler
    public void onEntityInteract(InventoryOpenEvent event)
    {
        final Inventory inv = event.getInventory();
        InventoryView view = event.getView();
        Class clazz = event.getClass();
        Player player = (Player) event.getPlayer();
    }
}
