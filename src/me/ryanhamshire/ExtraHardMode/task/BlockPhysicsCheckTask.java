package me.ryanhamshire.ExtraHardMode.task;

import me.ryanhamshire.ExtraHardMode.ExtraHardMode;
import me.ryanhamshire.ExtraHardMode.module.PhysicsModule;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class BlockPhysicsCheckTask implements Runnable {

   ExtraHardMode plugin;
   Block block;
   int recursionCount;

   public BlockPhysicsCheckTask(ExtraHardMode plugin, Block block, int recursionCount) {
      this.plugin = plugin;
      this.block = block;
      this.recursionCount = recursionCount;
   }

   @Override
   public void run() {
      PhysicsModule module = plugin.getModuleForClass(PhysicsModule.class);
      block = block.getWorld().getBlockAt(block.getLocation());
      boolean fall = false;
      Material material = block.getType();
      if((block.getRelative(BlockFace.DOWN).getType() == Material.AIR || block.getRelative(BlockFace.DOWN).isLiquid() || block.getRelative(
            BlockFace.DOWN).getType() == Material.TORCH)
            && (material == Material.SAND || material == Material.GRAVEL || plugin.getFallingBlocks().contains(material))) {
         module.applyPhysics(block);
         fall = true;
      }

      if(fall || this.recursionCount == 0) {
         if(recursionCount < 10) {
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
