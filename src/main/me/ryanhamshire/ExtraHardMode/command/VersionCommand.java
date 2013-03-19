/*
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
package me.ryanhamshire.ExtraHardMode.command;

import me.ryanhamshire.ExtraHardMode.ExtraHardMode;
import me.ryanhamshire.ExtraHardMode.service.ICommand;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class VersionCommand implements ICommand
{

    @Override
    public boolean execute(ExtraHardMode plugin, CommandSender sender, Command command, String label, String[] args)
    {
        sender.sendMessage(ChatColor.GRAY + "========= " + ChatColor.GOLD + plugin.getName() + ChatColor.GRAY + " =========");
        sender.sendMessage(ChatColor.BLUE + "Version: " + ChatColor.WHITE + plugin.getDescription().getVersion());
        sender.sendMessage(ChatColor.BLUE + "Authors:");
        for (String author : plugin.getDescription().getAuthors())
        {
            sender.sendMessage(ChatColor.GRAY + " - " + ChatColor.WHITE + author);
        }
        return true;
    }
}
