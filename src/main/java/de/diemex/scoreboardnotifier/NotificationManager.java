package de.diemex.scoreboardnotifier;


import de.diemex.scoreboardnotifier.message.MsgLineHolder;
import de.diemex.scoreboardnotifier.message.MsgSettings;
import de.diemex.scoreboardnotifier.message.StringUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class to show announcements via the Scoreboard
 *
 * @author Diemex
 */
public class NotificationManager implements Listener
{
    final Plugin plugin;

    /**
     * Every Player has his own {@link de.diemex.scoreboardnotifier.PlayerNotificationHandler} that handles messages of a given player
     */
    Map<String, PlayerNotificationHandler> managerScoreboards;


    /**
     * Constructor
     *
     * @param plugin plugin is used to register events and gives access to bukkit
     */
    public NotificationManager(Plugin plugin)
    {
        this.plugin = plugin;
        managerScoreboards = new HashMap<String, PlayerNotificationHandler>();
    }


    private boolean show_Popup(final String player, final String scoreboardTitle, final MsgSettings type, final List<MsgLineHolder> msg)
    {
        NotificationHolder popup = new NotificationHolder(type, scoreboardTitle, msg);

        final PlayerNotificationHandler store = getPlayerHandler(player, scoreboardTitle);

        final int id = store.displayMessage(popup);

        if (type.getLength() > 0)
            removeNotificationLater(id, store, type.getLength());

        return true;
    }


    /**
     * Shows a popup in the scoreboard. Breaks msg into lines automatically, keep in mind Color codes reduce line size.
     *
     * @param player          player for whom to show it
     * @param type            type defines color and length
     * @param scoreboardTitle scoreboard title is used
     * @param msg             message text already cut into lines with length <= 16
     *
     * @return a boolean for whatever reason I cant remember
     */
    private boolean showTimedPopup(final String player, final MsgSettings type, final String scoreboardTitle, final List<String> msg)
    {
        return show_Popup(player, scoreboardTitle, type, MsgLineHolder.fromString(msg, type.getTextColor()));
    }


    /**
     * Shows a popup in the scoreboard. Breaks msg into lines automatically, keep in mind Color codes reduce line size.
     *
     * @param player          player for whom to show it
     * @param type            type defines color and length
     * @param scoreboardTitle scoreboard title is used
     * @param msg             message text will be wrapped to fit the scoreboard limitations
     *
     * @return a boolean for whatever reason I cant remember
     */
    private boolean showTimedPopup(final String player, final MsgSettings type, final String scoreboardTitle, final String msg)
    {
        return show_Popup(player, scoreboardTitle, type, StringUtil.getLines(msg, type.getTextColor()));
    }


    /**
     * Shows a popup in the scoreboard. Breaks msg into lines automatically, keep in mind Color codes reduce line size.
     *
     * @param player          player for whom to show it
     * @param length          how long in ticks the message should display
     * @param scoreboardTitle scoreboard title is used
     * @param msg             message text will be wrapped to fit the scoreboard limitations
     *
     * @return a boolean for whatever reason I cant remember
     */
    public boolean showTimedPopup(final String player, int length, final String scoreboardTitle, final String msg)
    {
        return show_Popup(player, scoreboardTitle, new MsgSettings(length), StringUtil.getLines(msg));
    }


    /**
     * Shows a popup in the scoreboard. Breaks msg into lines automatically, keep in mind Color codes reduce line size.
     * This method should only be used if you want to be able to change the message contents or remove the message from your plugin.
     *
     * @param player          player for whom to show it
     * @param identifier      ídentifier of this message, message can be removed by its identifier later
     * @param length          how long in ticks the message should display, set to 0 if you want the message to remove the message yourself
     * @param titleColor      color of the title line
     * @param textColor       general textcolor of the message
     * @param scoreboardTitle scoreboard title is used
     * @param msg             message text will be wrapped to fit the scoreboard limitations
     *
     * @return a boolean for whatever reason I cant remember
     */
    public boolean showPopup(final String player, final String identifier, int length, ChatColor titleColor, ChatColor textColor, final String scoreboardTitle, final String msg)
    {
        return show_Popup(player, scoreboardTitle, new MsgSettings(identifier, length, titleColor, textColor), StringUtil.getLines(msg, textColor));
    }


    /**
     * Shows a popup in the scoreboard. Breaks msg into lines automatically, keep in mind Color codes reduce line size.
     * This method should only be used if you want to be able to change the message contents or remove the message from your plugin.
     *
     * @param player          player for whom to show it
     * @param identifier      ídentifier of this message, message can be removed by its identifier later
     * @param length          how long in ticks the message should display, set to 0 if you want the message to remove the message yourself
     * @param titleColor      color of the title line
     * @param textColor       general textcolor of the message
     * @param scoreboardTitle scoreboard title is used
     * @param msg             message text already cut into lines with length <= 16
     *
     * @return a boolean for whatever reason I cant remember
     */
    public boolean showPopup(final String player, final String identifier, int length, ChatColor titleColor, ChatColor textColor, final String scoreboardTitle, final List<String> msg)
    {
        return showTimedPopup(player, new MsgSettings(identifier, length, titleColor, textColor), scoreboardTitle, msg);
    }


    private boolean broadcast_Popup(final String scoreboardTitle, MsgSettings type, final List<MsgLineHolder> msg)
    {
        NotificationHolder popup = new NotificationHolder(type, scoreboardTitle, msg);

        for (Player player : plugin.getServer().getOnlinePlayers())
        {
            final PlayerNotificationHandler store = getPlayerHandler(player.getName(), scoreboardTitle);

            final int id = store.displayMessage(popup);

            if (!type.hasUniqueIdentifier())
                removeNotificationLater(id, store, type.getLength());
        }
        return true;
    }


    /**
     * Broadcast a message to all players on the server. Breaks msg into lines automatically, keep in mind Color codes reduce line size.
     *
     * @param type            type of the message
     * @param scoreboardTitle title of the scoreboard
     * @param msg             text of the message
     *
     * @return if successful
     */
    public boolean broadcastPopup(MsgSettings type, final String scoreboardTitle, final List<String> msg)
    {
        return broadcast_Popup(scoreboardTitle, type, MsgLineHolder.fromString(msg, type.getTextColor()));
    }


    /**
     * Broadcast a message to all players on the server. Breaks msg into lines automatically, keep in mind Color codes reduce line size.
     *
     * @param type            type of the message
     * @param scoreboardTitle title of the scoreboard
     * @param msg             text of the message
     *
     * @return if successful
     */
    public boolean broadcastPopup(MsgSettings type, final String scoreboardTitle, final String msg)
    {
        return broadcast_Popup(scoreboardTitle, type, StringUtil.getLines(msg, type.getTextColor()));
    }


    /**
     * Remove a message with the given identifier that you used when you created the message
     *
     * @param player     name of the player
     * @param identifier identifier for example: "de.myplugin.deathmsg"
     */
    public void removePopup(String player, final String identifier)
    {
        getPlayerHandler(player, "SbNotify").removeMessage(identifier);
    }


    /**
     * Gets the Store/Manager Object for a given Player and constructs a new one if needed
     *
     * @param player          player
     * @param scoreboardTitle title of the scoreboard
     *
     * @return storage object
     */
    private PlayerNotificationHandler getPlayerHandler(String player, String scoreboardTitle)
    {
        final PlayerNotificationHandler notificationHandler;

        if (managerScoreboards.containsKey(player))
            notificationHandler = managerScoreboards.get(player);
        else
        {
            notificationHandler = new PlayerNotificationHandler(scoreboardTitle, plugin, player);
            managerScoreboards.put(player, notificationHandler);
        }

        return notificationHandler;
    }


    /**
     * Schedule a task to remove a Popup after a given time
     *
     * @param id     id of the message to remove
     * @param store  message object
     * @param length after how many ticks shall we remove the message
     */
    private void removeNotificationLater(final int id, final PlayerNotificationHandler store, final int length)
    {
        //Remove a message after a given time
        plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable()
        {
            @Override
            public void run()
            {
                store.removeMessage(id);
            }
        }, length);
    }


    /**
     * Remove a players scoreboard on logout
     *
     * @param event logout event that occured
     */
    @EventHandler
    private void onPlayerLogout(PlayerQuitEvent event)
    {
        managerScoreboards.remove(event.getPlayer().getName());
    }
}
