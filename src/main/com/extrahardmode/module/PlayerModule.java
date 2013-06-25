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

package com.extrahardmode.module;


import com.extrahardmode.ExtraHardMode;
import com.extrahardmode.config.RootConfig;
import com.extrahardmode.config.RootNode;
import com.extrahardmode.features.Feature;
import com.extrahardmode.service.EHMModule;
import org.apache.commons.lang.Validate;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

/**
 * Player centric actions
 *
 * @author Max
 */
public class PlayerModule extends EHMModule
{
    private final RootConfig CFG;


    /**
     * Constructor
     */
    public PlayerModule(ExtraHardMode plugin)
    {
        super(plugin);
        CFG = plugin.getModuleForClass(RootConfig.class);
    }


    public boolean playerBypasses(Player player, Feature feature)
    {
        Validate.notNull(player, "We can't check if a Player bypasses if there is no Player!");

        final boolean bypassPermsEnabled = CFG.getBoolean(RootNode.BYPASS_PERMISSION, player.getWorld().getName());
        final boolean opsBypass = CFG.getBoolean(RootNode.BYPASS_OPS, player.getWorld().getName());
        final boolean creativeBypasses = CFG.getBoolean(RootNode.BYPASS_CREATIVE, player.getWorld().getName());

        boolean bypasses = false;

        if (bypassPermsEnabled)
            bypasses = player.hasPermission(feature.getBypassNode().getNode());
        if (!bypasses && opsBypass)
            bypasses = player.isOp();
        if (!bypasses && creativeBypasses)
            bypasses = player.getGameMode().equals(GameMode.CREATIVE);

        return bypasses;
    }


    @Override
    public void starting()
    {
    }


    @Override
    public void closing()
    {
    }
}
