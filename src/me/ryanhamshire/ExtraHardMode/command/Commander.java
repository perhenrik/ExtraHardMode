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
import me.ryanhamshire.ExtraHardMode.service.CommandHandler;
import me.ryanhamshire.ExtraHardMode.service.ICommand;
import me.ryanhamshire.ExtraHardMode.service.PermissionNode;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class Commander extends CommandHandler
{

    public Commander(ExtraHardMode plugin)
    {
        super(plugin, "ehm");
        HelpCommand help = new HelpCommand();
        registerCommand("help", help);
        registerCommand("?", help);
        registerCommand("reload", new ReloadCommand());
        registerCommand("version", new VersionCommand());
    }

    @Override
    public boolean noArgs(CommandSender sender, Command command, String label)
    {
        sender.sendMessage(ChatColor.GRAY + "========= " + ChatColor.RED + plugin.getName() + ChatColor.GRAY + " =========");
        sender.sendMessage(" /ehm");
        sender.sendMessage("    help" + ChatColor.YELLOW + " - Show the help menu");
        sender.sendMessage("    version" + ChatColor.YELLOW + " - Show version info");
        if (sender.hasPermission(PermissionNode.ADMIN.getNode()))
        {
            sender.sendMessage("    reload " + ChatColor.YELLOW + "- Reload the plugin");
        }
        return true;
    }

    @Override
    public boolean unknownCommand(CommandSender sender, Command command, String label, String[] args)
    {
        sender.sendMessage(ChatColor.YELLOW + plugin.getTag() + " Unknown command: " + ChatColor.WHITE + args[0]);
        return true;
    }

    private class HelpCommand implements ICommand
    {

        @Override
        public boolean execute(ExtraHardMode plugin, CommandSender sender, Command command, String label, String[] args)
        {
            return noArgs(sender, command, label);
        }

    }
}
