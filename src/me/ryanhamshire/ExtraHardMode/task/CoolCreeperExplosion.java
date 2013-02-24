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
import me.ryanhamshire.ExtraHardMode.module.UtilityModule;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Creeper;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;

/**
 * Launches a creeper into the air with fireworks and lets him explode midair
 */
public class CoolCreeperExplosion implements Runnable
{
    private Creeper deadBomber;
    private Creeper suicideBomber;
    private ExtraHardMode plugin;
    private UtilityModule utils;
    private BukkitScheduler scheduler;

    private final int numOfExplosions = 5;
    private final int ticksBetweenExplosions = 4;
    private final int ticksBeforeCatapult = 3;
    private final int ticksBeforeSuicide = 8;
    private int mainDelay = 0;

    public CoolCreeperExplosion(Creeper entity, ExtraHardMode plugin)
    {
        deadBomber = entity;
        this.plugin = plugin;
        utils = plugin.getModuleForClass(UtilityModule.class);
        scheduler = plugin.getServer().getScheduler();
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

        //For simplicity we let the creeper die and spawn a new one at the same location
        World world = deadBomber.getWorld();
        Location location = deadBomber.getLocation();
        suicideBomber = world.spawn(location, Creeper.class);
        if (suicideBomber instanceof Creeper)
        {
            if (deadBomber.isPowered())
                suicideBomber.setPowered(true);
        }
        //Everyone loves fireworks
        for (int i = 0; i < numOfExplosions; i++)
        {
            //TODO Method is deprecated but it doesn't suggest the new name in jdocs
            mainDelay += ticksBetweenExplosions;
            scheduler.scheduleAsyncDelayedTask(plugin, new Firework(), mainDelay);
        }
        //Catapult into air and explode midair
        mainDelay += ticksBeforeCatapult;
        scheduler.scheduleAsyncDelayedTask(plugin, new AscendToHeaven(), mainDelay);
    }

    private class Firework implements Runnable
    {
        @Override
        public void run()
        {
            utils.fireWorkRandomColors(FireworkEffect.Type.CREEPER, deadBomber.getLocation());
        }
    }

    /**
     * Give creeper helium and let hom slowly float upwards, toss a cigarette at him when he's at his highest and watch him go boom
     */
    private class AscendToHeaven implements Runnable
    {//Catapult Creeper into sky, afterwards explode in midair

        @Override
        public void run()
        {
            if (suicideBomber != null)
            {
                int ticksInbetween = 1;
                suicideBomber.setTarget(null);
                for (int i = 0; i < 10; i++)
                {
                    scheduler.scheduleAsyncDelayedTask(plugin, new RiseToGlory(), ticksInbetween);
                    ticksInbetween += ticksInbetween;
                }
                scheduler.scheduleAsyncDelayedTask(plugin, new Suicide(), ticksBeforeSuicide);
            }
        }
    }

    /**
     * Creeper slowly rises to the sky and will be delivered from his sins with a big bang
     */
    private class RiseToGlory implements Runnable
    {
        @Override
        public void run()
        {
            if (suicideBomber != null)
            {
                Vector holyGrail = suicideBomber.getVelocity().setY(0.5);
                suicideBomber.setVelocity(holyGrail);
            }
        }
    }

    /**
     * Creepers prize to pay is death! Kill him with a big bang.
     */
    private class Suicide implements Runnable
    {
        @Override
        public void run()
        {
            CreateExplosionTask boomBoom = new CreateExplosionTask(suicideBomber.getLocation(), 4F); //equal to tnt
            boomBoom.run();
            if (suicideBomber != null)
                suicideBomber.remove();
        }
    }

}