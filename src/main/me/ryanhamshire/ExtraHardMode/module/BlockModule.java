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
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.FallingBlock;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
     * @return the UUID of this FallingBlock
     */
    public UUID applyPhysics(Block block)
    {
        return applyPhysics(block, false);
    }

    /**
     * Makes a block subject to gravity
     *
     * @param block - Block to apply physics to.
     * @param damageEntities - if Entities should be damaged
     * @return the UUID of this FallingBlock
     */
    public UUID applyPhysics (Block block , boolean damageEntities)
    {
        // grass and mycel become dirt when they fall
        if ((block.getType() == Material.GRASS || block.getType() == Material.MYCEL) && CFG.getBoolean(RootNode.MORE_FALLING_BLOCKS_TURN_TO_DIRT, block.getWorld().getName()))
        {
            block.setType(Material.DIRT);
        }

        // falling block
        FallingBlock fallingBlock = block.getWorld().spawnFallingBlock(block.getLocation(), block.getTypeId(), block.getData());
        fallingBlock.setDropItem(false);
        fallingBlock.setMetadata("key", new FixedMetadataValue(plugin, true));

        // remove original block
        block.setType(Material.AIR);

        return fallingBlock.getUniqueId();
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
     * @return Blockfaces[]
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

    /**
     * Get all the blocks in a specific area centered around the Location passed in
     *
     * @param loc Center of the search area
     * @param height how many blocks up to check
     * @param radius of the search (cubic search radius)
     * @param type of Material to search for
     *
     * @return all the Block with the given Type in the specified radius
     */
    public Block[] getBlocksInArea (Location loc, int height, int radius, Material type)
    {
        List<Block> blocks = new ArrayList<Block>();
        //Height
        for (int y = 0; y < height; y++)
        {
            for (int x = -radius; x < radius; x++)
            {
                for (int z = -radius; z < radius; z++)
                {
                    Block checkBlock = loc.getBlock().getRelative(x,y,z);
                    if (checkBlock.getType().equals(type))
                    {
                        blocks.add(checkBlock);
                    }
                }
            }
        }
        return blocks.toArray(new Block[blocks.size()]);
    }

    /**
     * Will a FallingBlock which lands on this Material break and drop to the ground?
     * @param mat to check
     * @return boolean
     */
    public boolean breaksFallingBlock (Material mat)
    {
        return      mat.name().contains("TORCH") //redstone torches aswell
                ||  mat.name().endsWith("STEP")  //all SLABS (steps)
                ||  mat.name().contains("RAIL")  //all RAILS
                ||  mat == Material.RED_ROSE
                ||  mat == Material.YELLOW_FLOWER
                ||  mat == Material.BROWN_MUSHROOM
                ||  mat == Material.SIGN
                ||  mat == Material.TRAP_DOOR
                ||  mat == Material.DEAD_BUSH
                ||  mat == Material.LONG_GRASS
                ||  mat == Material.WEB;
    }

    @Override
    public void starting(){/*ignored*/}
    @Override
    public void closing(){/*ignored*/}
}
