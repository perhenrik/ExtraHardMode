package me.ryanhamshire.ExtraHardMode.command;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import me.ryanhamshire.ExtraHardMode.ExtraHardMode;
import me.ryanhamshire.ExtraHardMode.config.RootConfig;
import me.ryanhamshire.ExtraHardMode.config.RootNode;
import me.ryanhamshire.ExtraHardMode.config.messages.MessageConfig;
import me.ryanhamshire.ExtraHardMode.module.BlockModule;
import me.ryanhamshire.ExtraHardMode.module.DataStoreModule;
import me.ryanhamshire.ExtraHardMode.module.EntityModule;
import me.ryanhamshire.ExtraHardMode.service.ICommand;
import me.ryanhamshire.ExtraHardMode.service.PermissionNode;

/**
 * Reload command.
 */
public class ReloadCommand implements ICommand {

   @Override
   public boolean execute(ExtraHardMode plugin, CommandSender sender, Command command, String label, String[] args) {
      if(sender.hasPermission(PermissionNode.ADMIN.getNode())) {
         RootConfig root = plugin.getModuleForClass(RootConfig.class);
         root.reload();
         plugin.getModuleForClass(MessageConfig.class).reload();
         // get enabled world names from the config file
         List<String> enabledWorldNames = root.getStringList(RootNode.WORLDS);
         plugin.getEnabledWorlds().clear();
         // validate enabled world names
         for(String worldName : enabledWorldNames) {
            World world = plugin.getServer().getWorld(worldName);
            if(world == null) {
               plugin.getLogger().warning("Error: There's no world named '" + worldName + "'.  Please update your config.yml.");
            } else {
               plugin.getEnabledWorlds().add(world);
            }
         }
         // Restart data store.
         DataStoreModule dataStore = plugin.getModuleForClass(DataStoreModule.class);
         dataStore.closing();
         dataStore.starting();
         // Restart entity module.
         EntityModule entityModule = plugin.getModuleForClass(EntityModule.class);
         entityModule.closing();
         entityModule.starting();
         // Restart entity block module.
         BlockModule blockModule = plugin.getModuleForClass(BlockModule.class);
         blockModule.closing();
         blockModule.starting();
      } else {
         sender.sendMessage(ChatColor.RED + plugin.getTag() + " Lack permission: " + PermissionNode.ADMIN.getNode());
      }
      return true;
   }

}
