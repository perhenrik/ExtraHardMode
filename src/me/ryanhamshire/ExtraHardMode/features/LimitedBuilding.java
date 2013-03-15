package me.ryanhamshire.ExtraHardMode.features;


import me.ryanhamshire.ExtraHardMode.ExtraHardMode;
import me.ryanhamshire.ExtraHardMode.config.RootConfig;
import me.ryanhamshire.ExtraHardMode.config.RootNode;
import me.ryanhamshire.ExtraHardMode.config.messages.MessageNode;
import me.ryanhamshire.ExtraHardMode.module.UtilityModule;
import me.ryanhamshire.ExtraHardMode.service.PermissionNode;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.material.Torch;

public class LimitedBuilding implements Listener
{
    ExtraHardMode plugin = null;
    RootConfig rootC = null;
    UtilityModule utils = null;

    public LimitedBuilding (ExtraHardMode plugin)
    {
        this.plugin = plugin;
        rootC = plugin.getModuleForClass(RootConfig.class);
        utils = plugin.getModuleForClass(UtilityModule.class);
    }

    /**
     * FEATURE: players can't place blocks from weird angles (using shift to
     * hover over in the air beyond the edge of solid ground)
     * or directly beneath themselves, for that matter
     * @param placeEvent
     */
    @EventHandler
    public void onBlockPlace (BlockPlaceEvent placeEvent)
    {
        Player player = placeEvent.getPlayer();
        Block block = placeEvent.getBlock();
        World world = block.getWorld();

        if (rootC.getBoolean(RootNode.LIMITED_BLOCK_PLACEMENT) & !player.getGameMode().equals(GameMode.CREATIVE))
        {
            if (block.getX() == player.getLocation().getBlockX() && block.getZ() == player.getLocation().getBlockZ()
                    && block.getY() < player.getLocation().getBlockY())
            {
                utils.notifyPlayer(player, MessageNode.REALISTIC_BUILDING, PermissionNode.SILENT_REALISTIC_BUILDING);
                placeEvent.setCancelled(true);
                return;
            }

            Block playerBlock = player.getLocation().getBlock();
            Block underBlock = playerBlock.getRelative(BlockFace.DOWN);

            // if standing directly over lava, prevent placement
            if((underBlock.getType() == Material.AIR || underBlock.getType() == Material.LAVA || underBlock.getType() == Material.STATIONARY_LAVA)
                    && (!playerBlock.getType().name().contains("STEP") && !playerBlock.getType().name().contains("STAIRS")))
            {
                utils.notifyPlayer(player, MessageNode.REALISTIC_BUILDING, PermissionNode.SILENT_REALISTIC_BUILDING);
                placeEvent.setCancelled(true);
                return;
            }

            // otherwise if hovering over air, check one block lower
            else if (underBlock.getType() == Material.AIR)
            {
                underBlock = underBlock.getRelative(BlockFace.DOWN);

                // if over lava or more air, prevent placement
                if (underBlock.getType() == Material.AIR || underBlock.getType() == Material.LAVA || underBlock.getType() == Material.STATIONARY_LAVA)
                {
                    utils.notifyPlayer(player, MessageNode.REALISTIC_BUILDING, PermissionNode.SILENT_REALISTIC_BUILDING);
                    placeEvent.setCancelled(true);
                    return;
                }
            }
        }
    }
}
