package me.ryanhamshire.ExtraHardMode.features;


import me.ryanhamshire.ExtraHardMode.ExtraHardMode;
import me.ryanhamshire.ExtraHardMode.config.DynamicConfig;
import me.ryanhamshire.ExtraHardMode.config.RootNode;
import me.ryanhamshire.ExtraHardMode.config.messages.MessageNode;
import me.ryanhamshire.ExtraHardMode.module.UtilityModule;
import me.ryanhamshire.ExtraHardMode.service.PermissionNode;
import me.ryanhamshire.ExtraHardMode.task.RemoveExposedTorchesTask;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.material.Torch;

public class Torches implements Listener
{
    ExtraHardMode plugin = null;
    DynamicConfig dynC = null;
    UtilityModule utils = null;

    public Torches(ExtraHardMode plugin)
    {
        this.plugin = plugin;
        dynC = plugin.getModuleForClass(DynamicConfig.class);
        utils = plugin.getModuleForClass(UtilityModule.class);
    }

    @EventHandler
    public void onBlockPlace (BlockPlaceEvent placeEvent)
    {
        Player player = placeEvent.getPlayer();
        Block block = placeEvent.getBlock();
        World world = block.getWorld();

        final boolean limitedTorchPlacement = dynC.getBoolean(RootNode.LIMITED_TORCH_PLACEMENT, world.getName());
        final boolean soundFizzEnabled = dynC.getBoolean(RootNode.SOUNDS_TORCH_FIZZ, world.getName());
        final int torchMinY = dynC.getInt(RootNode.STANDARD_TORCH_MIN_Y, world.getName());
        final boolean playerPerm = player != null ? player.hasPermission(PermissionNode.BYPASS.getNode())
                                   || player.getGameMode().equals(GameMode.CREATIVE) : true;

        // FEATURE: players can't attach torches to common "soft" blocks
        if (block.getType().equals(Material.TORCH) && limitedTorchPlacement &! playerPerm)
        {
            Torch torch = new Torch(Material.TORCH, block.getData());
            Material attachmentMaterial = block.getRelative(torch.getAttachedFace()).getType();

            if (attachmentMaterial == Material.DIRT || attachmentMaterial == Material.GRASS || attachmentMaterial == Material.LONG_GRASS
                    || attachmentMaterial == Material.SAND)
            {
                if (soundFizzEnabled)
                {
                    utils.notifyPlayer(player, MessageNode.LIMITED_TORCH_PLACEMENTS, PermissionNode.SILENT_LIMITED_TORCH_PLACEMENT, Sound.FIZZ, 20);
                }
                placeEvent.setCancelled(true);
            }
        }

        // FEATURE: no standard torches, jack o lanterns, or fire on top of netherrack near diamond level
        if (torchMinY > 0 &! playerPerm)
        {
            if (world.getEnvironment() == World.Environment.NORMAL
                    && block.getY() < torchMinY
                    && (block.getType() == Material.TORCH || block.getType() == Material.JACK_O_LANTERN || (block.getType() == Material.FIRE && block
                    .getRelative(BlockFace.DOWN).getType() == Material.NETHERRACK)))
            {
                utils.notifyPlayer(player, MessageNode.NO_TORCHES_HERE, PermissionNode.SILENT_NO_TORCHES_HERE, Sound.FIZZ, 20);
                placeEvent.setCancelled(true);
                return;
            }
        }
    }

    /**
     * When the weather changes...
     * rainfall breaks exposed torches (exposed to the sky)
     * @param event - Event that occurred.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onWeatherChange(WeatherChangeEvent event)
    {
        World world = event.getWorld();

        final boolean rainBreaksTorchesEnabled = dynC.getBoolean(RootNode.RAIN_BREAKS_TORCHES, world.getName());
        final boolean snowBreaksCrops = dynC.getBoolean(RootNode.SNOW_BREAKS_CROPS, world.getName());

        if (event.toWeatherState()) //is it raining
        {
            if (rainBreaksTorchesEnabled || snowBreaksCrops)
            {
                // plan to remove torches chunk by chunk gradually throughout the rainperiod
                Chunk[] chunks = world.getLoadedChunks();
                if (chunks.length > 0)
                {
                    int startOffset = plugin.getRandom().nextInt(chunks.length);
                    for (int i = 0; i < chunks.length; i++)
                    {
                        Chunk chunk = chunks[(startOffset + i) % chunks.length];

                        RemoveExposedTorchesTask task = new RemoveExposedTorchesTask(plugin, chunk);
                        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, task, i * 20L);
                    }
                }
            }
        }
    }
}
