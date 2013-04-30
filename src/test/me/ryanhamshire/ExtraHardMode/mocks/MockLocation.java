package me.ryanhamshire.ExtraHardMode.mocks;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Basic Location
 * <pre>
 *     mock getBlock(X/Y/Z)
 *     mock get(X/Y/Z)
 *     override mock getBlock()
 * </pre>
 */
public class MockLocation
{
    /**
     * Location Object
     */
    private Location loc = mock(Location.class);

    /**
     * Block that resides at this Location
     */
    private MockBlock block;

    /**
     * A basic Constructor
     * @param world
     */
    public MockLocation(World world)
    {
        when( loc.getWorld()).thenReturn(world);
    }

    /**
     * Block at this Location
     * @return
     */
    public MockBlock getBlock()
    {
        return block;
    }

    /**
     * Set the Block that is at this Location
     * @param block
     */
    public void setBlock(MockBlock block)
    {
        this.block = block;
        when( loc.getBlock()).thenReturn(block.get());
    }

    public Location get()
    {
        return loc;
    }
}
