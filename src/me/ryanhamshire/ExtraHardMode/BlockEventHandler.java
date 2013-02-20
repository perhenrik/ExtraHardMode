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

import org.bukkit.*;
import org.bukkit.World.Environment;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Torch;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

//event handlers related to blocks
public class BlockEventHandler implements Listener 
{
	//TODO
	private final BlockFace[] blockFaces = new BlockFace[]
			{
			BlockFace.UP,
			BlockFace.DOWN,
			BlockFace.NORTH,
			BlockFace.EAST,
			BlockFace.SOUTH,
			BlockFace.WEST
			};
	//constructor
	public BlockEventHandler()
	{
		//UNECESSARY
	}
	
	//when a player breaks a block...
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onBlockBreak(BlockBreakEvent breakEvent)
	{	
		Block block = breakEvent.getBlock();
		World world = block.getWorld();
		Player player = breakEvent.getPlayer();
		
		if(!Config.Enabled_Worlds.contains(world.getName()) || player.hasPermission(DataStore.bypass_Perm)) return;
		
		//FEATURE: very limited building in the end
		//players are allowed to break only end stone, and only to create a stair up to ground level
		if(Config.Enderdragon__No_Building_In_End && world.getEnvironment() == Environment.THE_END)
		{
			if(block.getType() != Material.ENDER_STONE)
			{
				breakEvent.setCancelled(true);
				ExtraHardMode.sendMessage(player, TextMode.Err, Messages.LimitedEndBuilding);
				return;
			}
			else
			{
				int absoluteDistanceFromBlock = Math.abs(block.getX() - player.getLocation().getBlockX());
				int zdistance = Math.abs(block.getZ() - player.getLocation().getBlockZ());
				if(zdistance > absoluteDistanceFromBlock)
				{
					absoluteDistanceFromBlock = zdistance;
				}
				
				if(block.getY() < player.getLocation().getBlockY() + absoluteDistanceFromBlock)
				{
					breakEvent.setCancelled(true);
					ExtraHardMode.sendMessage(player, TextMode.Err, Messages.LimitedEndBuilding);
					return;
				}				
			}
		}
		
		//FEATURE: stone breaks tools much more quickly
		if(Config.World__Mining__Prevent_Tunneling_To_Encourage_Cave_Exploration)
		{			
			ItemStack inHandStack = player.getItemInHand();			
			
			//if breaking stone with an item in hand and the player does NOT have the bypass permission
			if(	block.getType() == Material.STONE && inHandStack != null)
			{				
				//if not using an iron or diamond pickaxe, don't allow breakage and explain to the player
				Material tool = inHandStack.getType();
				if(tool != Material.IRON_PICKAXE && tool != Material.DIAMOND_PICKAXE)
				{
					ExtraHardMode.sendMessage(player, TextMode.Instr, Messages.StoneMiningHelp);
					breakEvent.setCancelled(true);
					return;
				}
				
				//otherwise, drastically reduce tool durability when breaking stone
				else
				{
					short amount = 0;
					
					if(tool == Material.IRON_PICKAXE)
						amount = 8;
					else
						amount = 22;
					
					inHandStack.setDurability((short)(inHandStack.getDurability() + amount));
				}
			}
			
			//when ore is broken, it softens adjacent stone
			//important to ensure players can reach the ore they break
			if(block.getType().name().endsWith("ORE") || block.getType().name().endsWith("ORES"))
			{
                for (BlockFace face: blockFaces) {
                    Block adjacentBlock = block.getRelative(face);
                    if (adjacentBlock.getType() == Material.STONE)adjacentBlock.setType(Material.COBBLESTONE);
                }
			}
		}

		//FEATURE: trees chop more naturally
		if(block.getType() == Material.LOG && Config.World__Better_Tree_Chopping)
		{
			Block rootBlock = block;
			while(rootBlock.getType() == Material.LOG)
			{
				rootBlock = rootBlock.getRelative(BlockFace.DOWN);
			}

			if(rootBlock.getType() == Material.DIRT || rootBlock.getType() == Material.GRASS)
			{
				Block aboveLog = block.getRelative(BlockFace.UP);
				while(aboveLog.getType() == Material.LOG)
				{
					ExtraHardMode.applyPhysics(aboveLog);
					aboveLog = aboveLog.getRelative(BlockFace.UP);
				}
			}
		}

		//FEATURE: more falling blocks
		ExtraHardMode.physicsCheck(block, 0, true);
		
		//FEATURE: no nether wart farming (always drops exactly 1 nether wart when broken)
		if(Config.Farming__No_Farming_Nether_Wart)
		{
			if(block.getType() == Material.NETHER_WARTS)
			{
				block.getDrops().clear();
				block.getDrops().add(new ItemStack(Material.NETHER_STALK));
			}
		}
		
		//FEATURE: breaking netherrack may start a fire
		if(Config.World__Broken_Netherrack_Catches_Fire_Percent > 0 && block.getType() == Material.NETHERRACK)
		{
			Block underBlock = block.getRelative(BlockFace.DOWN);
			if(underBlock.getType() == Material.NETHERRACK && ExtraHardMode.random(Config.World__Broken_Netherrack_Catches_Fire_Percent))
			{
				breakEvent.setCancelled(true);
				block.setType(Material.FIRE);
			}
		}
	}
	
	//when a player places a block...
	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onBlockPlace(BlockPlaceEvent placeEvent)
	{
		Player player = placeEvent.getPlayer();
		Block block = placeEvent.getBlock();
		World world = block.getWorld();
		
		if(Config.Enabled_Worlds.contains(world.getName()) || player.hasPermission(DataStore.bypass_Perm) || player.getGameMode() == GameMode.CREATIVE) return;
		
		//FEATURE: very limited building in the end
		//players are allowed to break only end stone, and only to create a stair up to ground level
		if(Config.Enderdragon__No_Building_In_End && world.getEnvironment() == Environment.THE_END)
		{
			placeEvent.setCancelled(true);
			ExtraHardMode.sendMessage(player, TextMode.Err, Messages.LimitedEndBuilding);
			return;
		}
		
		//FIX: prevent players from placing ore as an exploit to work around the hardened stone rule
		if(Config.World__Mining__Prevent_Tunneling_To_Encourage_Cave_Exploration && (block.getType().name().endsWith("ORE") || block.getType().name().endsWith("ORES"))) //ORES for redpower
		{
			ArrayList <Block> adjacentBlocks = new ArrayList<Block>();
			for (BlockFace face: blockFaces)
			{
			    adjacentBlocks.add(block.getRelative(face));
			}
				
			for(Block adjacentBlock: adjacentBlocks)
			{
				if(adjacentBlock.getType() == Material.STONE)
				{
					ExtraHardMode.sendMessage(player, TextMode.Err, Messages.NoPlacingOreAgainstStone);
					placeEvent.setCancelled(true);
					return;
				}
			}
		}
			
		//FEATURE: no farming nether wart
		if(block.getType() == Material.NETHER_WARTS && Config.Farming__No_Farming_Nether_Wart)
		{
			placeEvent.setCancelled(true);
			return;
		}
		
		//FEATURE: more falling blocks
		ExtraHardMode.physicsCheck(block, 0, true);
		
		//FEATURE: no standard torches, jack o lanterns, or fire on top of netherrack near diamond level
		if(Config.World__Torches__Torch_Max_Y> 0)
		{
			if (	world.getEnvironment() == Environment.NORMAL && block.getY() < Config.World__Torches__Torch_Max_Y
				&& (    block.getType() == Material.TORCH ||
						block.getType() == Material.JACK_O_LANTERN ||
					   (block.getType() == Material.FIRE && block.getRelative(BlockFace.DOWN).getType() == Material.NETHERRACK)))
				{
					if (Config.World__Play_Sounds__Torch_Fizzing) player.playSound (block.getLocation(), Sound.FIZZ, 1, 20);
					ExtraHardMode.sendMessage(player, TextMode.Instr, Messages.NoTorchesHere);
					placeEvent.setCancelled(true);
					return;
				}
		}
		
		//FEATURE: players can't place blocks from weird angles (using shift to hover over in the air beyond the edge of solid ground)
		//or directly beneath themselves, for that matter
		if(Config.World__Limited_Block_Placement)
		{

			if(	block.getX() == player.getLocation().getBlockX() &&
				block.getZ() == player.getLocation().getBlockZ() &&
				block.getY() <  player.getLocation().getBlockY() )
			{
				ExtraHardMode.sendMessage(player, TextMode.Instr, Messages.RealisticBuilding);
				placeEvent.setCancelled(true);
				return;
			}
			
			Block playerBlock = player.getLocation().getBlock();
			Block underBlock = playerBlock.getRelative(BlockFace.DOWN);
			
			//if standing directly over lava, prevent placement
			if(underBlock.getType() == Material.LAVA || underBlock.getType() == Material.STATIONARY_LAVA)
			{
				ExtraHardMode.sendMessage(player, TextMode.Instr, Messages.RealisticBuilding);
				placeEvent.setCancelled(true);
				return;
			}
			
			//otherwise if hovering over air, check one block lower
			else if(underBlock.getType() == Material.AIR)
			{
				underBlock = underBlock.getRelative(BlockFace.DOWN);
				
				//if over lava or more air, prevent placement
				if((underBlock.getType() == Material.AIR || underBlock.getType() == Material.LAVA || underBlock.getType() == Material.STATIONARY_LAVA)
					&& (!playerBlock.getType().name().contains("STEP") && !playerBlock.getType().name().contains("STAIRS")))
				{
					
					ExtraHardMode.sendMessage(player, TextMode.Instr, Messages.RealisticBuilding);
					placeEvent.setCancelled(true);
					return;
				}
			}
		}
		
		//FEATURE: players can't attach torches to common "soft" blocks
		if(Config.World__Torches__Limited_Torch_Placement && block.getType() == Material.TORCH)
		{
			Torch torch = new Torch(Material.TORCH, block.getData());
			Material attachmentMaterial = block.getRelative(torch.getAttachedFace()).getType();
			
			if(	attachmentMaterial == Material.DIRT ||
				attachmentMaterial == Material.GRASS ||
				attachmentMaterial == Material.LONG_GRASS ||
				attachmentMaterial == Material.SAND)
			{
				placeEvent.setCancelled(true);
				if (Config.World__Play_Sounds__Torch_Fizzing) player.playSound (block.getLocation(), Sound.FIZZ, 1, 20);
				ExtraHardMode.sendMessage(player, TextMode.Instr, Messages.LimitedTorchPlacements);
			}
		}
	}
		
	//when a dispenser dispenses...
	void onBlockDispense(BlockDispenseEvent event)
	{
		//FEATURE: can't move water source blocks
		if(Config.World__Water__Dont_Move_Source_Blocks)
		{
			World world = event.getBlock().getWorld();
			if(!Config.Enabled_Worlds.contains(world.getName())) return;
			
			//only care about water
			if(event.getItem().getType() == Material.WATER_BUCKET)
			{
				//plan to evaporate the water next tick
				Block block;
				Vector velocity = event.getVelocity();
				if(velocity.getX() > 0)
				{
					block = event.getBlock().getLocation().add(1, 0, 0).getBlock();
				}
				else if(velocity.getX() < 0)
				{
					block = event.getBlock().getLocation().add(-1, 0, 0).getBlock();
				}
				else if(velocity.getZ() > 0)
				{
					block = event.getBlock().getLocation().add(0, 0, 1).getBlock();
				}
				else
				{
					block = event.getBlock().getLocation().add(0, 0, -1).getBlock();
				}
				
				EvaporateWaterTask task = new EvaporateWaterTask(block);
				ExtraHardMode.instance.getServer().getScheduler().scheduleSyncDelayedTask(ExtraHardMode.instance, task, 1L);
			}				
		}
	}
	
	//when a piston pushes...
	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onBlockPistonExtend (BlockPistonExtendEvent event)
	{		
		List<Block> blocks = event.getBlocks();
		World world = event.getBlock().getWorld();
		
		//FEATURE: prevent players from circumventing hardened stone rules by placing ore, then pushing the ore next to stone before breaking it
		
		if(!Config.World__Mining__Prevent_Tunneling_To_Encourage_Cave_Exploration|| !Config.Enabled_Worlds.contains(world.getName())) return;
				
		//which blocks are being pushed?
		for(Block block: blocks)
		{
			//if any are ore or stone, don't push
			Material material = block.getType();
			if(material == Material.STONE || material.name().endsWith("ORE") || material.name().endsWith("ORES"))
			{
				event.setCancelled(true);
				return;
			}
		}		
	}
	
	//when a piston pulls...
	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onBlockPistonRetract (BlockPistonRetractEvent event)
	{
		//FEATURE: prevent players from circumventing hardened stone rules by placing ore, then pulling the ore next to stone before breaking it
		
		//we only care about sticky pistons
		if(!event.isSticky()) return;
		
		Block block = event.getRetractLocation().getBlock();
		World world = block.getWorld();
		
		if(!Config.World__Mining__Prevent_Tunneling_To_Encourage_Cave_Exploration|| !Config.Enabled_Worlds.contains(world.getName())) return;
		
		Material material = block.getType();
		if(material == Material.STONE || material.name().endsWith("ORE") || material.name().endsWith("ORES"))
		{
			event.setCancelled(true);
		}
	} 
	
	//when the weather changes...
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onWeatherChange (WeatherChangeEvent event)
	{
		//FEATURE: rainfall breaks exposed torches (exposed to the sky)
		World world = event.getWorld();
		if (Config.Enabled_Worlds.contains(world.getName())) {

			if (!event.toWeatherState()) return;  //if not raining

			//plan to remove torches and cover crops chunk by chunk gradually throughout the rain period
			Chunk[] chunks = world.getLoadedChunks();
			if (chunks.length > 0) {
				int startOffset = ExtraHardMode.randomNumberGenerator.nextInt(chunks.length);
				for (int i = 0; i < chunks.length; i++) {
					Chunk chunk = chunks[(startOffset + i) % chunks.length];

					RemoveExposedTorchesTask task = new RemoveExposedTorchesTask(chunk);
					ExtraHardMode.instance.getServer().getScheduler().scheduleSyncDelayedTask(ExtraHardMode.instance, task, i * 20L);
				}
			}
		}
	} 
	
	//when a block grows...
	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onBlockGrow (BlockGrowEvent event)
	{
		//FEATURE: fewer seeds = shrinking crops.  when a plant grows to its full size, it may be replaced by a dead shrub
		if(ExtraHardMode.instance.plantDies(event.getBlock(), event.getNewState().getData().getData()))
		{
			event.setCancelled(true);
			event.getBlock().setType(Material.LONG_GRASS); //dead shrub
		}
	}

	//when a tree or mushroom grows...
	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onStructureGrow(StructureGrowEvent event)
	{
		World world = event.getWorld();
		Block block = event.getLocation().getBlock();
		if(!Config.Enabled_Worlds.contains(world.getName()) || (event.getPlayer() != null && event.getPlayer().hasPermission(DataStore.bypass_Perm))) return;

		//FEATURE: no big plant growth in deserts
		if(Config.Farming__Weak_Food_Crops__Arid_Infertile_Desserts)
		{
			Biome biome = block.getBiome();
			if(biome == Biome.DESERT || biome == Biome.DESERT_HILLS)
			{
				event.setCancelled(true);
			}
		}
	}
	
	//TODO FINISH FEATURE
	/*//FEATURE Make Cobblestone Generators generate SmoothStone
	@EventHandler
	public void onTransformFromTo (BlockFromToEvent event)
	{
		//the block that triggered the event
	    Block block = event.getBlock();
	    Material bMaterial = block.getType();
	    //the block that will be changed
	    Block toBlock = event.getToBlock();
	    Material toMaterial = toBlock.getType();
	    
	    if(bMaterial.name().contains("LAVA") || bMaterial.name().contains("WATER"))
	    {
	        if(toMaterial == Material.AIR)
	        {
	        	//Example: SourceBlock is Lava, then either water or stationary water can create cobble
	        	Material possibilityOne = (	Material.WATER == bMaterial ||//if
	        								Material.STATIONARY_WATER == bMaterial//or
	        								? Material.LAVA//then set to 
	        								: Material.WATER);//otherwise set to
	        	Material possibilityTwo = (	Material.WATER == bMaterial ||//if
	        								Material.STATIONARY_WATER == bMaterial//or
	        								? Material.STATIONARY_LAVA//then
	        								: Material.STATIONARY_WATER);//otherwise
	        	
	            if(generatesCobble(bMaterial.getId(), toBlock))
	            {
	                //event.setCancelled(true);
	            }
	        }
	    }
	}
	 
	public boolean generatesCobble(int id, Block b)
	{
	    int mirrorID1 = (id == 8 || id == 9 ? 10 : 8);
	    int mirrorID2 = (id == 8 || id == 9 ? 11 : 9);
	    for(BlockFace face : faces)
	    {
	        Block r = b.getRelative(face, 1);
	        ExtraHardMode.instance.getServer().getPlayer("Diemex94").sendMessage(ChatColor.RED + " Face: " + face.name() + " Material = " + r.getType().name());
	        if(r.getTypeId() == mirrorID1 || r.getTypeId() == mirrorID2)
	        {
	            return true;
	        }
	    }
	    return false;
	}*/
}
