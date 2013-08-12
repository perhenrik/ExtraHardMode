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


package com.extrahardmode.task;


import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Task to drop items at a specified location.
 */
class DropItemsTask implements Runnable
{

    /**
     * Items to drop.
     */
    private final List<ItemStack> itemsToDrop;

    /**
     * Location of item drop.
     */
    private final Location location;


    /**
     * Constructor.
     *
     * @param itemsToDrop
     *         - List of items to drop.
     * @param location
     *         - Drop point location.
     */
    public DropItemsTask(List<ItemStack> itemsToDrop, Location location)
    {
        this.itemsToDrop = itemsToDrop;
        this.location = location;
    }


    @Override
    public void run()
    {
        for (ItemStack item : itemsToDrop)
        {
            location.getWorld().dropItemNaturally(location, item);
        }
    }

}
