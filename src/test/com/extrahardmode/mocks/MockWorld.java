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

package com.extrahardmode.mocks;

import org.bukkit.World;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Mocks commonly used methods like
 * <pre>
 *     getName()
 * </pre>
 */
public class MockWorld
{
    /**
     * Our mocked World Object
     */
    private final World world = mock (World.class);
    /**
     * Environment of this world NORMAL/NETHER/END
     */
    private World.Environment environment;

    /**
     * Construct a new mocked World with a given name
     * @param name
     */
    public MockWorld (String name)
    {
        when(world.getName()).thenReturn(name);
    }

    /**
     * Set the Environment of this World
     * @param environment
     */
    public void setEnvironment (World.Environment environment)
    {
        this.environment = environment;
        when( this.get().getEnvironment()).thenReturn(environment);
    }

    /**
     * Get the Environment of this World
     */
    public World.Environment getEnvironment()
    {
        return environment;
    }

    /**
     * Get the mocked Object
     */
    public World get()
    {
        return world;
    }
}
