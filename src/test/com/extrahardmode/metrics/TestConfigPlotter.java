package com.extrahardmode.metrics;


import com.extrahardmode.service.MockConfigNode;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Diemex
 */
public class TestConfigPlotter
{
    @Test
    public void testLastPart()
    {
        assertEquals("test04", ConfigPlotter.getLastPart(MockConfigNode.BOOL_TRUE));
        assertEquals("test 01", ConfigPlotter.getLastPart(MockConfigNode.BOOL_FALSE));
    }
}
