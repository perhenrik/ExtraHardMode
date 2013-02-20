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

package me.ryanhamshire.ExtraHardMode;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

class PlayerEventHandler implements Listener 
{

	//FEATURE: respawning players start without full health or food
	@EventHandler(ignoreCancelled = true)
	public void onPlayerRespawn(PlayerRespawnEvent respawnEvent)
	{
		Player player = respawnEvent.getPlayer();
		World world = respawnEvent.getPlayer().getWorld();
		if(!Config.Enabled_Worlds.contains(world.getName()) || player.hasPermission(DataStore.bypass_Perm)) return;
		
		SetPlayerHealthAndFoodTask task = new SetPlayerHealthAndFoodTask(player, Config.Player__Respawn_Health, Config.Player__Respawn_Food_Level);
		ExtraHardMode.instance.getServer().getScheduler().scheduleSyncDelayedTask(ExtraHardMode.instance, task, 10L);  //half-second delay		
	}
	
	//when a player interacts with the world
	@EventHandler(priority = EventPriority.LOWEST)
	void onPlayerInteract(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();
		World world = event.getPlayer().getWorld();
		if(!Config.Enabled_Worlds.contains(world.getName()) || player.hasPermission(DataStore.bypass_Perm)) return;
		
		Action action = event.getAction();
		
		//FEATURE: bonemeal doesn't work on mushrooms
		if(Config.Farming__No_Bonemeal_On_Mushrooms && action == Action.RIGHT_CLICK_BLOCK)
		{
			Block block = event.getClickedBlock();
			if(block.getType() == Material.RED_MUSHROOM || block.getType() == Material.BROWN_MUSHROOM)
			{
				//what's the player holding?
				Material materialInHand = player.getItemInHand().getType();
				
				//if bonemeal, cancel the event
				if(materialInHand == Material.INK_SACK)  //TODO bukkit bug labels bone meal as ink sack
				{
					event.setCancelled(true);
				}
			}
		}
		
		//FEATURE: seed reduction.  some plants die even when a player uses bonemeal.
		if(Config.Farming__Weak_Food_Crops__Enable && action == Action.RIGHT_CLICK_BLOCK)
		{
			Block block = event.getClickedBlock();
			
			Material materialInHand = player.getItemInHand().getType();
			if(materialInHand == Material.INK_SACK && ExtraHardMode.instance.plantDies(block, Byte.MAX_VALUE))
			{
				event.setCancelled(true);
				block.setType(Material.LONG_GRASS);  //dead shrub
			}
		}
		
		//FEATURE: putting out fire up close catches the player on fire
		Block block = event.getClickedBlock();
		if(Config.Player__Extinguishing_Fire_Ignites_Players && block != null && block.getType() != Material.AIR)
		{
			if(block.getRelative(event.getBlockFace()).getType() == Material.FIRE)
			{
				player.setFireTicks(100);  //20L ~ 1 seconds; 100L ~ 5 seconds 
			}
		}
	}		
	
	//when a player fills a bucket...
	@EventHandler(priority = EventPriority.LOW)
	void onPlayerFillBucket(PlayerBucketFillEvent event)
	{
		//FEATURE: can't move water source blocks
		if(Config.World__Water__Dont_Move_Source_Blocks)
		{
			Player player = event.getPlayer();
			World world = event.getPlayer().getWorld();
			if(!Config.Enabled_Worlds.contains(world.getName()) || player.hasPermission(DataStore.bypass_Perm)) return;
			
			//only care about stationary (source) water 
			Block block = event.getBlockClicked();
			if(block.getType() == Material.STATIONARY_WATER)
			{
				//cancel the event so that the water doesn't get removed
				event.setCancelled(true);
				
				//fill the player's bucket anyway
				//(beware, player may have a stack of empty buckets, and filled buckets DON'T stack)
				int extraBuckets = player.getItemInHand().getAmount() - 1; 
				player.getItemInHand().setType(Material.WATER_BUCKET);
				player.getItemInHand().setAmount(1);
				if(extraBuckets > 0)
				{
					player.getInventory().addItem(new ItemStack(Material.BUCKET, extraBuckets));
				}
				
				//send the player data so that his client doesn't incorrectly show the water as missing
				player.sendBlockChange(block.getLocation(), block.getTypeId(), block.getData());
			}
		}
	}
	
	//when a player empties a bucket...
	@EventHandler(priority = EventPriority.NORMAL)
	void onPlayerEmptyBucket(PlayerBucketEmptyEvent event)
	{
		//FEATURE: can't move water source blocks
		if(Config.World__Water__Dont_Move_Source_Blocks)
		{
			Player player = event.getPlayer();
			World world = event.getPlayer().getWorld();
			if(!Config.Enabled_Worlds.contains(world.getName()) || player.hasPermission(DataStore.bypass_Perm)) return;
			
			//only care about water buckets
			if(player.getItemInHand().getType() == Material.WATER_BUCKET)
			{
				//plan to change this block into a non-source block on the next tick
				Block block = event.getBlockClicked().getRelative(event.getBlockFace());
				EvaporateWaterTask task = new EvaporateWaterTask(block);
				ExtraHardMode.instance.getServer().getScheduler().scheduleSyncDelayedTask(ExtraHardMode.instance, task, 15L);
			}
		}
	}
	
	//when a player dumps a bucket...
	//block use of buckets within other players' claims
	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onPlayerBucketEmpty (PlayerBucketEmptyEvent bucketEvent)
	{
		Player player = bucketEvent.getPlayer();
		World world = player.getWorld();
		
		if(!Config.Enabled_Worlds.contains(world.getName()) || player.hasPermission(DataStore.bypass_Perm)) return;
		
		//FEATURE: very limited building in the end
		//players are allowed to break only end stone, and only to create a stair up to ground level
		if(Config.Enderdragon__No_Building_In_End && world.getEnvironment() == Environment.THE_END)
		{
			bucketEvent.setCancelled(true);
			ExtraHardMode.sendMessage(player, TextMode.Err, Messages.LimitedEndBuilding);
		}
	}
	
	//when a player changes worlds...
	@EventHandler(priority = EventPriority.MONITOR)
	void onPlayerChangeWorld(PlayerChangedWorldEvent event)
	{
		World world = event.getFrom();
		
		if(!Config.Enabled_Worlds.contains(world.getName())) return;
		
		//FEATURE: respawn the ender dragon when the last player leaves the end
		if(world.getEnvironment() != Environment.THE_END) return;
		
		if(world.getPlayers().size() > 0) return;
		
		//look for an ender dragon
		EnderDragon enderDragon = null;
		for(Entity entity : world.getEntities())
		{
			if(entity instanceof EnderDragon)
			{
				enderDragon = (EnderDragon)entity;
				break;
			}
			
			//clean up any summoned minions
			if(entity.getType() == EntityType.ZOMBIE || entity.getType() == EntityType.BLAZE || entity.getType() == EntityType.VILLAGER)
			{
				entity.remove();
			}
		}
		
		//if he's there, full health
		if(enderDragon != null)
		{
			enderDragon.setHealth(enderDragon.getMaxHealth());
		}
		
		//otherwise, spawn one
		else
		{
			world.spawnEntity(new Location(world, 0, world.getMaxHeight() - 1, 0), EntityType.ENDER_DRAGON);
		}		
	}

	//Bypass permission can be set to not default to ops
	@EventHandler (priority = EventPriority.LOW)
	public void onPlayerLogin (PlayerLoginEvent event)
	{
		if (event.getPlayer().isOp() && Config.Plugin__Ops_Bypass_By_Default)
			event.getPlayer().addAttachment(ExtraHardMode.instance, DataStore.bypass_Perm, true);
	}

	//when a player moves...
	@EventHandler(priority = EventPriority.NORMAL)
	void onPlayerMove(PlayerMoveEvent event)
	{
		//FEATURE: no swimming while heavy
		if(!Config.World__Water__No_Swimming_In_Armor) return;

		//only care about moving up
		Location from = event.getFrom();
		Location to = event.getTo();
		if(to.getY() <= from.getY()) return;

		//only when in water
		Block fromBlock = from.getBlock();
		if(!fromBlock.isLiquid()) return;

		Block toBlock = to.getBlock();
		if(!toBlock.isLiquid()) return;

		//only when in deep water
		Block underFromBlock = fromBlock.getRelative(BlockFace.DOWN).getRelative(BlockFace.DOWN);
		if(!underFromBlock.isLiquid()) return;

		//only enabled worlds, and players without bypass permission
		Player player = event.getPlayer();
		World world = player.getWorld();
		if(!Config.Enabled_Worlds.contains(world.getName()) || player.hasPermission("extrahardmode.bypass")) return;

		PlayerData playerData = ExtraHardMode.instance.dataStore.getPlayerData(player.getName());

		//if no cached value, calculate
		if(playerData.cachedWeightStatus == null)
		{
			//count worn clothing (counts double)
			PlayerInventory inventory = player.getInventory();
			int weight = 0;
			ItemStack [] armor = inventory.getArmorContents();
			for(ItemStack armorPiece : armor)
			{
				if(armorPiece != null && armorPiece.getType() != Material.AIR)
				{
					weight += 2;
				}
			}

			//count contents
			for(ItemStack itemStack : inventory.getContents())
			{
				if(itemStack != null && itemStack.getType() != Material.AIR)
				{
					weight++;
					if(weight > 18)
					{
						break;
					}
				}
			}

			playerData.cachedWeightStatus = weight > 18;
		}

		//if too heavy, not allowed to swim
		if(playerData.cachedWeightStatus == true)
		{
			event.setCancelled(true);
			ExtraHardMode.sendMessage(player, TextMode.Warn, Messages.NoSwimmingInArmor);
		}
	}

	//when a player drops an item
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	void onPlayerDropItem(PlayerDropItemEvent event)
	{
		//FEATURE: players can't swim when they're carrying a lot of weight
		Player player = event.getPlayer();
		PlayerData playerData = ExtraHardMode.instance.dataStore.getPlayerData(player.getName());
		playerData.cachedWeightStatus = null;
	}

	//when a player picks up an item
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	void onPlayerPickupItem(PlayerPickupItemEvent event)
	{
		//FEATURE: players can't swim when they're carrying a lot of weight
		Player player = event.getPlayer();
		PlayerData playerData = ExtraHardMode.instance.dataStore.getPlayerData(player.getName());
		playerData.cachedWeightStatus = null;
	}

	//when a player interacts with an inventory
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	void onPlayerInventoryClick(InventoryClickEvent event)
	{
		//FEATURE: players can't swim when they're carrying a lot of weight
		HumanEntity humanEntity = event.getWhoClicked();
		if(humanEntity instanceof Player)
		{
			Player player = (Player)humanEntity;
			PlayerData playerData = ExtraHardMode.instance.dataStore.getPlayerData(player.getName());
			playerData.cachedWeightStatus = null;
		}
	}

}
