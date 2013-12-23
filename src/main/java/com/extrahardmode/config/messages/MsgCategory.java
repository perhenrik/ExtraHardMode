package com.extrahardmode.config.messages;


/**
 * @author Diemex
 */
public enum MsgCategory
{
    /**
     * A message which will only be shown a few times and then won't show anymore at all
     */
    TUTORIAL,
    /**
     * An informative message which will show always but with a timeout
     */
    NOTIFICATION,
    /**
     * A message which get's broadcast to the whole server
     */
    BROADCAST,
    /**
     * Will only be shown once
     */
    ONE_TIME,
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
        this(null, null);
    }


    private MsgCategory(String identifier, MsgCategory owning)
    {
        this.identifier = identifier;
        this.cat = owning;
    }


    private final String identifier;
    private final MsgCategory cat;


    public boolean hasSubcategory()
    {
        return cat != null;
    }


    public MsgCategory getSubcategory()
    {
        return cat;
    }


    public boolean hasUniqueIdentifier()
    {
        return identifier != null;
    }


    public String getUniqueIdentifier()
    {
        return identifier;
    }
}