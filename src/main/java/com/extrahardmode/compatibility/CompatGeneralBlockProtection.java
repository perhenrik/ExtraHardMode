package com.extrahardmode.compatibility;


import com.extrahardmode.events.fakeevents.FakeBlockBreakEvent;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.plugin.Plugin;

/**
 * @author Diemex
 */
public class CompatGeneralBlockProtection implements ICompat, IBlockProtection
{
    private final Plugin plugin;


    public CompatGeneralBlockProtection(Plugin plugin)
    {
        this.plugin = plugin;
    }


    @Override
    public boolean isProtectedBlock(Block block, String playerName)
    {
        Player player = plugin.getServer().getPlayer(playerName);
        Cancellable event = null;
        if (player != null)
        {
            //We need to be really careful to not create infinite loops
            FakeBlockBreakEvent eventBreak = new FakeBlockBreakEvent(block, player);
            event = eventBreak;
            plugin.getServer().getPluginManager().callEvent(eventBreak);
        }
        return event != null && event.isCancelled();
    }


    @Override
    public boolean isEnabled()
    {
        return true;
    }
}
