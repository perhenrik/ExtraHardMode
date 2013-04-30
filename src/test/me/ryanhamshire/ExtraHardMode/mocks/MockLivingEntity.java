package me.ryanhamshire.ExtraHardMode.mocks;

import org.bukkit.World;
import org.bukkit.entity.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * A very basic Mock only overrides
 * <pre>
 *     getWorld
 * </pre>
 */
public class MockLivingEntity
{
    LivingEntity entity;

    /**
     * Basic constructor
     * @param world the world where this Entity resides in
     * @param type to stub
     */
    public MockLivingEntity(World world, EntityType type)
    {
        mockForType(type);
        when( entity.getType()).thenReturn(type);
        when( entity.getWorld()).thenReturn(world);
    }


    /**
     * Get the mocked Object
     */
    public LivingEntity get()
    {
        return entity;
    }


    private void  mockForType(EntityType type)
    {
        switch( type )
        {
            case BLAZE:
                entity = mock( Blaze.class );
                break;
            case CAVE_SPIDER:
                entity = mock( CaveSpider.class );
                break;
            case ENDERMAN:
                entity = mock( Enderman.class );
                break;
            case GHAST:
                entity = mock( Ghast.class );
                break;
            case PIG_ZOMBIE:
                entity = mock( PigZombie.class );
                break;
            case SILVERFISH:
                entity = mock( Silverfish.class );
                break;
            case SKELETON:
                entity = mock( Skeleton.class );
                break;
            case SLIME:
                entity = mock( Slime.class );
                break;
            case SPIDER:
                entity = mock( Spider.class );
                break;
            case WITCH:
                entity = mock( Witch.class );
                break;
            case WITHER:
                entity = mock( Wither.class );
                break;
            case ZOMBIE:
                entity = mock( Zombie.class );
                break;
            default:
                entity = mock( LivingEntity.class );
                break;
        }
    }
}
