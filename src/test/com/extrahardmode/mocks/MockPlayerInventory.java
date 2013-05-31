package com.extrahardmode.mocks;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Mocks the inventory of a Player and the getInventory of the Player
 *
 * @author Max
 */
public class MockPlayerInventory
{
    /**
     * Inventory instance
     */
    private PlayerInventory inv = mock(PlayerInventory.class);
    /**
     * InventoryHolder
     */
    private Player player;
    /**
     * Armor worn by player
     */
    ItemStack [] armorContents;
    /**
     * Rest of the inventory
     */
    ItemStack [] inventory;

    /**
     * Constructor
     *
     * @param player name of the Player to whom this inventory belongs to
     */
    public MockPlayerInventory(Player player, ItemStack [] armorContents, ItemStack [] inventoryContents)
    {
        this.player = player;
        this.armorContents = armorContents;
        this.inventory = inventoryContents;

        when(this.player.getInventory()).thenReturn(inv);
        when(inv.getHolder()).thenReturn(this.player);
        when(inv.getArmorContents()).thenReturn(this.armorContents);
        when(inv.getContents()).thenReturn(this.inventory);
    }

    /**
     * Get the actual Inventory with it's mocked methods
     */
    public PlayerInventory get()
    {
        return inv;
    }
}
