package me.ryanhamshire.ExtraHardMode.features;

import me.ryanhamshire.ExtraHardMode.ExtraHardMode;
import me.ryanhamshire.ExtraHardMode.config.RootConfig;
import me.ryanhamshire.ExtraHardMode.config.RootNode;
import me.ryanhamshire.ExtraHardMode.config.messages.MessageConfig;
import me.ryanhamshire.ExtraHardMode.module.BlockModule;
import me.ryanhamshire.ExtraHardMode.module.EntityModule;
import me.ryanhamshire.ExtraHardMode.module.UtilityModule;
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
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;

/**
 * A MonsterGrinder Inhibitor which disables drops for Monsters which appear to be farmed or which have been killed in
 * conditions where the Player had a clear advantage
 */
public class Antigrinder implements Listener
{
    ExtraHardMode plugin;
    RootConfig CFG;
    MessageConfig messages;
    EntityModule entityModule;
    BlockModule blockModule;
    UtilityModule utils;

    /**
     * For Testing Purposes
     * Constructor to allow dependency injection
     */
    public Antigrinder (RootConfig CFG, EntityModule entityModule, BlockModule blockModule, UtilityModule utils)
    {
        this. CFG = CFG;
        this. entityModule = entityModule;
        this. blockModule = blockModule;
        this. utils = utils;
    }

    public Antigrinder (ExtraHardMode plugin)
    {
        this.plugin = plugin;
        CFG = plugin.getModuleForClass(RootConfig.class);
        messages = plugin.getModuleForClass(MessageConfig.class);
        entityModule = plugin.getModuleForClass(EntityModule.class);
        blockModule = plugin.getModuleForClass(BlockModule.class);
        utils = plugin.getModuleForClass(UtilityModule.class);
    }

    /**
     * When an Animal/Monster spawns check if the Location is "natural"
     * @param event
     * @return true succeeded and false if cancelled or marked lootless
     */
    @EventHandler(priority = EventPriority.LOW)
    public boolean onEntitySpawn(CreatureSpawnEvent event)
    {
        Location location = event.getLocation();
        World world = location.getWorld();
        LivingEntity entity = event.getEntity();
        CreatureSpawnEvent.SpawnReason reason = event.getSpawnReason();

        final boolean inhibitMonsterGrindersEnabled = CFG.getBoolean(RootNode.INHIBIT_MONSTER_GRINDERS, world.getName());

        // FEATURE: inhibited monster grinders/farms
        if (inhibitMonsterGrindersEnabled && entity instanceof Monster)
        {
            switch (reason)
            {
                case SPAWNER:
                {
                    // Block all Spawner drops completely
                    entityModule.markLootLess(entity);
                    return false;
                }
                case NATURAL: case VILLAGE_INVASION:
                {
                    // consider environment to stop monsters from spawning in non-natural places
                    World.Environment environment = location.getWorld().getEnvironment();

                    Material underBlockType = location.getBlock().getRelative(BlockFace.DOWN).getType();
                    switch (environment)
                    {
                        case NORMAL:
                            if (!utils.isNaturalSpawnMaterial(underBlockType))
                            {
                                event.setCancelled(true);
                                return false;
                            }
                            break;
                        case NETHER:
                            if (!utils.isNaturalNetherSpawnMaterial(underBlockType))
                            {
                                event.setCancelled(true);
                                return false;
                            }
                            break;
                        case THE_END:
                            if (underBlockType != Material.ENDER_STONE && underBlockType != Material.OBSIDIAN && underBlockType != Material.AIR/*dragon*/)
                            {
                                event.setCancelled(true);
                                return false;
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
        }
        return true;
    }

    /**
     * When an entity dies check if loot should be blocked due to AntiGrinder
     * @param event
     * @return true if drops loot, false if loot was blocked
     */
    @EventHandler
    public boolean onEntityDeath(EntityDeathEvent event)
    {
        LivingEntity entity = event.getEntity();
        World world = entity.getWorld();

        final boolean inhibitMonsterGrindersEnabled = CFG.getBoolean(RootNode.INHIBIT_MONSTER_GRINDERS, world.getName());

        // FEATURE: monsters which take environmental damage or spawn from spawners don't drop loot and exp (monster grinder inhibitor)
        if (inhibitMonsterGrindersEnabled && entity instanceof Monster && entity.getType() != EntityType.SQUID)
        {
            if (entityModule.isLootLess(entity))
            {
                clearDrops(event);
                return false;
            }
            else
            {   //Evaluate if this kill was a too easy kill
                switch (entity.getType())
                {
                    case SKELETON:case ENDERMAN:
                    {
                        // tall monsters can get stuck when they spawn like WitherSkeletons
                        if (entity.getEyeLocation().getBlock().getType() != Material.AIR)
                            return clearDrops(event);
                        break;
                    }
                    default:
                    {
                        // no loot for monsters which die standing in water, to make building grinders even more difficult
                        Block block = entity.getLocation().getBlock();
                        Block underBlock = block.getRelative(BlockFace.DOWN);

                        BlockFace[] adjacentFaces = blockModule.getHorizontalAdjacentFaces();
                        Block[] adjacentBlocks = new Block[adjacentFaces.length * 2 + 1];

                        //All Blocks directly surrounding the Monster
                        adjacentBlocks[0] = block;
                        for (int i = 0; i < adjacentFaces.length; i++)
                        {
                            adjacentBlocks[i+1] = block.getRelative(adjacentFaces[i]);
                        }
                        for (int i = 0; i < adjacentFaces.length; i++)
                        {
                            adjacentBlocks[i+adjacentFaces.length] = underBlock.getRelative(adjacentFaces[i]);
                        }

                        for (Block adjacentBlock : adjacentBlocks)
                        {
                            if (adjacentBlock.getType() == Material.WATER || adjacentBlock.getType() == Material.STATIONARY_WATER)
                                return clearDrops(event);
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
                                // monster is blocked at eye level, unable to advance toward killer
                                if (middleLocation.getBlock().getType() != Material.AIR)
                                    return clearDrops(event);
                                // monster doesn't have room above to hurdle a foot level block, unable to advance toward killer
                                else
                                {
                                    Block bottom = middleLocation.getBlock().getRelative(BlockFace.DOWN);
                                    Block top = middleLocation.getBlock().getRelative(BlockFace.UP);
                                    if (top.getType() != Material.AIR &&
                                            bottom.getType() != Material.AIR
                                            || bottom.getType() == Material.FENCE
                                            || bottom.getType() == Material.FENCE_GATE
                                            || bottom.getType() == Material.COBBLE_WALL
                                            || bottom.getType() == Material.NETHER_FENCE)
                                    {
                                        return clearDrops(event);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * Utility method to clear the drops
     * @return false which means that the drops have been cleared, there are no drops
     */
    private boolean clearDrops(EntityDeathEvent event)
    {
        event.setDroppedExp(0);
        event.getDrops().clear();
        return false;
    }

    /**
     * When an entity takes damage
     * @param event
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onEntityDamage(EntityDamageEvent event)
    {
        Entity entity = event.getEntity();
        World world = entity.getWorld();

        final boolean inhibitMonsterGrindersEnabled = CFG.getBoolean(RootNode.INHIBIT_MONSTER_GRINDERS, world.getName());

        // FEATURE: monsters which take environmental damage don't drop loot or experience (monster grinder inhibitor)
        if (inhibitMonsterGrindersEnabled && entity instanceof LivingEntity)
        {
            EntityDamageEvent.DamageCause damageCause = event.getCause();
            if (damageCause != EntityDamageEvent.DamageCause.ENTITY_ATTACK && damageCause != EntityDamageEvent.DamageCause.PROJECTILE && damageCause != EntityDamageEvent.DamageCause.BLOCK_EXPLOSION)
            {
                entityModule.addEnvironmentalDamage((LivingEntity) entity, event.getDamage());
            }
        }
    }

}
