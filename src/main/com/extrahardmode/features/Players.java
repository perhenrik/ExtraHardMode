package com.extrahardmode.features;

import com.extrahardmode.ExtraHardMode;
import com.extrahardmode.config.RootNode;
import com.extrahardmode.config.messages.MessageConfig;
import com.extrahardmode.module.EntityModule;
import com.extrahardmode.module.UtilityModule;
import com.extrahardmode.service.PermissionNode;
import com.extrahardmode.task.SetPlayerHealthAndFoodTask;
import com.extrahardmode.config.RootConfig;
import com.extrahardmode.module.DataStoreModule;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class Players implements Listener
{
    ExtraHardMode plugin = null;
    RootConfig CFG = null;
    MessageConfig messages;
    UtilityModule utils = null;
    EntityModule entityModule = null;

    public Players (ExtraHardMode plugin)
    {
        this.plugin = plugin;
        CFG = plugin.getModuleForClass(RootConfig.class);
        messages = plugin.getModuleForClass(MessageConfig.class);
        utils = plugin.getModuleForClass(UtilityModule.class);
        entityModule = plugin.getModuleForClass(EntityModule.class);
    }

    /**
     * FEATURE: respawning players start without full health or food
     *
     * @param respawnEvent - Event that occurred.
     */
    @EventHandler(ignoreCancelled = true)
    public void onPlayerRespawn(PlayerRespawnEvent respawnEvent)
    {
        Player player = respawnEvent.getPlayer();
        World world = respawnEvent.getPlayer().getWorld();

        final int respawnHealth = player.hasPermission(PermissionNode.BYPASS.getNode()) ? player.getMaxHealth()
                                  : CFG.getInt(RootNode.PLAYER_RESPAWN_HEALTH, world.getName());
        final int respawnFood = player.hasPermission(PermissionNode.BYPASS.getNode()) ? player.getMaxHealth()
                                : CFG.getInt(RootNode.PLAYER_RESPAWN_FOOD_LEVEL, world.getName());

        if (respawnFood < player.getMaxHealth() || respawnHealth < player.getMaxHealth()) //maxHealth and maxFoodLevel are both 20, but there is no method for maxFoodLevel
        {
            SetPlayerHealthAndFoodTask task = new SetPlayerHealthAndFoodTask(player, respawnHealth, respawnFood);
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, task, 10L); // half-second delay
        }
        // FEATURE: players can't swim when they're carrying a lot of weight, reset the cached value
        DataStoreModule.PlayerData playerData = plugin.getModuleForClass(DataStoreModule.class).getPlayerData(player.getName());
        playerData.cachedWeightStatus = -1F;
    }

    /**
     * When a Player dies he looses a percentage of his inventory
     * @param event
     */
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event)
    {
        LivingEntity entity = event.getEntity();
        World world = entity.getWorld();
        Player player = null;
        if (entity instanceof Player) player = (Player) entity;

        final int deathLossPercent = CFG.getInt(RootNode.PLAYER_DEATH_ITEM_STACKS_FORFEIT_PERCENT, world.getName());
        final boolean playerPerm = player != null ? player.hasPermission(PermissionNode.BYPASS_INVENTORY.getNode()) : true; //true: will cause the code not to run

        // FEATURE: some portion of player inventory is permanently lost on death
        if (!playerPerm)
        {
            List<ItemStack> drops = event.getDrops();
            int numberOfStacksToRemove = (int) (drops.size() * (deathLossPercent / 100f));
            for (int i = 0; i < numberOfStacksToRemove && drops.size() > 0; i++)
            {
                int indexOfStackToRemove = plugin.getRandom().nextInt(drops.size());
                drops.remove(indexOfStackToRemove);
                //TODO tools percentage, damage etc.
            }
        }

    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)//so we know if the event got cancelled
    public void onEntityDamage(EntityDamageEvent event)
    {
        Entity entity = event.getEntity();
        World world = entity.getWorld();
        Player player = null;
        if (entity instanceof Player)
        {
            player = (Player) entity;
        }

        final boolean enhancedEnvironmentalDmg = CFG.getBoolean(RootNode.ENHANCED_ENVIRONMENTAL_DAMAGE, world.getName());
        final boolean playerHasBypass = player != null ? player.hasPermission(PermissionNode.BYPASS.getNode())
                                   || player.getGameMode().equals(GameMode.CREATIVE) : true;

        // FEATURE: extra damage and effects from environmental damage
        if (enhancedEnvironmentalDmg &&! playerHasBypass)
        {
            EntityDamageEvent.DamageCause cause = event.getCause();

            switch (cause)
            {
                case BLOCK_EXPLOSION:
                case ENTITY_EXPLOSION:
                    if (event.getDamage() > 2)
                        player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 20 * 15, 3));
                    break;
                case FALL:
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * event.getDamage(), 4));
                    event.setDamage(event.getDamage() * 2);
                    break;
                case SUFFOCATION:
                    event.setDamage(event.getDamage() * 5);
                    break;
                case LAVA:
                    event.setDamage(event.getDamage() * 2);
                    break;
                case FIRE_TICK:
                    player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20, 1));
                    break;
            }
        }
    }

    /**
     * when a player interacts with the world
     *
     * @param event - Event that occurred.
     */
    @EventHandler(priority = EventPriority.LOWEST)
    void onPlayerInteract(PlayerInteractEvent event)
    {
        Player player = event.getPlayer();
        World world = event.getPlayer().getWorld();
        Block block = event.getClickedBlock();
        Action action = event.getAction();

        final boolean extinguishingFireIgnites = CFG.getBoolean(RootNode.EXTINGUISHING_FIRE_IGNITES_PLAYERS, world.getName())
                                                 &&! player.hasPermission(PermissionNode.BYPASS.getNode());

        // FEATURE: putting out fire up close catches the player on fire
        if (extinguishingFireIgnites && block != null && block.getType() != Material.AIR &&
                (action.equals(Action.LEFT_CLICK_BLOCK) || action.equals(Action.LEFT_CLICK_AIR)))
        {
            if (block.getRelative(event.getBlockFace()).getType() == Material.FIRE)
            {
                player.setFireTicks(100); // 20L ~ 1 seconds; 100L ~ 5 seconds
            }
        }
    }
}
