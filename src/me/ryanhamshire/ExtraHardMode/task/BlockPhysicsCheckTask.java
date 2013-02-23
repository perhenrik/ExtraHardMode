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
package me.ryanhamshire.ExtraHardMode.task;

import me.ryanhamshire.ExtraHardMode.ExtraHardMode;
import me.ryanhamshire.ExtraHardMode.module.BlockModule;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

/**
 * Called to apply physics to a block and its neighbors if necessary.
 */
public class BlockPhysicsCheckTask implements Runnable
{

    /**
     * Plugin instance.
     */
    private ExtraHardMode plugin;

    /**
     * Target block.
     */
    private Block block;

    /**
     * Recursion count.
     */
    private int recursionCount;

    /**
     * Constructor.
     *
     * @param plugin         - Plugin instance.
     * @param block          - Target block for task.
     * @param recursionCount - Recursion count for task.
     */
    public BlockPhysicsCheckTask(ExtraHardMode plugin, Block block, int recursionCount)
    {
        this.plugin = plugin;
        this.block = block;
        this.recursionCount = recursionCount;
    }

    @Override
    public void run()
    {
        BlockModule module = plugin.getModuleForClass(BlockModule.class);
        block = block.getWorld().getBlockAt(block.getLocation());
        boolean fall = false;
        Material material = block.getType();
        if ((block.getRelative(BlockFace.DOWN).getType() == Material.AIR || block.getRelative(BlockFace.DOWN).isLiquid() || block.getRelative(
                BlockFace.DOWN).getType() == Material.TORCH)
                && (material == Material.SAND || material == Material.GRAVEL || module.getFallingBlocks().contains(material)))
        {
            module.applyPhysics(block);
            fall = true;
        }

        if (fall || this.recursionCount == 0)
        {
            if (recursionCount < 10)
            {
                Block neighbor = block.getRelative(BlockFace.UP);
                module.physicsCheck(neighbor, recursionCount + 1, false);

                neighbor = block.getRelative(BlockFace.DOWN);
                module.physicsCheck(neighbor, recursionCount + 1, false);

                neighbor = block.getRelative(BlockFace.EAST);
                module.physicsCheck(neighbor, recursionCount + 1, false);

                neighbor = block.getRelative(BlockFace.WEST);
                module.physicsCheck(neighbor, recursionCount + 1, false);

                neighbor = block.getRelative(BlockFace.NORTH);
                module.physicsCheck(neighbor, recursionCount + 1, false);

                neighbor = block.getRelative(BlockFace.SOUTH);
                module.physicsCheck(neighbor, recursionCount + 1, false);
            }
        }
    }

}
