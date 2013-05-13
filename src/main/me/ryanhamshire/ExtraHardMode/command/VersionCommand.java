package me.ryanhamshire.ExtraHardMode.command;

import me.ryanhamshire.ExtraHardMode.ExtraHardMode;
import me.ryanhamshire.ExtraHardMode.service.ICommand;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import javax.print.DocFlavor;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class VersionCommand implements ICommand
{

    @Override
    public boolean execute(ExtraHardMode plugin, CommandSender sender, Command command, String label, String[] args)
    {
        sender.sendMessage(ChatColor.GRAY + "========= " + ChatColor.GOLD + plugin.getName() + ChatColor.GRAY + " =========");
        sender.sendMessage(ChatColor.BLUE + "Version: " + ChatColor.WHITE + plugin.getDescription().getVersion());
        //Read buildnumber from manifest if availible
        {
            JarFile pluginFile = null;
            java.net.URL file =  plugin.getClass().getProtectionDomain().getCodeSource().getLocation();
            try {
                pluginFile = new JarFile(file.getFile());
            } catch (IOException ignored) {}

            if (pluginFile != null)
            {
                try {
                    Manifest manifest = pluginFile.getManifest();
                    if (manifest != null)
                    {
                        String buildNumber = manifest.getMainAttributes().getValue("Build-Number");
                        if (buildNumber.length() > 0)
                            sender.sendMessage(ChatColor.BLUE + "Build: " + ChatColor.WHITE + buildNumber);
                    }
                } catch (IOException ignored) {}
            }
        }

        sender.sendMessage(ChatColor.BLUE + "Author:");
        List<String> authors = plugin.getDescription().getAuthors();
        sender.sendMessage(ChatColor.GRAY + " - " + ChatColor.WHITE + authors.get(0)); //author defined by "author: xyz" gets always loaded first

        sender.sendMessage(ChatColor.BLUE + "Contributors:");
        for (int i = 1; i < authors.size(); i++)
        {
            sender.sendMessage(ChatColor.GRAY + " - " + ChatColor.WHITE + authors.get(i));
        }
        return true;
    }
}
