package me.ryanhamshire.ExtraHardMode.mocks;

import org.bukkit.Location;
import org.powermock.api.mockito.PowerMockito;

import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Location that returns the passed in values when called
 */
public class MockLocation
{
    Location mockLocation = PowerMockito.mock(Location.class);
    /**
     * Location that will return coordinates when getX() etc is called
     * @param x
     * @param y
     * @param z
     */
    public MockLocation (long x, long y, long z)
    {
        when(mockLocation.getX()).thenReturn(Double.valueOf(x));
        when(mockLocation.getY()).thenReturn(Double.valueOf(y));
        when(mockLocation.getZ()).thenReturn(Double.valueOf(z));

        when(mockLocation.getBlockX()).thenReturn((int)Math.floor(x));
        when(mockLocation.getBlockY()).thenReturn((int)Math.floor(y));
        when(mockLocation.getBlockZ()).thenReturn((int)Math.floor(z));
    }

    public Location get()
    {
        return mockLocation;
    }
}
