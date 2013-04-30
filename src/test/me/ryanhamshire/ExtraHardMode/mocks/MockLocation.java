package me.ryanhamshire.ExtraHardMode.mocks;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

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
    private Location loc = mock(Location.class);

    public MockLocation(Block block, World world)
    {
        when( loc.getBlock()).thenReturn(block);
        when( loc.getWorld()).thenReturn(world);
    }

    public Location get()
    {
        return loc;
    }
}
