package com.extrahardmode.compatibility;


import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

/**
 * Interface for block loggers aka CoreProtect, Prism
 */
public interface IBlockLogger
{
    /**
     * Tag (player's name) to use whenever a FallingBlock starts to fall
     */
    final String fallingBlockFallTag = "#ehm_block_fall#";

    /**
     * Tag (player's name) to use whenever a FallingBlock lands
     */
    final String fallingBlockLandTag = "#ehm_block_land#";

    /**
     * Log when a FallingBlock starts to fall
     *
     * @param block that turned into a FallingBlock
     */
    public void logFallingBlockFall(Block block);

    /**
     * Log when a FallingBlock lands
     *
     * @param block falling block that landed
     */
    public void logFallingBlockLand (BlockState block);
}
