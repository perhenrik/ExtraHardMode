package me.ryanhamshire.ExtraHardMode.mocks;

import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.powermock.core.classloader.annotations.PrepareForTest;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * A very basic Mock only overrides
 * <pre>
 *     getWorld
 * </pre>
 */
@PrepareForTest(LivingEntity.class)
public class MockLivingEntity
{
    LivingEntity entity = mock(LivingEntity.class);

    /**
     * Basic constructor
     * @param world the world where this Entity resides in
     */
    public MockLivingEntity(World world)
    {
        when(entity.getWorld()).thenReturn(world);
    }

    /**
     * Get the mocked Object
     */
    public LivingEntity get()
    {
        return entity;
    }
}
