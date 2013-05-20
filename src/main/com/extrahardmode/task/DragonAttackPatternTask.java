/*
    ExtraHardMode Server Plugin for Minecraft
    Copyright (C) 2012 Ryan Hamshire

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


package com.extrahardmode.task;

import com.extrahardmode.ExtraHardMode;
import com.extrahardmode.config.RootConfig;
import com.extrahardmode.config.RootNode;
import com.extrahardmode.module.DataStoreModule;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Task to handle the dragon's attack pattern.
 */
//TODO move to dragon class
public class DragonAttackPatternTask implements Runnable
{
    /**
     * Plugin instance.
     */
    private ExtraHardMode plugin;
    /**
     * Config instance
     */
    private RootConfig CFG;
    /**
     * Target player.
     */
    private Player player;
    /**
     * Dragon entity.
     */
    private LivingEntity dragon;
    /**
     * We save the Players fighting the dragon here
     */
    DataStoreModule data;

    /**
     * Constructor.
     *
     * @param plugin                - plugin instance.
     * @param dragon                - Dragon.
     * @param player                - Target player.
     * @param playersFightingDragon - All fighting players.
     */
    public DragonAttackPatternTask(ExtraHardMode plugin, LivingEntity dragon, Player player, List<String> playersFightingDragon)
    {
        this.plugin = plugin;
        CFG = plugin.getModuleForClass(RootConfig.class);
        this.dragon = dragon;
        this.player = player;
        data = plugin.getModuleForClass(DataStoreModule.class);
    }

    @Override
    public void run()
    {
        if (this.dragon.isDead())
            return;

        World world = this.dragon.getWorld();

        final boolean dragonAnnouncements = CFG.getBoolean(RootNode.ENDER_DRAGON_COMBAT_ANNOUNCEMENTS, world.getName());

        // if the player has been defeated
        if (!this.player.isOnline() || world != this.player.getWorld() || this.player.isDead())
        {
            // restore some of the dragon's health
            int newHealth = (int) (this.dragon.getHealth() + this.dragon.getMaxHealth() * .25);
            if (newHealth > this.dragon.getMaxHealth())
            {
                this.dragon.setHealth(this.dragon.getMaxHealth());
            }
            else
            {
                this.dragon.setHealth(newHealth);
            }

            return;
        }

        for (int i = 0; i < 3; i++)
        {
            DragonAttackTask task = new DragonAttackTask(plugin, this.dragon, this.player);
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, task, 20L * i + (plugin.getRandom().nextInt(20)));
        }

        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, this, 20L * 30);
    }
}
