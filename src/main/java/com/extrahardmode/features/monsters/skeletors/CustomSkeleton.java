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


import org.apache.commons.lang.Validate;
import org.bukkit.entity.LivingEntity;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class CustomSkeleton
{
    /**
     * Potion effect to apply for the sake of visualisation, so player knows which minions to expect
     */
    private PotionEffectType effectType;
    /**
     * Percentage of arrows to spawn a minion
     */
    private int releaseMinionPercent;
    /**
     * Type of minion to spawn
     */
    private Minion minion;
    /**
     * Remove minions after summoner has died
     */
    private boolean removeMinions;
    /**
     * The percentage of arrows that pass through skeletons
     */
    private int arrowsReflectPerc;
    /**
     * Percentage of arrows that knock back
     */
    private int knockbackPercent;
    /**
     * Id to tag the skeli as custom
     */
    private final String identifier;
    /**
     * Spawn Weight
     */
    private int spawnWeight;
    /**
     * Key to mark the type of skeleton
     */
    public final static String skeliTypeStr = "extrahardmode.skeleton.type";
    /**
     * Key to access the minions of the Skeleton
     */
    public final static String skeliMinionStorage = "extrahardmode.skeleton.minions";
    /**
     * Total count of minions spawned by this host
     */
    public final static String skeliTotalMinionCount = "extrahardmode.skeleton.minions.totalcount";


    public CustomSkeleton(String identifier, PotionEffectType type, Minion minion, int releaseMinionPercent, boolean removeMinions, int arrowsReflectPerc, int knockbackPercent, int spawnWeight)
    {
        Validate.notEmpty(identifier);
        this.identifier = identifier;
        this.effectType = type;
        this.minion = minion;
        this.releaseMinionPercent = releaseMinionPercent;
        this.removeMinions = removeMinions;
        this.arrowsReflectPerc = arrowsReflectPerc;
        this.knockbackPercent = knockbackPercent;
        this.spawnWeight = spawnWeight;
    }


    /**
     * Get the unique identifier to recognize the skeli via meta
     *
     * @return the id
     */
    public String getIdentifier()
    {
        return identifier;
    }


    /**
     * The potion effect to visulaize which skeleton is being dealt with
     */
    public PotionEffectType getEffectType()
    {
        return effectType;
    }


    public void setEffectType(PotionEffectType effectType)
    {
        this.effectType = effectType;
    }


    /**
     * Properties of the mob to be used as a minion
     */
    public Minion getMinionType()
    {
        return minion;
    }


    public void setMinionType(Minion minion)
    {
        this.minion = minion;
    }


    /**
     * Will minions be removed when the summoner has died
     */
    public boolean willRemoveMinions()
    {
        return removeMinions;
    }


    public void setRemoveMinions(boolean removeMinions)
    {
        this.removeMinions = removeMinions;
    }


    /**
     * Should an arrow pass through a skeleton, random based on the percentage of arrows that should pass through.
     *
     * @return if an arrow should passThrough
     */
    public boolean isArrowsPassThrough()
    {
        return new Random().nextInt(101) < arrowsReflectPerc;
    }


    public void setArrowsReflectPerc(int arrowsReflectPerc)
    {
        this.arrowsReflectPerc = arrowsReflectPerc;
    }


    public int getArrowsReflectPerc()
    {
        return arrowsReflectPerc;
    }


    public int getKnockbackPercent()
    {
        return knockbackPercent;
    }


    public void setKnockbackPercent(int knockbackPercent)
    {
        this.knockbackPercent = knockbackPercent;
    }


    public int getSpawnWeight()
    {
        return spawnWeight;
    }


    public int getReleaseMinionPercent()
    {
        return releaseMinionPercent;
    }


    public void setReleaseMinionPercent(int releaseMinionPercent)
    {
        this.releaseMinionPercent = releaseMinionPercent;
    }


    /**
     * Mark a Skelton as a custom skeli
     *
     * @param entity         entity to apply the flag
     * @param plugin         owning plugin to set the meta
     * @param customSkeleton type of custom skeli to set
     */
    public static void setCustom(LivingEntity entity, Plugin plugin, CustomSkeleton customSkeleton)
    {
        if (customSkeleton.getEffectType() != null)
            entity.addPotionEffect(new PotionEffect(customSkeleton.getEffectType(), Integer.MAX_VALUE, 1, false));
        entity.setMetadata(skeliTypeStr, new FixedMetadataValue(plugin, customSkeleton.getIdentifier()));
    }


    /**
     * Try to determine what kind of Skeleton we have based on the List of CustomSkeletons
     *
     * @param entity          entity to check for type
     * @param plugin          owning plugin for MetaData
     * @param customSkeletons List of CustomSkeletons, cant be empty, first entry is the default skeleton
     *
     * @return the matched CustomSkeleton or the default Skeleton if no match found
     *
     * @throws InvalidArgumenException if List of Skelis is empty
     */
    public static CustomSkeleton getCustom(LivingEntity entity, Plugin plugin, List<CustomSkeleton> customSkeletons)
    {
        Validate.notEmpty(customSkeletons, "Need at least one Skeleton as a default skeli");
        List<MetadataValue> meta = entity.getMetadata(skeliTypeStr);
        if (!meta.isEmpty())
        {
            String key = meta.get(0).asString();
            for (CustomSkeleton skeleton : customSkeletons)
                if (skeleton.getIdentifier().equals(key))
                    return skeleton;
        }
        return customSkeletons.get(0);
    }


    /**
     * Add a minion to the list of minions spawned by this entity
     *
     * @param summoner entity that summoned another entiry
     * @param minion   minion that has been summoned
     * @param plugin   owning plugin for metadata
     */
    @SuppressWarnings("unchecked")
    public static void addMinion(LivingEntity summoner, LivingEntity minion, Plugin plugin)
    {
        List<UUID> idList = new ArrayList<UUID>(1);
        //Get minions already set and append the new Minion
        List<MetadataValue> meta = summoner.getMetadata(skeliMinionStorage);
        for (MetadataValue val : meta)
            if (val.getOwningPlugin() == plugin)
                if (val.value() instanceof List)
                    idList = (List<UUID>) val.value();
        idList.add(minion.getUniqueId());
        summoner.setMetadata(skeliMinionStorage, new FixedMetadataValue(plugin, idList));

        //Increment the total count of minions summoned
        List<MetadataValue> countMeta = summoner.getMetadata(skeliTotalMinionCount);
        int totalCount = getTotalSummoned(summoner, plugin);
        totalCount++;
        summoner.setMetadata(skeliTotalMinionCount, new FixedMetadataValue(plugin, totalCount));
    }


    /**
     * Remove the minion from the list of summoned minions
     *
     * @param minionId id of the minion
     * @param summoner the entity that summoned the minion
     */
    public static void removeMinion(UUID minionId, LivingEntity summoner)
    {
        List<MetadataValue> meta = summoner.getMetadata(skeliMinionStorage);
        if (!meta.isEmpty())
        {
            MetadataValue value = meta.get(0);
            if (value.value() instanceof List)
            {
                Iterator<UUID> iter = ((List<UUID>) value.value()).iterator();
                while (iter.hasNext())
                {
                    if (minionId == iter.next())
                        iter.remove();
                }
            }
        }
    }


    /**
     * Get all the Ids of the Minions spawned by this CustomSkeleton
     *
     * @param entity entity to get the minions for
     * @param plugin owning plugin to access meta
     *
     * @return Unique ids of all minions that have been spawned (minions can be dead)
     */
    @SuppressWarnings("unchecked")
    public static List<UUID> getSpawnedMinions(LivingEntity entity, Plugin plugin)
    {
        List<MetadataValue> meta = entity.getMetadata(skeliMinionStorage);
        List<UUID> ids = new ArrayList<UUID>(meta.size());
        for (MetadataValue val : meta)
            if (val.getOwningPlugin() == plugin)
                if (val.value() instanceof List)
                    ids = (List<UUID>) val.value();
        return ids;
    }


    /**
     * Get the total number of minions summoned by this entity
     *
     * @param entity entity to get minion count
     * @param plugin owning plugin to access MetaData
     *
     * @return count or 0 if not set
     */
    public static int getTotalSummoned(LivingEntity entity, Plugin plugin)
    {
        List<MetadataValue> meta = entity.getMetadata(skeliTotalMinionCount);
        int totalCount = 0;
        if (!meta.isEmpty())
        {
            MetadataValue value = meta.get(0);
            if (value.value() instanceof Integer)
                totalCount = (Integer) value.value();
        }
        return totalCount;
    }
}
