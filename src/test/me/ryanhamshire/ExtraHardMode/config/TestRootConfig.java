package me.ryanhamshire.ExtraHardMode.config;

import me.ryanhamshire.ExtraHardMode.ExtraHardMode;
import me.ryanhamshire.ExtraHardMode.MockExtraHardMode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 *  Test the MultiWorldConfig
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest({RootConfig.class, ExtraHardMode.class})
public class TestRootConfig
{
    //Mock Plugin
    ExtraHardMode plugin = new MockExtraHardMode().get();
    RootConfig cfg = new RootConfig(plugin);

    public TestRootConfig()
    {
        cfg.set("world", RootNode.WEAK_FOOD_CROPS, true);
        cfg.set("pvp", RootNode.WEAK_FOOD_CROPS, false);
        cfg.set("world_the_end", RootNode.WEAK_FOOD_CROPS, false);
        cfg.set("miningWorld", RootNode.WEAK_FOOD_CROPS, true);

        cfg.set("world", RootNode.ALWAYS_ANGRY_PIG_ZOMBIES, false);
        cfg.set("world_nether", RootNode.ALWAYS_ANGRY_PIG_ZOMBIES, true);
        cfg.set("worlds", RootNode.ALWAYS_ANGRY_PIG_ZOMBIES, true);


        cfg.set("world_nether", RootNode.DONT_MOVE_WATER_SOURCE_BLOCKS, false);
        cfg.set("worlds", RootNode.DONT_MOVE_WATER_SOURCE_BLOCKS, false);
        cfg.set("pvp", RootNode.DONT_MOVE_WATER_SOURCE_BLOCKS, false);
        cfg.set("world_the_end", RootNode.DONT_MOVE_WATER_SOURCE_BLOCKS, false);

        cfg.set("world", RootNode.IRON_DURABILITY_PENALTY, 24);
        cfg.set("world_nether", RootNode.IRON_DURABILITY_PENALTY, 22);
        cfg.set("worlds", RootNode.IRON_DURABILITY_PENALTY, 22);
        cfg.set("pvp", RootNode.IRON_DURABILITY_PENALTY, 80);
        cfg.set("world_the_end", RootNode.IRON_DURABILITY_PENALTY, 80);
    }

    /**
     * Do we retrieve the same values we put in the config earlier?
     */
    @Test
    public void testGetBoolean()
    {
        assertEquals(false, cfg.getBoolean(RootNode.DONT_MOVE_WATER_SOURCE_BLOCKS, "pvp"));
        assertEquals(false, cfg.getBoolean(RootNode.WEAK_FOOD_CROPS, "world_the_end"));
        assertEquals(false, cfg.getBoolean(RootNode.ALWAYS_ANGRY_PIG_ZOMBIES, "world"));
        assertEquals(true, cfg.getBoolean(RootNode.ALWAYS_ANGRY_PIG_ZOMBIES, "worlds"));
    }

    @Test
    public void testGetInt()
    {
        assertEquals(24, cfg.getInt(RootNode.IRON_DURABILITY_PENALTY, "world"));
        assertEquals(22, cfg.getInt(RootNode.IRON_DURABILITY_PENALTY, "worlds"));
        assertEquals(80, cfg.getInt(RootNode.IRON_DURABILITY_PENALTY, "pvp"));
    }

    /**
     * Test what happens if we query a value that doesn't exist in the config
     */
    @Test
    public void testNotExisting()
    {
        //Integer
        assertEquals(0, cfg.getInt(RootNode.BONUS_NETHER_BLAZE_SPAWN_PERCENT, "test123"));
        //Boolean
        assertEquals(false, cfg.getBoolean(RootNode.DONT_MOVE_WATER_SOURCE_BLOCKS, "test123"));
        //String
        assertEquals("", cfg.getString(RootNode.MODE, "test123"));
        //Double
        assertEquals(0.0, cfg.getDouble(RootNode.NO_SWIMMING_IN_ARMOR_ARMOR_POINTS, "test123"), 0.0);
        //StringList
        assertEquals(Collections.<String>emptyList(), cfg.getStringList(RootNode.MORE_FALLING_BLOCKS, "test123"));
    }

    /**
     * Getting a boolean with an illegal argument
     */
    @Test (expected = IllegalArgumentException.class)
    public void illegalArgumentBoolean()
    {
        cfg.getBoolean(RootNode.WORLDS, "");
    }

    /**
     * Getting an int with an illegal argument
     */
    @Test (expected = IllegalArgumentException.class)
    public void illegalArgumentInt()
    {
        cfg.getInt(RootNode.MORE_FALLING_BLOCKS, "");
    }

    /**
     * Getting a double with an illegal argument
     */
    @Test (expected = IllegalArgumentException.class)
    public void illegalArgumentDouble()
    {
        cfg.getDouble(RootNode.ARID_DESSERTS, "");
    }

    /**
     * Getting a string with an illegal argument
     */
    @Test (expected = IllegalArgumentException.class)
    public void illegalArgumentString()
    {
        cfg.getString(RootNode.BONUS_NETHER_BLAZE_SPAWN_PERCENT, "");
    }

    /**
     * Getting a list with an illegal argument
     */
    @Test (expected = IllegalArgumentException.class)
    public void illegalArgumentStringList()
    {
        cfg.getStringList(RootNode.ANIMAL_EXP_NERF, "");
    }

    @Test
    public void testGetEnabledWorlds()
    {
        //HashSet because the order doesn't matter
        HashSet<String> expectedWorlds = new HashSet<String>(Arrays.asList(new String[]{"world", "pvp", "world_the_end", "miningWorld", "world_nether", "worlds"}));
        HashSet<String> inputWorlds = new HashSet<String>(Arrays.asList(cfg.getEnabledWorlds()));
        assertTrue(expectedWorlds.equals(inputWorlds));
    }
}