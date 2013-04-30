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
    private World world = mock (World.class);
    /**
     * Environment of this world NORMAL/NETHER/END
     */
    private World.Environment environment;

    /**
     * Construct a new mocked World with a given name
     * @param name
     */
    public MockWorld (String name)
    {
        when(world.getName()).thenReturn(name);
    }

    /**
     * Set the Environment of this World
     * @param environment
     */
    public void setEnvironment (World.Environment environment)
    {
        this.environment = environment;
        when( this.get().getEnvironment()).thenReturn(environment);
    }

    /**
     * Get the Environment of this World
     */
    public World.Environment getEnvironment()
    {
        return environment;
    }

    /**
     * Get the mocked Object
     */
    public World get()
    {
        return world;
    }
}
