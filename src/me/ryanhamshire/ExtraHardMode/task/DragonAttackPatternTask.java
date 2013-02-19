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

import java.util.ArrayList;

import me.ryanhamshire.ExtraHardMode.ExtraHardMode;
import me.ryanhamshire.ExtraHardMode.config.RootConfig;
import me.ryanhamshire.ExtraHardMode.config.RootNode;

import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class DragonAttackPatternTask implements Runnable {
   private ExtraHardMode plugin;
   private Player player;
   private LivingEntity dragon;
   private ArrayList<Player> playersFightingDragon;

   public DragonAttackPatternTask(ExtraHardMode plugin, LivingEntity dragon, Player player, ArrayList<Player> playersFightingDragon) {
      this.plugin = plugin;
      this.dragon = dragon;
      this.player = player;
      this.playersFightingDragon = playersFightingDragon;
   }

   @Override
   public void run() {
      if(this.dragon.isDead())
         return;

      World world = this.dragon.getWorld();
      RootConfig config = plugin.getModuleForClass(RootConfig.class);

      // if the player has been defeated
      if(!this.player.isOnline() || world != this.player.getWorld() || this.player.isDead()) {
         // announce the combat result
         this.playersFightingDragon.remove(this.player);
         if(config.getBoolean(RootNode.ENDER_DRAGON_COMBAT_ANNOUNCEMENTS) && !this.player.isDead()) {
            plugin.getServer().broadcastMessage(this.player.getName() + " has been defeated by the dragon!");
         }

         // restore some of the dragon's health
         int newHealth = (int) (this.dragon.getHealth() + this.dragon.getMaxHealth() * .25);
         if(newHealth > this.dragon.getMaxHealth()) {
            this.dragon.setHealth(this.dragon.getMaxHealth());
         } else {
            this.dragon.setHealth(newHealth);
         }

         return;
      }

      for(int i = 0; i < 3; i++) {
         DragonAttackTask task = new DragonAttackTask(plugin, this.dragon, this.player);
         plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, task, 20L * i + (plugin.getRandom().nextInt(20)));
      }

      plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, this, 20L * 30);
   }
}
