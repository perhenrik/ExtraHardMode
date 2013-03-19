package me.ryanhamshire.ExtraHardMode.features;

import me.ryanhamshire.ExtraHardMode.features.AntiFarming;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * Test the new AntiFarmingTest
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest(AntiFarming.class)
public class AntiFarmingTest
{
    @Test
    public void testAntiFarmConfig ()
    {
        /*
        ExtraHardMode ehm = mock(ExtraHardMode.class);
        //BlockBreakEvent - netherwart - world
        //Player is final hence PowerMockito
        BlockBreakEvent breakEvent = mock(BlockBreakEvent.class);
        Block block = PowerMockito.mock(Block.class);

        Player player = PowerMockito.mock(Player.class);
        when(breakEvent.getPlayer()).thenReturn(player);
        when(player.hasPermission(PermissionNode.BYPASS.getNode())).thenReturn(false);

        World world = mock(World.class);
        when(block.getWorld()).thenReturn(world);
        when(world.getName()).thenReturn("world");

        when(block.getType()).thenReturn(Material.NETHER_WARTS);
        when(breakEvent.getBlock()).thenReturn(block);
        AntiFarming antiFarming = new AntiFarming(ehm);
        antiFarming.onBlockBreak(breakEvent);

        verify(block).getDrops().clear();

        //BlockBreakEvent - netherwart - world_nether
        BlockBreakEvent breakEvent1 = PowerMockito.mock(BlockBreakEvent.class);

        when(breakEvent1.getPlayer()).thenReturn(player);

        Block block_nether = mock(Block.class);
        when(block_nether.getType()).thenReturn(Material.NETHER_WARTS);
        when(breakEvent1.getBlock()).thenReturn(block_nether);

        World world_nether = mock(World.class);
        when(world_nether.getName()).thenReturn("world_nether");

        verify(block).getDrops().clear();
        */
    }

    @Test
    public void testOnBlockDispense ()
    {

    }
}