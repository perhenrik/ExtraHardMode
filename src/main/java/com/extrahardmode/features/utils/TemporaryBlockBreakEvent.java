package com.extrahardmode.features.utils;


import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TemporaryBlockBreakEvent extends Event
{
    private Location loc;


    public TemporaryBlockBreakEvent(Location loc)
    {
        this.loc = loc;
    }


    public Location getLoc()
    {
        return loc;
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
