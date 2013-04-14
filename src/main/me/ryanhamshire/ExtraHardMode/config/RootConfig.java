package me.ryanhamshire.ExtraHardMode.config;

import me.ryanhamshire.ExtraHardMode.ExtraHardMode;
import me.ryanhamshire.ExtraHardMode.service.ConfigNode;
import me.ryanhamshire.ExtraHardMode.service.MultiWorldConfig;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class RootConfig extends MultiWorldConfig
{
    public RootConfig (ExtraHardMode plugin)
    {
        super(plugin);
    }

    @Override
    public void starting (){ load(); }

    @Override
    public void closing (){}

    @Override
    public void load()
    {
        File[] configFiles= getConfigFiles(plugin.getDataFolder());
        LinkedHashMap<String, FileConfiguration> configurations= loadConfigFiles(configFiles);
        load (configurations);
    }

    /**
     * Loads all FileConfigurations into memory
     * @param configs FileName + respective FileConfiguration
     */
    public void load (Map <String, FileConfiguration> configs)
    {
        {   //MAIN Config loaded first, so we can override it later
            if (!configs.containsKey("config.yml"))
            {
                configs.put("config.yml", new YamlConfiguration());
            }
            FileConfiguration configYml = configs.get("config.yml");
            configYml = store(configYml, Mode.MAIN);
            if (/*returned config has been adjusted*/ configYml!=null)
                try
                {
                    configYml.save(new File(plugin.getDataFolder() + File.separator + "config.yml"));
                }catch (IOException e)
                {
                    e.printStackTrace();
                }
            configs.remove("config.yml");
        }

        for (Map.Entry<String, FileConfiguration> entry : configs.entrySet())
        {
            File outputFile = new File (plugin.getDataFolder() + File.separator + entry.getKey());
            FileConfiguration input = entry.getValue();

            String configType = (String) getObjectForNode(input, RootNode.MODE, false);
            Mode mode = Mode.OVERRIDE;
            try {
                mode = Mode.valueOf(configType.toUpperCase());
            } catch (IllegalArgumentException ignored){}

            FileConfiguration output;

            switch (mode)
            {
                case MAIN:
                {
                    output = store(input, Mode.MAIN);
                    break;
                }
                case DEFAULT_DISABLED:
                {
                    output = store(input, Mode.DEFAULT_DISABLED);
                    break;
                }
                case OVERRIDE:
                {
                    output = store(input, Mode.OVERRIDE);
                    break;
                }
                default:
                {
                    if (entry.getKey().equals("config.yml"))
                        output = store(input, Mode.MAIN);
                    else
                        output = store(input, Mode.OVERRIDE);
                    break;
                }
            }

            if (/*config has been adjusted*/ output != null)
            {
                try
                {
                    output.save(outputFile);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Store the Options from the FileConfiguration into memory
     * @param config
     * @return FileConfiguration if values have been adjusted - null: if not adjusted
     */
    private FileConfiguration store (FileConfiguration config, Mode mode)
    {
        boolean changed = false;
        List<String> worlds = (List<String>) getObjectForNode(config, RootNode.WORLDS, false);

        YamlConfiguration output = new YamlConfiguration();
        for (RootNode node : RootNode.values())
        {
            Object obj = getObjectForNode(config, node, false);
            if (node.getVarType().equals(ConfigNode.VarType.INTEGER))
            {
                Object valObj = validateInt(node, obj);
                if (valObj != obj ) changed = true;
                obj = valObj;
            }

            switch (mode) //special actions regarding default values
            {
                case MAIN:
                {
                    if (/*no value in config*/obj ==  null)
                    {
                        //get with defaults on
                        obj = getObjectForNode(config, node, true);
                        changed = true;
                    }
                    break;
                }
                case DEFAULT_DISABLED:
                {
                    if (/*no value in config*/obj == null)
                    {
                        obj = node.getValueToDisable();
                     }
                }
                case OVERRIDE: //a value that isn't found here will just be ignored as long as it's not the mode <- ?
                    if (node.equals(RootNode.MODE))
                    {

                    }
                    break;
                default:
                    throw new UnsupportedOperationException("Mode: " + mode.name() + " isn't handled");
            }
            if (worlds == null) worlds = Collections.emptyList();
            for (String world : worlds)
            {
                set(world, node, obj);
            }
            output.set(node.getPath(), obj);
        }
        if(changed)
            return output;
        else
            return null;
    }
}