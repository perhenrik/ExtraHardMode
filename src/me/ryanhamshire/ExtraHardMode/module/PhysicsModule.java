package me.ryanhamshire.ExtraHardMode.module;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;

import me.ryanhamshire.ExtraHardMode.ExtraHardMode;
import me.ryanhamshire.ExtraHardMode.service.EHMModule;
import me.ryanhamshire.ExtraHardMode.task.BlockPhysicsCheckTask;

public class PhysicsModule extends EHMModule {

   public PhysicsModule(ExtraHardMode plugin) {
      super(plugin);
   }

   public void physicsCheck(Block block, int recursionCount, boolean skipCenterBlock) {
      // TODO check if it was scheduled. If not, notify in console.
      plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new BlockPhysicsCheckTask(plugin, block, recursionCount), 5L);
   }

   // makes a block subject to gravity
   public void applyPhysics(Block block) {
      // grass and mycel become dirt when they fall
      if(block.getType() == Material.GRASS || block.getType() == Material.MYCEL) {
         block.setType(Material.DIRT);
      }

      // create falling block
      FallingBlock fallingBlock = block.getWorld().spawnFallingBlock(block.getLocation(), block.getTypeId(), block.getData());
      fallingBlock.setDropItem(true);

      // remove original block
      block.setType(Material.AIR);
   }

   @Override
   public void starting() {
   }

   @Override
   public void closing() {
   }
}
