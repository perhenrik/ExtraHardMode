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

import org.bukkit.entity.Player;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Mocks a Player and his name
 * @author Max
 */
public class MockPlayer
{
    private final Player myPlayer = mock(Player.class);

    /**
     * Mock a Player with some basic methods
     * @param name
     */
    public MockPlayer (String name)
    {
        when(myPlayer.getName()).thenReturn(name);
    }

    public Player get()
    {
        return myPlayer;
    }
}
