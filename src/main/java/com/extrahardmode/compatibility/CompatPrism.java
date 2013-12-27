package com.extrahardmode.compatibility;


import me.botsko.prism.actionlibs.ActionFactory;
import me.botsko.prism.actionlibs.RecordingQueue;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.plugin.Plugin;

/**
 * Provides compatibility for {@link me.botsko.prism.Prism}
 */
public class CompatPrism implements ICompat, IBlockLogger
{
    private final Plugin plugin;
    //private Prism prismPlugin;


    public CompatPrism(Plugin plugin)
    {
        this.plugin = plugin;
    }

    @Override
    public void logFallingBlockFall(Block block)
    {
        RecordingQueue.addToQueue(ActionFactory.create("block-fall", block, fallingBlockFallTag));
    }


    @Override
    public void logFallingBlockLand(BlockState block)
    {
        RecordingQueue.addToQueue(ActionFactory.create("block-form", block, fallingBlockLandTag));
    }


    @Override
    public boolean isEnabled()
    {
        return plugin.getServer().getPluginManager().isPluginEnabled("Prism");
    }
}
