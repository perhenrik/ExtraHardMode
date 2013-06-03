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

package com.extrahardmode.command;

import com.extrahardmode.ExtraHardMode;
import com.extrahardmode.service.ICommand;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

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
