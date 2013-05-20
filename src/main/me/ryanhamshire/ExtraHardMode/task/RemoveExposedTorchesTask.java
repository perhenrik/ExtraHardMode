
/*
    ExtraHardMode Server Plugin for Minecraft
    Copyright (C) 2012 Ryan Hamshire

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

package me.ryanhamshire.ExtraHardMode.task;

import me.ryanhamshire.ExtraHardMode.ExtraHardMode;
import me.ryanhamshire.ExtraHardMode.config.RootConfig;
import me.ryanhamshire.ExtraHardMode.config.RootNode;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;

/**
 * Task to remove exposed torches.
 */
public class RemoveExposedTorchesTask implements Runnable
{
    /**
     * Plugin instance.
     */
    private ExtraHardMode plugin;
    /**
     * Chunk to iterate over.
     */
    private Chunk chunk;
    /**
     * Config instance
     */
    private RootConfig CFG;

    /**
     * Constructor.
     *
     * @param plugin - Plugin instance.
     * @param chunk  - Target chunk.
     */
    public RemoveExposedTorchesTask(ExtraHardMode plugin, Chunk chunk)
    {
        this.plugin = plugin;
        this.chunk = chunk;
        CFG = this.plugin.getModuleForClass(RootConfig.class);
    }

    @Override
    public void run()
    {
        final boolean rainBreaksTorches = CFG.getBoolean(RootNode.RAIN_BREAKS_TORCHES, this.chunk.getWorld().getName());
        final boolean snowBreaksCrops = CFG.getBoolean(RootNode.SNOW_BREAKS_CROPS, this.chunk.getWorld().getName());

        long time = System.currentTimeMillis();

        if (this.chunk.getWorld().hasStorm())
        {
            for (int x = 0; x < 16; x++)
            {
                for (int z = 0; z < 16; z++)
                {
                    /* Biome is saved on a per column basis */
                    Biome biome = chunk.getBlock(x, z, 1).getBiome();
                    loopDown : for (int y = chunk.getWorld().getMaxHeight() - 1; y > 0; y--)
                    {
                        Block block = chunk.getBlock(x, y, z);
                        Material blockType = block.getType();

                        switch (blockType)
                        {
                            case AIR: /* we continue down until we hit something which isn't AIR */
                                continue loopDown;
                            case TORCH:
                            {
                                if (rainBreaksTorches && biome != Biome.DESERT && biome != Biome.DESERT_HILLS)
                                {
                                    /* Reduce lag by torches lying on the ground */
                                    if (plugin.getRandom().nextInt(5) == 1) {
                                        block.breakNaturally();
                                    }
                                    else {
                                        block.setType(Material.AIR);
                                    }
                                }
                                break loopDown;
                            }
                            case CROPS: case MELON_STEM: case CARROT:case PUMPKIN_STEM: case POTATO: case RED_ROSE: case YELLOW_FLOWER: case LONG_GRASS:
                        {
                            if (snowBreaksCrops)
                            {
                                switch (biome)
                                {
                                    case FROZEN_OCEAN: case FROZEN_RIVER: case ICE_MOUNTAINS: case ICE_PLAINS: case TAIGA: case TAIGA_HILLS:
                                {
                                    if (plugin.getRandom().nextInt(5) == 1)
                                        block.breakNaturally();
                                    block.setType(Material.SNOW);
                                    if (plugin.getRandom().nextBoolean())
                                    {
                                        block.setData((byte) 1);
                                    }
                                    else
                                    {
                                        block.setData((byte) 2);
                                    }
                                }
                                }
                            }
                            break loopDown;
                        }
                            default: /* Anything which isn't AIR will protect torches and Crops */
                            {
                                break loopDown;
                            }
                        }
                    }
                }
            }
        }
    }
}
