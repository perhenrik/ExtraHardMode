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
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.UUID;

/**
 * @author Diemex
 */
public class Minion
{
    private OnDamage damagePlayer;
    private EntityType minionType;
    private int duration;
    private int currentSpawnLimit;
    private int totalSpawnLimit;
    private int lootPercentage;


    public Minion(OnDamage damagePlayer, EntityType minionType, int duration, int currentSpawnLimit, int totalSpawnLimit, int lootPercentage)
    {
        this.damagePlayer = damagePlayer;
        this.minionType = minionType;
        this.duration = duration;
        this.currentSpawnLimit = currentSpawnLimit;
        this.totalSpawnLimit = totalSpawnLimit;
        this.lootPercentage = lootPercentage;
    }


    public int getLootPercentage()
    {
        return lootPercentage;
    }


    public int getCurrentSpawnLimit()
    {
        return currentSpawnLimit;
    }


    public int getTotalSpawnLimit()
    {
        return totalSpawnLimit;
    }


    public int getEffectDuration()
    {
        return duration;
    }


    public void setEffectDuration(int duration)
    {
        this.duration = duration;
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


    private static String parentKey = "extrahardmode.minion.parent";


    /**
     * Set the parent that summoned this minion
     *
     * @param summoner parent summoner
     * @param minion   summoned minion
     * @param owning   plugin that spawned the minion
     */
    public static void setParent(LivingEntity summoner, LivingEntity minion, Plugin owning)
    {
        minion.setMetadata(parentKey, new FixedMetadataValue(owning, summoner.getUniqueId()));
    }


    /**
     * Get the parent that summoned the minion
     *
     * @param minion minion to get the parent for
     * @param plugin owning plugin for MetaData
     *
     * @return id of parent or id of minion if parent not set
     */
    public static UUID getParent(LivingEntity minion, Plugin plugin)
    {
        List<MetadataValue> meta = minion.getMetadata(parentKey);
        if (!meta.isEmpty())
        {
            MetadataValue metaVal = meta.get(0);
            if (metaVal.value() instanceof UUID)
                return (UUID) metaVal.value();
        }
        return minion.getUniqueId();
    }
}
