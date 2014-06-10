package com.extrahardmode.features.utils;


import com.extrahardmode.ExtraHardMode;
import com.extrahardmode.service.ListenerModule;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.HashMap;
import java.util.Map;

public class TemporaryBlockHandler extends ListenerModule
{
    private Map<Location, TemporaryBlock> temporaryBlockList = new HashMap<Location, TemporaryBlock>();


    public TemporaryBlockHandler(ExtraHardMode plugin)
    {
        super(plugin);
    }


    /**
     * int addTemporaryBlock(Block block)
     * removeBlock (int)
     * onBlockBreak -> mark as broken
     * onTempBlockBreakEvent
     * onZombieRespawnTask -> check if broken
     */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event)
    {
        Block block = event.getBlock();
        if (temporaryBlockList.containsKey(block.getLocation()))
        {
            temporaryBlockList.get(block.getLocation()).isBroken = true;
            plugin.getServer().getPluginManager().callEvent(new TemporaryBlockBreakEvent(event.getBlock().getLocation()));
        }
    }


    public void addTemporaryBlock(Location loc)
    {
        TemporaryBlock temporaryBlock = new TemporaryBlock(loc);
        temporaryBlockList.put(loc, temporaryBlock);
    }
}
