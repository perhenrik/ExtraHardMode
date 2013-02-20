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
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Calendar;

public class MoreMonstersTask implements Runnable
{

    //static is bad....
	private static ArrayList<SimpleEntry<Player, Location>> previousLocations;
	
	@Override
	public void run()
	{
		if(MoreMonstersTask.previousLocations == null)
		{
			MoreMonstersTask.previousLocations = new ArrayList<SimpleEntry<Player, Location>>();
		}
			
		//spawn monsters from the last pass
		for(int i = 0; i < MoreMonstersTask.previousLocations.size(); i++)
		{
			SimpleEntry<Player, Location> entry = MoreMonstersTask.previousLocations.get(i);
			Player player = entry.getKey();
			Location location = entry.getValue();
			Chunk chunk = location.getChunk();
			World world = location.getWorld();
			
			try
			{
				//chunk must be loaded, player must not be close, and there must be no other players in the chunk
				if(location.getChunk().isLoaded() && player.isOnline() /*TODO&& location.distanceSquared(player.getLocation()) > 150*/)
				{
					boolean playerInChunk = false;
					Entity [] entities = chunk.getEntities();
					for(Entity entity: entities)
					{
						/*if(entity.getType() == EntityType.PLAYER)
						{
							playerInChunk = true;
							break;
						}*/
					}
					
					if(!playerInChunk)
					{
						//spawn random monster(s)
						if(world.getEnvironment() == Environment.NORMAL)
						{
							int random = ExtraHardMode.randomNumberGenerator.nextInt();
							EntityType monsterType;
							int typeMultiplier = 1;
							
							//decide which kind and how many
							if(random < 30)  //silverfish are most common
							{
								monsterType = EntityType.SILVERFISH;
								typeMultiplier = 2;  //twice as many if silverfish
							}
							else if(random < 47)
							{
								monsterType = EntityType.SKELETON;
							}
							else if(random < 64)
							{
								monsterType = EntityType.ZOMBIE;
							}
							else if(random < 81)
							{
								monsterType = EntityType.CREEPER;
							}
							else
							{
								monsterType = EntityType.SPIDER;
							}
							
							int totalToSpawn = typeMultiplier;
							for(int j = 0; j < totalToSpawn; j++)
							{
								world.spawnEntity(location, monsterType);
								//TODO
								DateFormat format = new SimpleDateFormat("HH:mm:ss");
								Calendar cal = Calendar.getInstance();
								player.sendMessage("[" + format.format(cal.getTime()) + "] "+ ChatColor.GREEN + "Spawned " + monsterType.getName() + " on " + location.getBlock().getRelative(BlockFace.DOWN, 1).getType().name() + ChatColor.AQUA + " at X:" + location.getBlockX() + " Y: " + location.getY() + " Z: " + location.getZ() + ChatColor.BLUE + " - Distance: " + player.getLocation().distance(location));
								
							}
						}
						
						else if(world.getEnvironment() == Environment.NETHER)
						{
							int random = ExtraHardMode.randomNumberGenerator.nextInt();
							
							if(random < 80)
							{
								PigZombie zombie = (PigZombie) world.spawnEntity(location, EntityType.PIG_ZOMBIE);
								zombie.setAnger(Integer.MAX_VALUE);
							}
							else
							{
								world.spawnEntity(location, EntityType.BLAZE);
							}
						}
					}
				}
			}
			catch(IllegalArgumentException ignored) { }  //in case the player is in a different world from the saved location
		}
		
		//plan for the next pass
		MoreMonstersTask.previousLocations.clear();
		for(Player player: ExtraHardMode.instance.getServer().getOnlinePlayers())
		{
			Location location = player.getLocation();
			Block playerBlock = location.getBlock();
			World world = player.getWorld();

        if (Config.Enabled_Worlds.contains(player.getWorld().getName())
            && player.hasPermission(DataStore.bypass_Perm)
            && player.getGameMode() == GameMode.SURVIVAL)
        {
            //Only spawn monsters in normal world. End is crowded with enderman and nether is too extreme anyway, add config later
            if (world.getEnvironment() == Environment.NORMAL
                    && ( location.getY() > Config.General_Monster_Rules__Monsters_Spawn_In_Light_Max_Y
                    ||   location.getBlock().getLightFromSky() > 0 ))
            {
                //the playerBlock should always be air, but if the player stands on a slab he actually is in the slab
                if (playerBlock.getType().equals(Material.AIR))
                {
                    for (int i = 0; i <= 3; i++)
                    {
                        Location checkUnder = location.subtract(0,1,0);
                        Block checkUnderBlock = checkUnder.getBlock();
                        if (checkUnderBlock.getType() != Material.AIR)
                        {
                            location = checkUnder;
                            playerBlock = location.getBlock();
                            //the playerBlock is now the block where the monster should spawn on, next up: verify block
                            break;
                        }
                    }
                }
                //no spawning on steps, stairs and transparent blocks
                if ( playerBlock.getType().name().endsWith("STEP")
                   ||playerBlock.getType().name().endsWith("STAIRS")
                   ||playerBlock.getType().isTransparent()
                   ||playerBlock.getType().isOccluding()
                   ||playerBlock.getType().equals(Material.AIR))
                   {
                    //don't spawn here
                    return;
                   }

                //Once we are here the block is safe to spawn on
                MoreMonstersTask.previousLocations.add(new SimpleEntry<Player, Location>(player, location));

                }
            }
        }
    }
}

