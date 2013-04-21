package me.ryanhamshire.ExtraHardMode.service;

import me.ryanhamshire.ExtraHardMode.ExtraHardMode;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(PowerMockRunner.class)
@PrepareForTest(MultiWorldConfig.class)
public class TestMultiWorldConfig
{
    ExtraHardMode plugin = PowerMockito.mock(ExtraHardMode.class);
    private class Mock extends MultiWorldConfig{
        public Mock (ExtraHardMode plugin){super(plugin);}
        @Override public void load (){}
        @Override public void starting (){}
        @Override public void closing (){}
    }
    MultiWorldConfig module = new Mock(plugin);

    @Test
    public void testLoadNodeValidInput()
    {
        FileConfiguration config = getMatchingConfig();

        assertEquals    (false, module.loadNode(config, MockConfigNode.BOOL_TRUE, false).getContent());
        assertTrue      (module.loadNode(config, MockConfigNode.BOOL_TRUE, false).getStatusCode() == MultiWorldConfig.Status.OK);

        assertEquals    (false, module.loadNode(config, MockConfigNode.BOOL_FALSE, false).getContent());
        assertTrue      (module.loadNode(config, MockConfigNode.BOOL_FALSE, false).getStatusCode() == MultiWorldConfig.Status.OK);

        assertEquals    (4, module.loadNode(config, MockConfigNode.INT_0, false).getContent());
        assertTrue      (module.loadNode(config, MockConfigNode.INT_0, false).getStatusCode() == MultiWorldConfig.Status.OK);

        assertEquals    (9, module.loadNode(config, MockConfigNode.INT_9, false).getContent());
        assertTrue      (module.loadNode(config, MockConfigNode.INT_0, false).getStatusCode() == MultiWorldConfig.Status.OK);

        assertEquals    ("inherit", (String) module.loadNode(config, MockConfigNode.STR_0, false).getContent());
        assertTrue      (module.loadNode(config, MockConfigNode.STR_0, false).getStatusCode() == MultiWorldConfig.Status.INHERITS);
    }

    @Test
    public void testLoadNodeNotFound()
    {
        FileConfiguration config = getMatchingConfig();

        assertEquals    (null, module.loadNode(config, MockConfigNode.NOTFOUND_BOOL, false).getContent());
        assertTrue      (module.loadNode(config, MockConfigNode.NOTFOUND_BOOL, false).getStatusCode() == MultiWorldConfig.Status.NOT_FOUND);

        assertEquals    (null, module.loadNode(config, MockConfigNode.NOTFOUND_DOUBLE, false).getContent());
        assertTrue      (module.loadNode(config, MockConfigNode.NOTFOUND_DOUBLE, false).getStatusCode() == MultiWorldConfig.Status.NOT_FOUND);

        assertEquals    (null, module.loadNode(config, MockConfigNode.NOTFOUND_INT, false).getContent());
        assertTrue      (module.loadNode(config, MockConfigNode.NOTFOUND_INT, false).getStatusCode() == MultiWorldConfig.Status.NOT_FOUND);

        assertEquals    (null, module.loadNode(config, MockConfigNode.NOTFOUND_STR, false).getContent());
        assertTrue      (module.loadNode(config, MockConfigNode.NOTFOUND_STR, false).getStatusCode() == MultiWorldConfig.Status.NOT_FOUND);

        assertEquals    (null, module.loadNode(config, MockConfigNode.NOTFOUND_LIST, false).getContent());
        assertTrue      (module.loadNode(config, MockConfigNode.NOTFOUND_LIST, false).getStatusCode() == MultiWorldConfig.Status.NOT_FOUND);



        assertEquals    (true, module.loadNode(config, MockConfigNode.NOTFOUND_BOOL, true).getContent());
        assertTrue      (module.loadNode(config, MockConfigNode.NOTFOUND_BOOL, true).getStatusCode() == MultiWorldConfig.Status.ADJUSTED);

        assertEquals    (1, module.loadNode(config, MockConfigNode.NOTFOUND_INT, true).getContent());
        assertTrue      (module.loadNode(config, MockConfigNode.NOTFOUND_INT, true).getStatusCode() == MultiWorldConfig.Status.ADJUSTED);

    }

    @Test
    public void testLoadNodeInherited()
    {
        FileConfiguration config = getMatchingConfig();

        assertEquals    (null, module.loadNode(config, MockConfigNode.INHERITS_BOOL, false).getContent());
        assertTrue      (module.loadNode(config, MockConfigNode.INHERITS_BOOL, false).getStatusCode() == MultiWorldConfig.Status.INHERITS);

        assertEquals    (null, module.loadNode(config, MockConfigNode.INHERITS_INT, false).getContent());
        assertTrue      (module.loadNode(config, MockConfigNode.INHERITS_INT, false).getStatusCode() == MultiWorldConfig.Status.INHERITS);

        assertEquals    (null, module.loadNode(config, MockConfigNode.INHERITS_DOUBLE, false).getContent());
        assertTrue      (module.loadNode(config, MockConfigNode.INHERITS_DOUBLE, false).getStatusCode() == MultiWorldConfig.Status.INHERITS);

        assertEquals    (null, module.loadNode(config, MockConfigNode.INHERITS_STR, false).getContent());
        assertTrue      (module.loadNode(config, MockConfigNode.INHERITS_STR, false).getStatusCode() == MultiWorldConfig.Status.INHERITS);

        assertEquals    (null, module.loadNode(config, MockConfigNode.INHERITS_LIST, false).getContent());
        assertTrue      (module.loadNode(config, MockConfigNode.INHERITS_LIST, false).getStatusCode() == MultiWorldConfig.Status.INHERITS);
    }

    /**
     * Get an example config:
     * <pre>
     * BOOL_TRUE = false
     * BOOL_FALSE = false
     * INT_0 = 4
     * INT_9 = 9
     * STR_0 = inherit
     * NOTFOUND_X = all not found
     * INHERITS_X = all inherit
     * </pre>
     * @return a config
     */
    public FileConfiguration getMatchingConfig()
    {
        FileConfiguration configuration = new YamlConfiguration();

        //normal values
        configuration.set (MockConfigNode.BOOL_TRUE.getPath(), false);
        configuration.set (MockConfigNode.BOOL_FALSE.getPath(), false);
        configuration.set (MockConfigNode.INT_0.getPath(), 4);
        configuration.set (MockConfigNode.INT_9.getPath(), 9);
        configuration.set (MockConfigNode.STR_0.getPath(), "inherit");

        //inherited values
        configuration.set (MockConfigNode.INHERITS_BOOL.getPath(),  "inherit");
        configuration.set (MockConfigNode.INHERITS_INT.getPath(),   "inherit");
        configuration.set (MockConfigNode.INHERITS_DOUBLE.getPath(),"inherit");
        configuration.set (MockConfigNode.INHERITS_STR.getPath(),   "inherit");
        configuration.set (MockConfigNode.INHERITS_LIST.getPath(),  "inherit");

        return configuration;
    }

}
