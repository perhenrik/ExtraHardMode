package com.extrahardmode.features;


import org.bukkit.World;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;

/**
 * @author Diemex
 */
public class Tutorial implements Listener
{
    /**
     * When an Entity targets another Entity
     * <p/>
     * Display some warnings to a Player when he is targetted by a dangerous mob
     */
    @EventHandler
    public void onEntityTarget(EntityTargetLivingEntityEvent event)
    {
        if (event.getTarget() instanceof Player)
        {
            Player player = (Player) event.getTarget();
            World world = player.getWorld();

            switch (event.getEntity().getType())
            {
                case CREEPER:
                {
                    Creeper creeper = (Creeper) event.getEntity();
                    if (creeper.isPowered())
                    {
                        //TODO send msg
                    }
                }
                case BLAZE:
                {
                    switch (world.getEnvironment())
                    {
                        case NORMAL:
                            //TODO
                            break;
                        case NETHER:
                            //TODO
                            break;
                    }
                }
                case GHAST:
                {
                    //TODO Run! More Loot
                    break;
                }
                case PIG_ZOMBIE:
                {
                    //TODO always aggressive
                    //TODO very strong
                    //TODO drop netherwart
                }
                case MAGMA_CUBE:
                {
                    //TODO Explode when hit!
                }
                case SKELETON:
                {
                    //TODO No Arrows
                    //TODO shoot silverfish
                }
                case SPIDER:
                {
                    //TODO web
                }
                case WITCH:
                {
                    //TODO zombies, poison explosions
                }
                case ENDERMAN:
                {
                    //TODO you picked the wrong fight
                }
                case ZOMBIE:
                {
                    //TODO SlowPlayers, Respawn
                }
            }
        }
    }

    //TODO BobsTeleportAwayEvent

    //TODO ZombieRespawnEvent

    //TODO CreeperDropTntEvent

    //TODO Netherwarning (Portcreate, NetherEnter)

    //TODO SkeletonDeflectArrowEvent

    //TODO FillBucketEvent Can pick but can't place

    //TODO Farming: NetherWart, Mushrooms, !!!Arid Desserts

    //TODO OnSheepDye
}
