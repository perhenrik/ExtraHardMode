package me.ryanhamshire.ExtraHardMode;

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
