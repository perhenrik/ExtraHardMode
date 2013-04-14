package me.ryanhamshire.ExtraHardMode.service;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import me.ryanhamshire.ExtraHardMode.ExtraHardMode;
import me.ryanhamshire.ExtraHardMode.config.RootNode;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FilenameFilter;
import java.util.*;

/**
 * Modular configuration class that utilizes a ConfigNode enumeration as easy
 * access and storage of configuration option values.
 *
 * @author Mitsugaru (original author)
 * @author Diemex (modifies to allow multiworld)
 */
public abstract class MultiWorldConfig extends EHMModule
{
    private Table<String/*world*/, ConfigNode, Object> OPTIONS = HashBasedTable.create();
    /**
     * Constructor.
     *
     * @param plugin - Plugin instance.
     */
    public MultiWorldConfig (ExtraHardMode plugin)
    {
        super(plugin);
    }

    /**
     * Search the base directory for yml-files
     * @param baseDir
     * @return File[] containing all the *.yml Files
     */
    protected File[] getConfigFiles (File baseDir)
    {
        String [] filePaths = baseDir.list(new FilenameFilter()
        {
            @Override
            public boolean accept (File dir, String name)
            {
                return name.endsWith(".yml");
            }
        });
        ArrayList<File> files = new ArrayList<File>();
        for (String fileName : filePaths)
            files.add(new File(plugin.getDataFolder() + File.separator + fileName));
        return files.toArray(new File[]{});
    }

    /**
     * Load the given Files
     * @param files
     * @return a Hashmap<fileName, FileConfiguration>, containing all valid FileConfigurations and config.yml
     */
    protected LinkedHashMap<String, FileConfiguration> loadConfigFiles (File[] files)
    {
        LinkedHashMap<String, FileConfiguration> fileNameConfigMap = new LinkedHashMap<String, FileConfiguration>();
        for (File file : files)
        {
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            fileNameConfigMap.put(file.getName(), config);
        }

        //Check if the config is a valid config: It has to contain a world attribute and additionally the BaseNode of RootNode
        Iterator iter = fileNameConfigMap.entrySet().iterator();
        while (iter.hasNext())
        {
            Map.Entry<String, FileConfiguration> entry = (Map.Entry) iter.next();
            FileConfiguration config = entry.getValue();
            if ((config.getValues(true).containsKey(RootNode.baseNode())
                    && config.isSet(RootNode.WORLDS.getPath())
                    && config.getStringList(RootNode.WORLDS.getPath()) != null)
                    || entry.getKey().equals("config.yml")); //holds all default values for the other configs
            else iter.remove();
        }
        return fileNameConfigMap;
    }

    /**
     * <pre>
     * Get the value for the ConfigNode from the FileConfiguration passed in.
     * Can return null if not found.
     * Don't forget to set() if you want to save the returned value
     * </pre>
     *
     * @param config   -  FileConfiguration to load from
     * @param node     -  ConfigNode for the Path and DefaultValue
     * @param defaults -  will return the default value if not found in config
     * @return the Object matching the type of the ConfigNode, otherwise null if not found
     */
    public Object getObjectForNode (ConfigurationSection config, ConfigNode node, boolean defaults)
    {
        Object obj;
        switch (node.getVarType())
        {
            case LIST:
            {
                obj = config.getStringList(node.getPath());
                break;
            }
            case DOUBLE:
            {
                obj =  config.getDouble(node.getPath());
                break;
            }
            case STRING:
            {
                obj = config.getString(node.getPath());
                break;
            }
            case INTEGER:
            {
                obj = config.getInt(node.getPath());
                break;
            }
            case BOOLEAN:
            {
                obj = config.getBoolean(node.getPath());
                break;
            }
            default:
            {
                obj = config.get(node.getPath());
                throw new UnsupportedOperationException(node.getPath() + "No specific getter available for Type: " + " " + node.getVarType());
            }
        }
        if (!config.isSet(node.getPath()) && defaults)
        {
            obj = node.getDefaultValue();
        }
        return obj;
    }

    /**
     * Set a value for the given node and world
     *
     * @param world - World for the value
     * @param node  - ConfigNode for the given value
     * @param value - the Object to save
     */
    public void set (final String world, final ConfigNode node, Object value)
    {
        switch (node.getVarType())
        {
            case LIST:
            {
                if (value instanceof List)
                {
                    List<String> list = (List<String>) value;
                    OPTIONS.put(world, node, list);
                    break;
                }
            }
            case DOUBLE:
            {
                if (value instanceof Double)
                {
                    OPTIONS.put(world, node, value);
                    break;
                }
            }
            case STRING:
            {
                if (value instanceof String)
                {
                    OPTIONS.put(world, node, value);
                    break;
                }
            }
            case INTEGER:
            {
                if (value instanceof Integer)
                {
                    OPTIONS.put(world, node, value);
                    break;
                }
            }
            case BOOLEAN:
            {
                if (value instanceof Boolean)
                {
                    OPTIONS.put(world, node, value);
                    break;
                }
            }
            default:
            {
                OPTIONS.put(world, node, node.getDefaultValue());
                String inputClassName = value != null ? value.getClass().getName() : "null";
                throw new IllegalArgumentException(node.getPath() + " expects " + node.getVarType() + " but got " + inputClassName);
            }
        }
    }

    /**
     * Verify that the ConfigNode contains a valid value and is usable by the Plugin
     *
     * @param node  the ConfigNode to validate, validates according to the SubType of the ConfigNode
     * @param value the current value to validate
     *
     * @return the original/adjusted value
     */
    public int validateInt (final ConfigNode node, Object value)
    {
        int newValue = 0;
        if (node.getSubType() != null && node.getVarType().equals(ConfigNode.VarType.INTEGER))
        {
            int oldValue = (Integer) value;
            Validate val = new Validate(plugin);

            switch (node.getSubType())
            {
                case PERCENTAGE:
                {
                    newValue = val.percentage(node, oldValue);
                    break;
                }
                case Y_VALUE:
                {
                    //TODO
                    /*we are going to just check that it's > 0 so we don't need the worlds as argument. Doesn't
                      matter if the supplied y-value is greater than the world height, nothing will break. */
                    newValue = val.customBounds(node, 0, 0, oldValue);
                    break;
                }
                case HEALTH:
                {
                    newValue = val.customBounds(node, 1, 20, oldValue);
                    break;
                }
                case NATURAL_NUMBER:
                {
                    newValue = val.customBounds(node, 0, 0, oldValue);
                    break;
                }
                default:
                    throw new UnsupportedOperationException("SubType of " + node.getPath() + " doesn't have a validation method");
            }
        }
        return newValue;
    }

    /**
     * Return all world names were EHM is activated
     * @return world names
     */
    public String[] getEnabledWorlds ()
    {
        ArrayList <String> worlds = new ArrayList<String>();
        for (Map.Entry<String, Map<ConfigNode, Object>> entry : OPTIONS.rowMap().entrySet())
            worlds.add(entry.getKey());
        return worlds.toArray(new String[worlds.size()]);
    }


    /**
     * Get the integer value of the node.
     *
     * @param node - Node to use.
     *
     * @return Value of the node. Returns -1 if unknown.
     */
    public int getInt (final ConfigNode node, final String world)
    {
        int i = -1;
        switch (node.getVarType())
        {
            case INTEGER:
            {
                Object obj = OPTIONS.get(world, node);
                i = obj instanceof Integer ? (Integer)obj : (Integer)node.getDefaultValue();
                break;
            }
            default:
            {
                throw new IllegalArgumentException("Attempted to get " + node.toString() + " of type " + node.getVarType() + " as an integer.");
            }
        }
        return i;
    }

    /**
     * Get the double value of the node.
     *
     * @param node - Node to use.
     *
     * @return Value of the node. Returns 0 if unknown.
     */
    public double getDouble (final ConfigNode node, final String world)
    {
        double d = 0.0;
        switch (node.getVarType())
        {
            case DOUBLE:
            {
                Object obj = OPTIONS.get(world, node);
                d = obj instanceof Double ? (Double)obj : (Double) node.getDefaultValue();
                break;
            }
            default:
            {
                throw new IllegalArgumentException("Attempted to get " + node.toString() + " of type " + node.getVarType() + " as a double.");
            }
        }
        return d;
    }

    /**
     * Get the boolean value of the node.
     *
     * @param node - Node to use.
     *
     * @return Value of the node. Returns false if unknown.
     */
    public boolean getBoolean (final ConfigNode node, final String world)
    {
        boolean b = false;
        switch (node.getVarType())
        {
            case BOOLEAN:
            {
                Object obj = OPTIONS.get(world, node);
                b = obj instanceof Boolean ? (Boolean) obj : (Boolean) node.getDefaultValue();
                break;
            }
            default:
            {
                throw new IllegalArgumentException("Attempted to get " + node.toString() + " of type " + node.getVarType() + " as a boolean.");
            }
        }
        return b;
    }

    /**
     * Get the string value of the node.
     *
     * @param node - Node to use.
     *
     * @return Value of the node. Returns and empty string if unknown.
     */
    public String getString (final ConfigNode node, final String world)
    {
        String out = "";
        switch (node.getVarType())
        {
            case STRING:
            {
                Object obj = OPTIONS.get(world, node);
                out = obj instanceof String ? (String)obj : (String) node.getDefaultValue();
                break;
            }
            default:
            {
                throw new IllegalArgumentException("Attempted to get " + node.toString() + " of type " + node.getVarType() + " as a string.");
            }
        }
        return out;
    }

    /**
     * Get the list value of the node.
     *
     * @param node - Node to use.
     *
     * @return Value of the node. Returns an empty list if unknown.
     */
    public List<String> getStringList (final ConfigNode node, final String world)
    {
        List<String> list = new ArrayList<String>();
        switch (node.getVarType())
        {
            case LIST:
            {
                Object obj = OPTIONS.get(world, node);
                list = obj instanceof List ? (List<String>)obj : (List<String>)node.getDefaultValue();
                break;
            }
            default:
            {
                throw new IllegalArgumentException("Attempted to get " + node.toString() + " of type " + node.getVarType() + " as a List<String>.");
            }
        }
        return list;
    }

    public abstract void load ();



    /**
     * Determines how to load the specific ConfigFile
     */
    protected enum Mode
    {
        /**
         * This is the main configFile and gets overriden by other Configs
         */
        MAIN,
        /**
         * Override the settings of the main config in specific worlds
         */
        OVERRIDE,
        /**
         * All options which aren't found default to disabled, this allows to only activate a few things and not having to disable everything else
         */
        DEFAULT_DISABLED
    }
}