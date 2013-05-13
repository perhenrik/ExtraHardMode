package me.ryanhamshire.ExtraHardMode.service;

import me.ryanhamshire.ExtraHardMode.mocks.MockExtraHardMode;
import me.ryanhamshire.ExtraHardMode.mocks.MockLocation;
import me.ryanhamshire.ExtraHardMode.mocks.MockWorld;
import org.bukkit.metadata.FixedMetadataValue;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @author Diemex
 */
public class TestHackCreeper
{
    MockExtraHardMode plugin = new MockExtraHardMode();
    HackCreeper creeps;

    String myKey = "MockingRules";
    Object obj = 0;
    FixedMetadataValue meta = new FixedMetadataValue(plugin.get(), obj);

    /**
     * Call this every time so we have a fresh Object
     */
    public void init ()
    {
        MockWorld world = new MockWorld("world");
        MockLocation loc = new MockLocation(world.get());

        creeps = new HackCreeper(loc.get());
    }

    /**
     * Has the MetaData been set successfully?
     */
    @Test
    public void testMetaDataAdd ()
    {
        init();
        creeps.setMetadata(myKey, meta);

        //Has the meta been set successfully
        assertEquals(true, creeps.hasMetadata(myKey));
    }

    /**
     * Is the returned MetaData the same as the MetaData we set?
     */
    @Test
    public void testReturnedMeta ()
    {
        init();
        creeps.setMetadata(myKey, meta);

        assertEquals(obj, creeps.getMetadata(myKey).get(0).asInt());
    }

    /**
     * Will removing meta previously set actually remove it?
     */
    @Test
    public void testRemoveMeta()
    {
        init();
        creeps.setMetadata(myKey, meta);
        assertEquals(obj, creeps.getMetadata(myKey).get(0).asInt());

        creeps.removeMetadata(myKey, plugin.get());
        assertFalse(creeps.hasMetadata(myKey));
    }
}
