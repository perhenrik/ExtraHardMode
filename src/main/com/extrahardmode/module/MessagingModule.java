package com.extrahardmode.module;

import com.extrahardmode.ExtraHardMode;
import com.extrahardmode.config.messages.MessageConfig;
import com.extrahardmode.config.messages.MessageNode;
import com.extrahardmode.service.EHMModule;
import com.extrahardmode.service.PermissionNode;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.libs.com.google.gson.internal.Pair;
import org.bukkit.entity.Player;

import java.util.Calendar;

/**
 * @author Max
 */
public class MessagingModule extends EHMModule
{
    private final ExtraHardMode plugin;
    private final MessageConfig messages;

    /**
     * Constructor
     *
     * @param plugin
     */
    public MessagingModule(ExtraHardMode plugin)
    {
        super(plugin);
        this.plugin = plugin;
        messages = plugin.getModuleForClass(MessageConfig.class);
    }

    /**
     * Sends a message and logs the timestamp of the sendmessage to prevent spam
     *
     * @param player to send the message to
     * @param node to log the message
     * @param message to send
     */
    private void sendAndSave (Player player, MessageNode node, String message)
    {
        if (player == null)
        {
            plugin.getLogger().warning("Could not send the following message: " + message);
        }
        else
        {
            // FEATURE: don't spam messages
            DataStoreModule.PlayerData playerData = plugin.getModuleForClass(DataStoreModule.class).getPlayerData(player.getName());
            long now = Calendar.getInstance().getTimeInMillis();

            if (!message.equals(playerData.lastMessageSent) || now - playerData.lastMessageTimestamp > 30000)
            {
                player.sendMessage(message);
                playerData.lastMessageSent = node;
                playerData.lastMessageTimestamp = now;
            }

        }
    }

    /**
     * Sends a message to a player. Attempts to not spam the player with messages.
     *
     * @param player  - Target player.
     * @param message - Message to send.
     */
    public void sendMessage(Player player, MessageNode message)
    {
        sendAndSave(player, message, messages.getString(message));
    }

    /**
     * Sends a message with variables which will be inserted in the specified areas
     *
     * @param player Player to send the message to
     * @param message to send
     * @param args variables to fill in
     */
    public void sendMessage (Player player, MessageNode message, Pair <String, String> ... args)
    {
        String msgText = null;
        for (Pair<String, String> pair : args)
        {   /* Replace the placeholder with the actual value */
            msgText = messages.getString(message).replaceAll (pair.first, pair.second);
        }
        sendAndSave(player, message, msgText);
    }

    /**
     * Send the player an informative message to explain what he's doing wrong.
     * Play an optional sound aswell
     * <p>
     *
     * @param player     to send msg to
     * @param perm       permission to silence the message
     * @param sound      errorsound to play after the event got cancelled
     * @param soundPitch 20-35 is good
     */
    public void notifyPlayer(Player player, MessageNode node, PermissionNode perm, Sound sound, float soundPitch)
    {
        if (!player.hasPermission(perm.getNode()))
        {
            sendMessage(player, node);
            if (sound != null)
                player.playSound(player.getLocation(), sound, 1, soundPitch);
        }
    }

    public void notifyPlayer(Player player, MessageNode node, PermissionNode perm)
    {
        notifyPlayer(player, node, perm, null, 0);
    }

    /**
     * Broadcast a message to the whole server
     */
    public void broadcast(MessageNode message, Pair<String, String> ... vars)
    {
        String msgText = null;
        for (Pair <String, String> pair : vars)
        {
            msgText = messages.getString(message).replace(pair.first, pair.second);
        }
        plugin.getServer().broadcastMessage(msgText);
    }

    @Override
    public void starting() {
    }

    @Override
    public void closing() {
    }
}
