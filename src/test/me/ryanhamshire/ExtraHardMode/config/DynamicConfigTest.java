package me.ryanhamshire.ExtraHardMode.config;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import me.ryanhamshire.ExtraHardMode.ExtraHardMode;
import me.ryanhamshire.ExtraHardMode.service.ConfigNode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.whenNew;


/**
 * Created with IntelliJ IDEA.
 * User: max
 * Date: 3/17/13
 * Time: 11:09 AM
 * To change this template use File | Settings | File Templates.
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest({DynamicConfig.class, ExtraHardMode.class})
public class DynamicConfigTest
{
    //Mock Plugin
    ExtraHardMode plugin = PowerMockito.mock(ExtraHardMode.class);
    Table<Integer, ConfigNode, Object> testMap = HashBasedTable.create();
    DynamicConfig cfg = new DynamicConfig(plugin);

    @Test
    public void testGetBoolean()
    {
        generateAdvancedCfg();
        assertEquals(false, cfg.getBoolean(RootNode.DONT_MOVE_WATER_SOURCE_BLOCKS, "pvp"));
        assertEquals(false, cfg.getBoolean(RootNode.WEAK_FOOD_CROPS, "world_the_end"));
        assertEquals(false, cfg.getBoolean(RootNode.ALWAYS_ANGRY_PIG_ZOMBIES, "world"));
        assertEquals(true, cfg.getBoolean(RootNode.ALWAYS_ANGRY_PIG_ZOMBIES, "worlds"));
    }

    @Test
    public void testGetInt()
    {
        generateAdvancedCfg();
        assertEquals(24, cfg.getInt(RootNode.IRON_DURABILITY_PENALTY, "world"));
        assertEquals(22, cfg.getInt(RootNode.IRON_DURABILITY_PENALTY, "worlds"));
        assertEquals(80, cfg.getInt(RootNode.IRON_DURABILITY_PENALTY, "pvp"));
    }

    @Test
    public void testGetLastIndex()
    {
        generateAdvancedCfg();
        //More Data
        assertEquals(2, cfg.getLastIndex(RootNode.WEAK_FOOD_CROPS, "pvp"));
        assertEquals(0, cfg.getLastIndex(RootNode.ALWAYS_ANGRY_PIG_ZOMBIES, "world"));
        assertEquals(0, cfg.getLastIndex(RootNode.WEAK_FOOD_CROPS, "miningWorld"));
        assertEquals(1, cfg.getLastIndex(RootNode.DONT_MOVE_WATER_SOURCE_BLOCKS, "world_nether"));
        assertEquals(2, cfg.getLastIndex(RootNode.DONT_MOVE_WATER_SOURCE_BLOCKS, "world_the_end"));
    }

    public void generateAdvancedCfg()
    {
        cfg.initTable();
        for (int i = 0; i < 3; i++)
        {
            ArrayList <String> worlds = new ArrayList<String>();
            switch (i)
            {
                case 0:
                    worlds.add("world");
                    worlds.add("pvp");
                    worlds.add("miningWorld");
                    cfg.updateOption(i, RootNode.WEAK_FOOD_CROPS, true);
                    cfg.updateOption(i, RootNode.ALWAYS_ANGRY_PIG_ZOMBIES, false);
                    cfg.updateOption(i, RootNode.IRON_DURABILITY_PENALTY, 24);
                    break;
                case 1:
                    worlds.add("world_nether");
                    worlds.add("worlds");
                    cfg.updateOption(i, RootNode.ALWAYS_ANGRY_PIG_ZOMBIES, true);
                    cfg.updateOption(i, RootNode.DONT_MOVE_WATER_SOURCE_BLOCKS, false);
                    cfg.updateOption(i, RootNode.IRON_DURABILITY_PENALTY, 22);
                    break;
                case 2:
                    worlds.add("world_the_end");
                    worlds.add("pvp");
                    cfg.updateOption(i, RootNode.WEAK_FOOD_CROPS, false);
                    cfg.updateOption(i, RootNode.DONT_MOVE_WATER_SOURCE_BLOCKS, false);
                    cfg.updateOption(i, RootNode.IRON_DURABILITY_PENALTY, 80);
                    break;
            }
            cfg.updateOption(i, RootNode.WORLDS, worlds);
        }
    }
}
