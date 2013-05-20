package com.extrahardmode.command;

import com.extrahardmode.ExtraHardMode;
import com.extrahardmode.service.CommandHandler;
import com.extrahardmode.service.ICommand;
import com.extrahardmode.service.PermissionNode;
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
