/*
 * This file is part of
 * ExtraHardMode Server Plugin for Minecraft
 *
 * Copyright (C) 2012 Ryan Hamshire
 * Copyright (C) 2013 Diemex
 *
 * ExtraHardMode is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ExtraHardMode is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero Public License
 * along with ExtraHardMode.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.extrahardmode.service.config;


import com.extrahardmode.ExtraHardMode;
import com.extrahardmode.config.RootNode;
import com.extrahardmode.service.EHMModule;
import com.extrahardmode.service.Response;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FilenameFilter;
import java.util.*;

/**
 * Modular configuration class that utilizes a ConfigNode enumeration as easy access and storage of configuration option values.
 *
 * @author Mitsugaru (original author)
 * @author Diemex (modifies to allow multiworld)
 */
public abstract class MultiWorldConfig extends EHMModule
{

    /**
     * For mods like MystCraft which allow Players to create their own dimensions, so the admin doesnt have to add worlds manually
     */
    protected boolean enabledForAll = false;

    /**
     * String that will enabled the plugin for all possible worlds
     */
    protected final String ALL_WORLDS = "@all";

    private Table<String/*world*/, ConfigNode, Object> OPTIONS;


    /**
     * Constructor.
     *
     * @param plugin - Plugin instance.
     */
    public MultiWorldConfig(ExtraHardMode plugin)
    {
        super(plugin);
        init();
    }


    /**
     * Inits Objects and deletes old ones at the same time
     */
    protected void init()
    {
        OPTIONS = HashBasedTable.create();
    }


    /**
     * Search the base directory for yml-files
     *
     * @return File[] containing all the *.yml Files in a lexical order
     */
    protected File[] getConfigFiles(File baseDir)
    {
        String[] filePaths = baseDir.list(new FilenameFilter()
        {
            @Override
            public boolean accept(File dir, String name)
            {
                return name.endsWith(".yml");
            }
        });
        if (filePaths == null) filePaths = new String[]{};
        Arrays.sort(filePaths); //lexicality
        ArrayList<File> files = new ArrayList<File>();
        for (String fileName : filePaths)
            files.add(new File(plugin.getDataFolder() + File.separator + fileName));
        return files.toArray(new File[]{});
    }


    /**
     * Load the given Files in a List as Config Objects, which hold the reference to the File and the loaded FileConfiguration Ignores files that don't have the RootNode in them.
     *
     * @return a HashMap containing all valid FileConfigurations and config.yml
     */
    protected List<Config> loadFilesFromDisk(File[] files)
    {
        List<Config> configs = new ArrayList<Config>();
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

            if ((fileConfig.getValues(true).containsKey(RootNode.baseNode()) && fileConfig.getStringList(RootNode.WORLDS.getPath()) != null) || config.getFileName().equals("config.yml"))
                ;
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
     *
     * @return the Object matching the type of the ConfigNode, otherwise null if not found
     */
    public Response loadNode(ConfigurationSection config, ConfigNode node, boolean defaults)
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
                    obj = config.getDouble(node.getPath());
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
                    if (((String) obj).toUpperCase().equals(Mode.DISABLE.name()))
                    {
                        status = Status.DISABLES;
                    } else if (((String) obj).toUpperCase().equals(Mode.INHERIT.name()))
                    {
                        status = Status.INHERITS;
                    }
                }
            } else
            {   //hasn't been loaded
                if (config.getString(node.getPath()).toUpperCase().equals(Mode.INHERIT.name()))
                {   //inherits in config
                    status = Status.INHERITS;
                    obj = Mode.INHERIT.name().toLowerCase();
                } else if (config.getString(node.getPath()).toUpperCase().equals(Mode.DISABLE.name()))
                {   //disabled in config
                    status = Status.DISABLES;
                    obj = Mode.DISABLE.name().toLowerCase();
                } else
                {   //should not be reached, but... we don't want to return null
                    status = Status.NOT_FOUND;
                    obj = node.getDefaultValue();
                }
            }
        } else
        {   //default value gets returned for both, but the status represents the actual Status
            if (defaults)
            {
                obj = node.getDefaultValue();
                status = Status.ADJUSTED;
            } else
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
    public void set(final String world, final ConfigNode node, Object value)
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
                if (value instanceof Integer || value instanceof Double)
                {
                    //fix error when double is provided which can be casted
                    if (value instanceof  Double)
                        value = ((Double) value).intValue();
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
     * Return all world names were EHM is activated
     *
     * @return world names
     */
    public String[] getEnabledWorlds()
    {
        ArrayList<String> worlds = new ArrayList<String>();
        for (Map.Entry<String, Map<ConfigNode, Object>> entry : OPTIONS.rowMap().entrySet())
            worlds.add(entry.getKey());
        return worlds.toArray(new String[worlds.size()]);
    }


    /**
     * Does this config apply to all loaded worlds
     * @return if applies to all worlds
     */
    public boolean isEnabledForAll()
    {
        return enabledForAll;
    }

    public String getAllWorldString()
    {
        return ALL_WORLDS;
    }


    /**
     * Get the integer value of the node.
     *
     * @param node - Node to use.
     *
     * @return Value of the node. Returns -1 if unknown.
     */
    public int getInt(final ConfigNode node, final String world)
    {
        int i = -1;
        switch (node.getVarType())
        {
            case INTEGER:
            {
                Object obj = null;
                if (OPTIONS.contains(world, node))
                    obj = OPTIONS.get(world, node);
                else if (enabledForAll)
                    obj = OPTIONS.get(ALL_WORLDS, node);
                i = obj instanceof Integer ? (Integer) obj : (Integer) node.getValueToDisable();
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
    public double getDouble(final ConfigNode node, final String world)
    {
        double d;
        switch (node.getVarType())
        {
            case DOUBLE:
            {
                Object obj = null;
                if (OPTIONS.contains(world, node))
                    obj = OPTIONS.get(world, node);
                else if (enabledForAll)
                    obj = OPTIONS.get(ALL_WORLDS, node);
                d = obj instanceof Number ? ((Number) obj).doubleValue() : (Double) node.getValueToDisable();
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
    public boolean getBoolean(final ConfigNode node, final String world)
    {
        boolean bool = false;
        switch (node.getVarType())
        {
            case BOOLEAN:
            {
                Object obj = null;
                if (OPTIONS.contains(world, node))
                    obj = OPTIONS.get(world, node);
                else if (enabledForAll)
                    obj = OPTIONS.get(ALL_WORLDS, node);
                bool = obj instanceof Boolean ? (Boolean) obj : (Boolean) node.getValueToDisable();
                break;
            }
            default:
            {
                throw new IllegalArgumentException("Attempted to get " + node.toString() + " of type " + node.getVarType() + " as a boolean.");
            }
        }
        return bool;
    }


    /**
     * Get the string value of the node.
     *
     * @param node - Node to use.
     *
     * @return Value of the node. Returns and empty string if unknown.
     */
    public String getString(final ConfigNode node, final String world)
    {
        String out = "";
        switch (node.getVarType())
        {
            case STRING:
            {
                Object obj = null;
                if (OPTIONS.contains(world, node))
                    obj = OPTIONS.get(world, node);
                else if (enabledForAll)
                    obj = OPTIONS.get(ALL_WORLDS, node);
                out = obj instanceof String ? (String) obj : (String) node.getValueToDisable();
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
    public List<String> getStringList(final ConfigNode node, final String world)
    {
        List<String> list = new ArrayList<String>();
        switch (node.getVarType())
        {
            case LIST:
            {
                Object obj = null;
                if (OPTIONS.contains(world, node))
                    obj = OPTIONS.get(world, node);
                else if (enabledForAll)
                    obj = OPTIONS.get(ALL_WORLDS, node);
                list = obj instanceof List ? (List<String>) obj : (List<String>) node.getValueToDisable();
                break;
            }
            default:
            {
                throw new IllegalArgumentException("Attempted to get " + node.toString() + " of type " + node.getVarType() + " as a List<String>.");
            }
        }
        return list;
    }


    public abstract void load();


    /**
     * Clear all the loaded config options. Primarily for unit testing purposes.
     */
    public void clearCache()
    {
        OPTIONS.clear();
    }


    /**
     * Verify that the ConfigNode contains a valid value and is usable by the Plugin
     *
     * @param node  the ConfigNode to validate, validates according to the SubType of the ConfigNode
     * @param value the current value to validate
     *
     * @return a Response containing if the Object has been adjusted and the value (adjusted/original)
     */
    public Response<Integer> validateInt(final ConfigNode node, Object value)
    {
        Response response = new Response<Object>(Status.NOT_FOUND, value);

        if (node.getVarType() == (ConfigNode.VarType.INTEGER))
        {
            if (value instanceof Integer)
            {
                int valMe = (Integer) value;

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
            } else
            {
                response = new Response<Object>(Status.ADJUSTED, (Integer) node.getDefaultValue());
            }
        } else
        {
            throw new IllegalArgumentException("Expected a ConfigNode with Type Integer but got " + node.getVarType() + " for " + node.getPath());
        }
        return response;
    }


    /**
     * Validate Y coordinate limit for the given configuration option against the list of enabled worlds.
     *
     * @param node   - Root node to validate.
     * @param worlds - List of worlds to check against.
     * @param value  - Integer to validate
     *
     * @return a Response containing either the original value or adjusted if out of bounds and the Status
     */
    Response validateYCoordinate(ConfigNode node, List<String> worlds, Integer value)
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
    Response validatePercentage(ConfigNode node, Integer value)
    {
        Status status = Status.OK;
        if (value < 0)
        {
            if (plugin.getLogger() != null) //testing
                plugin.getLogger().warning(ChatColor.YELLOW + " Percentage for " + node.getPath() + " cannot be less than 0.");
            value = 0;
            status = Status.ADJUSTED;
        } else if (value > 100)
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
    Response validateCustomBounds(ConfigNode node, int minVal, int maxVal, Integer value)
    {
        Status status = Status.OK;
        if (value < minVal)
        {
            if (plugin.getLogger() != null) //testing
                plugin.getLogger().warning(plugin.getTag() + " Value for " + node.getPath() + " cannot be smaller than " + minVal);
            value = minVal;
            status = Status.ADJUSTED;
        } else if (minVal < maxVal && value > maxVal)
        {
            if (plugin.getLogger() != null) //testing
                plugin.getLogger().warning(plugin.getTag() + " Value for " + node.getPath() + " cannot be greater than " + maxVal);
            value = maxVal;
            status = Status.ADJUSTED;
        }
        return new Response(status, value);
    }
}