package com.extrahardmode.features;

import com.extrahardmode.ExtraHardMode;
import com.extrahardmode.config.RootNode;
import com.extrahardmode.module.BlockModule;
import com.extrahardmode.service.PermissionNode;
import com.extrahardmode.config.RootConfig;
import com.extrahardmode.module.EntityModule;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class Physics implements Listener
{
    ExtraHardMode plugin;
    RootConfig CFG;
    BlockModule blockModule;
    EntityModule entityModule;

    public Physics (ExtraHardMode plugin)
    {
        this.plugin = plugin;
        CFG = plugin.getModuleForClass(RootConfig.class);
        blockModule = plugin.getModuleForClass(BlockModule.class);
        entityModule = plugin.getModuleForClass(EntityModule.class);
    }
    /**
     * When a player places a block...
     *
     * @param placeEvent - Event that occurred
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH/*so this gets called after the building limitations*/)
    public void onBlockPlace(BlockPlaceEvent placeEvent)
    {
        Player player = placeEvent.getPlayer();
        Block block = placeEvent.getBlock();
        World world = block.getWorld();

        final boolean physixEnabled = CFG.getBoolean(RootNode.MORE_FALLING_BLOCKS_ENABLE, world.getName())
                                      &&! player.hasPermission(PermissionNode.BYPASS.getNode())
                                      &&! player.getGameMode().equals(GameMode.CREATIVE);

        if (physixEnabled)
        {
            blockModule.physicsCheck(block, 10, true, 0);
        }
    }

    /**
     * When a player breaks a block...
     *
     * @param breakEvent - Event that occurred.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent breakEvent)
    {
        Block block = breakEvent.getBlock();
        World world = block.getWorld();
        Player player = breakEvent.getPlayer();

        final boolean moreFallingBlocksEnabled = CFG.getBoolean(RootNode.MORE_FALLING_BLOCKS_ENABLE, world.getName());
        final int netherRackFirePercent = CFG.getInt(RootNode.BROKEN_NETHERRACK_CATCHES_FIRE_PERCENT, world.getName());
        final boolean playerHasBypass = player != null ? player.hasPermission(PermissionNode.BYPASS.getNode())
                                   || player.getGameMode().equals(GameMode.CREATIVE) : true;



        // FEATURE: more falling blocks
        if (moreFallingBlocksEnabled &&! playerHasBypass)
        {
            blockModule.physicsCheck(block, 10, true, 5);
        }

        // FEATURE: breaking netherrack may start a fire
        if (netherRackFirePercent > 0 && block.getType() == Material.NETHERRACK &&! playerHasBypass)
        {
            Block underBlock = block.getRelative(BlockFace.DOWN);
            if (underBlock.getType() == Material.NETHERRACK && plugin.random(netherRackFirePercent))
            {
                breakEvent.setCancelled(true);
                block.setType(Material.FIRE);
            }
        }
    }

    /**
     * Called when an Entity forms a Block
     * - Damage Player when a FallingBlock hits him
     * @param event
     */
    @EventHandler
    public void whenBlockLands(EntityChangeBlockEvent event)
    {
        Entity entity = event.getEntity();
        World world = entity.getWorld();

        final int damageAmount = CFG.getInt(RootNode.MORE_FALLING_BLOCKS_DMG_AMOUNT, world.getName());

        //Only when Block has been marked to deal damage
        if (entity.getType().equals(EntityType.FALLING_BLOCK) && damageAmount > 0 && entityModule.isMarkedForProcessing(entity))
        {
            List<Entity> entities =  entity.getNearbyEntities(0.1, 1, 0.1);
            for (Entity ent : entities)
            {
                if (ent instanceof LivingEntity)
                {
                    LivingEntity entityWithDamagedHead = (LivingEntity) ent;
                    //Frighten the player
                    entityWithDamagedHead.damage(damageAmount, entity);
                    entityWithDamagedHead.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 140, 10));
                }
            }
        }
    }

}
