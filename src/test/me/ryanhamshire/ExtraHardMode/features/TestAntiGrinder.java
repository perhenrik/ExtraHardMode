package me.ryanhamshire.ExtraHardMode.features;

import me.ryanhamshire.ExtraHardMode.ExtraHardMode;
import me.ryanhamshire.ExtraHardMode.config.RootConfig;
import me.ryanhamshire.ExtraHardMode.mocks.*;
import me.ryanhamshire.ExtraHardMode.mocks.events.MockCreatureSpawnEvent;
import me.ryanhamshire.ExtraHardMode.module.BlockModule;
import me.ryanhamshire.ExtraHardMode.module.EntityModule;
import me.ryanhamshire.ExtraHardMode.module.UtilityModule;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Antigrinder.class)
public class TestAntiGrinder
{
    ExtraHardMode plugin = new MockExtraHardMode().get();
    RootConfig CFG = new RootConfig();
    Antigrinder module = new Antigrinder(CFG, new EntityModule(plugin), new BlockModule(plugin), new UtilityModule(plugin));

    @Test
    public void spawnerSpawns()
    {
        CreatureSpawnEvent event = new MockCreatureSpawnEvent(EntityType.BLAZE, "world", CreatureSpawnEvent.SpawnReason.SPAWNER).get();
        assertTrue( "Spawners drop no exp", module.onEntitySpawn(event));

        event = new MockCreatureSpawnEvent(EntityType.ENDERMAN, "world", CreatureSpawnEvent.SpawnReason.SPAWNER).get();
        assertTrue( "Normal Spawns not blocked", module.onEntitySpawn(event));
    }

    @Test
    public void zombieSpawns()
    {
        CreatureSpawnEvent event = new MockCreatureSpawnEvent(EntityType.ZOMBIE, "world", CreatureSpawnEvent.SpawnReason.VILLAGE_INVASION).get();
        assertTrue( "Zombie spawn suceeds", module.onEntitySpawn(event));
    }

    @Test
    public void naturalSpawns()
    {
        CreatureSpawnEvent event = new MockCreatureSpawnEvent(EntityType.ZOMBIE, "world", CreatureSpawnEvent.SpawnReason.NATURAL).get();
        assertTrue( "Normal spawn suceeds", module.onEntitySpawn(event));
    }
}
