package me.ryanhamshire.ExtraHardMode.mocks.events;

import me.ryanhamshire.ExtraHardMode.mocks.MockBlock;
import me.ryanhamshire.ExtraHardMode.mocks.MockLivingEntity;
import me.ryanhamshire.ExtraHardMode.mocks.MockLocation;
import me.ryanhamshire.ExtraHardMode.mocks.MockWorld;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.event.entity.CreatureSpawnEvent;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Mocks frequently used  methods that are called on this event, by default
 * <pre>
 *     getWorld()
 *     getEntity()
 *     getSpawnReason()
 * </pre>
 * Generates a World Object, which can be retrieved via getWorld()
 */
public class MockCreatureSpawnEvent
{
    /**
     * Our mocked Event Object
     */
    private CreatureSpawnEvent cse = mock(CreatureSpawnEvent.class);
    /**
     * World where this event occurred
     */
    private MockWorld world;
    /**
     * Entity of this Event
     */
    private MockLivingEntity entity;
    /**
     * Block where this Event occured
     */
    private MockBlock block;
    /**
     * Location where the Event occurred
     */
    private MockLocation location;

    /**
     * Constructor
     * param entity Pass in an Entity with a mocked getWorld()-method
     * @param type of the spawned Entity
     * @param worldName name of the world where this Event occured
     * @param reason what caused the event
     */
    public MockCreatureSpawnEvent (EntityType type, String worldName, CreatureSpawnEvent.SpawnReason reason)
    {
        world = new MockWorld(worldName);
        entity = new MockLivingEntity(world.get(), type);
        location = new MockLocation(world.get());
        setLocation(location);

        when( cse.getEntity()).thenReturn(entity.get());
        when( cse.getSpawnReason()).thenReturn(reason);
    }

    /**
     * Set the Entity we mocked
     * @param entity
     */
    public void setEntity (MockLivingEntity entity)
    {
        this.entity = entity;
        when( this.get().getEntity()).thenReturn(entity.get());
    }

    /**
     * Get the MockEntity we set
     * @return
     */
    public MockLivingEntity getEntity ()
    {
        return entity;
    }

    /**
     * Set the MockWorld Object for this Event
     * @param world MockWorld where this Event occured
     */
    public void setWorld(MockWorld world)
    {
        this.world = world;
    }

    /**
     * Get the mocked World Object where this Event took place
     */
    public MockWorld getWorld ()
    {
        return world;
    }

    /**
     * Set the Location where the Event ocurred
     * @param location
     */
    public void setLocation( MockLocation location)
    {
        this.location = location;
        when( cse.getLocation()).thenReturn(location.get());
    }

    /**
     * Get the Location of the Event
     * @return
     */
    public MockLocation getLocation()
    {
        return location;
    }

    /**
     * Get the mocked Event
     */
    public CreatureSpawnEvent get()
    {
        return cse;
    }
}
