package com.extrahardmode.config.messages;


import de.diemex.sbpopupapi.MsgType;
import org.bukkit.ChatColor;

/**
 * @author Diemex
 */
public enum MyMsgTypes implements MsgType
{
    /**
     * Message will be displayed when the inventory has been opened and shows the weight of the inventory
     */
    WEIGHT_MSG ("extrahardmode.player.weight");


    private final String identifier;
    private final int lenght;
    private final ChatColor titleColor;
    private final ChatColor textColor;

    private MyMsgTypes(String identifier)
    {
        this(identifier, 0, null, null);
    }

    private MyMsgTypes (int lenght, ChatColor titleColor, ChatColor textColor)
    {
        this (null, lenght, titleColor, textColor);
    }

    private MyMsgTypes(String identifier, int lenght, ChatColor titleColor, ChatColor textColor)
    {
        this.identifier = identifier;
        this.lenght = lenght;
        this.titleColor = titleColor;
        this.textColor = textColor;
    }

    @Override
    public boolean hasUniqueIdentifier()
    {
        return identifier != null;
    }


    @Override
    public String getUniqueIdentifier()
    {
        return identifier;
    }


    @Override
    public int getLength()
    {
        return lenght;
    }


    @Override
    public ChatColor getTitleColor()
    {
        return titleColor;
    }


    @Override
    public void setTextColor(ChatColor chatColor)
    {
    }


    @Override
    public ChatColor getTextColor()
    {
        return textColor;
    }
}
