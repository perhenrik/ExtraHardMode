/*
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package me.ryanhamshire.ExtraHardMode.module;

import me.ryanhamshire.ExtraHardMode.ExtraHardMode;
import me.ryanhamshire.ExtraHardMode.config.messages.MessageConfig;
import me.ryanhamshire.ExtraHardMode.config.messages.MessageNode;
import me.ryanhamshire.ExtraHardMode.service.EHMModule;
import me.ryanhamshire.ExtraHardMode.service.PermissionNode;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.ArrayList;
import java.util.Map;

/**
 * Put all the Utility Stuff here that doesn't fit into the other modules
 */
@SuppressWarnings("SameParameterValue")
public class UtilityModule extends EHMModule
{
    /**
     * Constructor.
     *
     * @param plugin - Plugin instance.
     */
    public UtilityModule(ExtraHardMode plugin)
    {
        super(plugin);
    }

    /**
     * Generates a Firework with random colors/velocity and the given Firework Type
     *
     * @param type The type of firework
     * @return nothing
     */
    public void fireWorkRandomColors(FireworkEffect.Type type, Location location)
    {
        Firework firework = (Firework) location.getWorld().spawnEntity(location, EntityType.FIREWORK);
        FireworkMeta fireworkMeta = firework.getFireworkMeta();

        //Generate the colors
        int rdmInt1 = plugin.getRandom().nextInt(255);
        int rdmInt2 = plugin.getRandom().nextInt(255);
        int rdmInt3 = plugin.getRandom().nextInt(255);
        Color mainColor = Color.fromRGB(rdmInt1, rdmInt2, rdmInt3);

        FireworkEffect fwEffect = FireworkEffect.builder().withColor(mainColor).with(type).build();
        fireworkMeta.addEffect(fwEffect);
        fireworkMeta.setPower(1);
        firework.setFireworkMeta(fireworkMeta);
    }

    /**
     * Returns if Material is a plant that should be affected by the farming Rules
     */
    public boolean isPlant(Material material)
    {
        return material.equals(Material.CROPS)
                || material.equals(Material.POTATO)
                || material.equals(Material.CARROT)
                || material.equals(Material.MELON_STEM)
                || material.equals(Material.PUMPKIN_STEM);
    }

    /**
     * Is the given material a tool, e.g. doesn't stack
     */
    public boolean isTool (Material material)
    {
        return     material.name().endsWith("AXE") //axe & pickaxe
                || material.name().endsWith("SPADE")
                || material.name().endsWith("SWORD")
                || material.name().endsWith("HOE")
                || material.name().endsWith("BUCKET") //water, milk, lava,..
                || material.equals(Material.BOW)
                || material.equals(Material.FISHING_ROD)
                || material.equals(Material.WATCH)
                || material.equals(Material.COMPASS);
    }

    /**
     * is the given material armor
     */
    public boolean isArmor (Material material)
    {
        return     material.name().endsWith("HELMET")
                || material.name().endsWith("CHESTPLATE")
                || material.name().endsWith("LEGGINGS")
                || material.name().endsWith("BOOTS");
    }

    /**
     * Consider this block a natural block for spawning?
     */
    public boolean isNaturalSpawnMaterial (Material material)
    {
        return     material == Material.GRASS
                || material == Material.STONE
                || material == Material.SAND
                || material == Material.GRAVEL
                || material == Material.MOSSY_COBBLESTONE
                || material == Material.OBSIDIAN
                || material == Material.COBBLESTONE
                || material == Material.BEDROCK
                || material == Material.AIR      //Ghast, Bat
                || material == Material.WATER;  //Squid
    }

    /**
     * Is this a natural block for netherspawning?
     */
    //TODO name too long
    public boolean isNaturalNetherSpawnMaterial (Material material)
    {
        return     material == Material.NETHERRACK
                || material == Material.NETHER_BRICK
                || material == Material.SOUL_SAND
                || material == Material.GRAVEL
                || material == Material.AIR;
    }

    /**
     * Is the player currently on a ladder?
     * @param player
     * @return
     */
    public boolean isPlayerOnLadder(Player player)
    {
        return player.getLocation().getBlock().getType().equals(Material.LADDER);
    }

    /**
     * Calculates the weight of the players inventory with the given amount of weight per item
     * @param player
     * @param armorPoints Points per piece of worn armor
     * @param inventoryPoints Points per full stack of one item
     * @param toolPoints Points per tool which doesn't stack
     * @return
     */
    public float inventoryWeight (Player player, float armorPoints, float inventoryPoints, float toolPoints)
    {
        // count worn clothing
        PlayerInventory inventory = player.getInventory();
        float weight = 0F;
        ItemStack[] armor = inventory.getArmorContents();
        for (ItemStack armorPiece : armor)
        {
            if (armorPiece != null || armorPiece.getType() != Material.AIR)
            {
                weight += armorPoints;
            }
        }

        // count contents
        for (ItemStack itemStack : inventory.getContents())
        {
            if (itemStack != null && itemStack.getType() != Material.AIR)
            {
                float addWeight = 0.0F;
                if (isTool(itemStack.getType()))
                {
                    addWeight += toolPoints;
                }
                else
                {
                    //take stackSize into consideration
                    addWeight = inventoryPoints * itemStack.getAmount() / itemStack.getMaxStackSize();
                }
                weight += addWeight;
            }
        }
        return weight;
    }

    /**
     * Counts the number of items of a specific type
     * @param inv to count in
     * @param toCount the Material to count
     * @return the number of items as Integer
     */
    public static int countInvItem (PlayerInventory inv, Material toCount)
    {
        int counter = 0;
        for (ItemStack stack : inv.getContents())
        {
            if (stack != null && stack.getType().equals(toCount))
            {
                counter += stack.getAmount();
            }
        }
        return counter;
    }

    public boolean isSameShape(ArrayList<ItemStack> recipe1, ArrayList<ItemStack> recipe2)
    {
        //compare recipes
        boolean isSame = true;
        for (int i = 0; i < recipe1.size(); i++)
        {
            if (!recipe1.get(i).equals(recipe2.get(i)))
                isSame = false;
        }
        return isSame;
    }

    public boolean isSameRecipe (ShapedRecipe recipe1, ShapedRecipe recipe2)
    {
        boolean isSameResult = recipe1.getResult().getAmount() == recipe2.getResult().getAmount()
                && recipe1.getResult().getType().equals(recipe2.getResult().getType());
        boolean isSameShape = isSameShape(recipeToArrayList(recipe1), recipeToArrayList(recipe2));
        return isSameShape && isSameResult;
    }

    private ArrayList<ItemStack> recipeToArrayList(ShapedRecipe recipe)
    {
        String [] shape = recipe.getShape();
        Map<Character, ItemStack> ingredientMap = recipe.getIngredientMap();

        //Create an ArrayList with the actual recipe based of the shape and ingredientMap
        ArrayList <ItemStack> craftRecipe = new ArrayList <ItemStack>();
        String flatShape = strArrToStr(shape);
        for (int i = 0; i < flatShape.length(); i++)
        {
            char id = flatShape.charAt(i);
            ItemStack itemStack = ingredientMap.get(id);
            //to avoid null pointers, empty slots will be filled with air
            if (itemStack == null) itemStack = new ItemStack(Material.AIR);
            craftRecipe.add(itemStack);
        }
        return craftRecipe;
    }

    public String strArrToStr (String [] arr)
    {
        StringBuilder builder = new StringBuilder();
        for(String s : arr) {
            builder.append(s);
        }
        return builder.toString();
    }

    /**
     * Check the inventory after 1 tick and see how many items have been crafted, then add the amount defined by the multiplier
     */
    public static class addExtraItemsLater implements Runnable
    {
        int amountBefore = 0;
        int amountToAdd = 0;
        Material material;
        PlayerInventory inv = null;

        public addExtraItemsLater (PlayerInventory inventory, int amountBefore, Material toCompare, int amountToAdd)
        {
            this.amountBefore = amountBefore;
            this.amountToAdd = amountToAdd;
            material = toCompare;
            inv = inventory;
        }

        @Override
        public void run()
        {
            int amountAfter = countInvItem(inv, material);
            int amountToAdd = (amountAfter - amountBefore) * (this.amountToAdd);
            inv.addItem(new ItemStack(material, amountToAdd));
        }
    }

    /**
     * Send the player an informative message to explain what he's doing wrong.
     * Play an optional sound aswell
     * <p/>
     * TODO might want to move this out to a module. Yes indeed and move all logging methods and ways to send messages in there.
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
            MessageConfig config = plugin.getModuleForClass(MessageConfig.class);
            plugin.sendMessage(player, config.getString(node));
            if (sound != null)
                player.playSound(player.getLocation(), sound, 1, soundPitch);
        }
    }

    public void notifyPlayer(Player player, MessageNode node, PermissionNode perm)
    {
        notifyPlayer(player, node, perm, null, 0);
    }

    @Override
    public void starting()
    {
    }

    @Override
    public void closing()
    {
    }
}
