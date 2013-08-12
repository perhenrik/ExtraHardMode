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
import com.extrahardmode.module.BlockModule;
import com.extrahardmode.module.MsgModule;
import com.extrahardmode.module.PlayerModule;
import com.extrahardmode.module.UtilityModule;
import com.extrahardmode.service.Feature;
import com.extrahardmode.service.ListenerModule;
import com.extrahardmode.service.PermissionNode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;

/**
 * Limited building restricts building forcing players to think a bit harder:
 * <p/>
 * No straight pillaring up , No building while shifting over a corner , No building while jumping
 */
public class LimitedBuilding extends ListenerModule
{
    private final ExtraHardMode plugin;

    private final RootConfig CFG;

    private final UtilityModule utils;

    private final MsgModule messenger;

    private final PlayerModule playerModule;


    public LimitedBuilding(ExtraHardMode plugin)
    {
        super(plugin);
        this.plugin = plugin;
        CFG = plugin.getModuleForClass(RootConfig.class);
        utils = plugin.getModuleForClass(UtilityModule.class);
        messenger = plugin.getModuleForClass(MsgModule.class);
        playerModule = plugin.getModuleForClass(PlayerModule.class);
    }


    /**
     * FEATURE: players can't place blocks from weird angles (using shift to hover over in the air beyond the edge of
     * solid ground) or directly beneath themselves, for that matter
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onBlockPlace(BlockPlaceEvent placeEvent)
    {
        Player player = placeEvent.getPlayer();
        Block block = placeEvent.getBlock();
        World world = block.getWorld();

        final boolean limitedBlockPlacement = CFG.getBoolean(RootNode.LIMITED_BLOCK_PLACEMENT, world.getName());
        final boolean playerBypasses = playerModule.playerBypasses(player, Feature.LIMITED_BUILDING);

        if (!playerBypasses && limitedBlockPlacement)
        {
            Block playerBlock = player.getLocation().getBlock();
            Block underBlock = playerBlock.getRelative(BlockFace.DOWN);
            Block against = placeEvent.getBlockAgainst();

            //Blocks directly below player
            if (block.getX() == playerBlock.getX()
                    && block.getZ() == playerBlock.getZ()
                    && block.getY() < playerBlock.getY())
            {
                //TODO EhmLimitedBuildingEvent Case.BENEATH_PLAYER
                messenger.send(player, MessageNode.REALISTIC_BUILDING_BENEATH, PermissionNode.SILENT_REALISTIC_BUILDING);
                placeEvent.setCancelled(true);
            }

            // if standing directly over lava, prevent placement
            else if ((underBlock.getType() == Material.AIR || underBlock.getType() == Material.LAVA || underBlock.getType() == Material.STATIONARY_LAVA)
                    && !(playerBlock.getType().name().contains("STEP") && playerBlock.getType().name().contains("STAIRS"))
                    && block.getRelative(BlockFace.DOWN).getType() == Material.AIR)
            {
                //TODO EhmLimitedBuildingEvent Case.PLAYER_ABOVE_UNSAFE_LOC
                messenger.send(player, MessageNode.REALISTIC_BUILDING, PermissionNode.SILENT_REALISTIC_BUILDING);
                placeEvent.setCancelled(true);
            }

            else if (BlockModule.isOffAxis(playerBlock, block, against))
            {
                messenger.send(player, MessageNode.REALISTIC_BUILDING, PermissionNode.SILENT_REALISTIC_BUILDING);
                placeEvent.setCancelled(true);
            }

            /* Fences and glasspanes are half placed as vertical half blocks allowing the player to build in the air */
            //Block placing of blocks on the side of the block on which the player is currently standing
            else if ((against.getX() == playerBlock.getX() && against.getZ() == playerBlock.getZ()) && (against.getX() != block.getX() || against.getZ() != block.getZ()))
            {
                messenger.send(player, MessageNode.REALISTIC_BUILDING, PermissionNode.SILENT_REALISTIC_BUILDING);
                placeEvent.setCancelled(true);
            }
        }
    }
}
