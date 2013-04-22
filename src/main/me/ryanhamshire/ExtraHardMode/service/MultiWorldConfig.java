package me.ryanhamshire.ExtraHardMode.service;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import me.ryanhamshire.ExtraHardMode.ExtraHardMode;
import me.ryanhamshire.ExtraHardMode.config.RootNode;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
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
    private Table<String/*world*/, ConfigNode, Object> OPTIONS;

    /**
     * Constructor.
     *
     * @param plugin - Plugin instance.
     */
    public MultiWorldConfig (ExtraHardMode plugin)
    {
        super(plugin);
        init();
    }

    /**
     * Inits Objects and deletes old ones at the same time
     */
    public void init ()
    {
        OPTIONS = HashBasedTable.create();
    }

    /**
     * Search the base directory for yml-files
     *
     * @param baseDir
     * @return File[] containing all the *.yml Files in a lexical order
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
        Arrays.sort(filePaths); //lexicality
        ArrayList<File> files = new ArrayList<File>();
        for (String fileName : filePaths)
            files.add(new File(plugin.getDataFolder() + File.separator + fileName));
        return files.toArray(new File[]{});
    }

    /**
     * Load the given Files in a List as Config Objects, which hold the reference to the File and the loaded FileConfiguration
     * Ignores files that don't have the RootNode in them.
     * @param files
     * @return a Hashmap<fileName, FileConfiguration>, containing all valid FileConfigurations and config.yml
     */
    protected List<Config> loadFilesFromDisk (File[] files)
    {
        List <Config> configs = new ArrayList<Config>();
        for (File file : files)
        {
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            configs.add(new Config(config, file));
        }

        //Check if the config is a valid config: It has to contain a world attribute and additionally the BaseNode of RootNode
        Iterator<Config> iter = configs.iterator();
        while (iter.hasNext())
        {
            Config config = iter.next();
            FileConfiguration fileConfig = config.getConfig();

            if ((fileConfig.getValues(true).containsKey(RootNode.baseNode()) && fileConfig.getStringList(RootNode.WORLDS.getPath()) != null) || config.getFileName().equals("config.yml"));
                    //holds all default values for the other configs
            else iter.remove();
        }
        return configs;
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
    public Response loadNode (ConfigurationSection config, ConfigNode node, boolean defaults)
    {
        Validate.notNull(config, "config can't be null");
        Validate.notNull(config, "node can't be null");

        Status status;
        Object obj = null;

        switch (node.getVarType())
        {
            case LIST:
            {
                if (config.get(node.getPath()) instanceof List)
                    obj = config.getStringList(node.getPath());
                break;
            }
            case DOUBLE:
            {
                if (config.get(node.getPath()) instanceof Double)
                    obj =  config.getDouble(node.getPath());
                break;
            }
            case STRING:
            {
                if (config.get(node.getPath()) instanceof String)
                {
                    obj = config.getString(node.getPath());
                }
                break;
            }
            case INTEGER:
            {
                if (config.get(node.getPath()) instanceof Integer)
                    obj = config.getInt(node.getPath());
                break;
            }
            case BOOLEAN:
            {
                if (config.get(node.getPath()) instanceof Boolean)
                    obj = config.getBoolean(node.getPath());
                break;
            }
            default:
            {
                obj = config.get(node.getPath());
                throw new UnsupportedOperationException(node.getPath() + "No specific getter available for Type: " + " " + node.getVarType());
            }
        }

        if (config.isSet(node.getPath()))
        {
            if (obj != null)
            {   //is in config and has been loaded sucesfully
                status = Status.OK;
                if (obj instanceof String) //for Strings makes sure their modes get recognized
                {
                    if (((String) obj).toUpperCase() .equals (Mode.DISABLE.name()))
                    {
                        status = Status.DISABLES;
                    }
                    else if (((String) obj).toUpperCase() .equals (Mode.INHERIT.name()))
                    {
                        status = Status.INHERITS;
                    }
                }
            }
            else
            {   //hasn't been loaded
                if (config.getString(node.getPath()).toUpperCase().equals(Mode.INHERIT.name()))
                {   //inherits in config
                    status = Status.INHERITS;
                    obj = Mode.INHERIT.name().toLowerCase();
                }
                else if (config.getString(node.getPath()).toUpperCase().equals(Mode.DISABLE.name()))
                {   //disabled in config
                    status = Status.DISABLES;
                    obj = Mode.DISABLE.name().toLowerCase();
                }
                else
                {   //should not be reached, but... we don't want to return null
                    status = Status.NOT_FOUND;
                    obj = node.getDefaultValue();
                }
            }
        }
        else
        {   //default value gets returned for both, but the status represents the actual Status
            if (defaults)
            {
                obj = node.getDefaultValue();
                status = Status.ADJUSTED;
            }
            else
            {   //
                obj = node.getDefaultValue();
                status = Status.NOT_FOUND;
            }
        }

        return new Response(status, obj);
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
     * @return a Response containing if the Object has been adjusted and the value (adjusted/original)
     */
    public Response<Integer> validateInt (final ConfigNode node, Object value)
    {
        Response response = new Response (Status.NOT_FOUND, value);

        if (node.getVarType() == (ConfigNode.VarType.INTEGER))
        {
            if (value instanceof Integer)
            {
                int valMe = (Integer)value;

                if (node.getSubType() != null)
                {
                    switch (node.getSubType())
                    {
                        case PERCENTAGE:
                        {
                            response = validatePercentage(node, valMe);
                            break;
                        }
                        case Y_VALUE:
                        {
                            response = validateYCoordinate(node, Arrays.asList(getEnabledWorlds()), valMe);
                            break;
                        }
                        case HEALTH:
                        {
                            response = validateCustomBounds(node, 1, 20, valMe);
                            break;
                        }
                        case NATURAL_NUMBER:
                        {
                            response = validateCustomBounds(node, 0, 0, valMe);
                            break;
                        }
                        default:
                            throw new UnsupportedOperationException("SubType of " + node.getPath() + " doesn't have a validation method");
                    }
                }
            }
            else
            {
                response = new Response(Status.ADJUSTED, (Integer)node.getDefaultValue());
            }
        }
        else
        {
            throw new IllegalArgumentException("Expected a ConfigNode with Type Integer but got " + node.getVarType() + " for " + node.getPath());
        }
       return response;
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
     * Validate Y coordinate limit for the given configuration option against the
     * list of enabled worlds.
     *
     * @param node   - Root node to validate.
     * @param worlds - List of worlds to check against.
     * @param value  - Integer to validate
     *
     * @return a Response containing either the original value or adjusted if out of bounds and the Status
     */
    public Response validateYCoordinate (ConfigNode node, List<String> worlds, Integer value)
    {
        //Either 255 or the height of the first world loaded is the default max height
        Status status = Status.OK;
        int maxHeight = plugin.getServer().getWorlds().size() > 0 ? plugin.getServer().getWorlds().get(0).getMaxHeight() : 255;
        if (value < 0)
        {
            if (plugin.getLogger() != null) //testing
                plugin.getLogger().warning(ChatColor.YELLOW + " Y coordinate for " + node.getPath() + " cannot be less than 0.");
            value = 0;
        }
        for (String worldName : worlds)
        {
            World world = plugin.getServer().getWorld(worldName);
            //if world is not loaded (yet)
            if (world != null) maxHeight = world.getMaxHeight();
            if (value > maxHeight)
            {
                if (plugin.getLogger() != null) //testing
                    plugin.getLogger().warning(ChatColor.YELLOW + " Y coordinate for " + node.getPath() + " is greater than the max height for world " + worldName);
                value = maxHeight;
                status = Status.ADJUSTED;
            }
        }
        return new Response(status, value);
    }

    /**
     * Validate percentage (0-100) value for given configuration option.
     *
     * @param node  - Root node to validate.
     * @param value - Integer to validate
     *
     * @return a Response containing either the original value or adjusted if out of bounds and the Status
     */
    public Response validatePercentage (ConfigNode node, Integer value)
    {
        Status status = Status.OK;
        if (value < 0)
        {
            if (plugin.getLogger() != null) //testing
                plugin.getLogger().warning(ChatColor.YELLOW + " Percentage for " + node.getPath() + " cannot be less than 0.");
            value = 0;
            status = Status.ADJUSTED;
        }
        else if (value > 100)
        {
            if (plugin.getLogger() != null) //testing
                plugin.getLogger().warning(ChatColor.YELLOW + " Percentage for " + node.getPath() + " cannot be greater than 100.");
            value = 100;
            status = Status.ADJUSTED;
        }
        return new Response(status, value);
    }

    /**
     * Validates a configOption with custom bounds
     *
     * @param node   the configNode
     * @param minVal the minimum value the config is allowed to have
     * @param maxVal the maximum value for the config, if == minVal then it doesn't get checked
     * @param value  - Integer to validate
     *
     * @return a Response containing either the original value or adjusted if out of bounds and the Status
     */
    public Response validateCustomBounds (ConfigNode node, int minVal, int maxVal, Integer value)
    {
        Status status = Status.OK;
        if (value < minVal)
        {
            if (plugin.getLogger() != null) //testing
                plugin.getLogger().warning(plugin.getTag() + " Value for " + node.getPath() + " cannot be smaller than " + minVal);
            value = minVal;
            status = Status.ADJUSTED;
        }
        else if (minVal < maxVal && value > maxVal)
        {
            if (plugin.getLogger() != null) //testing
                plugin.getLogger().warning(plugin.getTag() + " Value for " + node.getPath() + " cannot be greater than " + maxVal);
            value = maxVal;
            status = Status.ADJUSTED;
        }
        return new Response(status, value);
    }

    /**
     * A Wrapper that contains a FileConfiguration and a reference to the file as such
     */
    protected class Config
    {
        /**
         * Loaded FileConfiguration
         */
        private FileConfiguration config;
        /**
         * Location we loaded the File from
         */
        private File configFile;
        /**
         * Mode with which this config will get loaded
         */
        private Mode mode = Mode.NOT_SET;
        /**
         * Some status information about this Config
         */
        private Status status = Status.OK;

        /**
         * Constructor
         * @param config that's loaded                         config
         * @param fullFilePath fileName including the directory!
         */
        public Config (FileConfiguration config, String fullFilePath)
        {
            this.config = config;
            configFile = new File(fullFilePath);
            if (!configFile.exists())
            {
                try
                {
                    configFile.createNewFile();
                } catch (IOException e)
                {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                Validate.isTrue(configFile.exists() && configFile.canWrite(), "FilePath " + fullFilePath + " doesn't exist or is not writable");
            }
        }

        /**
         * Constructor
         * @param config that's loaded
         * @param file File-Object to save to
         */
        public Config (FileConfiguration config, File file)
        {
            this.config = config;
            configFile = file;
        }

        /**
         * Get loaded FileConfiguration
         * @return
         */
        public FileConfiguration getConfig ()
        {
            return config;
        }

        /**
         * Set FileConfiguration
         * @param config
         */
        public void setConfig (FileConfiguration config)
        {
            this.config = config;
        }

        /**
         * Returns the File to save the FileConfiguration to
         * @return
         */
        public File getConfigFile ()
        {
            return configFile;
        }

        /**
         * Get the fileName of this Config
         */
        public String getFileName ()
        {
            return configFile.getName();
        }

        /**
         * Set where to save this Config
         * @param configFile
         */
        public void setConfigFile (File configFile)
        {
            this.configFile = configFile;
        }

        /**
         * Get the Mode this config should be loaded
         * @return mode or Mode.NOT_SET if not set yet
         */
        public Mode getMode ()
        {
            return mode;
        }

        /**
         * Set the Mode with which this Config should be loaded
         * @param mode to set
         */
        public void setMode (Mode mode)
        {
            this.mode = mode;
        }

        /**
         * Get the Status of this config
         * @return Status, initialized with Status.OK
         */
        public Status getStatus ()
        {
            return status;
        }

        /**
         * Set the Status of this Config
         * @param status of the Config
         */
        public void setStatus (Status status)
        {
            this.status = status;
        }
    }

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
        INHERIT,
        /**
         * All options which aren't found default to disabled, this allows to only activate a few things and not having to disable everything else
         */
        DISABLE,
        /**
         * The mode hasn't been set yet
         */
        NOT_SET
    }

    /**
     * Easier to read than meaningless null return values
     */
    protected enum Status
    {
        /**
         * The config has been adjusted and needs to be saved
         */
        ADJUSTED,
        /**
         * Config hasn't been altered and doesn't need to be saved
         */
        OK,
        /**
         * Requested value not found
         */
        NOT_FOUND,
        /**
         * For use as a default for another Object (e.g. Config)
         */
        DEFAULTS,
        /**
         * This value inherits from something
         */
        INHERITS,
        /**
         * The values to disable this option should be loaded
         */
        DISABLES,
        /**
         * This Object has been fully processed and will be ignored
         */
        PROCESSED
    }

    /**
     * Attach some information to a returned value
     * only public for testing purposes
     */
    protected class Response <T>
    {
        /**
         * Statuscode of this Response
         */
        private Status status;
        /**
         * Object to return
         */
        private T response;

        /**
         * A parameterized Response with StatusCode
         * @param status
         * @param response
         */
        public Response(Status status, T response)
        {
            this.status = status;
            this.response = response;
        }

        @Override
        public boolean equals(Object other)
        {
            if (other instanceof Response)
            {
                Response otherR = (Response) other;
                return otherR.getStatusCode() == this.getStatusCode() && otherR.getContent() == this.getContent();
            }
            else
                return false;
        }

        /**
         * Get the Status of this Response
         * @return
         */
        public Status getStatusCode()
        {
            return status;
        }

        /**
         * Get the actual content of the response
         * @return
         */
        public T getContent ()
        {
            return response;
        }

        /**
         * Set the status of the Response
         * @param status code to set
         */
        public void setStatus(Status status)
        {
            this.status = status;
        }

        /**
         * Set the returned content of the Response
         * @param response to set
         */
        public void setContent (T response)
        {
            this.response = response;
        }
    }
}