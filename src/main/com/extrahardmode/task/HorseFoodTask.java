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


import com.extrahardmode.features.monsters.Horses;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * @author Diemex
 */
public class HorseFoodTask extends BukkitRunnable
{
    private final Horses horses;

    //Max health is 100 = 50 whole food chicken hunches, running 11 minutes drains the whole bar and requires 25 carrots to fill up again
    private final int DELAY = 8 * 20; //every 8 seconds drain half a food bar


    public HorseFoodTask(Horses horses)
    {
        this.horses = horses;
    }


    @Override
    public void run()
    {
    }
}
