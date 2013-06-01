package com.extrahardmode.events;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

/**
 * This Event is called when EHM determines if a tool is supposed to be affected by the hardened stone code.
 * It will be called everytime when a Player breaks stone also if the tool wouldn't be able to break the Block.
 *
 * This allows for plugins to step over the toolcheck and add their own tools. (bukkitforge etc.)
 * @author Max
 */

public class EhmHardenedStoneEvent extends Event
{
    /**
     * Player who broke the Stone
     */
    Player player;
    /**
     * The Tool which broke the block
     */
    private final ItemStack tool;
    /**
     * The amount of blocks this tool can mine
     */
    private int numOfBlocks;

    /**
     * Constructor
     *
     * @param toolId the tool which broke the Stone
     * @param numOfBlocks amount of blocks tool can mine
     */
    public EhmHardenedStoneEvent(Player player, ItemStack tool, int numOfBlocks)
    {
        this.player = player;
        this.tool = tool;
        this.numOfBlocks = numOfBlocks;
    }

    /**
     * Get the tool the Player used to break the stone
     *
     * @return ItemStack Player is holding
     */
    public ItemStack getTool()
    {
        return tool;
    }

    /**
     * Get the id
     *
     * @return id of the Tool which broke the Stone
     */
    public int getToolId()
    {
        return tool.getTypeId();
    }

    /**
     * Get the Material of the tool
     *
     * @return the Material of a tool as Bukkit enum
     */
    public Material getToolMaterial()
    {
        return tool.getType();
    }

    /**
     * Set the amount of blocks the tool can mine
     * Just set this higher than 0 and ehm will allow the tool to break blocks
     */
    public void setNumOfBlocks(int blocks)
    {
        this.numOfBlocks = blocks;
    }

    /**
     * Get the amount of stone blocks this tool would be able to break.
     * <p>
     * Will be 0 if the tool isn't able to break stone by default.
     */
    public int getNumOfBlocks()
    {
        return numOfBlocks;
    }

    private static final HandlerList handlers = new HandlerList();

    public HandlerList getHandlers()
    {
        return handlers;
    }

    public static HandlerList getHandlerList()
    {
        return handlers;
    }
}