package me.ryanhamshire.ExtraHardMode.features.monsters;


import me.ryanhamshire.ExtraHardMode.ExtraHardMode;
import me.ryanhamshire.ExtraHardMode.config.RootConfig;
import me.ryanhamshire.ExtraHardMode.config.RootNode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

public class Silverfish implements Listener
{
    ExtraHardMode plugin = null;
    RootConfig rootC = null;

    public Silverfish (ExtraHardMode plugin)
    {
        this.plugin = plugin;
        rootC = plugin.getModuleForClass(RootConfig.class);
    }

    /**
     * when an entity tries to change a block (does not include player block
     * changes) don't allow silverfish to change blocks
     *
     * @param event - Event that occurred.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onEntityChangeBlock(EntityChangeBlockEvent event)
    {
        //Prevent Silverfish from entering blocks?
        if (!rootC.getBoolean(RootNode.SILVERFISH_CANT_ENTER_BLOCKS))
        {
            Block block = event.getBlock();
            World world = block.getWorld();
            if (!rootC.getStringList(RootNode.WORLDS).contains(world.getName()))
                return;

            if (event.getEntity().getType() == EntityType.SILVERFISH && event.getTo() == Material.MONSTER_EGGS)
            {
                event.setCancelled(true);
            }
        }
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

        // FEATURE: silverfish drop cobblestone
        if (entity.getType() == EntityType.SILVERFISH)
        {
            event.getDrops().add(new ItemStack(Material.COBBLESTONE));
        }
    }
}
