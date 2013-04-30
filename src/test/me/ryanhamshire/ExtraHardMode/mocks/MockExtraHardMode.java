package me.ryanhamshire.ExtraHardMode.mocks;

import me.ryanhamshire.ExtraHardMode.ExtraHardMode;

import static org.powermock.api.mockito.PowerMockito.mock;

/**
 *
 */
public class MockExtraHardMode
{
    ExtraHardMode ehm;

    public ExtraHardMode get ()
    {
        ehm = mock(ExtraHardMode.class);
        return ehm;
    }
}
