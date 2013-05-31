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
    Player myPlayer = mock(Player.class);

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
