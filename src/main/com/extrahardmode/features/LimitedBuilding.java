/*
 * This file is part of
 * ExtraHardMode Server Plugin for Minecraft
 *
 * Copyright (C) 2012 Ryan Hamshire
 * Copyright (C) 2013 Diemex
 *
 * ExtraHardMode is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ExtraHardMode is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero Public License
 * along with ExtraHardMode.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.extrahardmode.features;


import com.extrahardmode.ExtraHardMode;
import com.extrahardmode.config.RootConfig;
import com.extrahardmode.config.RootNode;
import com.extrahardmode.config.messages.MessageNode;
import com.extrahardmode.module.MessagingModule;
import com.extrahardmode.module.PlayerModule;
import com.extrahardmode.module.UtilityModule;
import com.extrahardmode.service.PermissionNode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class LimitedBuilding implements Listener
{
    ExtraHardMode plugin;
    RootConfig CFG;
    UtilityModule utils;
    MessagingModule messenger;
    PlayerModule playerModule;

    public LimitedBuilding (ExtraHardMode plugin)
    {
        this.plugin = plugin;
        CFG = plugin.getModuleForClass(RootConfig.class);
        utils = plugin.getModuleForClass(UtilityModule.class);
        messenger = plugin.getModuleForClass(MessagingModule.class);
        playerModule = plugin.getModuleForClass(PlayerModule.class);
    }

    /**
     * FEATURE: players can't place blocks from weird angles (using shift to
     * hover over in the air beyond the edge of solid ground)
     * or directly beneath themselves, for that matter
     * @param placeEvent
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onBlockPlace (BlockPlaceEvent placeEvent)
    {
        Player player = placeEvent.getPlayer();
        Block block = placeEvent.getBlock();
        World world = block.getWorld();

        final boolean limitedBlockPlacement = CFG.getBoolean(RootNode.LIMITED_BLOCK_PLACEMENT, world.getName());
        final boolean playerBypasses = playerModule.playerBypasses(player, Feature.LIMITED_BUILDING);

        if (!playerBypasses && limitedBlockPlacement)
        {
            if (block.getX() == player.getLocation().getBlockX()
                && block.getZ() == player.getLocation().getBlockZ()
                && block.getY() < player.getLocation().getBlockY())
            {
                messenger.notifyPlayer(player, MessageNode.REALISTIC_BUILDING, PermissionNode.SILENT_REALISTIC_BUILDING);
                placeEvent.setCancelled(true);
                return;
            }

            Block playerBlock = player.getLocation().getBlock();
            Block underBlock = playerBlock.getRelative(BlockFace.DOWN);

            // if standing directly over lava, prevent placement
            if((underBlock.getType() == Material.AIR || underBlock.getType() == Material.LAVA || underBlock.getType() == Material.STATIONARY_LAVA)
                    && (!playerBlock.getType().name().contains("STEP") && !playerBlock.getType().name().contains("STAIRS")))
            {
                messenger.notifyPlayer(player, MessageNode.REALISTIC_BUILDING, PermissionNode.SILENT_REALISTIC_BUILDING);
                placeEvent.setCancelled(true);
                return;
            }

            // otherwise if hovering over air, check one block lower
            else if (underBlock.getType() == Material.AIR && (!playerBlock.getType().name().contains("STEP") && !playerBlock.getType().name().contains("STAIRS")))
            {
                underBlock = underBlock.getRelative(BlockFace.DOWN);

                // if over lava or more air, prevent placement
                if (underBlock.getType() == Material.AIR || underBlock.getType() == Material.LAVA || underBlock.getType() == Material.STATIONARY_LAVA)
                {
                    messenger.notifyPlayer(player, MessageNode.REALISTIC_BUILDING, PermissionNode.SILENT_REALISTIC_BUILDING);
                    placeEvent.setCancelled(true);
                    return;
                }
            }
        }
    }
}
