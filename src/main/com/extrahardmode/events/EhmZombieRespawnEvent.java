package com.extrahardmode.events;

import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * This Event is called everytime Ehm determines if it should respawn a Zombie.
 *
 * @author Diemex
 */
public class EhmZombieRespawnEvent extends Event implements Cancellable
{
    private final Player player;
    private final Zombie zombie;
    private final int respawnChance;
    private boolean cancelled = false;

    /**
     * Your constructor of choice
     *
     * @param player killer if it was a Player kill
     * @param zombie that was killed
     * @param respawnChance percentage that was used
     */
    public EhmZombieRespawnEvent(final Player player, final Zombie zombie, int respawnChance)
    {
        this.player = player;
        this.zombie = zombie;
        this.respawnChance = respawnChance;
    }

    /**
     * Your constructor of choice
     *
     * @param player killer if it was a Player kill
     * @param zombie that was killed
     * @param respawnChance percentage that was used
     * @param cancelled if the Event is cancelled by default
     */
    public EhmZombieRespawnEvent(final Player player, final Zombie zombie, int respawnChance, boolean cancelled)
    {
        this(player, zombie, respawnChance);
        this.cancelled = cancelled;
    }

    /**
     * @return zombie which was killed
     */
    public Zombie getZombie() {
        return zombie;
    }

    /**
     * @return the Player, note: can be null if it was no Player kill
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Use this if you want to change something based on the percentage
     *
     * @return the percentage that was used to calculate if the Zombie should respawn
     */
    public int getRespawnChance() {
        return respawnChance;
    }

    /**
     * @return if the Event is cancelled, if cancelled the Zombie won't be respawned
     */
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * @param cancelled if true Zombie won't be respawned
     */
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    private static final HandlerList HANDLERS = new HandlerList();

    public HandlerList getHandlers()
    {
        return HANDLERS;
    }

    public static HandlerList getHandlerList()
    {
        return HANDLERS;
    }
}
