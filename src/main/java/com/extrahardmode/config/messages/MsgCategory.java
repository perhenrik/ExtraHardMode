package com.extrahardmode.config.messages;


import org.bukkit.ChatColor;

/**
 * @author Diemex
 */
public enum MsgCategory
{
    /**
     * A message which will only be shown a few times and then won't show anymore at all
     */
    TUTORIAL(1000),
    /**
     * An informative message which will show always but with a timeout
     */
    NOTIFICATION(300),
    /**
     * A message which get's broadcast to the whole server
     */
    BROADCAST(500),
    /**
     * Will only be shown once
     */
    ONE_TIME(500),
    /**
     * Currently disabled
     */
    DISABLED,
    /**
     * Message will be displayed when the inventory has been opened and shows the weight of the inventory
     */
    WEIGHT_MSG("extrahardmode.player.weight", NOTIFICATION);


    private MsgCategory()
    {
        this(null, 0, null, null);
    }


    private MsgCategory(int lenght)
    {
        this(null, lenght, null, null);
    }


    private MsgCategory(String identifier, MsgCategory owning)
    {
        this(identifier, 0,owning.getTitleColor(), owning.getTextColor());
    }


    private MsgCategory(int lenght, ChatColor titleColor, ChatColor textColor)
    {
        this(null, lenght, titleColor, textColor);
    }


    private MsgCategory(String identifier, int lenght, ChatColor titleColor, ChatColor textColor)
    {
        this.identifier = identifier;
        this.lenght = lenght;
        this.titleColor = titleColor;
        this.textColor = textColor;
    }


    private final String identifier;
    private final int lenght;
    private final ChatColor titleColor;
    private final ChatColor textColor;


    public boolean hasUniqueIdentifier()
    {
        return identifier != null;
    }


    public String getUniqueIdentifier()
    {
        return identifier;
    }


    public int getLength()
    {
        return lenght;
    }


    public ChatColor getTitleColor()
    {
        return titleColor;
    }


    public ChatColor getTextColor()
    {
        return textColor;
    }
}