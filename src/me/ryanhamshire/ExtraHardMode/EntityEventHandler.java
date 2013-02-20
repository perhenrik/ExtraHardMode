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
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

//handles events related to entities
class EntityEventHandler implements Listener
{
	//when there's an explosion...
	@EventHandler(priority = EventPriority.NORMAL)
	public void onExplosion(EntityExplodeEvent event)
	{
		World world = event.getLocation().getWorld();
		if(!Config.Enabled_Worlds.contains(world.getName())) return;
		
		Entity entity = event.getEntity();
		
		//FEATURE: bigger TNT booms, all explosions have 100% block yield
		if(Config.World__Better_Tnt)
		{
			event.setYield(1);
			
			if(entity != null && entity.getType() == EntityType.PRIMED_TNT && !Config.Work_Around_Explosions_Bugs)
			{
				//create more explosions nearby
				long serverTime = world.getFullTime();
				int random1 = (int)(serverTime + entity.getLocation().getBlockZ()) % 8;
				int random2 = (int)(serverTime + entity.getLocation().getBlockX()) % 8;
				
				Location [] locations = new Location [4];
				
				locations[0] = entity.getLocation().add(random1, 1, random2);
				locations[1] = entity.getLocation().add(-random2, 0, random1 / 2);
				locations[2] = entity.getLocation().add(-random1 / 2, -1, -random2);
				locations[3] = entity.getLocation().add(random1 / 2, 0, -random2 / 2);
				
				for(int i = 0; i < locations.length; i++)
				{
					CreateExplosionTask task = new CreateExplosionTask(locations[i], 6F);
					ExtraHardMode.instance.getServer().getScheduler().scheduleSyncDelayedTask(ExtraHardMode.instance, task, 3L * (i + 1));
				}
			}
		}
		
		//FEATURE: ender dragon fireballs may summon minions and/or set fires
		if(entity != null && entity.getType() == EntityType.FIREBALL)
		{
			Fireball fireball = (Fireball)entity;
			Entity spawnedMonster = null;
			if(fireball.getShooter() != null && fireball.getShooter().getType() == EntityType.ENDER_DRAGON)
			{
				int random = ExtraHardMode.randomNumberGenerator.nextInt(100);
				if(random < 40)
				{				
					spawnedMonster = entity.getWorld().spawnEntity(entity.getLocation(), EntityType.BLAZE);
					
					for(int x1 = -2; x1 <= 2; x1++)
					{
						for(int z1 = -2; z1 <= 2; z1++)
						{
							for(int y1 = 2; y1 >= -2; y1--)
							{
								Block block = fireball.getLocation().add(x1, y1, z1).getBlock();
								Material underType = block.getRelative(BlockFace.DOWN).getType();
								if(block.getType() == Material.AIR && underType != Material.AIR && underType != Material.FIRE)
								{
									block.setType(Material.FIRE);
								}
							}
						}
					}
					
					Location location = fireball.getLocation().add(0, 1, 0);
					for(int i = 0; i < 10; i++)
					{
						FallingBlock fire = world.spawnFallingBlock(location, Material.FIRE, (byte)0);
						Vector velocity = Vector.getRandom();
						if(velocity.getY() < 0)
						{
							velocity.setY(velocity.getY() * -1);
						}
						if(ExtraHardMode.randomNumberGenerator.nextBoolean())
						{
							velocity.setZ(velocity.getZ() * -1);
						}
						if(ExtraHardMode.randomNumberGenerator.nextBoolean())
						{
							velocity.setX(velocity.getX() * -1);
						}						
						fire.setVelocity(velocity);						
					}
				}				
				
				else if(random < 70)
				{
					for(int i = 0; i < 2; i++)
					{
						spawnedMonster = entity.getWorld().spawnEntity(entity.getLocation(), EntityType.ZOMBIE);
						EntityEventHandler.markLootLess((LivingEntity)spawnedMonster);
						Zombie zombie = (Zombie)spawnedMonster; 
						zombie.setVillager(true);
					}					
				}
				else
				{
					spawnedMonster = entity.getWorld().spawnEntity(entity.getLocation(), EntityType.ENDERMAN);
				}
			}
			
			if(spawnedMonster != null)
			{
				EntityEventHandler.markLootLess((LivingEntity)spawnedMonster);
			}
		}
		
		//FEATURE: in hardened stone mode, TNT only softens stone to cobble
		if(Config.World__Mining__Prevent_Tunneling_To_Encourage_Cave_Exploration)
		{
			List<Block> blocks = event.blockList();
			for(int i = 0; i < blocks.size(); i++)
			{
				Block block = blocks.get(i);
				if(block.getType() == Material.STONE)
				{
					block.setType(Material.COBBLESTONE);
					blocks.remove(i--);					
				}
				
				//FEATURE: more falling blocks
				ExtraHardMode.physicsCheck(block, 0, true);					
			}
		}
		
		//FEATURE: more powerful ghast fireballs
		if(entity != null && entity instanceof Fireball && !Config.Work_Around_Explosions_Bugs)
		{
			Fireball fireball = (Fireball)entity;
			if(fireball.getShooter() != null && fireball.getShooter().getType() == EntityType.GHAST)
			{
				event.setCancelled(true);
				entity.getWorld().createExplosion(entity.getLocation(), 4F, true);  //same as vanilla TNT, plus fire
			}
		}
		
		//FEATURE: bigger creeper explosions (for more-frequent cave-ins)
		if(entity != null && entity instanceof Creeper && !Config.Work_Around_Explosions_Bugs)
		{
			event.setCancelled(true);
			entity.getWorld().createExplosion(entity.getLocation(), 3F, false);  //same as vanilla TNT
		}
	}
	
	//when a splash potion breaks...
	@EventHandler(priority = EventPriority.LOW)
	public void onPotionSplash(PotionSplashEvent event)
	{
		ThrownPotion potion = event.getPotion();
		Location location = potion.getLocation();
		World world = location.getWorld();
		if(!Config.Enabled_Worlds.contains(world.getName())) return;
		
		//FEATURE: enhanced witches.  they throw wolf spawner and teleport potions as well as poison potions
		if (potion.getShooter() == null) return; //fired from a dispenser
		LivingEntity shooter = potion.getShooter();
		if(shooter.getType() == EntityType.WITCH)
		{
			Witch witch = (Witch)shooter;
			
			int random = ExtraHardMode.randomNumberGenerator.nextInt(100);
			
			boolean makeExplosion = false;
			
			//30% summon zombie
			if(random < 30)
			{
				event.setCancelled(true);
				
				Entity [] entities = location.getChunk().getEntities();
				boolean zombieNearby = false;
				for(Entity entity: entities)
				{
					if(entity.getType() == EntityType.ZOMBIE)
					{
						Zombie zombie = (Zombie)entity;
						if(zombie.isVillager() && zombie.isBaby())
						{
							zombieNearby = true;
							break;
						}							
					}
				}
					
				if(!zombieNearby)
				{
					Zombie zombie = (Zombie)location.getWorld().spawnEntity(location, EntityType.ZOMBIE);
					zombie.setVillager(true);
					zombie.setBaby(true);
					if(zombie.getTarget() != null)
					{
						zombie.setTarget(witch.getTarget());
					}						
					
					markLootLess(zombie);
				}
				else
				{
					makeExplosion = true;
				}
			}
			
			//30% teleport
			else if (random < 60)
			{				
				event.setCancelled(true);
				witch.teleport(location);
			}
			
			//30% explosion
			else if(random < 90)
			{
				event.setCancelled(true);
				makeExplosion = true;
			}
				
			//otherwise poison potion (selective target)
			else
			{
				Collection<LivingEntity> targets = event.getAffectedEntities();
				for (LivingEntity target : targets) {
					if (target.getType() != EntityType.PLAYER) {
						event.setIntensity(target, 0);
					}
				}
			}
			
			//if explosive potion, direct damage to players in the area
			if(makeExplosion)
			{				
				//explosion just for show, no damage
				location.getWorld().createExplosion(location, 0F);
				
				Collection<LivingEntity> targets = event.getAffectedEntities();
				for (LivingEntity target : targets) {
					if (target.getType() == EntityType.PLAYER) {
						target.damage(3);
					}
				}
			}
		}
		
		
		
	}
	
	//when a creature spawns...
	@EventHandler(priority = EventPriority.LOW)
	public void onEntitySpawn(CreatureSpawnEvent event)
	{
		Location location = event.getLocation();
		World world = location.getWorld();
		if(!Config.Enabled_Worlds.contains(world.getName())) return;
		
		//avoid infinite loops
		if(event.getSpawnReason() == SpawnReason.CUSTOM) return;
		
		LivingEntity entity = event.getEntity();
		
		EntityType entityType = entity.getType();
		
		//FEATURE: inhibited monster grinders/farms
		if(Config.General_Monster_Rules__Inhibit_Monster_Grinders)
		{
			SpawnReason reason = event.getSpawnReason();
			
			//spawners and spawn eggs always spawn a monster, but the monster doesn't drop any loot
			if(reason == SpawnReason.SPAWNER && (Config.Blazes__Bonus_Nether_Spawn_Percent > 0 || !(entity instanceof Blaze)))
			{
				EntityEventHandler.markLootLess(entity);
			}
			
			//otherwise, consider environment to stop monsters from spawning in non-natural places
			else if((reason == SpawnReason.NATURAL || reason == SpawnReason.VILLAGE_INVASION) && entity instanceof Monster)
			{
				Environment environment = location.getWorld().getEnvironment();
				
				Material underBlockType = location.getBlock().getRelative(BlockFace.DOWN).getType();
				if(environment == Environment.NORMAL)
				{
					if(	underBlockType != Material.GRASS && 
						underBlockType != Material.STONE && 
						underBlockType != Material.SAND && 
						underBlockType != Material.GRAVEL && 
						underBlockType != Material.MOSSY_COBBLESTONE &&
						underBlockType != Material.OBSIDIAN &&
						underBlockType != Material.COBBLESTONE &&
						underBlockType != Material.BEDROCK &&
						underBlockType != Material.AIR &&  //bats
						underBlockType != Material.WATER)  //squid
					{
							event.setCancelled(true);
							return;
					}
				}
				else if(environment == Environment.NETHER)
				{
					if(	underBlockType != Material.NETHERRACK && 
						underBlockType != Material.NETHER_BRICK && 
						underBlockType != Material.SOUL_SAND &&
						underBlockType != Material.AIR )  //ghasts
					{
							event.setCancelled(true);
							return;
					}
				}
				else
				{
					if(	underBlockType != Material.ENDER_STONE && 
						underBlockType != Material.OBSIDIAN && 
						underBlockType != Material.AIR )  //ender dragon
					{
							event.setCancelled(true);
							return;
					}
				}
			}
		}
		
		//FEATURE: charged creeper spawns
		if(entityType == EntityType.CREEPER)
		{
			if(ExtraHardMode.random(Config.Creepers__Charged_Spawn_Percent))
			{
				((Creeper)entity).setPowered(true);
			}				
		}
		
		//FEATURE: more witches above ground (on grass)
		if(entityType == EntityType.ZOMBIE && world.getEnvironment() == Environment.NORMAL && entity.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == Material.GRASS)
		{
			if(ExtraHardMode.random(Config.Witches__Bonus_Spawn_Percent))
			{
				event.setCancelled(true);
				entityType = EntityType.WITCH;
				world.spawnEntity(location, entityType);
			}
		}
		
		//FEATURE: more spiders underground
		if(entityType == EntityType.ZOMBIE && world.getEnvironment() == Environment.NORMAL && location.getBlockY() < world.getSeaLevel() - 5)
		{
			if(ExtraHardMode.random(Config.Spiders__Bonus_Underground_Spawn_Percent))
			{
				event.setCancelled(true);
				entityType = EntityType.SPIDER;
				world.spawnEntity(location, entityType);
			}
		}
		
		//FEATURE: blazes near bedrock
		else if(entityType == EntityType.SKELETON && world.getEnvironment() == Environment.NORMAL && location.getBlockY() < 20)
		{
			if(ExtraHardMode.random(Config.Blazes__Near_Bedrock_Spawn_Percent))
			{
				event.setCancelled(true);
				entityType = EntityType.BLAZE;
				world.spawnEntity(location, entityType);
			}
		}
		
		//FEATURE: more blazes
		else if(entityType == EntityType.PIG_ZOMBIE)
		{
			if(ExtraHardMode.random(Config.Blazes__Bonus_Nether_Spawn_Percent))
			{
				event.setCancelled(true);
				entityType = EntityType.BLAZE;
				
				//FEATURE: magma cubes spawn with blazes
				if(ExtraHardMode.random(Config.MagmaCubes__Spawn_A_MagmaCube_With_A_Blaze_Percent))
				{
					MagmaCube cube = (MagmaCube)(world.spawnEntity(location, EntityType.MAGMA_CUBE));
					cube.setSize(1);
				}
				world.spawnEntity(location, entityType);
			}
		}
				
		//FEATURE: extra monster spawns underground
		if(Config.General_Monster_Rules__More_Monsters__Max_Y> 0)
		{
			if(	world.getEnvironment() == Environment.NORMAL &&
				event.getLocation().getBlockY() < Config.General_Monster_Rules__More_Monsters__Max_Y &&
				entity instanceof Monster &&
				entityType != null)
			{
				for(int i = 1; i < Config.General_Monster_Rules__More_Monsters__Multiplier; i++)
				{
					Entity newEntity = world.spawnEntity(event.getLocation(), entityType);
					/*TODO DateFormat format = new SimpleDateFormat("HH:mm:ss");
					Calendar cal = Calendar.getInstance();
					ExtraHardMode.instance.getServer().broadcastMessage("[" + format.format(cal.getTime()) + "] "+ ChatColor.GREEN + "Spawned " + entityType.getName() + " on " + location.getBlock().getRelative(BlockFace.DOWN, 1).getType().name() + ChatColor.AQUA + " at X:" + location.getBlockX() + " Y: " + location.getY() + " Z: " + location.getZ());*/
					if(EntityEventHandler.isLootLess(entity))
					{
						EntityEventHandler.markLootLess((LivingEntity)newEntity);
					}
				}
			}
		}
		
		//FEATURE: always-angry pig zombies
		if(Config.PigZombie__Always_Angry)
		{
			if(entity instanceof PigZombie)
			{
				PigZombie pigZombie = (PigZombie)entity;
				pigZombie.setAnger(Integer.MAX_VALUE);
			}
		}
	}
	
	//when an entity shoots a bow...
	@EventHandler
	public void onShootProjectile(ProjectileLaunchEvent event)
	{
		Location location = event.getEntity().getLocation();
		World world = location.getWorld();
		EntityType entityType = event.getEntityType();
		
		if(!Config.Enabled_Worlds.contains(world.getName())) return;
		
		if(event.getEntity() == null) return;
		
		//FEATURE: skeletons sometimes release silverfish to attack their targets
		if(entityType != EntityType.ARROW) return;
		
		Arrow arrow = (Arrow)event.getEntity();
		
		LivingEntity shooter = arrow.getShooter();
		if(shooter != null && shooter.getType() == EntityType.SKELETON && ExtraHardMode.random(Config.Skeletons__Shoot_Silverfish_Percent))
		{
			Skeleton skeleton = (Skeleton)shooter;
			
			//cancel arrow fire
			event.setCancelled(true);
			
			//replace with silverfish, quarter velocity of arrow, wants to attack same target as skeleton
			Creature silverFish = (Creature)world.spawnEntity(skeleton.getLocation().add(0, 1.5, 0), EntityType.SILVERFISH);
			silverFish.setVelocity(arrow.getVelocity().multiply(.25));
			silverFish.setTarget(skeleton.getTarget());
			EntityEventHandler.markLootLess(silverFish);  //this silverfish doesn't drop loot
		}
	}

	//when a chunk loads...
	@EventHandler
	public void onChunkLoad(ChunkLoadEvent event)
	{
		Chunk chunk = event.getChunk();
		World world = chunk.getWorld();
		
		if(!Config.Enabled_Worlds.contains(world.getName())) return;
		
		//FEATURE: always-angry pig zombies
		if(Config.PigZombie__Always_Angry)
		{
			for (Entity entity: chunk.getEntities())
			{			
				if(entity instanceof PigZombie)
				{
					PigZombie pigZombie = (PigZombie)entity;
					pigZombie.setAnger(Integer.MAX_VALUE);
				}
			}
		}		
	}		
	
	//when an entity dies...
	@EventHandler
	public void onEntityDeath(EntityDeathEvent event)
	{
		LivingEntity entity = event.getEntity();
		World world = entity.getWorld();

		if(Config.Enabled_Worlds.contains(world.getName()) && entity instanceof Player)
		{
			//FEATURE: some portion of player inventory is permanently lost on death
			Player player = (Player)entity;
			if(!player.hasPermission(DataStore.bypass_Perm))
			{
				List<ItemStack> drops = event.getDrops();
				int numberOfStacksToRemove = (int)(drops.size() * (Config.Player__Death_Item_Stacks_Forfeit_Percent/ 100f));
				for(int i = 0; i < numberOfStacksToRemove && drops.size() > 0; i++)
				{
					int indexOfStackToRemove = ExtraHardMode.randomNumberGenerator.nextInt(drops.size());
					drops.remove(indexOfStackToRemove);
				}
			}
		}

		//FEATURE: silverfish drop cobblestone
		if(entity.getType() == EntityType.SILVERFISH)
		{
			event.getDrops().add(new ItemStack(Material.COBBLESTONE));
		}

		//FEATURE: zombies may reanimate if not on fire when they die
		if(Config.Zombies__Reanimate_Percent > 0)
		{
			if(entity.getType() == EntityType.ZOMBIE)
			{
				Zombie zombie = (Zombie)entity;

				if(!zombie.isVillager() && entity.getFireTicks() < 1 && ExtraHardMode.random(Config.Zombies__Reanimate_Percent))
				{
					Player playerTarget = null;
					Entity target = zombie.getTarget();
					if(target instanceof Player)
					{
						playerTarget = (Player)target;
					}

					RespawnZombieTask task = new RespawnZombieTask(entity.getLocation(), playerTarget);
					int respawnSeconds = ExtraHardMode.randomNumberGenerator.nextInt(6) + 3;  //3-8 seconds
					ExtraHardMode.instance.getServer().getScheduler().scheduleSyncDelayedTask(ExtraHardMode.instance, task, 20L * respawnSeconds);  ///20L ~ 1 second
				}
			}
		}

		//FEATURE: creepers may drop activated TNT when they die
		if(Config.Creepers__Drop_Tnt_On_Death_Percent > 0)
		{
			if(	entity.getType() == EntityType.CREEPER &&
				ExtraHardMode.random(Config.Creepers__Drop_Tnt_On_Death_Percent) &&
				entity.getLocation().getY() < Config.Creepers__Drop_Tnt_On_Death_Max_Y)
			{
				if (Config.World__Play_Sounds__Creeper_Tnt_Warning)
					world.playEffect(entity.getLocation(), Effect.GHAST_SHRIEK, 1, 35);
				world.spawnEntity(entity.getLocation(), EntityType.PRIMED_TNT);
			}
		}

		//FEATURE: pig zombies drop nether wart when slain in nether fortresses
		if(Config.PigZombie__Drop_Warts_In_Fortresses && world.getEnvironment() == Environment.NETHER && entity instanceof PigZombie)
		{
			Block underBlock = entity.getLocation().getBlock().getRelative(BlockFace.DOWN);
			if(underBlock.getType() == Material.NETHER_BRICK)
			{
				event.getDrops().add(new ItemStack(Material.NETHER_STALK));
			}
		}

		//FEATURE: nether blazes drop extra loot (glowstone and gunpowder)
		if(Config.Blazes__Drop_Bonus_Loot && entity instanceof Blaze)
		{
			if(world.getEnvironment() == Environment.NETHER)
			{
				//50% chance of each
				if(ExtraHardMode.randomNumberGenerator.nextInt(2) == 0)
				{
					event.getDrops().add(new ItemStack(Material.SULPHUR, 2));
				}
				else
				{
					event.getDrops().add(new ItemStack(Material.GLOWSTONE_DUST, 2));
				}
			}
			else  //no drops in the normal world (restricting blaze rods to the nether)
			{
				event.getDrops().clear();
			}
		}

		//FEATURE: ender dragon drops prizes on death
		if(entity instanceof EnderDragon)
		{
			if(Config.Enderdragon__Drops_Villager_Eggs)
			{
				ItemStack itemStack = new ItemStack(Material.MONSTER_EGG, 2, (short)120);
				world.dropItemNaturally(entity.getLocation().add(10, 0, 0), itemStack);
			}

			if(Config.Enderdragon__Drops_Dragon_Egg)
			{
				world.dropItemNaturally(entity.getLocation().add(10, 0, 0), new ItemStack(Material.DRAGON_EGG));
			}

			if(Config.Enderdragon__Combat_Announcements)
			{
				StringBuilder builder = new StringBuilder("The dragon has been defeated!  ( By: ");
				for(Player player : this.playersFightingDragon)
				{
					builder.append(player.getName() + " ");
				}
				builder.append(")");

				ExtraHardMode.instance.getServer().broadcastMessage(builder.toString());
			}

			if(Config.Enderdragon__No_Building_In_End)
			{
				for(Player player : this.playersFightingDragon)
				{
					ExtraHardMode.sendMessage(player, TextMode.Success, Messages.DragonFountainTip);
				}
			}

			this.playersFightingDragon.clear();
		}

		//FEATURE: monsters which take environmental damage or spawn from spawners don't drop loot and exp (monster grinder inhibitor)
		if(Config.General_Monster_Rules__Inhibit_Monster_Grinders && entity.getType() != EntityType.PLAYER && entity.getType() != EntityType.SQUID)
		{
			boolean noLoot = false;

			if(EntityEventHandler.isLootLess(entity))
			{
				noLoot = true;
			}

			else if(entity instanceof Skeleton)
			{
				Skeleton skeleton = (Skeleton)entity;
				if(skeleton.getSkeletonType() == Skeleton.SkeletonType.WITHER && skeleton.getEyeLocation().getBlock().getType() != Material.AIR)
				{
					noLoot = true;
				}
			}

			else if(entity instanceof Enderman)
			{
				if(entity.getEyeLocation().getBlock().getType() != Material.AIR)
				{
					noLoot = true;
				}
			}

			else
			{
				//also no loot for monsters which die standing in water
				Block block = entity.getLocation().getBlock();
				Block underBlock = block.getRelative(BlockFace.DOWN);
				Block [] adjacentBlocks = new Block []
				{
					block,
					block.getRelative(BlockFace.EAST),
					block.getRelative(BlockFace.WEST),
					block.getRelative(BlockFace.NORTH),
					block.getRelative(BlockFace.SOUTH),
					block.getRelative(BlockFace.NORTH_EAST),
					block.getRelative(BlockFace.SOUTH_EAST),
					block.getRelative(BlockFace.NORTH_WEST),
					block.getRelative(BlockFace.SOUTH_WEST),
					underBlock,
					underBlock.getRelative(BlockFace.EAST),
					underBlock.getRelative(BlockFace.WEST),
					underBlock.getRelative(BlockFace.NORTH),
					underBlock.getRelative(BlockFace.SOUTH),
					underBlock.getRelative(BlockFace.NORTH_EAST),
					underBlock.getRelative(BlockFace.SOUTH_EAST),
					underBlock.getRelative(BlockFace.NORTH_WEST),
					underBlock.getRelative(BlockFace.SOUTH_WEST)
				};

				for (Block adjacentBlock : adjacentBlocks) {
					if (adjacentBlock.getType() == Material.WATER || adjacentBlock.getType() == Material.STATIONARY_WATER) {
						noLoot = true;
						break;
					}
				}

				//also no loot for monsters who can't reach their (melee) killers
				Player killer = entity.getKiller();
				if(killer != null)
				{
					Location monsterEyeLocation = entity.getEyeLocation();
					Location playerEyeLocation = killer.getEyeLocation();

					//interpolate locations
					Location [] locations = new Location []
					{
						new Location(
							monsterEyeLocation.getWorld(),
							.2 * monsterEyeLocation.getX() + .8 * playerEyeLocation.getX(),
							monsterEyeLocation.getY(),
							.2 * monsterEyeLocation.getZ() + .8 * playerEyeLocation.getZ()),
						new Location(
							monsterEyeLocation.getWorld(),
							.5 * monsterEyeLocation.getX() + .5 * playerEyeLocation.getX(),
							monsterEyeLocation.getY(),
							.5 * monsterEyeLocation.getZ() + .5 * playerEyeLocation.getZ()),
						new Location(
							monsterEyeLocation.getWorld(),
							.8 * monsterEyeLocation.getX() + .2 * playerEyeLocation.getX(),
							monsterEyeLocation.getY(),
							.8 * monsterEyeLocation.getZ() + .2 * playerEyeLocation.getZ()),
					};

					for(Location middleLocation: locations)
					{
						//monster is blocked at eye level, unable to advance toward killer
						if(middleLocation.getBlock().getType() != Material.AIR)
						{
							noLoot = true;
						}

						//monster doesn't have room above to hurdle a foot level block, unable to advance toward killer
						else
						{
							Block bottom = middleLocation.getBlock().getRelative(BlockFace.DOWN);
							Block top = middleLocation.getBlock().getRelative(BlockFace.UP);
							if(top.getType() != Material.AIR && bottom.getType() != Material.AIR ||
									bottom.getType() == Material.FENCE ||
									bottom.getType() == Material.FENCE_GATE ||
									bottom.getType() == Material.COBBLE_WALL ||
									bottom.getType() == Material.NETHER_FENCE)
							{
								noLoot = true;
							}
						}
					}
				}
			}

			if(noLoot)
			{
				event.setDroppedExp(0);
				event.getDrops().clear();
			}
		}

		//FEATURE: animals don't drop experience (because they're easy to "farm")
		if(Config.General_Monster_Rules__Inhibit_Monster_Grinders && entity instanceof Animals)
		{
			event.setDroppedExp(0);
		}

		//FEATURE: ghasts deflect arrows and drop extra loot and exp
		if(Config.Ghasts__Deflect_Arrows)
		{
			if(entity instanceof Ghast)
			{
				event.setDroppedExp(event.getDroppedExp() * 10);
				List<ItemStack> itemDrops = event.getDrops();
				for(ItemStack itemDrop: itemDrops)
				{
					itemDrop.setAmount(itemDrop.getAmount() * 10);
				}
			}
		}

		//FEATURE: blazes explode on death in normal world
		if(Config.Blazes__Explode_On_Death && entity instanceof Blaze && world.getEnvironment() == Environment.NORMAL && !Config.Work_Around_Explosions_Bugs)
		{
			//create explosion
			world.createExplosion(entity.getLocation(), 2F, true);  //equal to a TNT blast, sets fires

			//fire a fireball straight up in normal worlds
			Fireball fireball = (Fireball) world.spawnEntity(entity.getLocation(), EntityType.FIREBALL);
			fireball.setDirection(new Vector(0, 10, 0));
			fireball.setYield(1);
		}

		//FEATURE: nether blazes may multiply on death
		if(Config.Blazes__Nether_Blazes_Split_On_Death_Percent > 0 && world.getEnvironment() == Environment.NETHER && entity instanceof Blaze)
		{
			if(ExtraHardMode.random(Config.Blazes__Nether_Blazes_Split_On_Death_Percent))
			{
				Entity firstNewBlaze = world.spawnEntity(entity.getLocation(), EntityType.BLAZE);
				firstNewBlaze.setVelocity(new Vector(1, 0, 1));

				Entity secondNewBlaze = world.spawnEntity(entity.getLocation(), EntityType.BLAZE);
				secondNewBlaze.setVelocity(new Vector(-1, 0, -1));

				//if this blaze was marked lootless, mark the new blazes the same
				if(EntityEventHandler.isLootLess((LivingEntity)entity))
				{
					EntityEventHandler.markLootLess((LivingEntity)firstNewBlaze);
					EntityEventHandler.markLootLess((LivingEntity)secondNewBlaze);
				}
			}
		}

		//FEATURE: spiders drop web on death
		if(Config.Spiders__Drop_Web_On_Death)
		{
			if(entity instanceof Spider)
			{
				//random web placement
				long serverTime = world.getFullTime();
				int random1 = (int)(serverTime + entity.getLocation().getBlockZ()) % 9;
				int random2 = (int)(serverTime + entity.getLocation().getBlockX()) % 9;

				Location [] locations = new Location [4];

				locations[0] = entity.getLocation().add(random1, 0, random2);
				locations[1] = entity.getLocation().add(-random2, 0, random1 / 2);
				locations[2] = entity.getLocation().add(-random1 / 2, 0, -random2);
				locations[3] = entity.getLocation().add(random1 / 2, 0, -random2 / 2);

				ArrayList<Block> changedBlocks = new ArrayList<Block>();
				for(Location location: locations)
				{
					Block block = location.getBlock();

					//don't replace anything solid with web
					if(block.getType() != Material.AIR) continue;

					//only place web on the ground, not hanging up in the air
					do
					{
						block = block.getRelative(BlockFace.DOWN);
					}while(block.getType() == Material.AIR);

					//don't place web over fluids or stack webs
					if(!block.isLiquid() && block.getType() != Material.WEB)
					{
						block = block.getRelative(BlockFace.UP);

						//don't place next to cactus, because it will break the cactus
						Block [] adjacentBlocks = new Block []
						{
							block.getRelative(BlockFace.EAST),
							block.getRelative(BlockFace.WEST),
							block.getRelative(BlockFace.NORTH),
							block.getRelative(BlockFace.SOUTH)
						};

						boolean nextToCactus = false;
						for(Block adjacentBlock: adjacentBlocks)
						{
							if(adjacentBlock.getType() == Material.CACTUS)
							{
								nextToCactus = true;
								break;
							}
						}

						if(!nextToCactus)
						{
							block.setType(Material.WEB);
							changedBlocks.add(block);
						}
					}
				}

				//any webs placed above sea level will be automatically cleaned up after a short time
				if(entity.getLocation().getBlockY() >= entity.getLocation().getWorld().getSeaLevel() - 5)
				{
					WebCleanupTask task = new WebCleanupTask(changedBlocks);
					ExtraHardMode.instance.getServer().getScheduler().scheduleSyncDelayedTask(ExtraHardMode.instance, task, 20L * 30);
				}
			}
		}
	}
	
	private ArrayList<Player> playersFightingDragon = new ArrayList<Player>();
	
	//when an entity is damaged
	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onEntityDamage (EntityDamageEvent event)
	{
		Entity entity = event.getEntity();
		EntityType entityType = entity.getType();
		World world = entity.getWorld();
		
		if(!Config.Enabled_Worlds.contains(world.getName())) return;
		
		//is this an entity damaged by entity event?
		EntityDamageByEntityEvent subEvent = null;		
		if(event instanceof EntityDamageByEntityEvent)
		{
			subEvent = (EntityDamageByEntityEvent)event;
		}
		
		//FEATURE: don't allow explosions to destroy items on the ground
		//REASONS: charged creepers explode twice, enhanced TNT explodes 5 times
		if(entityType == EntityType.DROPPED_ITEM)
		{
			event.setCancelled(true);
		}				
		
		//FEATURE: the dragon has new attacks
		if(subEvent != null && entity.getType() == EntityType.ENDER_DRAGON && Config.Enderdragon__Additional_Attacks)
		{
			Player damager = null;
			if(subEvent.getDamager() instanceof Player)
			{
				damager = (Player) subEvent.getDamager();
			}
			else if(subEvent.getDamager() instanceof Projectile)
			{
				Projectile projectile = (Projectile)subEvent.getDamager();
				if(projectile.getShooter() != null && projectile.getShooter() instanceof Player)
				{
					damager = (Player)projectile.getShooter();
				}
			}
			
			if(damager != null)
			{
				if(!this.playersFightingDragon.contains(damager))
				{
					this.playersFightingDragon.add(damager);
					
					DragonAttackPatternTask task = new DragonAttackPatternTask((LivingEntity)entity, damager, this.playersFightingDragon);
					ExtraHardMode.instance.getServer().getScheduler().scheduleSyncDelayedTask(ExtraHardMode.instance, task, 1L);
					
					if(Config.Enderdragon__Combat_Announcements)
					{
						ExtraHardMode.instance.getServer().broadcastMessage(damager.getName() + " is challenging the dragon!");
					}
				}
				
				for(int i = 0; i < 5; i++)
				{
					DragonAttackTask task = new DragonAttackTask(entity, damager);
					ExtraHardMode.instance.getServer().getScheduler().scheduleSyncDelayedTask(ExtraHardMode.instance, task, 20L * (ExtraHardMode.randomNumberGenerator.nextInt(15)));
				}
				
				Chunk chunk = damager.getLocation().getChunk();
				Entity [] entities = chunk.getEntities();
				for(Entity enderEntity: entities)
				{
					if(entity.getType() == EntityType.ENDERMAN)
					{
						Enderman enderman = (Enderman)enderEntity;
						enderman.setTarget(damager);
					}
				}
			}
		}
		
		//FEATURE: zombies can apply a debilitating effect
		if(Config.Zombies__Debilitate_Players)
		{
			if(subEvent != null && subEvent.getDamager() instanceof Zombie)
			{
				if(entity instanceof Player)
				{
					Player player = (Player)entity;
					if(!player.hasPermission(DataStore.bypass_Perm))
					{
						player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * 10, 3));
					}
				}
			}
		}
		
		//FEATURE: magma cubes become blazes when they take damage
		if(entityType == EntityType.MAGMA_CUBE && Config.MagmaCubes__Become_Blazes_On_Damage && !entity.isDead() && !Config.Work_Around_Explosions_Bugs)
		{
			entity.remove();  //remove magma cube
			entity.getWorld().spawnEntity(entity.getLocation().add(0, 2, 0), EntityType.BLAZE);  //replace with blaze
			entity.getWorld().createExplosion(entity.getLocation(), 2F, true); //fiery explosion for effect				
		}
		
		//FEATURE: arrows pass through skeletons
		if(entityType == EntityType.SKELETON && subEvent != null && Config.Skeletons__Deflect_Arrows_Percent > 0)
		{
			Entity damageSource = subEvent.getDamager();
			
			//only arrows
			if(damageSource instanceof Arrow)
			{
				Arrow arrow = (Arrow)damageSource;
				
				//percent chance
				if(ExtraHardMode.random(Config.Skeletons__Deflect_Arrows_Percent))
				{
					//cancel the damage
					event.setCancelled(true);
					
					//teleport the arrow a single block farther along its flight path
					//note that .6 and 12 were the unexplained recommended values for speed and spread, reflectively, in the bukkit wiki
					arrow.remove();
					world.spawnArrow(arrow.getLocation().add((arrow.getVelocity().normalize()).multiply(2)), arrow.getVelocity(), .6f, 12f);
				}
			}
		}
		
		//FEATURE: extra damage and effects from environmental damage
		if(Config.Player__Enhanced_Environmental_Damage)
		{
			Player player = null;
			if(entity instanceof Player)
			{
				player = (Player)entity;
			}
			
			if(player != null && !player.hasPermission(DataStore.bypass_Perm))
			{
				DamageCause cause = event.getCause();
				
				if(event.getDamage() > 2 && (cause == DamageCause.BLOCK_EXPLOSION || cause == DamageCause.ENTITY_EXPLOSION))
				{
					player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 20 * 15, 3));
				}
				else if(cause == DamageCause.FALL)
				{
					player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * event.getDamage(), 4));
					event.setDamage(event.getDamage() * 2);
				}
				else if(cause == DamageCause.SUFFOCATION)
				{
					event.setDamage(event.getDamage() * 5);
				}
				else if(cause == DamageCause.LAVA)
				{
					event.setDamage(event.getDamage() * 2);					
				}				
				else if(cause == DamageCause.FIRE_TICK)
				{
					player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * 1, 1));
				}
			}
		}	
		
		//FEATURE: skeletons can knock back
		if(Config.Skeletons__Knock_Back_Percent > 0)
		{
			if(subEvent != null)
			{
				if(subEvent.getDamager() instanceof Arrow)
				{
					Arrow arrow = (Arrow)(subEvent.getDamager());
					if(arrow.getShooter() != null && arrow.getShooter() instanceof Skeleton)
					{
						if(ExtraHardMode.random(Config.Skeletons__Knock_Back_Percent))
						{
							//cut damage in half
							event.setDamage(event.getDamage() / 2);
							
							//knock back target with half the arrow's velocity
							entity.setVelocity(arrow.getVelocity());
						}
					}
				}
			}
		}
		
		//FEATURE: monsters trapped in webbing break out of the webbing when hit
		if(entity instanceof Monster)
		{
			this.clearWebbing(entity);
		}
		
		//FEATURE: blazes drop fire on hit
		if(Config.Blazes__Drop_Fire_On_Damage)
		{
			if(entityType == EntityType.BLAZE)
			{
				Blaze blaze = (Blaze)entity;
				
				if(blaze.getHealth() > blaze.getMaxHealth() / 2)
				{
				
					Block block = entity.getLocation().getBlock();
					
					Block underBlock = block.getRelative(BlockFace.DOWN);
					while(underBlock.getType() == Material.AIR)
						underBlock = underBlock.getRelative(BlockFace.DOWN);
					
					block = underBlock.getRelative(BlockFace.UP); 
					if(block.getType() == Material.AIR && underBlock.getType() != Material.AIR && !underBlock.isLiquid() && underBlock.getY() > 0)
					{
						block.setType(Material.FIRE);
					}
				}
			}
		}
		
		//FEATURE: charged creepers explode on hit
		if(Config.Creepers__Charged_Creepers_Explode_On_Hit && Config.Work_Around_Explosions_Bugs)
		{			
			if(entityType == EntityType.CREEPER && !entity.isDead())
			{
				Creeper creeper = (Creeper)entity;
				if(	(	creeper.isPowered() && creeper.getTarget() != null && 
						creeper.getTarget() instanceof Player	
					) ||
						//For some odd reason the first damage event is always null
					(	creeper.isPowered() &&
						creeper.getLastDamageCause() != null &&
						!creeper.getLastDamageCause().isCancelled() &&
						creeper.getLastDamageCause().getCause() == DamageCause.PROJECTILE)
					)
				{
					markLootLess((LivingEntity)entity);
					entity.remove();
					world.createExplosion(entity.getLocation(), 4F);  //equal to a TNT blast
				}
			}
		}
		
		//FEATURE: flaming creepers explode
		if(Config.Creepers__Flaming_Creepers_Explode && !Config.Work_Around_Explosions_Bugs)
		{			
			if(entityType == EntityType.CREEPER && !entity.isDead())
			{
				Creeper creeper = (Creeper)entity;
				if(creeper.getFireTicks() > 0 && ExtraHardMode.randomNumberGenerator.nextBoolean())
				{
					markLootLess((LivingEntity)entity);
					entity.remove();
					world.createExplosion(entity.getLocation(), 4F);  //equal to a TNT blast
				}
			}
		}
		
		//FEATURE: ghasts deflect arrows and drop extra loot
		if(Config.Ghasts__Deflect_Arrows)
		{
			//only ghasts, and only if damaged by another entity (as opposed to environmental damage)
			if(entity instanceof Ghast && event instanceof EntityDamageByEntityEvent)
			{
				Entity damageSource = subEvent.getDamager();
				
				//only arrows
				if(damageSource instanceof Arrow)
				{
					//who shot it?
					Arrow arrow = (Arrow)damageSource;
					if(arrow.getShooter() != null && arrow.getShooter() instanceof Player)
					{
						//check permissions when it's shot by a player
						Player player = (Player)arrow.getShooter();
						event.setCancelled(!player.hasPermission(DataStore.bypass_Perm));
					}
					else
					{
						//otherwise always deflect
						event.setCancelled(true);
						return;
					}
				}
			}				
		}
		
		//FEATURE: monsters which take environmental damage don't drop loot or experience (monster grinder inhibitor)
		if(Config.General_Monster_Rules__Inhibit_Monster_Grinders && entity instanceof LivingEntity)
		{
			DamageCause damageCause = event.getCause();
			if(damageCause != DamageCause.ENTITY_ATTACK && damageCause != DamageCause.PROJECTILE && damageCause != DamageCause.BLOCK_EXPLOSION)
			{
				EntityEventHandler.addEnvironmentalDamage((LivingEntity)entity, event.getDamage());
			}
		}
	}
	
	//when a sheep regrows its wool...
	@EventHandler
	public void onSheepRegrowWool(SheepRegrowWoolEvent event)
	{
		World world = event.getEntity().getWorld();
        if (Config.Enabled_Worlds.contains(world.getName()))
        {
			//FEATURE: sheep are all white, and may be dyed only temporarily
			if(Config.Farming__Sheep_Only_Regrow_White_Wool)
			{
				Sheep sheep = event.getEntity();
				sheep.setColor(DyeColor.WHITE);
			}
        }
	}
	
	//when an entity (not a player) teleports...
	@EventHandler
	public void onEntityTeleport(EntityTeleportEvent event)
	{
		Entity entity = event.getEntity();
		World world = entity.getWorld();
		if(Config.Enabled_Worlds.contains(world.getName()) && world.getEnvironment() != Environment.NORMAL)
		{
			if(entity instanceof Enderman && Config.Enderman__Improved_Teleportation)
			{
				Enderman enderman = (Enderman)entity;

				//ignore endermen which aren't fighting players
				if(enderman.getTarget() == null || !(enderman.getTarget() instanceof Player)) return;

				//ignore endermen which are taking damage from the environment (to avoid rapid teleportation due to rain or suffocation)
				if(enderman.getLastDamageCause() != null && enderman.getLastDamageCause().getCause() != DamageCause.ENTITY_ATTACK) return;

				//ignore endermen which are in caves (standing on stone)
				if(enderman.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == Material.STONE) return;

				Player player = (Player)enderman.getTarget();

				//ignore when player is in a different world from the enderman
				if(!player.getWorld().equals(enderman.getWorld())) return;

				//half the time, teleport the player instead
				if(ExtraHardMode.random(50))
				{
					event.setCancelled(true);
					int distanceSquared = (int)player.getLocation().distanceSquared(enderman.getLocation());

					//play sound at old location
					world.playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 1, 1);
					Block destinationBlock;

					//if the player is far away
					if(distanceSquared > 75)
					{
						//have the enderman swap places with the player
						destinationBlock = enderman.getLocation().getBlock();
						enderman.teleport(player.getLocation());
					}

					//otherwise if the player is close
					else
					{
						//teleport the player to the enderman's destination
						destinationBlock = event.getTo().getBlock();
					}

					while(destinationBlock.getType() != Material.AIR || destinationBlock.getRelative(BlockFace.UP).getType() != Material.AIR)
					{
						destinationBlock = destinationBlock.getRelative(BlockFace.UP);
					}

					player.teleport(destinationBlock.getLocation(), TeleportCause.ENDER_PEARL);

					//play sound at new location
					world.playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 1, 1);
				}
			}
		}
	}
	
	//when an entity targets something (as in to attack it)...
	@EventHandler
	public void onEntityTarget(EntityTargetEvent event)
	{
		Entity entity = event.getEntity();
		World world = entity.getWorld();
		if(!Config.Enabled_Worlds.contains(world.getName())) return;
		
		//FEATURE: a monster which gains a target breaks out of any webbing it might have been trapped within
		if(entity instanceof Monster)
		{
			this.clearWebbing(entity);
		}
		
		//FEATURE: monsters don't target the ender dragon
		if(event.getTarget() != null && event.getTarget() instanceof EnderDragon)
		{
			event.setCancelled(true);
		}
	}
	
	//when a player crafts something...
	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onItemCrafted(CraftItemEvent event)
	{
		HumanEntity entity = event.getWhoClicked();
		if(entity == null || !(entity instanceof Player)) return;
		
		Player player = (Player)entity;
		World world = player.getWorld();
		
		if(!Config.Enabled_Worlds.contains(world.getName()) || player.hasPermission(DataStore.bypass_Perm)) return;
		
		Material result = event.getRecipe().getResult().getType();
		
		//FEATURE: no crafting melon seeds
		if(Config.Farming__No_Crafting_Melon_Seeds && result == Material.MELON_SEEDS || result == Material.PUMPKIN_SEEDS)
		{
			event.setCancelled(true);
			ExtraHardMode.sendMessage(player, TextMode.Instr, Messages.NoCraftingMelonSeeds);
			return;
		}
		
		//FEATURE: extra TNT from the TNT recipe
		if(Config.World__Better_Tnt && event.getRecipe().getResult().getType() == Material.TNT)
		{			
			player.getInventory().addItem(new ItemStack(Material.TNT, 2));
		}
	}
	
	//when a player teleports BUG HERE: last i checked, this event didn't fire from bukkit.  so this code is incomplete (i stopped working on it)
	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onPlayerTeleport(PlayerTeleportEvent event)
	{
		//TODO Add Price for Entering the End
		/*Player player = event.getPlayer();
		World world = player.getWorld();
		if(event.getCause() != TeleportCause.END_PORTAL || !Config.Enabled_Worlds.contains(world.getName()) || player.hasPermission(DataStore.bypass_Perm) || world.getEnvironment() == Environment.THE_END) return;*/
	}	
	
	//when an item spawns
	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onItemSpawn(ItemSpawnEvent event)
	{
		//FEATURE: fountain effect from dragon fireball explosions sometimes causes fire to drop as an item.  this is the fix for that.		
		Item item = event.getEntity();
		World world = item.getWorld();
		if(!Config.Enabled_Worlds.contains(world.getName()) || world.getEnvironment() != Environment.THE_END) return;
		
		if(item.getItemStack().getType() == Material.FIRE)
		{
			event.setCancelled(true);
		}
	}
	
	//when an entity tries to change a block (does not include player block changes)
	//don't allow endermen to change blocks
	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
	public void onEntityChangeBlock(EntityChangeBlockEvent event)
	{
		Block block = event.getBlock();
		World world = block.getWorld();
		if(!Config.Enabled_Worlds.contains(world.getName())) return;
		
		if(event.getEntity().getType() == EntityType.SILVERFISH && event.getTo() == Material.MONSTER_EGGS)
		{
			event.setCancelled(true);
		}		
	}
	
	//marks an entity so that the plugin can remember not to drop loot or experience if it's killed
	static void markLootLess(LivingEntity entity)
	{		
		entity.setMetadata("extrahard_environmentalDamage", new FixedMetadataValue(ExtraHardMode.instance, entity.getMaxHealth()));
	}
	
	//tracks total environmental damage done to an entity
	static void addEnvironmentalDamage(LivingEntity entity, int damage)
	{
		if(!entity.hasMetadata("extrahard_environmentalDamage"))
		{
			entity.setMetadata("extrahard_environmentalDamage", new FixedMetadataValue(ExtraHardMode.instance, damage));
		}
		else
		{
			int currentTotalDamage = entity.getMetadata("extrahard_environmentalDamage").get(0).asInt();			
			entity.setMetadata("extrahard_environmentalDamage", new FixedMetadataValue(ExtraHardMode.instance, currentTotalDamage + damage));
		}
	}
	
	//checks whether an entity should drop items when it dies
	static boolean isLootLess(LivingEntity entity)
	{
		if(entity instanceof Creature && entity.hasMetadata("extrahard_environmentalDamage"))
		{
			int totalDamage = entity.getMetadata("extrahard_environmentalDamage").get(0).asInt();
			if(!(entity instanceof Wither))
			{
				return (totalDamage > entity.getMaxHealth() / 2);
			}
			else
			{
				return false;  //wither is exempt.  he can't be farmed because creating him requires combining not-farmable components
			}
		}
		
		return false;
	}
	
	//clears any webbing which may be trapping this entity (assumes two-block-tall entity)
	void clearWebbing(Entity entity)
	{
		Block feetBlock = entity.getLocation().getBlock();
		Block headBlock = feetBlock.getRelative(BlockFace.UP);
		
		Block [] blocks = {feetBlock, headBlock};			
		for(Block block: blocks)
		{
			if(block.getType() == Material.WEB)
			{
				block.setType(Material.AIR);
			}
		}
	}
}
