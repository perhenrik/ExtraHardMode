package me.ryanhamshire.ExtraHardMode.features.monsters;

import me.ryanhamshire.ExtraHardMode.ExtraHardMode;
import me.ryanhamshire.ExtraHardMode.config.DynamicConfig;
import me.ryanhamshire.ExtraHardMode.config.RootNode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent;


public class Bobs implements Listener
{
    ExtraHardMode plugin = null;
    DynamicConfig dynC = null;

    public Bobs(ExtraHardMode plugin)
    {
        this.plugin = plugin;
        dynC = plugin.getModuleForClass(DynamicConfig.class);
    }

    /**
     * when an entity (not a player) teleports...
     *
     * @param event - Event that occurred.
     */
    @EventHandler
    public void onEntityTeleport(EntityTeleportEvent event)
    {
        Entity entity = event.getEntity();
        World world = entity.getWorld();

        final boolean improvedEndermanTeleportation = dynC.getBoolean(RootNode.IMPROVED_ENDERMAN_TELEPORTATION, world.getName());

        if (entity instanceof Enderman && improvedEndermanTeleportation && world.getEnvironment().equals(World.Environment.NORMAL))
        {
            Enderman enderman = (Enderman) entity;

            // ignore endermen which aren't fighting players
            if (enderman.getTarget() == null || !(enderman.getTarget() instanceof Player))
                return;

            // ignore endermen which are taking damage from the environment (to
            // avoid rapid teleportation due to rain or suffocation)
            if (enderman.getLastDamageCause() != null && enderman.getLastDamageCause().getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK)
                return;

            // ignore endermen which are in caves (standing on stone)
            if (enderman.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == Material.STONE)
                return;

            Player player = (Player) enderman.getTarget();

            // ignore when player is in a different world from the enderman
            if (!player.getWorld().equals(enderman.getWorld()))
                return;

            // half the time, teleport the player instead
            if (plugin.random(50))
            {
                event.setCancelled(true);
                int distanceSquared = (int) player.getLocation().distanceSquared(enderman.getLocation());

                // play sound at old location
                world.playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 1, 1);
                Block destinationBlock;

                // if the player is far away
                if (distanceSquared > 75)
                {
                    // have the enderman swap places with the player
                    destinationBlock = enderman.getLocation().getBlock();
                    enderman.teleport(player.getLocation());
                }

                // otherwise if the player is close
                else
                {
                    // teleport the player to the enderman's destination
                    destinationBlock = event.getTo().getBlock();
                }

                while (destinationBlock.getType() != Material.AIR || destinationBlock.getRelative(BlockFace.UP).getType() != Material.AIR)
                {
                    destinationBlock = destinationBlock.getRelative(BlockFace.UP);
                }

                player.teleport(destinationBlock.getLocation(), PlayerTeleportEvent.TeleportCause.ENDER_PEARL);

                // play sound at new location
                world.playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 1, 1);
            }
        }
    }
}
