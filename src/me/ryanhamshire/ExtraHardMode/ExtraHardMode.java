/*
    ExtraHardMode Server Plugin for Minecraft
    Copyright (C) 2012 Ryan Hamshire

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.ryanhamshire.ExtraHardMode;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import me.ryanhamshire.ExtraHardMode.config.RootConfig;
import me.ryanhamshire.ExtraHardMode.config.RootNode;
import me.ryanhamshire.ExtraHardMode.config.messages.MessageConfig;
import me.ryanhamshire.ExtraHardMode.event.BlockEventHandler;
import me.ryanhamshire.ExtraHardMode.event.EntityEventHandler;
import me.ryanhamshire.ExtraHardMode.event.PlayerEventHandler;
import me.ryanhamshire.ExtraHardMode.module.DataStore;
import me.ryanhamshire.ExtraHardMode.module.DataStore.PlayerData;
import me.ryanhamshire.ExtraHardMode.module.EntityModule;
import me.ryanhamshire.ExtraHardMode.module.PhysicsModule;
import me.ryanhamshire.ExtraHardMode.service.IModule;
import me.ryanhamshire.ExtraHardMode.task.MoreMonstersTask;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main plugin class.
 */
public class ExtraHardMode extends JavaPlugin {

   /**
    * Plugin tag.
    */
   public static final String TAG = "[EHM]";

   /**
    * Registered modules.
    */
   private final Map<Class<? extends IModule>, IModule> modules = new HashMap<>();

   /**
    * for computing random chance
    */
   private final Random randomNumberGenerator = new Random();

   /**
    * list of worlds where extra hard mode rules apply
    */
   private final List<World> config_enabled_worlds = new ArrayList<World>();

   /**
    * which materials beyond sand and gravel should be subject to gravity
    */
   private final List<Material> config_moreFallingBlocks = new ArrayList<Material>();

   /**
    * initializes well... everything
    */
   @Override
   public void onEnable() {
      // Generate Root Config
      RootConfig rootConfig = new RootConfig(this);
      // Register modules
      registerModule(RootConfig.class, rootConfig);
      registerModule(MessageConfig.class, new MessageConfig(this));
      registerModule(DataStore.class, new DataStore(this));
      registerModule(EntityModule.class, new EntityModule(this));
      registerModule(PhysicsModule.class, new PhysicsModule(this));

      // get enabled world names from the config file
      List<String> enabledWorldNames = rootConfig.getStringList(RootNode.WORLDS);

      // validate enabled world names
      for(String worldName : enabledWorldNames) {
         World world = this.getServer().getWorld(worldName);
         if(world == null) {
            this.getLogger().warning("Error: There's no world named '" + worldName + "'.  Please update your config.yml.");
         } else {
            this.config_enabled_worlds.add(world);
         }
      }

      // try to load the list from the config file
      List<String> moreFallingBlocksList = rootConfig.getStringList(RootNode.MORE_FALLING_BLOCKS);

      // parse this final list of additional falling blocks
      for(String materialName : moreFallingBlocksList) {
         Material material = Material.getMaterial(materialName);
         if(material == null) {
            getLogger().warning("Additional Falling Blocks Configuration: Material not found: " + materialName + ".");
         } else {
            this.config_moreFallingBlocks.add(material);
         }
      }

      // register for events
      PluginManager pluginManager = this.getServer().getPluginManager();

      // player events
      PlayerEventHandler playerEventHandler = new PlayerEventHandler(this);
      pluginManager.registerEvents(playerEventHandler, this);

      // block events
      BlockEventHandler blockEventHandler = new BlockEventHandler(this);
      pluginManager.registerEvents(blockEventHandler, this);

      // entity events
      EntityEventHandler entityEventHandler = new EntityEventHandler(this);
      pluginManager.registerEvents(entityEventHandler, this);

      // FEATURE: monsters spawn in the light under a configurable Y level
      MoreMonstersTask task = new MoreMonstersTask(this);
      // Every 60 seconds
      this.getServer().getScheduler().scheduleSyncRepeatingTask(this, task, 1200L, 1200L);
   }

   /**
    * sends a color-coded message to a player
    * 
    * @param player
    * @param color
    * @param message
    */
   public void sendMessage(Player player, ChatColor color, String message) {
      if(player == null) {
         getLogger().info(color + message);
      } else {
         // FEATURE: don't spam messages
         PlayerData playerData = getModuleForClass(DataStore.class).getPlayerData(player.getName());
         long now = Calendar.getInstance().getTimeInMillis();
         if(!message.equals(playerData.lastMessageSent) || now - playerData.lastMessageTimestamp > 30000) {
            player.sendMessage(color + message);
            playerData.lastMessageSent = message;
            playerData.lastMessageTimestamp = now;
         }
      }
   }

   /**
    * Computes random chance
    * 
    * @param percentChance
    *           - Percentage of success.
    * @return True if it was successful, else false.
    */
   public boolean random(int percentChance) {
      return randomNumberGenerator.nextInt(101) < percentChance;
   }

   /**
    * Get random generator.
    * 
    * @return
    */
   public Random getRandom() {
      return randomNumberGenerator;
   }

   public boolean plantDies(Block block, byte newDataValue) {
      World world = block.getWorld();
      RootConfig config = getModuleForClass(RootConfig.class);
      if(!this.config_enabled_worlds.contains(world) || !config.getBoolean(RootNode.WEAK_FOOD_CROPS))
         return false;

      // not evaluated until the plant is nearly full grown
      if(newDataValue <= (byte) 6)
         return false;

      Material material = block.getType();
      if(material == Material.CROPS || material == Material.MELON_STEM || material == Material.CARROT || material == Material.PUMPKIN_STEM
            || material == Material.POTATO) {
         int deathProbability = 25;

         // plants in the dark always die
         if(block.getLightFromSky() < 10) {
            deathProbability = 100;
         }

         else {

            Biome biome = block.getBiome();

            // the desert environment is very rough on crops
            if(biome == Biome.DESERT || biome == Biome.DESERT_HILLS) {
               deathProbability += 50;
            }

            // unwatered crops are more likely to die
            Block belowBlock = block.getRelative(BlockFace.DOWN);
            byte moistureLevel = 0;
            if(belowBlock.getType() == Material.SOIL) {
               moistureLevel = belowBlock.getData();
            }

            if(moistureLevel == 0) {
               deathProbability += 25;
            }
         }

         if(random(deathProbability)) {
            return true;
         }
      }

      return false;
   }

   public List<Material> getFallingBlocks() {
      return config_moreFallingBlocks;
   }

   public List<World> getEnabledWorlds() {
      return config_enabled_worlds;
   }

   public String getTag() {
      return TAG;
   }

   /**
    * Register a module.
    * 
    * @param clazz
    *           - Class of the instance.
    * @param module
    *           - Module instance.
    * @throws IllegalArgumentException
    *            - Thrown if an argument is null.
    */
   public <T extends IModule> void registerModule(Class<T> clazz, T module) {
      // Check arguments.
      if(clazz == null) {
         throw new IllegalArgumentException("Class cannot be null");
      } else if(module == null) {
         throw new IllegalArgumentException("Module cannot be null");
      }
      // Add module.
      modules.put(clazz, module);
      // Tell module to start.
      module.starting();
   }

   /**
    * Deregister a module.
    * 
    * @param clazz
    *           - Class of the instance.
    * @return Module that was removed. Returns null if no instance of the module
    *         is registered.
    */
   public <T extends IModule> T deregisterModuleForClass(Class<T> clazz) {
      // Check arguments.
      if(clazz == null) {
         throw new IllegalArgumentException("Class cannot be null");
      }
      // Grab module and tell it its closing.
      T module = clazz.cast(modules.get(clazz));
      if(module != null) {
         module.closing();
      }
      return module;
   }

   /**
    * Retrieve a registered module.
    * 
    * @param clazz
    *           - Class identifier.
    * @return Module instance. Returns null is an instance of the given class
    *         has not been registered with the API.
    */
   public <T extends IModule> T getModuleForClass(Class<T> clazz) {
      return clazz.cast(modules.get(clazz));
   }
}
