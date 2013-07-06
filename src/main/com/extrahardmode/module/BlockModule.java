/*
 * This file is part of
 * ExtraHardMode Server Plugin for Minecraft
 *
 * Copyright (C) 2012 Ryan Hamshire
 * Copyright (C) 2013 Diemex
 *
 * ExtraHardMode is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ExtraHardMode is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero Public License
 * along with ExtraHardMode.  If not, see <http://www.gnu.org/licenses/>.
 */


package com.extrahardmode.module;


import com.extrahardmode.ExtraHardMode;
import com.extrahardmode.config.RootConfig;
import com.extrahardmode.config.RootNode;
import com.extrahardmode.service.EHMModule;
import com.extrahardmode.task.BlockPhysicsCheckTask;
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
    /**
     * Marks a block/location for whatever reason... currently used by waterbucket restrictions
     */
    private final String MARK = "ExtraHardMode.Mark";

    private final RootConfig CFG;


    /**
     * Constructor.
     *
     * @param plugin
     *         - plugin instance.
     */
    public BlockModule(ExtraHardMode plugin)
    {
        super(plugin);
        CFG = plugin.getModuleForClass(RootConfig.class);
    }


    /**
     * Schedule the physics task
     *
     * @param block
     *         - Target block.
     * @param recursionCount
     *         - Number of times to execute.
     * @param forceCheck
     *         - Whether to force adjacent blocks to be checked for the first iteration
     * @param wait
     *         - how many ticks to wait before the next task, mainly to prevent crashes when FallingBlocks collide
     */
    public void physicsCheck(Block block, int recursionCount, boolean forceCheck, int wait)
    {
        int id = plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new BlockPhysicsCheckTask(plugin, block, recursionCount, forceCheck), wait);
        // check if it was scheduled. If not, notify in console.
        if (id == -1)
        {
            plugin.getLogger().severe("Failed schedule BlockPhysicsCheck task!");
        }
    }


    /**
     * Makes one single block subject to gravity
     *
     * @param block
     *         - Block to apply physics to.
     * @param damageEntities
     *         - if Entities should be damaged
     *
     * @return the UUID of this FallingBlock
     */
    public UUID applyPhysics(Block block, boolean damageEntities)
    {
        /* Spawning Falling Blocks with type = AIR crashes the Minecraft client */
        if (block.getType() == Material.AIR)
            return null;

        // grass and mycel become dirt when they fall
        if ((block.getType() == Material.GRASS || block.getType() == Material.MYCEL) && CFG.getBoolean(RootNode.MORE_FALLING_BLOCKS_TURN_TO_DIRT, block.getWorld().getName()))
        {
            block.setType(Material.DIRT);
        }

        FallingBlock fallingBlock = block.getWorld().spawnFallingBlock(block.getLocation(), block.getTypeId(), block.getData());
        fallingBlock.setDropItem(false);
        // remove original block
        block.setType(Material.AIR);

        if (damageEntities)
        {
            EntityHelper.markForProcessing(plugin, fallingBlock);
        }


        return fallingBlock.getUniqueId();
    }


    /**
     * Mark this block for whatever reason
     * <p/>
     * remember to remove the mark as block metadata persists
     *
     * @param block
     *         to mark
     */
    public void mark(Block block)
    {
        block.setMetadata(MARK, new FixedMetadataValue(plugin, true));
    }


    /**
     * Removes Metadata from the block
     *
     * @param block
     *         to remove the metadata from
     */
    public void removeMark(Block block)
    {
        block.removeMetadata(MARK, plugin);
    }


    /**
     * Has this block been marked?
     *
     * @param block
     *         to check
     *
     * @return if it has been marked
     */
    public boolean isMarked(Block block)
    {
        return block.getMetadata(MARK).size() > 0;
    }


    /**
     * Check if the given plant at the block should die.
     *
     * @param block
     *         - Block to check.
     * @param newDataValue
     *         - Data value to replace.
     *
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
                    } else
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
     *
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
     * @param loc
     *         Center of the search area
     * @param height
     *         how many blocks up to check
     * @param radius
     *         of the search (cubic search radius)
     * @param type
     *         of Material to search for
     *
     * @return all the Block with the given Type in the specified radius
     */
    public Block[] getBlocksInArea(Location loc, int height, int radius, Material type)
    {
        List<Block> blocks = new ArrayList<Block>();
        //Height
        for (int y = 0; y < height; y++)
        {
            for (int x = -radius; x <= radius; x++)
            {
                for (int z = -radius; z <= radius; z++)
                {
                    Block checkBlock = loc.getBlock().getRelative(x, y, z);
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
     *
     * @param mat
     *         to check
     *
     * @return boolean
     */
    public boolean breaksFallingBlock(Material mat)
    {
        return mat.name().contains("TORCH") //redstone torches aswell
                || mat.name().endsWith("STEP")  //all SLABS (steps)
                || mat.name().contains("RAIL")  //all RAILS
                || mat == Material.RED_ROSE
                || mat == Material.YELLOW_FLOWER
                || mat == Material.BROWN_MUSHROOM
                || mat == Material.SIGN
                || mat == Material.TRAP_DOOR
                || mat == Material.DEAD_BUSH
                || mat == Material.LONG_GRASS
                || mat == Material.WEB;
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
    public static boolean isTool(Material material)
    {
        return material.name().endsWith("AXE") //axe & pickaxe
                || material.name().endsWith("SPADE")
                || material.name().endsWith("SWORD")
                || material.name().endsWith("HOE")
                || material.name().endsWith("BUCKET") //water, milk, lava,..
                || material.equals(Material.BOW)
                || material.equals(Material.FISHING_ROD)
                || material.equals(Material.WATCH)
                || material.equals(Material.COMPASS)
                || material.equals(Material.FLINT_AND_STEEL);
    }


    /**
     * is the given material armor
     */
    public boolean isArmor(Material material)
    {
        return material.name().endsWith("HELMET")
                || material.name().endsWith("CHESTPLATE")
                || material.name().endsWith("LEGGINGS")
                || material.name().endsWith("BOOTS");
    }


    /**
     * Consider this block a natural block for spawning?
     */
    public boolean isNaturalSpawnMaterial(Material material)
    {
        return material == Material.GRASS
                || material == Material.DIRT
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
    public boolean isNaturalNetherSpawn(Material material)
    {
        return material == Material.NETHERRACK
                || material == Material.NETHER_BRICK
                || material == Material.SOUL_SAND
                || material == Material.GRAVEL
                || material == Material.AIR;
    }


    @Override
    public void starting()
    {/*ignored*/}


    @Override
    public void closing()
    {/*ignored*/}
}
