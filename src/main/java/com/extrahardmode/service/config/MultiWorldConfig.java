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
import com.extrahardmode.service.EHMModule;
import com.extrahardmode.service.Response;
import com.extrahardmode.service.config.customtypes.BlockRelationsList;
import com.extrahardmode.service.config.customtypes.BlockType;
import com.extrahardmode.service.config.customtypes.BlockTypeList;
import com.extrahardmode.service.config.customtypes.PotionEffectHolder;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
     * String that will enable the plugin in all worlds
     */
    public static final String ALL_WORLDS = "@all";

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
     * <pre>
     * Get the value for the ConfigNode from the FileConfiguration passed in.
     * Can return null if not found.
     * Don't forget to set() if you want to save the returned value
     * </pre>
     *
     * @param config   -  FileConfiguration to load from
     * @param node     -  ConfigNode for the Path and DefaultValue
    //* @param defaults -  will return the default value if not found in config
     *
     * @return the Object matching the type of the ConfigNode, otherwise null if not found
     */
    public Response loadNode(ConfigurationSection config, ConfigNode node/*, boolean defaults*/)
    {
        Validate.notNull(config, "config can't be null");
        Validate.notNull(config, "node can't be null");

        Status status = Status.NOT_FOUND;
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
            case POTION_EFFECT:
            {
                ConfigurationSection section = config.getConfigurationSection(node.getPath());
                obj = PotionEffectHolder.loadFromConfig(section);
                break;
            }
            case BLOCKTYPE:
            {
                if (config.getString(node.getPath()) != null)
                    obj = BlockType.loadFromConfig(config.getString(node.getPath()));
                break;
            }
            case BLOCKTYPE_LIST:
            {
                if (config.get(node.getPath()) instanceof List)
                {
                    List<String> list = config.getStringList(node.getPath());
                    BlockTypeList blocks = new BlockTypeList();
                    for (String str : list)
                    {
                        BlockType block = BlockType.loadFromConfig(str);
                        if (block != null)
                            blocks.add(block);
                    }
                    obj = blocks;
                }
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
        }
//        else
//        {   //default value gets returned for both, but the status represents the actual Status
//            if (defaults)
//            {
//                obj = node.getDefaultValue();
//                status = Status.ADJUSTED;
//            } else
//            {   //
//                obj = node.getDefaultValue();
//                status = Status.NOT_FOUND;
//            }
//        }

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
                    List list = (List) value;
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
                    if (value instanceof Double)
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
            case POTION_EFFECT:
            {
                if (value instanceof PotionEffectHolder)
                {
                    OPTIONS.put(world, node, value);
                    break;
                }
            }
            case BLOCKTYPE:
            {
                if (value instanceof BlockType)
                {
                    OPTIONS.put(world, node, value);
                    break;
                }
            }
            case BLOCKTYPE_LIST:
            {
                if (value instanceof BlockTypeList)
                {
                    OPTIONS.put(world, node, value);
                    break;
                }
            }
            case BLOCK_RELATION_LIST:
            {
                if (value instanceof BlockRelationsList)
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


//     __            _     _          _   _               _ _   __    __           _     _
//    /__\ __   __ _| |__ | | ___  __| | (_)_ __     __ _| | | / / /\ \ \___  _ __| | __| |___
//   /_\| '_ \ / _` | '_ \| |/ _ \/ _` | | | '_ \   / _` | | | \ \/  \/ / _ \| '__| |/ _` / __|
//  //__| | | | (_| | |_) | |  __/ (_| | | | | | | | (_| | | |  \  /\  / (_) | |  | | (_| \__ \
//  \__/|_| |_|\__,_|_.__/|_|\___|\__,_| |_|_| |_|  \__,_|_|_|   \/  \/ \___/|_|  |_|\__,_|___/
//


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


    public boolean isEnabledIn(String world)
    {
        return OPTIONS.containsRow(world);
    }


    /**
     * Does this config apply to all loaded worlds
     *
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


//     ___     _   _
//    / _ \___| |_| |_ ___ _ __ ___
//   / /_\/ _ \ __| __/ _ \ '__/ __|
//  / /_\\  __/ |_| ||  __/ |  \__ \
//  \____/\___|\__|\__\___|_|  |___/
//


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
    public List getStringList(final ConfigNode node, final String world)
    {
        List list = new ArrayList<String>();
        switch (node.getVarType())
        {
            case LIST:
            {
                Object obj = null;
                if (OPTIONS.contains(world, node))
                    obj = OPTIONS.get(world, node);
                else if (enabledForAll)
                    obj = OPTIONS.get(ALL_WORLDS, node);
                list = obj instanceof List ? (List) obj : (List) node.getValueToDisable();
                break;
            }
            default:
            {
                throw new IllegalArgumentException("Attempted to get " + node.toString() + " of type " + node.getVarType() + " as a List<String>.");
            }
        }
        return list;
    }


    public PotionEffectHolder getPotionEffect(final ConfigNode node, final String world)
    {
        PotionEffectHolder effect;

        switch (node.getVarType())
        {
            case POTION_EFFECT:
            {
                Object obj = null;
                if (OPTIONS.contains(world, node))
                    obj = OPTIONS.get(world, node);
                else if (enabledForAll)
                    obj = OPTIONS.get(ALL_WORLDS, node);
                effect = obj instanceof PotionEffectHolder ? (PotionEffectHolder) obj : (PotionEffectHolder) node.getValueToDisable();
                break;
            }
            default:
            {
                throw new IllegalArgumentException("Attempted to get " + node.toString() + " of type " + node.getVarType() + " as a PotionEffectHolder.");
            }
        }
        return effect;
    }


    public BlockTypeList getBlocktypeList(final ConfigNode node, final String world)
    {
        BlockTypeList blockList;

        switch (node.getVarType())
        {
            case BLOCKTYPE_LIST:
            {
                Object obj = null;
                if (OPTIONS.contains(world, node))
                    obj = OPTIONS.get(world, node);
                else if (enabledForAll)
                    obj = OPTIONS.get(ALL_WORLDS, node);
                blockList = obj instanceof BlockTypeList ? (BlockTypeList) obj : (BlockTypeList) node.getValueToDisable();
                break;
            }
            default:
            {
                throw new IllegalArgumentException("Attempted to get " + node.toString() + " of type " + node.getVarType() + " as a BlockTypeList.");
            }
        }
        return blockList;
    }


    public BlockRelationsList getBlockRelationList(final ConfigNode node, final String world)
    {
        BlockRelationsList blockList;

        switch (node.getVarType())
        {
            case BLOCK_RELATION_LIST:
            {
                Object obj = null;
                if (OPTIONS.contains(world, node))
                    obj = OPTIONS.get(world, node);
                else if (enabledForAll)
                    obj = OPTIONS.get(ALL_WORLDS, node);
                blockList = obj instanceof BlockRelationsList ? (BlockRelationsList) obj : (BlockRelationsList) node.getValueToDisable();
                break;
            }
            default:
            {
                throw new IllegalArgumentException("Attempted to get " + node.toString() + " of type " + node.getVarType() + " as a BlockRelationsList.");
            }
        }
        return blockList;
    }


    public abstract void load();


    /**
     * Clear all the loaded config options. Primarily for unit testing purposes.
     */
    public void clearCache()
    {
        OPTIONS.clear();
    }
}