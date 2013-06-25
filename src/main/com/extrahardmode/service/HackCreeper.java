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

package com.extrahardmode.service;


import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * A instance of a Creeper so we can label Explosions as Creeper Explosions. This is considered a temporary "hack" in
 * the hope that other plugins don't do much with the Creeper that is passed in by the Explosion
 *
 * @author Diemex
 */
public class HackCreeper implements Creeper
{
    /**
     * Location where this Creeper has exploded, e.g. the Location of the Explosion
     */
    private final Location loc;

    /**
     * Metadata to mark this Explosion to be ignored by EHM
     */
    private final Map</*metaDataKey*/String, List<MetadataValue>> meta = new HashMap<String, List<MetadataValue>>();


    /**
     * Constructor to "mock" most methods for use with other plugins
     */
    public HackCreeper(Location loc)
    {
        this.loc = loc;
    }


    @Override
    public Location getLocation()
    {
        return loc;
    }


    @Override
    public Location getLocation(Location loc)
    {
        return loc;
    }


    @Override
    public Location getEyeLocation()
    {
        return loc.clone().add(0, 1, 0); //add doesn't modify the original object
    }


    @Override
    public void setMetadata(String metadataKey, MetadataValue newMetadataValue)
    {
        List<MetadataValue> metaValue = meta.get(metadataKey) != null ? meta.get(metadataKey) : new ArrayList<MetadataValue>();
        metaValue.add(newMetadataValue);
        meta.put(metadataKey, metaValue);
    }


    @Override
    public List<MetadataValue> getMetadata(String metadataKey)
    {
        return meta.get(metadataKey);
    }


    @Override
    public boolean hasMetadata(String metadataKey)
    {
        return meta.containsKey(metadataKey);
    }


    @Override
    public void removeMetadata(String metadataKey, Plugin owningPlugin)
    {
        meta.remove(metadataKey);
    }


    @Override
    public EntityType getType()
    {
        return EntityType.CREEPER;
    }


    @Override
    public World getWorld()
    {
        return loc.getWorld();
    }


    @Override
    public boolean isDead()
    {
        return true; //hope other plugins ignore us then!
    }


    @Override
    public boolean isValid()
    {
        return false; //our hack creeper ain't valid is it?
    }


    /**
     * No methods are overwritten below this line #################################################################################################################
     */

    @Override
    public boolean isPowered()
    {
        return false;
    }


    @Override
    public void setPowered(boolean value)
    {
    }


    @Override
    public void setTarget(LivingEntity target)
    {
    }


    @Override
    public LivingEntity getTarget()
    {
        return null;
    }


    @Override
    public double getEyeHeight()
    {
        return 0;
    }


    @Override
    public double getEyeHeight(boolean ignoreSneaking)
    {
        return 0;
    }


    @Override
    public List<Block> getLineOfSight(HashSet<Byte> transparent, int maxDistance)
    {
        return null;
    }


    @Override
    public Block getTargetBlock(HashSet<Byte> transparent, int maxDistance)
    {
        return null;
    }


    @Override
    public List<Block> getLastTwoTargetBlocks(HashSet<Byte> transparent, int maxDistance)
    {
        return null;
    }


    @Override
    public Egg throwEgg()
    {
        return null;
    }


    @Override
    public Snowball throwSnowball()
    {
        return null;
    }


    @Override
    public Arrow shootArrow()
    {
        return null;
    }


    @Override
    public <T extends Projectile> T launchProjectile(Class<? extends T> projectile)
    {
        return null;
    }


    @Override
    public int getRemainingAir()
    {
        return 0;
    }


    @Override
    public void setRemainingAir(int ticks)
    {
    }


    @Override
    public int getMaximumAir()
    {
        return 0;
    }


    @Override
    public void setMaximumAir(int ticks)
    {
    }


    @Override
    public int getMaximumNoDamageTicks()
    {
        return 0;
    }


    @Override
    public void setMaximumNoDamageTicks(int ticks)
    {
    }


    @Override
    public int getLastDamage()
    {
        return 0;
    }


    @Override
    public void setLastDamage(int damage)
    {
    }


    @Override
    public int getNoDamageTicks()
    {
        return 0;
    }


    @Override
    public void setNoDamageTicks(int ticks)
    {
    }


    @Override
    public Player getKiller()
    {
        return null;
    }


    @Override
    public boolean addPotionEffect(PotionEffect effect)
    {
        return false;
    }


    @Override
    public boolean addPotionEffect(PotionEffect effect, boolean force)
    {
        return false;
    }


    @Override
    public boolean addPotionEffects(Collection<PotionEffect> effects)
    {
        return false;
    }


    @Override
    public boolean hasPotionEffect(PotionEffectType type)
    {
        return false;
    }


    @Override
    public void removePotionEffect(PotionEffectType type)
    {
    }


    @Override
    public Collection<PotionEffect> getActivePotionEffects()
    {
        return null;
    }


    @Override
    public boolean hasLineOfSight(Entity other)
    {
        return false;
    }


    @Override
    public boolean getRemoveWhenFarAway()
    {
        return false;
    }


    @Override
    public void setRemoveWhenFarAway(boolean remove)
    {
    }


    @Override
    public EntityEquipment getEquipment()
    {
        return null;
    }


    @Override
    public void setCanPickupItems(boolean pickup)
    {
    }


    @Override
    public boolean getCanPickupItems()
    {
        return false;
    }


    @Override
    public void setCustomName(String name)
    {
    }


    @Override
    public String getCustomName()
    {
        return null;
    }


    @Override
    public void setCustomNameVisible(boolean flag)
    {
    }


    @Override
    public boolean isCustomNameVisible()
    {
        return false;
    }


    @Override
    public void damage(int amount)
    {
    }


    @Override
    public void damage(int amount, Entity source)
    {
    }


    @Override
    public int getHealth()
    {
        return 0;
    }


    @Override
    public void setHealth(int health)
    {
    }


    @Override
    public int getMaxHealth()
    {
        return 0;
    }


    @Override
    public void setMaxHealth(int health)
    {
    }


    @Override
    public void resetMaxHealth()
    {
    }


    @Override
    public void setVelocity(Vector velocity)
    {
    }


    @Override
    public Vector getVelocity()
    {
        return null;
    }


    @Override
    public boolean isOnGround()
    {
        return false;
    }


    @Override
    public boolean teleport(Location location)
    {
        return false;
    }


    @Override
    public boolean teleport(Location location, PlayerTeleportEvent.TeleportCause cause)
    {
        return false;
    }


    @Override
    public boolean teleport(Entity destination)
    {
        return false;
    }


    @Override
    public boolean teleport(Entity destination, PlayerTeleportEvent.TeleportCause cause)
    {
        return false;
    }


    @Override
    public List<Entity> getNearbyEntities(double x, double y, double z)
    {
        return null;
    }


    @Override
    public int getEntityId()
    {
        return 0;
    }


    @Override
    public int getFireTicks()
    {
        return 0;
    }


    @Override
    public int getMaxFireTicks()
    {
        return 0;
    }


    @Override
    public void setFireTicks(int ticks)
    {
    }


    @Override
    public void remove()
    {
    }


    @Override
    public Server getServer()
    {
        return null;
    }


    @Override
    public Entity getPassenger()
    {
        return null;
    }


    @Override
    public boolean setPassenger(Entity passenger)
    {
        return false;
    }


    @Override
    public boolean isEmpty()
    {
        return false;
    }


    @Override
    public boolean eject()
    {
        return false;
    }


    @Override
    public float getFallDistance()
    {
        return 0;
    }


    @Override
    public void setFallDistance(float distance)
    {
    }


    @Override
    public void setLastDamageCause(EntityDamageEvent event)
    {
    }


    @Override
    public EntityDamageEvent getLastDamageCause()
    {
        return null;
    }


    @Override
    public UUID getUniqueId()
    {
        return null;
    }


    @Override
    public int getTicksLived()
    {
        return 0;
    }


    @Override
    public void setTicksLived(int value)
    {
    }


    @Override
    public void playEffect(EntityEffect type)
    {
    }


    @Override
    public boolean isInsideVehicle()
    {
        return false;
    }


    @Override
    public boolean leaveVehicle()
    {
        return false;
    }


    @Override
    public Entity getVehicle()
    {
        return null;
    }
}