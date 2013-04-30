package me.ryanhamshire.ExtraHardMode.features;

import me.ryanhamshire.ExtraHardMode.ExtraHardMode;
import me.ryanhamshire.ExtraHardMode.config.RootConfig;
import me.ryanhamshire.ExtraHardMode.mocks.MockBlock;
import me.ryanhamshire.ExtraHardMode.mocks.MockExtraHardMode;
import me.ryanhamshire.ExtraHardMode.mocks.MockLocation;
import me.ryanhamshire.ExtraHardMode.mocks.MockWorld;
import me.ryanhamshire.ExtraHardMode.mocks.events.MockCreatureSpawnEvent;
import me.ryanhamshire.ExtraHardMode.module.BlockModule;
import me.ryanhamshire.ExtraHardMode.module.EntityModule;
import me.ryanhamshire.ExtraHardMode.module.UtilityModule;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestAntiGrinder
{
    ExtraHardMode plugin = new MockExtraHardMode().get();
    RootConfig CFG = new RootConfig();
    Antigrinder module = new Antigrinder(CFG, new EntityModule(plugin), new BlockModule(plugin), new UtilityModule(plugin));

    @Test
    public void spawnerSpawns()
    {
        CreatureSpawnEvent event = new MockCreatureSpawnEvent(EntityType.BLAZE, "world", CreatureSpawnEvent.SpawnReason.SPAWNER).get();
        assertFalse("Spawners drop no exp", module.onEntitySpawn(event));

        event = new MockCreatureSpawnEvent(EntityType.ENDERMAN, "world", CreatureSpawnEvent.SpawnReason.SPAWNER).get();
        assertFalse("Normal Spawns not blocked", module.onEntitySpawn(event));
    }

    @Test
    public void zombieSpawns()
    {
        MockCreatureSpawnEvent event = new MockCreatureSpawnEvent(EntityType.ZOMBIE, "world", CreatureSpawnEvent.SpawnReason.VILLAGE_INVASION);

        //Set a Block at the given Location
        MockBlock block = new MockBlock(event.getWorld().get());
        MockLocation location = event.getLocation();
        location.setBlock(block);
        event.setLocation(location);

        //Set a Block beneath the "SpawnBlock"
        MockBlock relative = new MockBlock(event.getWorld().get());
        relative.setMaterial(Material.DIRT);
        block.setRelative(BlockFace.DOWN, relative.get());

        //Set the Environment to OVERWORLD
        MockWorld world = event.getWorld();
        world.setEnvironment(World.Environment.NORMAL);

        assertTrue("Zombie spawn suceeds", module.onEntitySpawn(event.get()));
    }

    @Test
    public void naturalSpawns()
    {
        MockCreatureSpawnEvent event = new MockCreatureSpawnEvent(EntityType.PIG_ZOMBIE, "world", CreatureSpawnEvent.SpawnReason.NATURAL);

        //Set a Block at the given Location
        MockBlock block = new MockBlock(event.getWorld().get());
        MockLocation location = event.getLocation();
        location.setBlock(block);
        event.setLocation(location);

        //Set a Block beneath the "SpawnBlock"
        MockBlock relative = new MockBlock(event.getWorld().get());
        relative.setMaterial(Material.NETHERRACK);
        block.setRelative(BlockFace.DOWN, relative.get());

        //Set the Environment to OVERWORLD
        MockWorld world = event.getWorld();
        world.setEnvironment(World.Environment.NETHER);

        assertTrue( "Natural spawn in the Nether failed", module.onEntitySpawn(event.get()));


        //Cobble is not natural for the nether
        relative.setMaterial(Material.COBBLESTONE);
        block.setRelative(BlockFace.DOWN, relative.get());

        assertFalse( "Natural spawn in a not natural Location suceeded", module.onEntitySpawn(event.get()));


        //Netherrack doesnt spawn in the OverWorld
        relative.setMaterial(Material.NETHERRACK);
        block.setRelative(BlockFace.DOWN, relative.get());

        world.setEnvironment(World.Environment.NORMAL);

        assertFalse( "Spawn on Netherrack in the OverWorld should have failed", module.onEntitySpawn(event.get()));
    }
}
