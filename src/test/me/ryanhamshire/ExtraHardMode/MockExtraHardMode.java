package me.ryanhamshire.ExtraHardMode;

import org.powermock.api.mockito.PowerMockito;

/**
 * Mock Plugin Object
 */
public class MockExtraHardMode
{
    ExtraHardMode instance = PowerMockito.mock(ExtraHardMode.class);

    public MockExtraHardMode()
    {

    }

    public ExtraHardMode getInstance()
    {
        return instance;
    }
}
