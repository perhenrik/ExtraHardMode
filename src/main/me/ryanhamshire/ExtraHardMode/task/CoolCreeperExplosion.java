/*
    ExtraHard
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
package me.ryanhamshire.ExtraHardMode.task;

import me.ryanhamshire.ExtraHardMode.ExtraHardMode;
import me.ryanhamshire.ExtraHardMode.config.RootConfig;
import me.ryanhamshire.ExtraHardMode.config.ExplosionType;
import me.ryanhamshire.ExtraHardMode.config.RootNode;
import me.ryanhamshire.ExtraHardMode.module.UtilityModule;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.Creeper;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;

/**
 * Launches a creeper into the air with fireworks and lets him explode midair
 */
public class CoolCreeperExplosion implements Runnable
{
    private Creeper creeper;
    private Location loc;
    private ExtraHardMode plugin;
    private UtilityModule utils;
    private BukkitScheduler scheduler;
    private RootConfig CFG;

    private int numOfFireworks = 3;
    private final int ticksBetweenFireworks = 5;
    private final int ticksBeforeCatapult = 3;
    private final int ticksBeforeSuicide = 8;
    private long mainDelay = 0;
    private double creeperAscendSpeed = 0.5;

    public CoolCreeperExplosion(Creeper entity, ExtraHardMode plugin)
    {
        this.plugin = plugin;
        CFG = plugin.getModuleForClass(RootConfig.class);
        creeper = entity;
        loc = creeper.getLocation();
        utils = plugin.getModuleForClass(UtilityModule.class);
        scheduler = plugin.getServer().getScheduler();
        numOfFireworks = CFG.getInt(RootNode.FLAMING_CREEPERS_FIREWORK, loc.getWorld().getName());
        creeperAscendSpeed = CFG.getDouble(RootNode.FLAMING_CREEPERS_ROCKET, loc.getWorld().getName());
    }

    /**
     * Contains the mainLogic for creating a cool explosion
     */
    @Override
    public void run()
    {
        /*
            Using Runnables and Asyncs seems to be the only way to introduce delays between actions without blocking the main thread
         */
        //Everyone loves fireworks
        for (int i = 0; i < numOfFireworks; i++)
        {
            mainDelay += ticksBetweenFireworks;
            scheduler.runTaskLater(plugin, new Firework(), mainDelay);
        }
        //Catapult into air and explode midair
        mainDelay += ticksBeforeCatapult;
        scheduler.runTaskLater(plugin, new AscendToHeaven(), mainDelay) ;
    }

    private class Firework implements Runnable
    {
        @Override
        public void run()
        {
            utils.fireWorkRandomColors(FireworkEffect.Type.CREEPER, loc);
        }
    }

    /**
     * Schedules multiple tasks to slowly let a creeper float upwards
     */
    private class AscendToHeaven implements Runnable
    {//Catapult Creeper into sky, afterwards explode in midair

        @Override
        public void run()
        {
            if (creeper != null)
            {
                int ticksInbetween = 1;
                creeper.setTarget(null);
                for (int i = 0; i < 10; i++)
                {
                    scheduler.runTaskLater(plugin, new RiseToGlory(), ticksInbetween);
                    ticksInbetween += ticksInbetween;
                }
                scheduler.runTaskLater(plugin, new Suicide(), ticksBeforeSuicide);
            }
        }
    }

    /**
     * This task slowly lets a creeper float upwards, it has to be called multiple times
     */
    private class RiseToGlory implements Runnable
    {
        @Override
        public void run()
        {
            if (creeper != null)
            {
                Vector holyGrail = creeper.getVelocity().setY(creeperAscendSpeed);
                creeper.setVelocity(holyGrail);
            }
        }
    }

    /**
     * Creeper explodes in midair
     */
    private class Suicide implements Runnable
    {
        @Override
        public void run()
        {
            if (creeper != null &&! creeper.isDead())
            {
                new CreateExplosionTask(plugin, creeper.getLocation(), ExplosionType.CREEPER, creeper);
            }
            if (creeper != null)
                creeper.remove();
        }
    }
}