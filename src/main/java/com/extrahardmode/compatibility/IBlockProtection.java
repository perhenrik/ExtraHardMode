package com.extrahardmode.compatibility;


import org.bukkit.block.Block;

/**
 * @author Diemex
 */
public interface IBlockProtection
{
    boolean isProtectedBlock(Block block, String playerName);
}
