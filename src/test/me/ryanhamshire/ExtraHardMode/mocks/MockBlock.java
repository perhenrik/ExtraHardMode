package me.ryanhamshire.ExtraHardMode.mocks;

import org.bukkit.World;
import org.bukkit.block.Block;

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
        when(block.getWorld()).thenReturn(world);
    }

    public Block get()
    {
        return block;
    }
}
