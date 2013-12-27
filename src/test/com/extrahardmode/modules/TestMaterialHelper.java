package com.extrahardmode.modules;


import com.extrahardmode.module.MaterialHelper;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Diemex
 */
public class TestMaterialHelper
{
    @Test
    public void print_singleItems()
    {
        assertEquals("1 bookshelf", MaterialHelper.print(new ItemStack(Material.BOOKSHELF)));
        assertEquals("1 dirt", MaterialHelper.print(new ItemStack(Material.DIRT)));
    }


    @Test
    public void print_multiItems()
    {
        assertEquals("2 stones", MaterialHelper.print(new ItemStack(Material.STONE, 2)));
    }


    @Test
    public void print_singleSpecial()
    {
        assertEquals("1 repeater", MaterialHelper.print(new ItemStack(Material.DIODE, 1)));
        assertEquals("1 ironhorsearmor", MaterialHelper.print(new ItemStack(Material.IRON_BARDING, 1)));
    }


    @Test
    public void print_multiSpecial()
    {
        assertEquals("2 repeaters", MaterialHelper.print(new ItemStack(Material.DIODE, 2)));
        assertEquals("2 ironhorsearmors", MaterialHelper.print(new ItemStack(Material.IRON_BARDING, 2)));
    }
}
