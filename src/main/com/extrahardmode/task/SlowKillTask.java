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


import com.extrahardmode.module.EntityHelper;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Damage a LivingEntity until it's dead, stop the task once the Entity is dead
 *
 * @author Diemex
 */
public class SlowKillTask extends BukkitRunnable
{
    private final LivingEntity entity;
    private final Plugin plugin;

    public SlowKillTask(LivingEntity entity, Plugin plugin)
    {
        this.entity = entity;
        this.plugin = plugin;
        runTaskTimer(plugin, 0L, 30L);
    }


    @Override
    public void run()
    {
        if (!entity.isDead())
        {
            entity.damage(1.0);
            EntityHelper.addEnvironmentalDamage(plugin, entity, 1.0);
        }
        else
            cancel();
    }
}
