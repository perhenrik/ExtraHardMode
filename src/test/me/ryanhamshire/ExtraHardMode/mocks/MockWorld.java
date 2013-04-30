package me.ryanhamshire.ExtraHardMode.mocks;

import org.bukkit.World;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Mocks commonly used methods like
 * <pre>
 *     getName()
 * </pre>
 */
public class MockWorld
{
    /**
     * Our mocked World Object
     */
    World world = mock (World.class);

    /**
     * Construct a new mocked World with a given name
     * @param name
     */
    public MockWorld (String name)
    {
        when(world.getName()).thenReturn(name);
    }

    /**
     * Get the mocked Object
     */
    public World get()
    {
        return world;
    }
}
