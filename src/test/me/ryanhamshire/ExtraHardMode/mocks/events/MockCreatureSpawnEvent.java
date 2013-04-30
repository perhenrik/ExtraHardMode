package me.ryanhamshire.ExtraHardMode.mocks.events;

import me.ryanhamshire.ExtraHardMode.mocks.MockBlock;
import me.ryanhamshire.ExtraHardMode.mocks.MockLivingEntity;
import me.ryanhamshire.ExtraHardMode.mocks.MockLocation;
import me.ryanhamshire.ExtraHardMode.mocks.MockWorld;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Mocks most methods that are called on this event including
 * <pre>
 *     getWorld()
 *     getWorld().getName()
 *     getEntity()
 *     getEntity().getType()
 *     getSpawnReason()
 * </pre>
 */
public class MockCreatureSpawnEvent
{
    /**
     * Our mocked Event Object
     */
    private CreatureSpawnEvent cse = mock(CreatureSpawnEvent.class);

    /**
     * Constructor
     * param entity Pass in an Entity with a mocked getWorld()-method
     * @param reason what caused the event
     */
    public MockCreatureSpawnEvent (EntityType type, String worldName, CreatureSpawnEvent.SpawnReason reason)
    {
        World world = new MockWorld(worldName).get();
        Block block = new MockBlock(world).get();
        Location location = new MockLocation(block, world).get();
        LivingEntity entity = new MockLivingEntity(world).get();

        when (cse.getLocation()).thenReturn(location);
        when(cse.getEntity()).thenReturn(entity);
        when (cse.getSpawnReason()).thenReturn(reason);
    }

    /**
     * Get the mocked Event
     */
    public CreatureSpawnEvent get()
    {
        return cse;
    }
}
