package com.extrahardmode.mocks;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Mock a Block
 * <pre>
 *     getWorld
 * </pre>
 */
public class MockBlock
{
    private Block block = mock(Block.class);

    public MockBlock (World world)
    {
        when( block.getWorld()).thenReturn(world);
    }

    public void setRelative (BlockFace face, Block block)
    {
        when( this.block.getRelative(any(BlockFace.class))).thenReturn( block);
    }

    public void setMaterial (Material material)
    {
        when( block.getType()).thenReturn(material);
    }

    public Block get()
    {
        return block;
    }
}
