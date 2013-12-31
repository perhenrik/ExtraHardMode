package com.extrahardmode.compatibility;


import org.bukkit.block.Block;

/**
 * @author Diemex
 */
public class SafeMethods
{
    /**
     * Break a block as if calling block.breakNaturally(), but check if breaking the block is allowed before hand
     *
     * @param block the block to break
     *
     * @return if block was broken
     */
    public static boolean breakNaturally(Block block)
    {
        boolean allowed = !CompatHandler.isProtectedBlock(block);
        if (allowed)
            block.breakNaturally();
        return allowed;
    }


    /**
     * Break a block as if calling block.breakNaturally(), but check if breaking the block is allowed before hand
     *
     * @param block      the block to break
     * @param playerName player that should break the block
     *
     * @return if block was broken
     */
    public static boolean breakNaturally(Block block, String playerName)
    {
        boolean allowed;

        if (playerName == null)
            allowed = !CompatHandler.isProtectedBlock(block);
        else
            allowed = !CompatHandler.isProtectedBlock(block, playerName);

        if (allowed)
            block.breakNaturally();
        return allowed;
    }
}
