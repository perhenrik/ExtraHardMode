package com.extrahardmode.features;

import com.extrahardmode.ExtraHardMode;
import com.extrahardmode.config.RootConfig;
import com.extrahardmode.config.RootNode;
import com.extrahardmode.mocks.MockBlock;
import com.extrahardmode.mocks.MockExtraHardMode;
import com.extrahardmode.mocks.MockLocation;
import com.extrahardmode.mocks.MockWorld;
import com.extrahardmode.mocks.events.MockCreatureSpawnEvent;
import com.extrahardmode.module.BlockModule;
import com.extrahardmode.module.EntityModule;
import com.extrahardmode.module.UtilityModule;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestAntiGrinder
{
    ExtraHardMode plugin = new MockExtraHardMode().get();
    RootConfig CFG = new RootConfig(plugin);
    AntiGrinder module = new AntiGrinder(CFG, new EntityModule(plugin), new BlockModule(plugin), new UtilityModule(plugin));

    @Before
    public void prepare()
    {
        //Enable AntiGrinder in the Config
        CFG.set("world", RootNode.INHIBIT_MONSTER_GRINDERS, true);
    }

    @Test
    public void spawnerSpawns()
    {
        CreatureSpawnEvent event = new MockCreatureSpawnEvent(EntityType.BLAZE, "world", CreatureSpawnEvent.SpawnReason.SPAWNER).get();
        assertFalse("Spawners should drop no exp", module.onEntitySpawn(event));

        event = new MockCreatureSpawnEvent(EntityType.ENDERMAN, "world", CreatureSpawnEvent.SpawnReason.SPAWNER).get();
        assertFalse("Spawners should drop no exp", module.onEntitySpawn(event));
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

        //Set the Environment to OverWorld
        MockWorld world = event.getWorld();
        world.setEnvironment(World.Environment.NORMAL);

        assertTrue("Zombie spawn succeeds", module.onEntitySpawn(event.get()));
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


        world.setEnvironment(World.Environment.NETHER);

        //Cobble is not natural for the nether
        relative.setMaterial(Material.COBBLESTONE);
        block.setRelative(BlockFace.DOWN, relative.get());

        assertFalse( "Natural spawn in a not natural Location succeeded", module.onEntitySpawn(event.get()));


        //NetherRack doesn't spawn in the OverWorld
        relative.setMaterial(Material.NETHERRACK);
        block.setRelative(BlockFace.DOWN, relative.get());

        world.setEnvironment(World.Environment.NORMAL);

        assertFalse( "Spawn on NetherRack in the OverWorld should have failed", module.onEntitySpawn(event.get()));
    }
}
