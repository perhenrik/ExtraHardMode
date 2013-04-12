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
import me.ryanhamshire.ExtraHardMode.config.RootConfig;
import me.ryanhamshire.ExtraHardMode.config.RootNode;
import me.ryanhamshire.ExtraHardMode.service.EHMModule;
import me.ryanhamshire.ExtraHardMode.task.BlockPhysicsCheckTask;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.FallingBlock;

/**
 * Module that manages blocks and physics logic.
 */
public class BlockModule extends EHMModule
{

    private RootConfig CFG;

    /**
     * Constructor.
     *
     * @param plugin - plugin instance.
     */
    public BlockModule(ExtraHardMode plugin)
    {
        super(plugin);
        CFG = plugin.getModuleForClass(RootConfig.class);
    }

    /**
     * Schedule the physics task
     *
     * @param block           - Target block.
     * @param recursionCount  - Number of times to execute.
     * @param skipCenterBlock - Whether to skip the center block or not.
     */
    public void physicsCheck(Block block, int recursionCount, boolean skipCenterBlock)
    {
        int id = plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new BlockPhysicsCheckTask(plugin, block, recursionCount), 5L);
        // check if it was scheduled. If not, notify in console.
        if (id == -1)
        {
            plugin.getLogger().severe("Failed schedule BlockPhysicsCheck task!");
        }
    }

    /**
     * Makes a block subject to gravity
     *
     * @param block - Block to apply physics to.
     */
    public void applyPhysics(Block block)
    {
        // grass and mycel become dirt when they fall
        if ((block.getType() == Material.GRASS || block.getType() == Material.MYCEL) && CFG.getBoolean(RootNode.MORE_FALLING_BLOCKS_TURN_TO_DIRT, block.getWorld().getName()))
        {
            block.setType(Material.DIRT);
        }

        // falling block
        FallingBlock fallingBlock = block.getWorld().spawnFallingBlock(block.getLocation(), block.getTypeId(), block.getData());
        fallingBlock.setDropItem(true);

        // remove original block
        block.setType(Material.AIR);
    }

    /**
     * Check if the given plant at the block should die.
     *
     * @param block        - Block to check.
     * @param newDataValue - Data value to replace.
     * @return True if plant should die, else false.
     */
    public boolean plantDies(Block block, byte newDataValue)
    {
        World world = block.getWorld();

        final boolean weakFoodCropsEnabled = CFG.getBoolean(RootNode.WEAK_FOOD_CROPS, world.getName());
        final int lossRate = CFG.getInt(RootNode.WEAK_FOOD_CROPS_LOSS_RATE, world.getName());
        final boolean aridDesertsEnabled = CFG.getBoolean(RootNode.ARID_DESSERTS, world.getName());


        if (weakFoodCropsEnabled)
        {
            // not evaluated until the plant is nearly full grown
            if (newDataValue > (byte) 6)
            {
                Material material = block.getType();
                if (material == Material.CROPS || material == Material.CARROT || material == Material.POTATO)
                {
                    int deathProbability = lossRate;

                    // plants in the dark always die
                    if (block.getLightFromSky() < 10)
                    {
                        deathProbability = 100;
                    }
                    else
                    {
                        Biome biome = block.getBiome();

                        // the desert environment is very rough on crops
                        if ((biome == Biome.DESERT || biome == Biome.DESERT_HILLS) && aridDesertsEnabled)
                        {
                            deathProbability += 50;
                        }

                        // unwatered crops are more likely to die
                        Block belowBlock = block.getRelative(BlockFace.DOWN);
                        byte moistureLevel = 0;
                        if (belowBlock.getType() == Material.SOIL)
                        {
                            moistureLevel = belowBlock.getData();
                        }

                        if (moistureLevel == 0)
                        {
                            deathProbability += 25;
                        }
                    }

                    if (plugin.random(deathProbability))
                    {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Get all "touching" BlockFaces including top/bottom
     */
    public BlockFace[] getTouchingFaces()
    {
      return new BlockFace[]{
              BlockFace.WEST,
              BlockFace.NORTH,
              BlockFace.EAST,
              BlockFace.SOUTH,
              BlockFace.UP,
              BlockFace.DOWN
      };
    }

    /**
     * All horizontal Blockfaces including diagonal onea
     * @return
     */
    public BlockFace[] getHorizontalAdjacentFaces()
    {
        return new BlockFace[]{
                BlockFace.WEST,
                BlockFace.NORTH_WEST,
                BlockFace.NORTH,
                BlockFace.NORTH_EAST,
                BlockFace.EAST,
                BlockFace.SOUTH_EAST,
                BlockFace.SOUTH,
                BlockFace.SOUTH_WEST
        };
    }
    @Override
    public void starting(){/*ignored*/}
    @Override
    public void closing(){/*ignored*/}
}
