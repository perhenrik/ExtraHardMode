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

package com.extrahardmode.modules;


import com.extrahardmode.ExtraHardMode;
import com.extrahardmode.mocks.MockExtraHardMode;
import com.extrahardmode.module.BlockModule;
import org.bukkit.Material;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class TestBlockModule
{
    private final ExtraHardMode plugin;

    private final BlockModule module;


    public TestBlockModule()
    {
        plugin = new MockExtraHardMode().get();
        module = new BlockModule(plugin);
    }


    @Test
    public void testBreaksFallingBlocks()
    {
        assertEquals(module.breaksFallingBlock(Material.STEP), true);
        assertEquals(module.breaksFallingBlock(Material.REDSTONE_TORCH_ON), true);
        assertEquals(module.breaksFallingBlock(Material.REDSTONE_TORCH_OFF), true);
        assertEquals(module.breaksFallingBlock(Material.TORCH), true);
        assertEquals(module.breaksFallingBlock(Material.RAILS), true);
        assertEquals(module.breaksFallingBlock(Material.ACTIVATOR_RAIL), true);
        assertEquals(module.breaksFallingBlock(Material.RED_ROSE), true);
        assertEquals(module.breaksFallingBlock(Material.BROWN_MUSHROOM), true);
        assertEquals(module.breaksFallingBlock(Material.WEB), true);

        assertEquals(module.breaksFallingBlock(Material.LOG), false);
    }
}
