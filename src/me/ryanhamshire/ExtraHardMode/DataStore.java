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

import java.io.*;
import java.util.*;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * class which manages all ExtraHardMode data (except for config options)
 * 
 * TODO complete redo to clean up the main class and make things easier. Convert
 * this to a module.
 */
public class DataStore {
   private ExtraHardMode plugin;
   /**
    * in-memory cache for messages
    */
   private String[] messages;

   /**
    * path information, for where stuff stored on disk is well... stored
    */
   protected final static String dataLayerFolderPath = "plugins" + File.separator + "ExtraHardModeData";
   final static String configFilePath = dataLayerFolderPath + File.separator + "config.yml";
   final static String messagesFilePath = dataLayerFolderPath + File.separator + "messages.yml";

   public DataStore(ExtraHardMode plugin) {
      this.plugin = plugin;
      this.initialize();
   }

   /**
    * initialization!
    */
   void initialize() {
      // load up all the messages from messages.yml
      this.loadMessages();
   }

   private void loadMessages() {
      CustomizableMessage.Type[] messageIDs = CustomizableMessage.Type.values();
      this.messages = new String[CustomizableMessage.Type.values().length];

      HashMap<String, CustomizableMessage> defaults = new HashMap<String, CustomizableMessage>();

      // initialize defaults
      this.addDefault(defaults, CustomizableMessage.Type.NoTorchesHere,
            "There's not enough air flow down here for permanent flames.  Use another method to light your way.", null);
      this.addDefault(
            defaults,
            CustomizableMessage.Type.StoneMiningHelp,
            "You'll need an iron or diamond pickaxe to break stone.  Try exploring natural formations for exposed ore like coal, which softens stone around it when broken.",
            null);
      this.addDefault(defaults, CustomizableMessage.Type.NoPlacingOreAgainstStone, "Sorry, you can't place ore next to stone.", null);
      this.addDefault(defaults, CustomizableMessage.Type.RealisticBuilding, "You can't build while in the air.", null);
      this.addDefault(defaults, CustomizableMessage.Type.LimitedTorchPlacements, "It's too soft there to fasten a torch.", null);
      this.addDefault(defaults, CustomizableMessage.Type.NoCraftingMelonSeeds, "This melon appears to be seedless!", null);

      // load the config file
      FileConfiguration config = YamlConfiguration.loadConfiguration(new File(messagesFilePath));

      // for each message ID
      for(int i = 0; i < messageIDs.length; i++) {
         // get default for this message
         CustomizableMessage.Type messageID = messageIDs[i];
         CustomizableMessage messageData = defaults.get(messageID.name());

         // if default is missing, log an error and use some fake data for now
         // so that the plugin can run
         if(messageData == null) {
            plugin.getLogger().info("Missing message for " + messageID.name() + ".  Please contact the developer.");
            messageData = new CustomizableMessage(messageID, "Missing message!  ID: " + messageID.name() + ".  Please contact a server admin.", null);
         }

         // read the message from the file, use default if necessary
         this.messages[messageID.ordinal()] = config.getString("Messages." + messageID.name() + ".Text", messageData.text);
         config.set("Messages." + messageID.name() + ".Text", this.messages[messageID.ordinal()]);

         if(messageData.notes != null) {
            messageData.notes = config.getString("Messages." + messageID.name() + ".Notes", messageData.notes);
            config.set("Messages." + messageID.name() + ".Notes", messageData.notes);
         }
      }

      // save any changes
      try {
         config.save(DataStore.messagesFilePath);
      } catch(IOException exception) {
         plugin.getLogger().info("Unable to write to the configuration file at \"" + DataStore.messagesFilePath + "\"");
      }

      defaults.clear();
      System.gc();
   }

   private void addDefault(HashMap<String, CustomizableMessage> defaults, CustomizableMessage.Type id, String text, String notes) {
      CustomizableMessage message = new CustomizableMessage(id, text, notes);
      defaults.put(id.name(), message);
   }

   synchronized public String getMessage(CustomizableMessage.Type messageID, String... args) {
      String message = messages[messageID.ordinal()];

      for(int i = 0; i < args.length; i++) {
         String param = args[i];
         message = message.replace("{" + i + "}", param);
      }

      return message;
   }
}
