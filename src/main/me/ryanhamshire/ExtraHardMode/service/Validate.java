package me.ryanhamshire.ExtraHardMode.service;

import me.ryanhamshire.ExtraHardMode.ExtraHardMode;
import org.bukkit.World;

import java.util.List;

/**
 * Validate the Bounds of a ConfigNode
 */
public class Validate
{
    private final ExtraHardMode plugin;

    public Validate (ExtraHardMode plugin)
    {
       this.plugin = plugin;
    }
    /**
     * Validate Y coordinate limit for the given configuration option against the
     * list of enabled worlds.
     *
     * @param node   - Root node to validate.
     * @param worlds - List of worlds to check against.
     * @param value  - Integer to validate
     *
     * @return either the original value or adjusted if out of bounds
     */
    public int yCoordinate (ConfigNode node, List<String> worlds, Integer value)
    {
        //Either 255 or the height of the first world loaded is the default max height
        int maxHeight = plugin.getServer().getWorlds().size() > 0 ? plugin.getServer().getWorlds().get(0).getMaxHeight() : 255;
        if (value < 0)
        {
            plugin.getLogger().warning(plugin.getTag() + " Y coordinate for " + node.getPath() + " cannot be less than 0.");
            value = 0;
        }
        for (String worldName : worlds)
        {
            World world = plugin.getServer().getWorld(worldName);
            //if world is not loaded (yet)
            if (world != null) maxHeight = world.getMaxHeight();
            if (value > maxHeight)
            {
                plugin.getLogger().warning(plugin.getTag() + " Y coordinate for " + node.getPath() + " is greater than the max height for world " + worldName);
                value = maxHeight;
            }
        }
        return value;
    }

    /**
     * Validate percentage (0-100) value for given configuration option.
     *
     * @param node  - Root node to validate.
     * @param value - Integer to validate
     *
     * @return either the original value or adjusted if out of bounds
     */
    public int percentage (ConfigNode node, Integer value)
    {
        if (value < 0)
        {
            plugin.getLogger().warning(plugin.getTag() + " Percentage for " + node.getPath() + " cannot be less than 0.");
            value = 0;
        }
        else if (value > 100)
        {
            plugin.getLogger().warning(plugin.getTag() + " Percentage for " + node.getPath() + " cannot be greater than 100.");
            value = 100;
        }
        return value;
    }

    /**
     * Validates a configOption with custom bounds
     *
     * @param node   the configNode
     * @param minVal the minimum value the config is allowed to have
     * @param maxVal the maximum value for the config, if == minVal then it doesn't get checked
     * @param value  - Integer to validate
     *
     * @return either the original value or adjusted if out of bounds
     */
    public int customBounds (ConfigNode node, int minVal, int maxVal, Integer value)
    {
        if (value < minVal)
        {
            plugin.getLogger().warning(plugin.getTag() + " Value for " + node.getPath() + " cannot be smaller than 0.");
            value = 0;
        }
        else if (minVal < maxVal && value > maxVal)
        {
            plugin.getLogger().warning(plugin.getTag() + " Value for " + node.getPath() + " cannot be greater than " + maxVal);
            value = maxVal;
        }
        return value;
    }
}
