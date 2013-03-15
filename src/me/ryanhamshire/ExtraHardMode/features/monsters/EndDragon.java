package me.ryanhamshire.ExtraHardMode.features.monsters;


import me.ryanhamshire.ExtraHardMode.ExtraHardMode;
import me.ryanhamshire.ExtraHardMode.config.RootConfig;
import me.ryanhamshire.ExtraHardMode.config.RootNode;
import me.ryanhamshire.ExtraHardMode.config.messages.MessageConfig;
import me.ryanhamshire.ExtraHardMode.config.messages.MessageNode;
import me.ryanhamshire.ExtraHardMode.service.PermissionNode;
import me.ryanhamshire.ExtraHardMode.task.DragonAttackPatternTask;
import me.ryanhamshire.ExtraHardMode.task.DragonAttackTask;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class EndDragon implements Listener
{
    ExtraHardMode plugin = null;
    RootConfig rootC = null;
    MessageConfig messages;
    List <String> playersFightingDragon;

    public EndDragon (ExtraHardMode plugin)
    {
        this.plugin = plugin;
        rootC = plugin.getModuleForClass(RootConfig.class);
        messages = plugin.getModuleForClass(MessageConfig.class);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent breakEvent)
    {
        Block block = breakEvent.getBlock();
        World world = block.getWorld();
        Player player = breakEvent.getPlayer();

        if (!rootC.getStringList(RootNode.WORLDS).contains(world.getName()) || player.hasPermission(PermissionNode.BYPASS.getNode()))
            return;

        // FEATURE: very limited building in the end
        // players are allowed to break only end stone, and only to create a stair
        // up to ground level
        if (rootC.getBoolean(RootNode.ENDER_DRAGON_NO_BUILDING) && world.getEnvironment() == World.Environment.THE_END)
        {
            if (block.getType() != Material.ENDER_STONE)
            {
                breakEvent.setCancelled(true);
                plugin.sendMessage(player, messages.getString(MessageNode.LIMITED_END_BUILDING));
                return;
            }
            else
            {
                int absoluteDistanceFromBlock = Math.abs(block.getX() - player.getLocation().getBlockX());
                int zdistance = Math.abs(block.getZ() - player.getLocation().getBlockZ());
                if (zdistance > absoluteDistanceFromBlock)
                {
                    absoluteDistanceFromBlock = zdistance;
                }

                if (block.getY() < player.getLocation().getBlockY() + absoluteDistanceFromBlock)
                {
                    breakEvent.setCancelled(true);
                    plugin.sendMessage(player, messages.getString(MessageNode.LIMITED_END_BUILDING));
                    return;
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onBlockPlace(BlockPlaceEvent placeEvent)
    {
        Player player = placeEvent.getPlayer();
        Block block = placeEvent.getBlock();
        World world = block.getWorld();

        MessageConfig messages = plugin.getModuleForClass(MessageConfig.class);

        if (!rootC.getStringList(RootNode.WORLDS).contains(world.getName()) || player.hasPermission(PermissionNode.BYPASS.getNode()))
            return;

        // FEATURE: very limited building in the end
        // players are allowed to break only end stone, and only to create a stair
        // up to ground level
        if (rootC.getBoolean(RootNode.ENDER_DRAGON_NO_BUILDING) && world.getEnvironment() == World.Environment.THE_END)
        {
            placeEvent.setCancelled(true);
            plugin.sendMessage(player, messages.getString(MessageNode.LIMITED_END_BUILDING));
            return;
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event)
    {
        LivingEntity entity = event.getEntity();
        World world = entity.getWorld();
        // FEATURE: ender dragon drops prizes on death
        if (entity instanceof EnderDragon)
        {
            if (rootC.getBoolean(RootNode.ENDER_DRAGON_DROPS_VILLAGER_EGGS))
            {
                ItemStack itemStack = new ItemStack(Material.MONSTER_EGG, 2, (short) 120);
                world.dropItemNaturally(entity.getLocation().add(10, 0, 0), itemStack);
            }

            if (rootC.getBoolean(RootNode.ENDER_DRAGON_DROPS_EGG))
            {
                world.dropItemNaturally(entity.getLocation().add(10, 0, 0), new ItemStack(Material.DRAGON_EGG));
            }

            if (rootC.getBoolean(RootNode.ENDER_DRAGON_COMBAT_ANNOUNCEMENTS))
            {
                StringBuilder builder = new StringBuilder("The dragon has been defeated!  ( By: ");
                for (String player : this.playersFightingDragon)
                {
                    builder.append(player).append(" ");
                }
                builder.append(")");

                plugin.getServer().broadcastMessage(builder.toString());
            }

            if (rootC.getBoolean(RootNode.ENDER_DRAGON_NO_BUILDING))
            {
                for (String player : this.playersFightingDragon)
                {
                    if (plugin.getServer().getPlayer(player) != null)
                    {
                        Player player1 = plugin.getServer().getPlayer(player);
                        plugin.sendMessage(player1, messages.getString(MessageNode.DRAGON_FOUNTAIN_TIP));
                    }
                }
            }

            this.playersFightingDragon.clear();
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onEntityDamage(EntityDamageEvent event)
    {
        Entity entity = event.getEntity();

        // is this an entity damaged by entity event?
        EntityDamageByEntityEvent damageByEntityEvent = null;
        if (event instanceof EntityDamageByEntityEvent)
        {
            damageByEntityEvent = (EntityDamageByEntityEvent) event;
        }

        // FEATURE: the dragon has new attacks
        if (damageByEntityEvent != null && entity.getType() == EntityType.ENDER_DRAGON && rootC.getBoolean(RootNode.ENDER_DRAGON_ADDITIONAL_ATTACKS))
        {
            Player damager = null;
            if (damageByEntityEvent.getDamager() instanceof Player)
            {
                damager = (Player) damageByEntityEvent.getDamager();
            }
            else if (damageByEntityEvent.getDamager() instanceof Projectile)
            {
                Projectile projectile = (Projectile) damageByEntityEvent.getDamager();
                if (projectile.getShooter() != null && projectile.getShooter() instanceof Player)
                {
                    damager = (Player) projectile.getShooter();
                }
            }

            if (damager != null)
            {
                if (!this.playersFightingDragon.contains(damager))
                {
                    this.playersFightingDragon.add(damager.getName());

                    DragonAttackPatternTask task = new DragonAttackPatternTask(plugin, (LivingEntity) entity, damager, this.playersFightingDragon);
                    plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, task, 1L);

                    if (rootC.getBoolean(RootNode.ENDER_DRAGON_COMBAT_ANNOUNCEMENTS))
                    {
                        plugin.getServer().broadcastMessage(damager.getName() + " is challenging the dragon!");
                    }
                }

                for (int i = 0; i < 5; i++)
                {
                    DragonAttackTask task = new DragonAttackTask(plugin, entity, damager);
                    plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, task, 20L * (plugin.getRandom().nextInt(15)));
                }

                Chunk chunk = damager.getLocation().getChunk();
                Entity[] entities = chunk.getEntities();
                for (Entity entity1 : entities)
                {
                    if (entity1.getType() == EntityType.ENDERMAN)
                    {
                        Enderman enderman = (Enderman) entity1;
                        enderman.setTarget(damager);
                    }
                }
            }
        }
    }

    /**
     * when a player changes worlds...
     *
     * @param event - Event that occurred.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    void onPlayerChangeWorld(PlayerChangedWorldEvent event)
    {
        World world = event.getFrom();

        if (!rootC.getStringList(RootNode.WORLDS).contains(world.getName()))
            return;

        // FEATURE: respawn the ender dragon when the last player leaves the end
        if (world.getEnvironment() != World.Environment.THE_END)
            return;

        if (world.getPlayers().size() > 0)
            return;

        // look for an ender dragon
        EnderDragon enderDragon = null;
        for (Entity entity : world.getEntities())
        {
            if (enderDragon != null && entity instanceof EnderDragon)
            {  //If there is already a dragon for whatever reason, remove it
                entity.remove();
            }
            if (entity instanceof EnderDragon)
            {
                enderDragon = (EnderDragon) entity;
            }
            // clean up any summoned minions
            if (entity.getType().equals(EntityType.ZOMBIE) || entity.getType().equals(EntityType.BLAZE))
            {
                entity.remove();
            }
        }

        // if he's there, full health
        if (enderDragon != null)
        {
            enderDragon.setHealth(enderDragon.getMaxHealth());
        }

        // otherwise, spawn one
        else
        {
            world.spawnEntity(new Location(world, 0, world.getMaxHeight() - 1, 0), EntityType.ENDER_DRAGON);
        }
    }

    /**
     * when an item spawns
     *
     * @param event - Event that occurred.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onItemSpawn(ItemSpawnEvent event)
    {
        // FEATURE: fountain effect from dragon fireball explosions sometimes
        // causes fire to drop as an item. this is the fix for that.
        Item item = event.getEntity();
        World world = item.getWorld();
        if (!rootC.getStringList(RootNode.WORLDS).contains(world.getName()) || world.getEnvironment() != World.Environment.THE_END)
            return;

        if (item.getItemStack().getType() == Material.FIRE)
        {
            event.setCancelled(true);
        }
    }

    /**
     * when an entity targets something (as in to attack it)...
     *
     * @param event - Event that occurred.
     */
    @EventHandler
    public void onEntityTarget(EntityTargetEvent event)
    {
        Entity entity = event.getEntity();
        World world = entity.getWorld();
        if (!rootC.getStringList(RootNode.WORLDS).contains(world.getName()))
            return;
        // FEATURE: monsters don't target the ender dragon
        if (event.getTarget() != null && event.getTarget() instanceof EnderDragon)
        {
            event.setCancelled(true);
        }
    }
}
