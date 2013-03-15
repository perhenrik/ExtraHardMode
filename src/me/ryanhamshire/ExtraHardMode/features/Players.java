package me.ryanhamshire.ExtraHardMode.features;

import me.ryanhamshire.ExtraHardMode.ExtraHardMode;
import me.ryanhamshire.ExtraHardMode.config.RootConfig;
import me.ryanhamshire.ExtraHardMode.config.RootNode;
import me.ryanhamshire.ExtraHardMode.config.messages.MessageConfig;
import me.ryanhamshire.ExtraHardMode.module.DataStoreModule;
import me.ryanhamshire.ExtraHardMode.module.EntityModule;
import me.ryanhamshire.ExtraHardMode.module.UtilityModule;
import me.ryanhamshire.ExtraHardMode.service.PermissionNode;
import me.ryanhamshire.ExtraHardMode.task.SetPlayerHealthAndFoodTask;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
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
    RootConfig rootC = null;
    MessageConfig messages;
    UtilityModule utils = null;
    EntityModule entityModule = null;

    public Players (ExtraHardMode plugin)
    {
        this.plugin = plugin;
        rootC = plugin.getModuleForClass(RootConfig.class);
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
        //TODO Fix this up
        Player player = respawnEvent.getPlayer();
        World world = respawnEvent.getPlayer().getWorld();
        if (!rootC.getStringList(RootNode.WORLDS).contains(world.getName()) || player.hasPermission(PermissionNode.BYPASS.getNode()))
        {
            return;
        }
        SetPlayerHealthAndFoodTask task = new SetPlayerHealthAndFoodTask(player, rootC.getInt(RootNode.PLAYER_RESPAWN_HEALTH), rootC.getInt(RootNode.PLAYER_RESPAWN_FOOD_LEVEL));
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, task, 10L); // half-second
        // delay
        // FEATURE: players can't swim when they're carrying a lot of weight
        DataStoreModule.PlayerData playerData = plugin.getModuleForClass(DataStoreModule.class).getPlayerData(player.getName());
        playerData.cachedWeightStatus = -1F;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event)
    {
        LivingEntity entity = event.getEntity();
        World world = entity.getWorld();

        if (!rootC.getStringList(RootNode.WORLDS).contains(world.getName()))
        {
            return;
        }

        // FEATURE: some portion of player inventory is permanently lost on death
        if (entity instanceof Player)
        {
            Player player = (Player) entity;
            if (!player.hasPermission(PermissionNode.BYPASS_INVENTORY.getNode()))
            {
                List<ItemStack> drops = event.getDrops();
                int numberOfStacksToRemove = (int) (drops.size() * (rootC.getInt(RootNode.PLAYER_DEATH_ITEM_STACKS_FORFEIT_PERCENT) / 100f));
                for (int i = 0; i < numberOfStacksToRemove && drops.size() > 0; i++)
                {
                    int indexOfStackToRemove = plugin.getRandom().nextInt(drops.size());
                    drops.remove(indexOfStackToRemove);
                }
            }
        }

    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onEntityDamage(EntityDamageEvent event)
    {
        Entity entity = event.getEntity();
        EntityType entityType = entity.getType();
        World world = entity.getWorld();

        if (!rootC.getStringList(RootNode.WORLDS).contains(world.getName()))
            return;

        // FEATURE: extra damage and effects from environmental damage
        if (rootC.getBoolean(RootNode.ENHANCED_ENVIRONMENTAL_DAMAGE))
        {
            Player player = null;
            if (entity instanceof Player)
            {
                player = (Player) entity;
            }

            if (player != null && !player.hasPermission(PermissionNode.BYPASS.getNode()))
            {
                EntityDamageEvent.DamageCause cause = event.getCause();

                if (event.getDamage() > 2 && (cause == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION || cause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION))
                {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 20 * 15, 3));
                }
                else if (cause == EntityDamageEvent.DamageCause.FALL)
                {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * event.getDamage(), 4));
                    event.setDamage(event.getDamage() * 2);
                }
                else if (cause == EntityDamageEvent.DamageCause.SUFFOCATION)
                {
                    event.setDamage(event.getDamage() * 5);
                }
                else if (cause == EntityDamageEvent.DamageCause.LAVA)
                {
                    event.setDamage(event.getDamage() * 2);
                }
                else if (cause == EntityDamageEvent.DamageCause.FIRE_TICK)
                {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20, 1));
                }
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
        if (!rootC.getStringList(RootNode.WORLDS).contains(world.getName()) || player.hasPermission(PermissionNode.BYPASS.getNode()))
            return;
        Action action = event.getAction();
        // FEATURE: putting out fire up close catches the player on fire
        Block block = event.getClickedBlock();
        if (rootC.getBoolean(RootNode.EXTINGUISHING_FIRE_IGNITES_PLAYERS) && block != null && block.getType() != Material.AIR)
        {
            if (block.getRelative(event.getBlockFace()).getType() == Material.FIRE)
            {
                player.setFireTicks(100); // 20L ~ 1 seconds; 100L ~ 5 seconds
            }
        }
    }
}
