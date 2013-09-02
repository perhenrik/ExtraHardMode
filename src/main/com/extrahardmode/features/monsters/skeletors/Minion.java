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

package com.extrahardmode.features.monsters.skeletors;


import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

/**
 * @author Diemex
 */
public class Minion
{
    private OnDamage damaged;
    private OnDamage damagePlayer;
    private EntityType minionType;
    private int duration;


    public Minion(OnDamage damaged, OnDamage damagePlayer, EntityType minionType, int duration)
    {
        this.damaged = damaged;
        this.damagePlayer = damagePlayer;
        this.minionType = minionType;
        this.duration = duration;
    }


    public int getEffectDuration()
    {
        return duration;
    }


    public void setEffectDuration(int duration)
    {
        this.duration = duration;
    }


    public OnDamage getDamaged()
    {
        return damaged;
    }


    public void setDamaged(OnDamage damaged)
    {
        this.damaged = damaged;
    }


    public OnDamage getDamagePlayer()
    {
        return damagePlayer;
    }


    public void setDamagePlayer(OnDamage damagePlayer)
    {
        this.damagePlayer = damagePlayer;
    }


    public EntityType getMinionType()
    {
        return minionType;
    }


    public void setMinionType(EntityType minionType)
    {
        this.minionType = minionType;
    }


    private static final String minionTag = "extrahardmode.skeleton.minion";


    public static void setMinion(Plugin plugin, LivingEntity entity)
    {
        entity.setMetadata(minionTag, new FixedMetadataValue(plugin, true));
    }


    public static boolean isMinion(LivingEntity entity)
    {
        return entity.hasMetadata(minionTag);
    }
}
