package com.extrahardmode.modules;

import com.extrahardmode.ExtraHardMode;
import com.extrahardmode.mocks.MockExtraHardMode;
import com.extrahardmode.module.BlockModule;
import org.bukkit.Material;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertEquals;

/**
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(BlockModule.class)
public class TestBlockModule
{
    ExtraHardMode plugin;
    BlockModule module;

    public TestBlockModule()
    {
        plugin  = new MockExtraHardMode().get();
        module  = new BlockModule(plugin);
    }

    @Test
    public void testBreaksFallingBlocks()
    {
        assertEquals (module.breaksFallingBlock(Material.STEP), true);
        assertEquals (module.breaksFallingBlock(Material.REDSTONE_TORCH_ON), true);
        assertEquals (module.breaksFallingBlock(Material.REDSTONE_TORCH_OFF), true);
        assertEquals (module.breaksFallingBlock(Material.TORCH), true);
        assertEquals (module.breaksFallingBlock(Material.RAILS), true);
        assertEquals (module.breaksFallingBlock(Material.ACTIVATOR_RAIL), true);
        assertEquals (module.breaksFallingBlock(Material.RED_ROSE), true);
        assertEquals (module.breaksFallingBlock(Material.BROWN_MUSHROOM), true);
        assertEquals (module.breaksFallingBlock(Material.WEB), true);

        assertEquals (module.breaksFallingBlock(Material.LOG), false);
    }
}
