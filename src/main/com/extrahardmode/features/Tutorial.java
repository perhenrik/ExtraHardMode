package com.extrahardmode.features;


import com.extrahardmode.ExtraHardMode;
import com.extrahardmode.config.messages.MessageNode;
import com.extrahardmode.events.*;
import com.extrahardmode.module.BlockModule;
import com.extrahardmode.module.MessagingModule;
import com.extrahardmode.module.PlayerModule;
import com.extrahardmode.service.FindAndReplace;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Diemex
 */
public class Tutorial implements Listener
{
    private final ExtraHardMode plugin;

    private final MessagingModule messenger;

    private final BlockModule blockModule;

    private final PlayerModule playerModule;


    public Tutorial(ExtraHardMode plugin)
    {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.messenger = plugin.getModuleForClass(MessagingModule.class);
        this.blockModule = plugin.getModuleForClass(BlockModule.class);
        this.playerModule = plugin.getModuleForClass(PlayerModule.class);
    }

    /**
     * When an Entity targets another Entity
     * <p/>
     * Display some warnings to a Player when he is targetted by a dangerous mob
     */
    @EventHandler
    public void onEntityTarget(EntityTargetEvent event)
    {
        if (event.getTarget() instanceof Player)
        {
            final Player player = (Player) event.getTarget();
            final World world = player.getWorld();

            switch (event.getEntity().getType())
            {
                case CREEPER:
                {
                    Creeper creeper = (Creeper) event.getEntity();
                    if (creeper.isPowered())
                        messenger.send(player, MessageNode.CHARGED_CREEPER_TARGET, MessageNode.MsgType.TUTORIAL);
                    break;
                }
                case BLAZE:
                {
                    switch (world.getEnvironment())
                    {
                        case NORMAL:
                            messenger.send(player, MessageNode.BLAZE_TARGET_NORMAL, MessageNode.MsgType.TUTORIAL);
                            break;
                        case NETHER:
                            messenger.send(player, MessageNode.BLAZE_TARGET_NETHER, MessageNode.MsgType.TUTORIAL);
                            break;
                    }
                    break;
                }
                case GHAST:
                {
                    messenger.send(player, MessageNode.GHAST_TARGET, MessageNode.MsgType.TUTORIAL);
                    break;
                }
                case PIG_ZOMBIE:
                {
                    messenger.send(player, MessageNode.PIGZOMBIE_TARGET, MessageNode.MsgType.TUTORIAL);
                    plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            messenger.send(player, MessageNode.PIGZOMBIE_TARGET_WART, MessageNode.MsgType.TUTORIAL);
                        }
                    }, 300L);
                    break;
                }
                case MAGMA_CUBE:
                {
                    messenger.send(player, MessageNode.PIGZOMBIE_TARGET, MessageNode.MsgType.TUTORIAL);
                    break;
                }
                case SKELETON:
                {
                    //TODO shoot silverfish
                    break;
                }
                case SPIDER:
                {
                    //TODO web
                    break;
                }
                case WITCH:
                {
                    //TODO zombies, poison explosions
                    break;
                }
                case ENDERMAN:
                {
                    messenger.send(player, MessageNode.ENDERMAN_GENERAL, MessageNode.MsgType.TUTORIAL);
                    break;
                }
                case ZOMBIE:
                {
                    messenger.send(player, MessageNode.ZOMBIE_SLOW_PLAYERS, MessageNode.MsgType.TUTORIAL);
                    break;
                }
            }
        }
    }


    /**
     * When an Enderman teleports a Player for the first time, cancel the Event and inform the Player of his imminent
     * death
     */
    @EventHandler
    public void onEndermanTeleport(EhmEndermanTeleportEvent event)
    {
        final Player player = event.getPlayer();
        final Enderman enderman = event.getEnderman();

        messenger.send(player, MessageNode.ENDERMAN_GENERAL, MessageNode.MsgType.TUTORIAL);
        if (playerModule.getArmorPoints(player) < 0.32) //basically leather armor
        {
            enderman.setTarget(null); //give new Players a chance if they don't know yet
            messenger.send(player, MessageNode.ENDERMAN_SUICIDAL, MessageNode.MsgType.TUTORIAL);
        }
    }


    /**
     * Inform Players about the respawning Zombies
     */
    @EventHandler(ignoreCancelled = true)
    public void onZombieRespawn(EhmZombieRespawnEvent event)
    {
        final Player player = event.getPlayer();
        if (player != null)
        {
            messenger.send(player, MessageNode.ZOMBIE_RESPAWN, MessageNode.MsgType.TUTORIAL);
        }
    }


    /**
     * Warn players before entering the nether
     */
    @EventHandler
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event)
    {
        final Player player = event.getPlayer();
        if (player.getWorld().getEnvironment() == World.Environment.NETHER)
        {
            messenger.send(player, MessageNode.NETHER_WARNING, MessageNode.MsgType.TUTORIAL);
        }
    }


    /**
     * Inform Players about creepers dropping tnt
     */
    @EventHandler
    public void onCreeperDropTnt(EhmCreeperDropTntEvent event)
    {
        final Player player = event.getPlayer();
        if (player != null)
        {
            messenger.send(player, MessageNode.CREEPER_DROP_TNT, MessageNode.MsgType.TUTORIAL);
        }
    }


    /**
     * When a Skeleton deflects an arrow
     */
    @EventHandler(ignoreCancelled = true)
    public void onSkeletonDeflect(EhmSkeletonDeflectEvent event)
    {
        if (event.getShooter() != null)
        {
            final Player player = event.getShooter();
            messenger.send(player, MessageNode.SKELETON_DEFLECT, MessageNode.MsgType.TUTORIAL);
        }
    }


    /**
     * Let Players know that they can use ice
     */
    @EventHandler
    public void onPlayerFillBucket(PlayerBucketFillEvent event)
    {
        final Player player = event.getPlayer();
        messenger.send(player, MessageNode.BUCKET_FILL, MessageNode.MsgType.TUTORIAL);
    }


    /**
     * Messages when planting with antifarming
     */
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event)
    {
        final Player player = event.getPlayer();
        final Block block = event.getBlock();
        //Too dark
        if (block.getType() == Material.SOIL)
        {
            Block above = block.getRelative(BlockFace.UP);
            if (above.getLightFromSky() < 10)
            {
                messenger.send(player, MessageNode.ANTIFARMING_NO_LIGHT, MessageNode.MsgType.TUTORIAL);
            }
        }

        Block below = block.getRelative(BlockFace.DOWN);

        //Unwatered
        if (blockModule.isPlant(block.getType()) && below.getState().getData().getData() == (byte) 0)
        {
            messenger.send(player, MessageNode.ANTIFARMING_UNWATERD, MessageNode.MsgType.TUTORIAL);
        }

        //Warn players before they build big farms in the desert
        if (block.getType() == Material.DIRT)
        {
            switch (block.getBiome())
            {
                case DESERT:
                case DESERT_HILLS:
                {
                    messenger.send(player, MessageNode.ANTIFARMING_DESSERT_WARNING, MessageNode.MsgType.TUTORIAL);
                    break;
                }
            }
        }
    }


    /**
     * Inform about Silverfish
     */
    @EventHandler
    public void onShootSilverfish(EhmSkeletonShootSilverfishEvent event)
    {
        //event.getSilverfish().addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 1, 1000));
        //event.getSilverfish().setFireTicks(100);
    }


    /**
     * Inform about not being able to extinguish fire with bare hands
     */
    @EventHandler
    public void onExtinguishFire(EhmPlayerExtinguishFireEvent event)
    {
        messenger.send(event.getPlayer(), MessageNode.EXTINGUISH_FIRE, MessageNode.MsgType.BROADCAST);
    }


    /**
     *
     * @param event
     */
    @EventHandler
    public void onPlayerInventoryLoss(EhmPlayerInventoryLossEvent event)
    {
        StringBuilder items = new StringBuilder();

        //Merge the item amounts: 1 stone, 1 stone => 2 stones
        List<ItemStack> lostItems = new ArrayList<ItemStack>();
        for (int i = 0; i < event.getStacksToRemove().size(); i++)
        {
            ItemStack item = event.getStacksToRemove().get(i);
            //Does an item of the same type exist already?
            int index = -1;
            for (int lostI = 0; lostI < lostItems.size(); lostI++)
            {
                ItemStack lost = lostItems.get(lostI);
                if (lost.getType() == item.getType())
                {
                    index = lostI;
                    break;
                }
            }
            if (index >= 0)
                lostItems.get(index).setAmount(lostItems.get(index).getAmount() + item.getAmount());
            else
                lostItems.add(item);

        }

        //Build the output String
        for (ItemStack item : lostItems)
        {
            if (items.length() > 0) items.append(", ");
            {
                items.append(item.getAmount());
                items.append(' ');
                items.append(item.getType().name().toLowerCase());
                if (item.getAmount() > 1)
                    items.append('s');
            }
        }

        //Only print if items have been removed
        if (event.getStacksToRemove().size() > 0)
        {
            messenger.broadcast(MessageNode.LOST_ITEMS,
                    new FindAndReplace(MessageNode.variables.PLAYER.getVarName(), event.getPlayer().getName()),
                    new FindAndReplace(MessageNode.variables.ITEMS.getVarName(), items.toString()));
            messenger.send(event.getPlayer(), MessageNode.LOST_ITEMS_PLAYER, MessageNode.MsgType.TUTORIAL);
        }
    }

    //TODO Farming: NetherWart, Mushrooms

    //TODO OnSheepDye
}
