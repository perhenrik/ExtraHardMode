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

package com.extrahardmode.config;


import com.extrahardmode.ExtraHardMode;
import com.extrahardmode.service.config.ConfigNode;
import com.extrahardmode.service.config.EHMConfig;
import com.extrahardmode.service.config.MultiWorldConfig;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

/**
 *
 */
public class RootConfig extends MultiWorldConfig
{
    /**
     * Constructor
     */
    public RootConfig(ExtraHardMode plugin)
    {
        super(plugin);
    }


    /**
     * search file
     * load file
     * load mode
     * load worlds
     * load nodes
     * verify nodes
     * save to file
     */

    @Override
    public void starting()
    {
        load();
    }


    @Override
    public void closing()
    {
    }


    @Override
    public void load()
    {
        init();
        //find all ymls
        File[] configFiles = findAllYmlFiles(plugin.getDataFolder());
        //load the ymls
        EHMConfig[] ehmConfigs = new EHMConfig[configFiles.length];
        for (int i = 0; i < configFiles.length; i++)
        {
            ehmConfigs[i] = new EHMConfig(configFiles[i]);
            ehmConfigs[i].registerNodes(RootNode.values());
            ehmConfigs[i].load();
        }
        //what is the main config.yml file?
        EHMConfig mainEhmConfig = null;
        for (EHMConfig ehmConfig : ehmConfigs)
        {
            if (ehmConfig.isMainConfig())
            {
                mainEhmConfig = ehmConfig;
                break;
            }
        }
        //has config.yml been found? not -> create it
        if (mainEhmConfig == null)
        {
            File mainFile = new File(plugin.getDataFolder(), "config.yml");
            if (!mainFile.exists())
            {
                try
                {
                    mainFile.createNewFile();
                } catch (IOException e)
                {
                    plugin.getLogger().severe("Couldn't create config.yml");
                    e.printStackTrace();
                }
            }
            mainEhmConfig = new EHMConfig(mainFile);
        }
        //Load config.yml
        for (Map.Entry<ConfigNode, Object> node : mainEhmConfig.getLoadedNodes().entrySet())
        {
            for (String world : mainEhmConfig.getWorlds())
            {
                set(world, node.getKey(), node.getValue());
            }
        }
        //Save files
        mainEhmConfig.save();

    }


    /**
     * find all yml files
     * loop over all files
     * determine main file
     * if doesn't exist -> create
     * load main file
     * loop remaining files
     * merge configs
     * determine disable/inherit values
     * save files with correct inheritance
     */


    /**
     * Search the base directory for yml-files
     *
     * @return File[] containing all the *.yml Files in a lexical order
     */
    protected File[] findAllYmlFiles(File baseDir)
    {
        String[] filePaths = baseDir.list(new FilenameFilter()
        {
            @Override
            public boolean accept(File dir, String name)
            {
                return name.endsWith(".yml"); //TODO - disables
            }
        });
        if (filePaths == null) filePaths = new String[]{};
        Arrays.sort(filePaths); //lexically
        ArrayList<File> files = new ArrayList<File>();
        for (String fileName : filePaths)
            files.add(new File(plugin.getDataFolder() + File.separator + fileName));
        return files.toArray(new File[files.size()]);
    }


//    /**
//     * Load the given Files in a List as Config Objects, which hold the reference to the File and the loaded FileConfiguration Ignores files that don't have the RootNode in them.
//     *
//     * @return a HashMap containing all valid FileConfigurations and config.yml
//     */
//    protected List<EHMConfig> loadFilesFromDisk(File[] files)
//    {
//        List<EHMConfig> ehmConfigs = new ArrayList<EHMConfig>();
//        for (File file : files)
//        {
//            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
//            ehmConfigs.add(new EHMConfig(config, file));
//        }
//
//        //Check if the config is a valid config: It has to contain a world attribute and additionally the BaseNode of RootNode
////        Iterator<EHMConfig> iter = ehmConfigs.iterator();
////        while (iter.hasNext())
////        {
////            EHMConfig ehmConfig = iter.next();
////            FileConfiguration fileConfig = ehmConfig.getFileConfiguration();
////
////            if ((fileConfig.getValues(true).containsKey(RootNode.baseNode()) && fileConfig.getStringList(RootNode.WORLDS.getPath()) != null) || ehmConfig.getFileName().equals("config.yml"))
////                ;
////                //holds all default values for the other configs
////            else iter.remove();
////        }
//        return ehmConfigs;
//    }
//
//    /**
//     * Loads all FileConfigurations into memory Insures that there is always a main config.yml loads all other Config's
//     * based on the Mode specified in the ConfigFile
//     *
//     * @param ehmConfigs FileName + respective FileConfiguration
//     */
//    private void loadEhmConfigs(List<EHMConfig> ehmConfigs)
//    {
//        ehmConfigs = loadMainEhmConfig(ehmConfigs);
//
//        EHMConfig defaults = null;
//        //LOAD MAIN
////        for (EHMConfig ehmConfig : ehmConfigs)
////        {   //loadMain insures that there is always a config.yml
////            if (ehmConfig.getFileName().equals("config.yml") && ehmConfig.getStatus() == Status.PROCESSED && ehmConfig.getMode() == Mode.MAIN)
////            {
////                defaults = ehmConfig;
////                ehmConfigs.remove(ehmConfig);
////                break;
////            }
////        }
//
//        for (EHMConfig ehmConfig : ehmConfigs)
//        {
////            {
////                //DETERMINE MODE FIRST
////                Response<String> response = (Response<String>) loadNode(ehmConfig.getFileConfiguration(), RootNode.MODE, false);
////                try
////                {
////                    if (response.getStatusCode() != Status.NOT_FOUND)
////                        ehmConfig.setMode(Mode.valueOf(response.getContent().toUpperCase()));
////                } catch (IllegalArgumentException ignored)
////                {
////                } finally
////                {
////                    if (ehmConfig.getMode() == null || ehmConfig.getMode() == Mode.NOT_SET)
////                        ehmConfig.setMode(Mode.INHERIT);
////                }
////            }
//
//            //LOAD CONFIGS
//            //TODO DEAL WITH
//            switch (ehmConfig.getMode())
//            {
//                case MAIN:
//                {
//                    //Inherit is the default mode, main just has to be loaded first
//                    ehmConfig.setMode(Mode.INHERIT); //fallthrough
//                }
//                case DISABLE:
//                case INHERIT:
//                {
//                    ehmConfig = loadEhmConfig(ehmConfig, defaults);
//                    break;
//                }
//                case NOT_SET:
//                default:
//                {
//                    ehmConfig.setMode(ehmConfig.getFileName().equals("config.yml") ? Mode.MAIN : Mode.INHERIT);
//                    ehmConfig = loadEhmConfig(ehmConfig, defaults);
//                    break;
//                }
//            }
//
//            //MODE SPECIFIC COMPARATIONS
//            //Check if all values in the config are the same as in the master config and mark them as "inheritent" if they are the same, this makes
//            //it easier to see what the admin has overriden. Or if a value in disable mode is already disabled.
//            if (ehmConfig.getMode() == Mode.INHERIT || ehmConfig.getMode() == Mode.DISABLE)
//            {
//                for (RootNode node : RootNode.values())
//                {
//                    Object thisValue = loadNode(ehmConfig.getFileConfiguration(), node, false).getContent();
//                    Object thatValue = loadNode(defaults.getFileConfiguration(), node, false).getContent();
//
//                    switch (ehmConfig.getMode())
//                    {
//                        case INHERIT:
//                        {   //floating point arithmetic is inaccurate...
//                            if ((thisValue == thatValue) || ((thisValue instanceof Double && thatValue instanceof Double)
//                                    && (BigDecimal.valueOf((Double) thisValue).equals(BigDecimal.valueOf((Double) thatValue)))))
//                            {
//                                ehmConfig.getFileConfiguration().set(node.getPath(), ehmConfig.getMode().name().toLowerCase());
//                                ehmConfig.setStatus(Status.ADJUSTED);
//                            }
//                            break;
//                        }
//                        case DISABLE:
//                        {
//                            if ((thisValue == node.getValueToDisable()) || ((thisValue instanceof Double && node.getValueToDisable() instanceof Double)
//                                    && (BigDecimal.valueOf((Double) thisValue).equals(BigDecimal.valueOf((Double) node.getValueToDisable())))))
//                            {
//                                ehmConfig.getFileConfiguration().set(node.getPath(), ehmConfig.getMode().name().toLowerCase());
//                                ehmConfig.setStatus(Status.ADJUSTED);
//                            }
//                            break;
//                        }
//                        default:
//                            throw new IllegalArgumentException(ehmConfig.getMode().name() + " not implemented");
//                    }
//
//                }
//            }
//
//            if (ehmConfig.getStatus() == Status.ADJUSTED)
//            {
//                saveConfig(ehmConfig);
//            }
//        }
//    }
//
//
//    /**
//     * Loads the main config.yml
//     *
//     * @param ehmConfigs to process
//     *
//     * @return all configs and the main config marked as processed
//     */
//    private List<EHMConfig> loadMainEhmConfig(List<EHMConfig> ehmConfigs)
//    {
//        EHMConfig main = null;
//
//        boolean contains = false;
//        for (EHMConfig ehmConfig : ehmConfigs)
//        {
//            if (ehmConfig.getFileName().equals("config.yml"))
//            {
//                contains = true;
//                main = ehmConfig;
//                ehmConfigs.remove(ehmConfig);
//                break;
//            }
//        }
//        if (!contains)
//        {
//            main = new EHMConfig(new YamlConfiguration(), plugin.getDataFolder().getAbsolutePath() + File.separator + "config.yml");
//        }
//
//        main.setMode(Mode.MAIN);
//        main = loadEhmConfig(main, null);
//        if (main.getStatus() == Status.ADJUSTED)
//        {
//            saveConfig(main);
//        }
//
//        main.setStatus(Status.PROCESSED);
//        ehmConfigs.add(main);
//        return ehmConfigs;
//    }
//
//
//    /**
//     * Store the Options from the FileConfiguration into memory
//     *
//     * @param ehmConfig Config, to load the values from, according to the Mode specified. The Mode determines how not found
//     *                  or same values are treated
//     * @param main      main file to use for reference values, can be null if we are loading with Mode.MAIN
//     *
//     * @return the passed in config, is marked as Status.ADJUSTED if the Config has been changed
//     */
//    private EHMConfig loadEhmConfig(EHMConfig ehmConfig, EHMConfig main)
//    {
//        //LOAD WORLDS
////        Response<List<String>> myWorlds = loadNode(ehmConfig.getFileConfiguration(), RootNode.WORLDS/*, false*/);
////        List<String> worlds = myWorlds.getContent();
////        //Check for * placeholder = Enables plugin for all worlds
////        if (worlds.contains(ALL_WORLDS))
////            enabledForAll = true;
//
//        for (RootNode node : RootNode.values())
//        {
//            Response response = loadNode(ehmConfig.getFileConfiguration(), node, false);
//
//            //TODO VALIDATION - EXTRACT
//            if (node.getVarType().equals(ConfigNode.VarType.INTEGER) && response.getStatusCode() == Status.OK)
//            {
//                response = validateInt(node, response.getContent());
//            }
//
//            switch (ehmConfig.getMode()) //special actions regarding default values
//            {
//                case MAIN:
//                {
//                    if (response.getStatusCode() == Status.NOT_FOUND)
//                    {
//                        //get with defaults on
//                        response = loadNode(ehmConfig.getFileConfiguration(), node, true);
//                        ehmConfig.setStatus(Status.ADJUSTED);
//                    }
//                    break;
//                }
//                case DISABLE:
//                {
//                    if (response.getStatusCode() == Status.NOT_FOUND || response.getStatusCode() == Status.INHERITS) //Status = Disable: nothing needs to be done
//                    {
//                        if ((!response.getContent().equals(Mode.DISABLE.name()) && response.getStatusCode() == Status.INHERITS)
//                                || response.getStatusCode() == Status.NOT_FOUND)
//                        {   //mark it only adjusted if it has been changed, we rewrite the option nevertheless
//                            ehmConfig.setStatus(Status.ADJUSTED);
//                        }
//
//                        response.setContent(Mode.DISABLE.name().toLowerCase());
//                        response.setStatus(Status.DISABLES);
//                    }
//                    break;
//                }
//                case INHERIT: //mark non found nodes as inherits
//                {
//                    if (response.getStatusCode() == Status.NOT_FOUND || response.getStatusCode() == Status.DISABLES)
//                    {
//                        if ((!response.getContent().equals(Mode.INHERIT.name()) && response.getStatusCode() == Status.DISABLES)
//                                || response.getStatusCode() == Status.NOT_FOUND)
//                        {   //mark it only adjusted if it has been changed, we rewrite the option nevertheless
//                            ehmConfig.setStatus(Status.ADJUSTED);
//                        }
//                        response.setContent(Mode.INHERIT.name().toLowerCase());
//                        response.setStatus(Status.INHERITS);
//                    }
//                    break;
//                }
//                default:
//                {
//                    throw new UnsupportedOperationException("Mode: " + ehmConfig.getMode().name() + " isn't handled");
//                }
//            }
//
//            //the Mode should always reflect in the yml file
//            /* Special node handling */
//            switch (node)
//            {
//                case MODE:
//                {
//                    /* Make sure that the mode we used is the same in the config */
//                    if ((response.getContent() instanceof String) && !((String) response.getContent()).equalsIgnoreCase(ehmConfig.getMode().name()))
//                    {
//                        response.setContent(ehmConfig.getMode().name());
//                        ehmConfig.setStatus(Status.ADJUSTED);
//                    }
//                    break;
//                }
//                case MORE_FALLING_BLOCKS:
//                case SUPER_HARD_STONE_TOOLS:
//                case SUPER_HARD_STONE_ORE_BLOCKS:
//                {
//                    if (response.getContent() instanceof List)
//                    {
//                        List<BlockType> blockTypes = new ArrayList<BlockType>();
//                        for (String blockString : (List<String>) response.getContent())
//                        {
//                            BlockType blockType = BlockType.loadFromConfig(blockString);
//                            if (blockType != null)
//                                blockTypes.add(blockType);
//                        }
//                        response.setContent(blockTypes);
//                    }
//                    break;
//                }
//            }
//
//            //TODO SEPERATE, THIS IS BS
//            // write config, some objects have multiple subnodes
//            switch (node.getVarType())
//            {
//                case POTION_EFFECT:
//                {
//                    ((PotionEffectHolder) response.getContent()).saveToConfig(ehmConfig.getFileConfiguration(), node.getPath());
//                    break;
//                }
//                case BLOCKTYPE_LIST:
//                {
//                    List<String> blockStrings = new ArrayList<String>();
//                    for (BlockType blockType : (List<BlockType>) response.getContent())
//                        blockStrings.add(blockType.saveToString());
//                    ehmConfig.getFileConfiguration().set(node.getPath(), blockStrings);
//                    break;
//                }
//                default:
//                {
//                    ehmConfig.getFileConfiguration().set(node.getPath(), response.getContent()); //has to be before we get the actual values
//                }
//            }
//
//            //the actual values that need to be loaded into memory for the two modes to work
//            if (response.getStatusCode() == Status.INHERITS || response.getStatusCode() == Status.DISABLES)
//            {
//                switch (ehmConfig.getMode())
//                {
//                    case INHERIT: //load the value from the main config
//                        response.setContent(loadNode(main.getFileConfiguration(), node, false).getContent());
//                        break;
//                    case DISABLE: //get the value to disable this option
//                        response.setContent(node.getValueToDisable());
//                        break;
//                }
//            }
//
//            if (myWorlds.getStatusCode() != Status.NOT_FOUND)
//            {
//                for (String world : worlds)
//                {
//                    set(world, node, response.getContent());
//                }
//            }
//        }
//        return ehmConfig;
//    }
//
//
//    /**
//     * Reorders and saves the config. Reorders the Config to the order specified by the enum in RootNode. This assumes
//     * that the Config only has valid Entries.
//     *
//     * @param ehmConfig Config to save
//     */
//    private void saveConfig(EHMConfig ehmConfig)
//    {
//        //Reorder
//        FileConfiguration reorderedConfig = new YamlConfiguration();
//        for (RootNode node : RootNode.values())
//        {
//            switch (node.getVarType())
//            {
////                case POTION_EFFECT:
////                {
////                    //Load PotionEffect with custom code and rewrite
////                    PotionEffectHolder potionEffect = PotionEffectHolder.loadFromConfig(ehmConfig.getFileConfiguration().getConfigurationSection(node.getPath()));
////                    if (potionEffect != null)
////                        potionEffect.saveToConfig(reorderedConfig, node.getPath());
////                    break;
////                }
////                case BLOCKTYPE_LIST:
////                {
////                    List<BlockType> blockTypes = new ArrayList<BlockType>();
////                    List<String> blockStrings = new ArrayList<String>();
////                    for (String blockString : ehmConfig.getFileConfiguration().getStringList(node.getPath()))
////                    {
////                        BlockType type = BlockType.loadFromConfig(blockString);
////                        if (type != null)
////                            blockTypes.add(type);
////                    }
////                    for (BlockType blockType : blockTypes)
////                    {
////                        blockStrings.add(blockType.saveToString());
////                    }
////                    reorderedConfig.set(node.getPath(), blockStrings);
////                    break;
////                }
////                default:
////                {
////                    switch (node)
////                    {
////                        case MORE_FALLING_BLOCKS:
////                        {
////                            //TODO RICHTIG DUMM
////                            List<BlockType> blockTypes = new ArrayList<BlockType>();
////                            List<String> blockStrings = new ArrayList<String>();
////                            for (String blockString : ehmConfig.getFileConfiguration().getStringList(node.getPath()))
////                            {
////                                BlockType type = BlockType.loadFromConfig(blockString);
////                                if (type != null)
////                                    blockTypes.add(type);
////                            }
////                            for (BlockType blockType : blockTypes)
////                            {
////                                blockStrings.add(blockType.saveToString());
////                            }
////                            reorderedConfig.set(node.getPath(), blockStrings);
////                            break;
////                        }
////                        default:
////                            reorderedConfig.set(node.getPath(), ehmConfig.getFileConfiguration().get(node.getPath()));
////                    }
////                }
//            }
//        }
//        ehmConfig.setFileConfiguration(reorderedConfig);
//
//        //save the reordered config
//        try
//        {
//            ehmConfig.getFileConfiguration().save(ehmConfig.getConfigFile());
//        } catch (IOException e)
//        {
//            e.printStackTrace();
//        }
//    }
}