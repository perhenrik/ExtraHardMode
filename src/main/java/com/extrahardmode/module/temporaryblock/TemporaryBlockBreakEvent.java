package com.extrahardmode.module.temporaryblock;


import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockBreakEvent;

public class TemporaryBlockBreakEvent extends Event
{
    private final BlockBreakEvent event;
    private final TemporaryBlock block;


    public TemporaryBlockBreakEvent(TemporaryBlock block, BlockBreakEvent event)
    {
        this.block = block;
        this.event = event;
    }


    public TemporaryBlock getBlock()
    {
        return block;
    }


    public BlockBreakEvent getBlockBreakEvent()
    {
        return event;
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
