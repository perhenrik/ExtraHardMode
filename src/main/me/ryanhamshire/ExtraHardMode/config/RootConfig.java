package me.ryanhamshire.ExtraHardMode.config;

import me.ryanhamshire.ExtraHardMode.ExtraHardMode;
import me.ryanhamshire.ExtraHardMode.service.ConfigNode;
import me.ryanhamshire.ExtraHardMode.service.MultiWorldConfig;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

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
    public void starting ()
    {
        load();
    }

    @Override
    public void closing (){}

    @Override
    public void load()
    {
        init();
        File[] configFiles = getConfigFiles(plugin.getDataFolder());
        List <Config> configs = loadFilesFromDisk(configFiles);
        load (configs);
    }

    /**
     * Loads all FileConfigurations into memory
     * Insures that there is always a main config.yml
     * loads all other Config's based on the Mode specified in the ConfigFile
     * @param configs FileName + respective FileConfiguration
     */
    public void load (List <Config> configs)
    {
        configs = loadMain(configs);

        Config defaults = null;
        for (Config config : configs)
        {   //loadMain insures that there is always a config.yml
            if (config.getFileName().equals("config.yml") && config.getStatus() == Status.PROCESSED && config.getMode() == Mode.MAIN)
            {
                defaults = config;
                configs.remove(config);
                break;
            }
        }

        for (Config config : configs)
        {
            {//Check if Mode is specified
                Response <String> response = (Response<String>) loadNode(config.getConfig(), RootNode.MODE, false);
                try
                {
                    if (response.getStatusCode() != Status.NOT_FOUND)
                        config.setMode(Mode.valueOf(response.getContent().toUpperCase()));
                } catch (IllegalArgumentException ignored){}
                finally
                {
                    if (config.getMode() == null || config.getMode() == Mode.NOT_SET)
                        config.setMode(Mode.INHERIT);
                }
            }

            switch (config.getMode())
            {
                case MAIN:
                {
                    throw new UnsupportedOperationException("There can only be one Config loaded with Mode.MAIN");
                }
                case DISABLE: case INHERIT:
                {
                    config = loadConfigToMem(config, defaults);
                    break;
                }
                case NOT_SET: default:
                {
                    config.setMode(config.getFileName().equals("config.yml") ? Mode.MAIN : Mode.INHERIT);
                    config = loadConfigToMem(config, defaults);
                    break;
                }
            }

            //Check if all values in the config are the same as in the master config and mark them as "inheritent" if they are the same, this makes
            //it easier to see what the admin has overriden. Or if a value in disable mode is already disabled.
            if (config.getMode() == Mode.INHERIT || config.getMode() == Mode.DISABLE)
            {
                for (RootNode node : RootNode.values())
                {
                    Object thisValue = loadNode(config.getConfig(), node, false).getContent();
                    Object thatValue = loadNode(defaults.getConfig(), node, false).getContent();

                    switch (config.getMode())
                    {
                        case INHERIT:
                        {   //floating point arithmetic is inaccurate...
                            if ((thisValue == thatValue) || ((thisValue instanceof Double && thatValue instanceof Double)
                                    && (BigDecimal.valueOf((Double)thisValue) .equals( BigDecimal.valueOf((Double)thatValue) ))))
                            {
                                config.getConfig().set(node.getPath(), config.getMode().name().toLowerCase());
                                config.setStatus (Status.ADJUSTED);
                            }
                            break;
                        }
                        case DISABLE:
                        {
                            if ((thisValue == node.getValueToDisable()) || ((thisValue instanceof Double && node.getValueToDisable() instanceof Double)
                                    && (BigDecimal.valueOf((Double)thisValue) .equals (BigDecimal.valueOf((Double)node.getValueToDisable())) )))
                            {
                                config.getConfig().set(node.getPath(), config.getMode().name().toLowerCase());
                                config.setStatus (Status.ADJUSTED);
                            }
                            break;
                        }
                        default:
                            throw new NotImplementedException();
                    }

                }
            }

            if (config.getStatus() == Status.ADJUSTED )
            {
                saveConfig(config);
            }
        }
    }

    /**
     * Loads the main config.yml
     * @param configs to process
     * @return all configs and the main config marked as processed
     */
    public List<Config> loadMain (List <Config> configs)
    {
        Config main = null;

        boolean contains = false;
        for (Config config : configs)
        {
            if (config.getFileName().equals("config.yml"))
            {
                contains = true;
                main = config;
                configs.remove(config);
                break;
            }
        }
        if (!contains)
        {
            main = new Config(new YamlConfiguration(), plugin.getDataFolder() + File.separator + "config.yml");
        }

        main.setMode(Mode.MAIN);
        main = loadConfigToMem(main, null);
        if (main.getStatus() == Status.ADJUSTED)
        {
            saveConfig(main);
        }

        main.setStatus(Status.PROCESSED);
        configs.add(main);
        return configs;
    }

    /**
     * Store the Options from the FileConfiguration into memory
     *
     * @param config Config, to load the values from, according to the Mode specified. The Mode determines how not found or same values are treated
     * @param main main file to use for reference values, can be null if we are loading with Mode.MAIN
     *
     * @return the passed in config, is marked as Status.ADJUSTED if the Config has been changed
     */
    private Config loadConfigToMem (Config config, Config main)
    {
        Response<List<String>> myWorlds  = loadNode(config.getConfig(), RootNode.WORLDS, false);
        List<String> worlds = myWorlds.getContent();

        for (RootNode node : RootNode.values())
        {
            Response response = loadNode(config.getConfig(), node, false);

            if (node.getVarType().equals(ConfigNode.VarType.INTEGER) && response.getStatusCode() == Status.OK)
            {
                response = validateInt(node, response.getContent());
            }

            switch (config.getMode()) //special actions regarding default values
            {
                case MAIN:
                {
                    if (response.getStatusCode() == Status.NOT_FOUND)
                    {
                        //get with defaults on
                        response = loadNode(config.getConfig(), node, true);
                        config.setStatus(Status.ADJUSTED);
                    }
                    break;
                }
                case DISABLE:
                {
                    if (response.getStatusCode() == Status.NOT_FOUND || response.getStatusCode() == Status.INHERITS) //Status = Disable: nothing needs to be done
                    {
                        if ((!response.getContent().equals(Mode.DISABLE.name()) && response.getStatusCode() == Status.INHERITS)
                                || response.getStatusCode() == Status.NOT_FOUND)
                        {   //mark it only adjusted if it has been changed, we rewrite the option nevertheless
                            config.setStatus(Status.ADJUSTED);
                        }

                        response.setContent (Mode.DISABLE.name().toLowerCase());
                        response.setStatus(Status.DISABLES);
                    }
                    break;
                }
                case INHERIT: //mark non found nodes as inherits
                {
                    if (response.getStatusCode() == Status.NOT_FOUND || response.getStatusCode() == Status.DISABLES)
                    {
                        if ((!response.getContent().equals(Mode.INHERIT.name()) && response.getStatusCode() == Status.DISABLES)
                                || response.getStatusCode() == Status.NOT_FOUND)
                        {   //mark it only adjusted if it has been changed, we rewrite the option nevertheless
                            config.setStatus(Status.ADJUSTED);
                        }
                        response.setContent(Mode.INHERIT.name().toLowerCase());
                        response.setStatus (Status.INHERITS);
                    }
                    break;
                }
                default:
                {
                    throw new UnsupportedOperationException("Mode: " + config.getMode().name() + " isn't handled");
                }
            }

            config.getConfig().set (node.getPath(), response.getContent()); //has to be before we get the actual values

            //the actual values that need to be loaded for the two modes to work
            if (response.getStatusCode() == Status.INHERITS || response.getStatusCode() == Status.DISABLES)
            {
                switch (config.getMode())
                {
                    case INHERIT: //load the value from the main config
                        response.setContent(loadNode (main.getConfig(), node, false).getContent());
                        break;
                    case DISABLE: //get the value to disable this option
                        response.setContent(node.getValueToDisable());
                        break;
                }
            }

            if (myWorlds.getStatusCode() != Status.NOT_FOUND)
            {
                for (String world : worlds)
                {
                    set(world, node, response.getContent());
                }
            }
        }
        return config;
    }

    /**
     * Reorders and saves the config.
     * Reorders the Config to the order specified by the enum in RootNode.
     * This assumes that the Config only has valid Entries.
     *
     * @param config Config to save
     */
    public void saveConfig (Config config)
    {
        //Reorder
        FileConfiguration reorderedConfig = new YamlConfiguration();
        for (RootNode node : RootNode.values())
        {
            reorderedConfig.set(node.getPath(), config.getConfig().get(node.getPath()));
        }
        config.setConfig(reorderedConfig);

        //save the reordered config
        try
        {
            config.getConfig().save(config.getConfigFile());
        }catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}