package me.ryanhamshire.ExtraHardMode.features;

import me.ryanhamshire.ExtraHardMode.ExtraHardMode;
import me.ryanhamshire.ExtraHardMode.config.RootConfig;
import me.ryanhamshire.ExtraHardMode.config.RootNode;
import me.ryanhamshire.ExtraHardMode.config.messages.MessageConfig;
import me.ryanhamshire.ExtraHardMode.module.EntityModule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;

//TODO world check
public class Antigrinder implements Listener
{
    ExtraHardMode plugin = null;
    RootConfig rootC = null;
    MessageConfig messages = null;
    EntityModule entityModule = null;

    public Antigrinder (ExtraHardMode plugin)
    {
        this.plugin = plugin;
        rootC = plugin.getModuleForClass(RootConfig.class);
        messages = plugin.getModuleForClass(MessageConfig.class);
        entityModule = plugin.getModuleForClass(EntityModule.class);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onEntitySpawn(CreatureSpawnEvent event)
    {
        Location location = event.getLocation();
        EntityModule module = plugin.getModuleForClass(EntityModule.class);

        LivingEntity entity = event.getEntity();

        CreatureSpawnEvent.SpawnReason reason = event.getSpawnReason();

        // FEATURE: inhibited monster grinders/farms
        if (rootC.getBoolean(RootNode.INHIBIT_MONSTER_GRINDERS))
        {

            // spawners and spawn eggs always spawn a monster, but the monster
            // doesn't drop any loot
            if (reason == CreatureSpawnEvent.SpawnReason.SPAWNER && (rootC.getInt(RootNode.BONUS_NETHER_BLAZE_SPAWN_PERCENT) > 0 || !(entity instanceof Blaze)))
            {
                module.markLootLess(entity);
            }

            // otherwise, consider environment to stop monsters from spawning in
            // non-natural places
            else if ((reason == CreatureSpawnEvent.SpawnReason.NATURAL || reason == CreatureSpawnEvent.SpawnReason.VILLAGE_INVASION) && entity instanceof Monster)
            {
                World.Environment environment = location.getWorld().getEnvironment();

                Material underBlockType = location.getBlock().getRelative(BlockFace.DOWN).getType();
                if (environment == World.Environment.NORMAL)
                {              //natural blocks
                    if (underBlockType != Material.GRASS && underBlockType != Material.STONE
                            && underBlockType != Material.SAND && underBlockType != Material.GRAVEL
                            && underBlockType != Material.MOSSY_COBBLESTONE && underBlockType != Material.OBSIDIAN
                            && underBlockType != Material.COBBLESTONE && underBlockType != Material.BEDROCK
                            && underBlockType != Material.AIR && underBlockType != Material.WATER) //bats, squid
                    {
                        event.setCancelled(true);
                        return;
                    }
                }
                else if (environment == World.Environment.NETHER)
                {
                    if (underBlockType != Material.NETHERRACK && underBlockType != Material.NETHER_BRICK
                            && underBlockType != Material.SOUL_SAND && underBlockType != Material.AIR)
                    {
                        // ghasts
                        event.setCancelled(true);
                        return;
                    }
                }
                else
                {
                    if (underBlockType != Material.ENDER_STONE && underBlockType != Material.OBSIDIAN && underBlockType != Material.AIR)
                    {
                        // ender dragon
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event)
    {
        LivingEntity entity = event.getEntity();
        World world = entity.getWorld();
        // FEATURE: monsters which take environmental damage or spawn from
        // spawners don't drop loot and exp (monster grinder inhibitor)
        if (rootC.getBoolean(RootNode.INHIBIT_MONSTER_GRINDERS) && entity.getType() != EntityType.PLAYER && entity.getType() != EntityType.SQUID)
        {
            boolean noLoot = false;

            //animals aren't affected by antigrinder
            if (entity instanceof Chicken || entity instanceof Cow || entity instanceof Pig)
                return;
            if (entityModule.isLootLess(entity))
            {
                noLoot = true;
            }
            else if (entity instanceof Skeleton)
            {
                Skeleton skeleton = (Skeleton) entity;
                if (skeleton.getSkeletonType() == Skeleton.SkeletonType.WITHER && skeleton.getEyeLocation().getBlock().getType() != Material.AIR)
                {
                    noLoot = true;
                }
            }
            else if (entity instanceof Enderman)
            {
                if (entity.getEyeLocation().getBlock().getType() != Material.AIR)
                {
                    noLoot = true;
                }
            }
            else
            {
                // also no loot for monsters which die standing in water
                Block block = entity.getLocation().getBlock();
                Block underBlock = block.getRelative(BlockFace.DOWN);
                Block[] adjacentBlocks = new Block[]{block, block.getRelative(BlockFace.EAST), block.getRelative(BlockFace.WEST),
                        block.getRelative(BlockFace.NORTH), block.getRelative(BlockFace.SOUTH), block.getRelative(BlockFace.NORTH_EAST),
                        block.getRelative(BlockFace.SOUTH_EAST), block.getRelative(BlockFace.NORTH_WEST), block.getRelative(BlockFace.SOUTH_WEST),
                        underBlock, underBlock.getRelative(BlockFace.EAST), underBlock.getRelative(BlockFace.WEST),
                        underBlock.getRelative(BlockFace.NORTH), underBlock.getRelative(BlockFace.SOUTH), underBlock.getRelative(BlockFace.NORTH_EAST),
                        underBlock.getRelative(BlockFace.SOUTH_EAST), underBlock.getRelative(BlockFace.NORTH_WEST),
                        underBlock.getRelative(BlockFace.SOUTH_WEST)};

                for (Block adjacentBlock : adjacentBlocks)
                {
                    block = adjacentBlock;
                    if (block.getType() == Material.WATER || block.getType() == Material.STATIONARY_WATER)
                    {
                        noLoot = true;
                        break;
                    }
                }

                // also no loot for monsters who can't reach their (melee) killers
                Player killer = entity.getKiller();
                if (killer != null)
                {
                    Location monsterEyeLocation = entity.getEyeLocation();
                    Location playerEyeLocation = killer.getEyeLocation();

                    // interpolate locations
                    Location[] locations = new Location[]{
                            new Location(monsterEyeLocation.getWorld(), .2 * monsterEyeLocation.getX() + .8 * playerEyeLocation.getX(),
                                    monsterEyeLocation.getY(), .2 * monsterEyeLocation.getZ() + .8 * playerEyeLocation.getZ()),
                            new Location(monsterEyeLocation.getWorld(), .5 * monsterEyeLocation.getX() + .5 * playerEyeLocation.getX(),
                                    monsterEyeLocation.getY(), .5 * monsterEyeLocation.getZ() + .5 * playerEyeLocation.getZ()),
                            new Location(monsterEyeLocation.getWorld(), .8 * monsterEyeLocation.getX() + .2 * playerEyeLocation.getX(),
                                    monsterEyeLocation.getY(), .8 * monsterEyeLocation.getZ() + .2 * playerEyeLocation.getZ()),};

                    for (Location middleLocation : locations)
                    {
                        // monster is blocked at eye level, unable to advance toward
                        // killer
                        if (middleLocation.getBlock().getType() != Material.AIR)
                        {
                            noLoot = true;
                        }

                        // monster doesn't have room above to hurdle a foot level
                        // block, unable to advance toward killer
                        else
                        {
                            Block bottom = middleLocation.getBlock().getRelative(BlockFace.DOWN);
                            Block top = middleLocation.getBlock().getRelative(BlockFace.UP);
                            if (top.getType() != Material.AIR && bottom.getType() != Material.AIR || bottom.getType() == Material.FENCE
                                    || bottom.getType() == Material.FENCE_GATE || bottom.getType() == Material.COBBLE_WALL
                                    || bottom.getType() == Material.NETHER_FENCE)
                            {
                                noLoot = true;
                            }
                        }
                    }
                }
            }

            if (noLoot)
            {
                event.setDroppedExp(0);
                event.getDrops().clear();
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onEntityDamage(EntityDamageEvent event)
    {
        Entity entity = event.getEntity();
        EntityType entityType = entity.getType();
        World world = entity.getWorld();

        if (!rootC.getStringList(RootNode.WORLDS).contains(world.getName()))
            return;

        // is this an entity damaged by entity event?
        EntityDamageByEntityEvent damageByEntityEvent = null;
        if (event instanceof EntityDamageByEntityEvent)
        {
            damageByEntityEvent = (EntityDamageByEntityEvent) event;
        }
        // FEATURE: monsters which take environmental damage don't drop loot or
        // experience (monster grinder inhibitor)
        if (rootC.getBoolean(RootNode.INHIBIT_MONSTER_GRINDERS) && entity instanceof LivingEntity)
        {
            EntityDamageEvent.DamageCause damageCause = event.getCause();
            if (damageCause != EntityDamageEvent.DamageCause.ENTITY_ATTACK && damageCause != EntityDamageEvent.DamageCause.PROJECTILE && damageCause != EntityDamageEvent.DamageCause.BLOCK_EXPLOSION)
            {
                entityModule.addEnvironmentalDamage((LivingEntity) entity, event.getDamage());
            }
        }
    }

}
