package me.ryanhamshire.ExtraHardMode.modules;

import me.ryanhamshire.ExtraHardMode.ExtraHardMode;
import me.ryanhamshire.ExtraHardMode.MockExtraHardMode;
import me.ryanhamshire.ExtraHardMode.mocks.MockLocation;
import me.ryanhamshire.ExtraHardMode.module.DataStoreModule;
import org.bukkit.Location;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.UUID;

import static org.junit.Assert.assertEquals;

/**
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(DataStoreModule.class)
public class TestDataStoreModule
{
    DataStoreModule module;
    ExtraHardMode plugin;

    public TestDataStoreModule()
    {
        plugin = new MockExtraHardMode().get();
        module = new DataStoreModule(plugin);
    }

    /*@Test
    public void testContainsFallLogById()
    {
        UUID id = new UUID (0x23, 0x34);
        Location loc = new MockLocation(10, 64, 10).get();

        module.addFallLog(id, loc);
        assertEquals(module.isMarkedForProcessing(id), true);
        assertEquals(module.isMarkedForProcessing(new UUID(0x22, 0x33)), false);
    }

    @Test
    public void testContainsFallLogByLoc()
    {
        UUID id = new UUID(0x1, 0x12);
        Location loc = new MockLocation(12, 34, 123).get();

        module.addFallLog(id, loc);
        assertEquals(module.isBlockFallingAtLoc(loc), true);
        assertEquals(module.isBlockFallingAtLoc(new MockLocation(1234, 32, 9877).get()), false);
    }

    @Test
    public void testRmFallLogById()
    {
        UUID id = new UUID(0x2, 0x3);
        Location loc = new MockLocation(-10, 64, 20).get();

        module.addFallLog(id, loc);
        assertEquals(module.isMarkedForProcessing(id), true);
        module.rmFallLogById(id);
        assertEquals(module.isMarkedForProcessing(id), false);
    }

    @Test
    public void testRmFallLogByLoc()
    {
        UUID id = new UUID(0x102, 0x1);
        Location loc = new MockLocation(-31, 23, 345).get();

        module.addFallLog(id, loc);
        assertEquals(module.isBlockFallingAtLoc(loc), true);
        module.rmFallLogById(id);
        assertEquals(module.isBlockFallingAtLoc(loc), false);
    }*/
}
