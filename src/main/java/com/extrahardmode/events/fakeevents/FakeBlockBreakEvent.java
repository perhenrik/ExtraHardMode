package com.extrahardmode.events.fakeevents;


import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

/**
 * A fake event that is called when we check if something can be broken (FallingBlocks)
 */
public class FakeBlockBreakEvent extends BlockBreakEvent
{
    public FakeBlockBreakEvent(Block theBlock, Player player)
    {
        super(theBlock, player);
    }
}
