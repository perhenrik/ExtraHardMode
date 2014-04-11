package com.extrahardmode.module;


import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Mainly just to have nice output for printing strings
 *
 * @author Diemex
 */
public class MaterialHelper
{
    public static String print(ItemStack stack)
    {
        StringBuilder output = new StringBuilder();

        output.append(stack.getAmount());
        output.append(' ');
        switch (stack.getType())
        {
            case MYCEL:
                output.append("mycelium");
                break;
            case SULPHUR:
                output.append("gunpowder");
                break;
            case DIODE:
                output.append("repeater");
                break;
            case NETHER_STALK:
                output.append("netherwart");
                break;
            //Barding = horse armor
            /*case IRON_BARDING:
                output.append("ironhorsearmor");
                break;
            case GOLD_BARDING:
                output.append("goldhorsearmor");
                break;
            case DIAMOND_BARDING:
                output.append("diamondhorsearmor");
                break;*/
            default:
                output.append(toReadableString(stack.getType()));
        }

        if (stack.getAmount() > 1)
        {
            switch (stack.getType())
            {
                case AIR:
                case DIRT:
                case WOOD:
                case WATER:
                case STATIONARY_WATER:
                case LAVA:
                case STATIONARY_LAVA:
                case SAND:
                case GRAVEL:
                case LEAVES:
                case GLASS:
                case WOOL:
                case TNT:
                case OBSIDIAN:
                case BIRCH_WOOD_STAIRS:
                case BRICK_STAIRS:
                case COBBLESTONE_STAIRS:
                case JUNGLE_WOOD_STAIRS:
                case NETHER_BRICK_STAIRS:
                case WOOD_STAIRS:
                case SPRUCE_WOOD_STAIRS:
                case SANDSTONE_STAIRS:
                case QUARTZ_STAIRS:
                case SMOOTH_STAIRS:
                case CROPS:
                case SOIL:
                case RAILS:
                case SNOW:
                case ICE:
                case CLAY:
                case NETHERRACK:
                case SOUL_SAND:
                case THIN_GLASS:
                case MYCEL:
                case NETHER_WARTS:
                case COCOA:
                case SULPHUR:
                case STRING:
                case SEEDS:
                case BREAD:
                case WHEAT:
                case CHAINMAIL_LEGGINGS:
                case DIAMOND_LEGGINGS:
                case GOLD_LEGGINGS:
                case IRON_LEGGINGS:
                case LEATHER_LEGGINGS:
                case CHAINMAIL_BOOTS:
                case DIAMOND_BOOTS:
                case GOLD_BOOTS:
                case IRON_BOOTS:
                case LEATHER_BOOTS:
                case FLINT:
                case GLOWSTONE_DUST:
                case RAW_FISH:
                case COOKED_FISH:
                case SUGAR:
                case SHEARS:
                case MELON_SEEDS:
                case PUMPKIN_SEEDS:
                case RAW_BEEF:
                case COOKED_BEEF:
                case ROTTEN_FLESH:
                case NETHER_STALK:
                case BLAZE_POWDER:
                case QUARTZ:
                    break; //Don't append an 's'
                case LONG_GRASS:
                case TORCH:
                case DEAD_BUSH:
                case WORKBENCH:
                case CACTUS: //cacti is also possible but this is simpler
                case JUKEBOX:
                    output.append("es");
                    break;
                case BOOKSHELF:
                    return stack.getAmount() + " bookshelves";
                default:
                    output.append('s');
            }
        }
        return output.toString();
    }


    private static String toReadableString(Material mat)
    {
        return mat.name().toLowerCase().replace('_', ' ');
    }
}
