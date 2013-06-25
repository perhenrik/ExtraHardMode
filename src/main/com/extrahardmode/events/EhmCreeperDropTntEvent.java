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

package com.extrahardmode.events;


import org.bukkit.Location;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event is called everytime a Creeper drops Tnt
 *
 * @author Diemex
 */
public class EhmCreeperDropTntEvent extends Event implements Cancellable
{
    private boolean cancelled;

    private final Player player;

    private final Creeper creeper;

    private Location location;


    /**
     * This looks like a constructor
     *
     * @param player
     *         Player who killed the Creeper, can be null if not a Player kill
     * @param creeper
     *         who has been killed and dropped the tnt
     * @param location
     *         of the tnt-drop
     */
    public EhmCreeperDropTntEvent(final Player player, final Creeper creeper, Location location)
    {
        this.player = player;
        this.creeper = creeper;
        this.location = location;
    }


    /**
     * Get the Location where the tnt will be dropped
     *
     * @return the location
     */
    public Location getLocation()
    {
        return location;
    }


    /**
     * Set the location where the tnt will drop
     *
     * @param location
     *         the location to set
     */
    public void setLocation(Location location)
    {
        this.location = location;
    }


    /**
     * Get the Creeper resposible for dropping the tnt
     *
     * @return the creeper
     */
    public Creeper getCreeper()
    {
        return creeper;
    }


    /**
     * Get the Player which killed the Creeper
     *
     * @return the player, can be null if not killed by a Player
     */
    public Player getPlayer()
    {
        return player;
    }


    /**
     * @return if Event got cancelled
     */
    @Override
    public boolean isCancelled()
    {
        return cancelled;
    }


    /**
     * @param cancelled
     *         set if the Event is cancelled which mean that the Skeleton will take normal damage
     */
    @Override
    public void setCancelled(boolean cancelled)
    {
        this.cancelled = cancelled;
    }


    private static final HandlerList HANDLERS = new HandlerList();


    public HandlerList getHandlers()
    {
        return HANDLERS;
    }


    public static HandlerList getHandlerList()
    {
        return HANDLERS;
    }
}
