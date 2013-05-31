package com.extrahardmode.modules;

import com.extrahardmode.mocks.MockExtraHardMode;
import com.extrahardmode.mocks.MockPlayer;
import com.extrahardmode.mocks.MockPlayerInventory;
import com.extrahardmode.module.UtilityModule;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Max
 */
public class TestInventoryWeight
{
    UtilityModule module;

    Player      myPlayer        = new MockPlayer("Diemex94").get();
    ItemStack   ironboots       = new ItemStack(Material.IRON_BOOTS),
                ironLeggings    = new ItemStack(Material.IRON_LEGGINGS),
                ironChest       = new ItemStack(Material.IRON_CHESTPLATE),
                ironHelmet      = new ItemStack(Material.IRON_HELMET);

    ItemStack [] oneArmor       = {ironboots, null, null, null};
    ItemStack [] twoArmor       = {null, ironLeggings, null, ironHelmet};
    ItemStack [] fullArmor      = {ironboots, ironLeggings, ironChest, ironHelmet};
    ItemStack [] emptyArmor     = new ItemStack [4];
    ItemStack [] emptyInv       = new ItemStack[ 4 * 9 ];

    @Before
    public void prepare()
    {
        module = new UtilityModule(new MockExtraHardMode().get());
    }

    /**
     * Test if armor gets calculated correctly
     */
    @Test
    public void armorTest()
    {
        new MockPlayerInventory (myPlayer, emptyArmor, emptyInv);
        assertEquals("Empty inventory = no weight ",0, module.inventoryWeight(myPlayer, 15, 5, 5), 0);

        new MockPlayerInventory (myPlayer, oneArmor, emptyInv);
        assertEquals("One piece ",15, module.inventoryWeight(myPlayer, 15, 5, 5), 0);

        new MockPlayerInventory (myPlayer, twoArmor, emptyInv);
        assertEquals("Two pieces ",12, module.inventoryWeight(myPlayer, 6, 5, 5), 0);

        new MockPlayerInventory (myPlayer, fullArmor, emptyInv);
        assertEquals("Full armor ",8, module.inventoryWeight(myPlayer, 2, 5, 5), 0);
    }

    /**
     * loaded inventory
     */
    @Test
    public void fullInvtest()
    {
        ItemStack [] inv = new ItemStack[ 4 * 9];

        inv[0] = new ItemStack(Material.DIAMOND_SWORD);
        inv[5] = new ItemStack(Material.DIAMOND_SPADE);
        inv[7] = new ItemStack(Material.FLINT_AND_STEEL);

        inv[9] = new ItemStack(Material.BOOK, 32);
        inv[12]= new ItemStack(Material.ARROW, 24);
        inv[13]= new ItemStack(Material.BREAD, 64);

        /*
         * Armor:5, Tools: 1, Stack: 64
         * Armor: 4 * 5 = 20 (full)
         * Tools: 3 * 1 = 3
         * Other: StackWeight (64) / MaxStackSize * Count
         * Book:  32 (64 / 64 * 32)
         * Arrow: 24
         * Bread: 64
         * Total: 143 */

        new MockPlayerInventory(myPlayer, fullArmor, inv);

        assertEquals("See comment for calculation", 143, module.inventoryWeight(myPlayer, 5, 64, 1), 0);
    }
}
