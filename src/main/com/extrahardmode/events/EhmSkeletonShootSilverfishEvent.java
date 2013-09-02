package com.extrahardmode.events;


import com.extrahardmode.features.monsters.skeletors.CustomSkeleton;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when a Skeleton deflects an arrow, e.g doesn't take damage fom an Arrow
 *
 * @author Diemex
 */
public class EhmSkeletonShootSilverfishEvent extends Event implements Cancellable
{
    private final CustomSkeleton type;
    private boolean cancelled;
    private final Player player;
    private final Skeleton skeleton;
    private final Creature silverfish;
    private final int shootSilverfishPercent;


    /**
     * Constructor
     *
     * @param player            Player being shot at
     * @param skeleton          Skeleton shooting the Silverfish
     * @param silverfish        the Silverfish being release by the Skeli
     * @param releasePercentage percentage of how often Silverfish get released
     */
    public EhmSkeletonShootSilverfishEvent(Player player, Skeleton skeleton, Creature silverfish, int releasePercentage, CustomSkeleton type)
    {
        this.player = player;
        this.skeleton = skeleton;
        this.silverfish = silverfish;
        this.shootSilverfishPercent = releasePercentage;
        this.type = type;
    }


    /**
     * Constructor
     *
     * @param player            Player being shot at
     * @param skeleton          Skeleton shooting the Silverfish
     * @param silverfish        the Silverfish being release by the Skeli
     * @param releasePercentage percentage of how often Silverfish get released
     * @param cancelled         if Event should be cancelled by default
     */
    public EhmSkeletonShootSilverfishEvent(Player player, Skeleton skeleton, Creature silverfish, int releasePercentage, CustomSkeleton type, boolean cancelled)
    {
        this(player, skeleton, silverfish, releasePercentage, type);
        this.cancelled = cancelled;
    }


    /**
     * @return the Skeleton that is being shot at
     */
    public Skeleton getSkeleton()
    {
        return skeleton;
    }


    /**
     * @return the Player shooting the Skeleton
     */
    public Player getPlayer()
    {
        return player;
    }


    /**
     * Get the Silverfish being spawned by the Skeleton
     *
     * @return silverfish
     */
    public Creature getSilverfish()
    {
        return silverfish;
    }


    /**
     * Get the percentage of Silverfish being released
     *
     * @return percentage
     */
    public int getShootSilverfishPercent()
    {
        return shootSilverfishPercent;
    }


    /**
     * @return the percentage of arrows getting deflected
     */
    public int getReleasePercentage()
    {
        return shootSilverfishPercent;
    }


    public CustomSkeleton getType()
    {
        return type;
    }


    /**
     * @return if Event got cancelled
     */
    @Override
    public boolean isCancelled()
    {
        return cancelled;
    }


    /**
     * @param cancelled set if the Event is cancelled which mean that the Skeleton will take normal damage
     */
    @Override
    public void setCancelled(boolean cancelled)
    {
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