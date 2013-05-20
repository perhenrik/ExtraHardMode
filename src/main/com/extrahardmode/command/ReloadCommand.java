package com.extrahardmode.command;

import com.extrahardmode.ExtraHardMode;
import com.extrahardmode.config.RootConfig;
import com.extrahardmode.config.messages.MessageConfig;
import com.extrahardmode.module.BlockModule;
import com.extrahardmode.module.DataStoreModule;
import com.extrahardmode.module.EntityModule;
import com.extrahardmode.service.ICommand;
import com.extrahardmode.service.PermissionNode;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 * Reload command.
 */
public class ReloadCommand implements ICommand
{

    @Override
    public boolean execute(ExtraHardMode plugin, CommandSender sender, Command command, String label, String[] args)
    {
        if (sender.hasPermission(PermissionNode.ADMIN.getNode()))
        {
            RootConfig CFG = plugin.getModuleForClass(RootConfig.class);
            CFG.closing();
            CFG.starting();
            plugin.getModuleForClass(MessageConfig.class).reload();
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
            sender.sendMessage(ChatColor.GREEN + plugin.getTag() + " Reloaded " + plugin.getName());
        }
        else
        {
            sender.sendMessage(ChatColor.RED + plugin.getTag() + " Lack permission: " + PermissionNode.ADMIN.getNode());
        }
        return true;
    }

}
