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
import com.extrahardmode.service.IModule;
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
            for (IModule module : plugin.getModules().values())
            {
                module.closing();
                module.starting();
            }
            sender.sendMessage(ChatColor.GREEN + plugin.getTag() + " Reloaded " + plugin.getName());
        }
        else
        {
            sender.sendMessage(ChatColor.RED + plugin.getTag() + " Lack permission: " + PermissionNode.ADMIN.getNode());
        }
        return true;
    }

}
