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

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Calendar;
import java.util.Random;
import java.util.logging.Logger;

public class ExtraHardMode extends JavaPlugin
{
	//for convenience, a reference to the instance of this plugin
	public static ExtraHardMode instance;
	
	//for logging to the console and logger file
	private static Logger logger;
	
	//this handles the config files (messages and plugin options)
	public DataStore dataStore;
	
	//for computing random chance
	static Random randomNumberGenerator = new Random();
	
	//adds a server log entry
	public static void log (String entry)
	{
		logger.info(entry);
	}
	
	//initializes well...   everything
	public void onEnable()
	{ 	
		long time = System.currentTimeMillis();
		logger = getServer().getLogger();
		this.dataStore = new DataStore();
		
		instance = this;
        logger = instance.getLogger();
		
		Config.load(instance);

		//register for events
		PluginManager pluginManager = this.getServer().getPluginManager();
		
		//player events
		PlayerEventHandler playerEventHandler = new PlayerEventHandler();
		pluginManager.registerEvents(playerEventHandler, this);
		
		//block events
		BlockEventHandler blockEventHandler = new BlockEventHandler();
		pluginManager.registerEvents(blockEventHandler, this);
				
		//entity events
		EntityEventHandler entityEventHandler = new EntityEventHandler();
		pluginManager.registerEvents(entityEventHandler, this);
		
		//FEATURE: monsters spawn in the light under a configurable Y level
		MoreMonstersTask task = new MoreMonstersTask();
		this.getServer().getScheduler().scheduleSyncRepeatingTask(this, task, 120L, 120L);  //TODO every 60 seconds
		time = System.currentTimeMillis() - time;
		log("Took " + time + " milliseconds to initialize");
	}
	
	//handles slash commands
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
	{
		return true;
	}
	
	public void onDisable()
	{
		instance = null;
		HandlerList.unregisterAll(); //unregister all Listeners
		getServer().getScheduler().cancelAllTasks(); //cancel all tasks
	}
	
	//sends a color-coded message to a player
	static void sendMessage(Player player, ChatColor color, Messages messageID, String... args)
	{
		String message = ExtraHardMode.instance.dataStore.getMessage(messageID, args);
		sendMessage(player, color, message);
	}
	
	//sends a color-coded message to a player
	static void sendMessage(Player player, ChatColor color, String message)
	{
		if(player == null)
		{
			ExtraHardMode.log(color + message);
		}
		else
		{
			//FEATURE: don't spam messages
			PlayerData playerData = ExtraHardMode.instance.dataStore.getPlayerData(player.getName());
			long now = Calendar.getInstance().getTimeInMillis();
			if(!message.equals(playerData.lastMessageSent) || now - playerData.lastMessageTimestamp > 30000)
			{
				player.sendMessage(color + message);
				playerData.lastMessageSent = message;
				playerData.lastMessageTimestamp = now;
			}
		}
	}	
	
	static void physicsCheck(Block block, int recursionCount, boolean skipCenterBlock)
	{
		ExtraHardMode.instance.getServer().getScheduler().scheduleSyncDelayedTask(
				ExtraHardMode.instance,
				new BlockPhysicsCheckTask(block, recursionCount),
				5L);
	}
	
	//makes a block subject to gravity
	static void applyPhysics(Block block)
	{
		//grass and mycel become dirt when they fall
		if(block.getType() == Material.GRASS || block.getType() == Material.MYCEL)
		{
			block.setType(Material.DIRT);
		}
		
		//create falling block
		FallingBlock fallingBlock = block.getWorld().spawnFallingBlock(block.getLocation(), block.getTypeId(), block.getData());
		fallingBlock.setDropItem (false);
		
		//remove original block
		block.setType(Material.AIR);
	}
	
	//computes random chance
	static boolean random(int percentChance)
	{
		return randomNumberGenerator.nextInt(101) < percentChance;
	}
	
	boolean plantDies(Block block, byte newDataValue)
	{
		World world = block.getWorld();
		if(!Config.Enabled_Worlds.contains(world.getName()) || !Config.Farming__Weak_Food_Crops__Enable) return false;
		
		//not evaluated until the plant is nearly full grown
		if(newDataValue <= (byte)6) return false;
		
		Material material = block.getType();				
		if( material == Material.CROPS ||
			material == Material.MELON_STEM ||
			material == Material.CARROT ||
			material == Material.PUMPKIN_STEM ||
			material == Material.POTATO )
		{
			int deathProbability = Config.Farming__Weak_Food_Crops__Vegetation_Loss_Percentage;
			
			//plants in the dark always die
			if(block.getLightFromSky() < 10)
			{
				deathProbability = 100 ;
			}
			
			else
			{
			    if (Config.Farming__Weak_Food_Crops__Arid_Infertile_Desserts)
			    {
					Biome biome = block.getBiome();

					//the desert environment is very rough on crops
					if(biome == Biome.DESERT || biome == Biome.DESERT_HILLS)
					{
					}
			    }
				//unwatered crops are more likely to die
				Block belowBlock = block.getRelative(BlockFace.DOWN);
				byte moistureLevel = 0;
				if(belowBlock.getType() == Material.SOIL)
				{
					moistureLevel = belowBlock.getData();
				}
				
				if(moistureLevel == 0)
				{
					deathProbability += 25;
				}
			}
			
			if(ExtraHardMode.random(deathProbability))
			{
				return true;
			}			
		}
		
		return false;
	}
}