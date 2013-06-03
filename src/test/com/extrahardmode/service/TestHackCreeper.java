/*
 * This file is part of
 * ExtraHardMode Server Plugin for Minecraft
 *
 * Copyright (C) 2012 Ryan Hamshire
 * Copyright (C) 2013 Diemex
 *
 * ExtraHardMode is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ExtraHardMode is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero Public License
 * along with ExtraHardMode.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.extrahardmode.service;

import com.extrahardmode.mocks.MockExtraHardMode;
import com.extrahardmode.mocks.MockLocation;
import com.extrahardmode.mocks.MockWorld;
import org.bukkit.metadata.FixedMetadataValue;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @author Diemex
 */
public class TestHackCreeper
{
    private final MockExtraHardMode plugin = new MockExtraHardMode();
    private HackCreeper creeps;

    private final String myKey = "MockingRules";
    private final Object obj = 0;
    private final FixedMetadataValue meta = new FixedMetadataValue(plugin.get(), obj);

    /**
     * Call this every time so we have a fresh Object
     */
    void init()
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
