package com.extrahardmode.features.monsters;


import com.extrahardmode.ExtraHardMode;
import com.extrahardmode.config.RootConfig;
import com.extrahardmode.config.RootNode;
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
    RootConfig CFG = null;

    public Silverfish (ExtraHardMode plugin)
    {
        this.plugin = plugin;
        CFG = plugin.getModuleForClass(RootConfig.class);
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
        Block block = event.getBlock();
        World world = block.getWorld();

        final boolean silverFishCantEnter = CFG.getBoolean(RootNode.SILVERFISH_CANT_ENTER_BLOCKS, world.getName());

        //Prevent Silverfish from entering blocks?
        if (silverFishCantEnter)
        {
            if (event.getEntity().getType() == EntityType.SILVERFISH && event.getTo() == Material.MONSTER_EGGS)
            {
                event.setCancelled(true);
            }
        }
    }

    /**
     * When an entity dies, drop cobble for SilverFish
     * @param event
     */
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event)
    {
        LivingEntity entity = event.getEntity();
        World world = entity.getWorld();

        final boolean dropCobble = CFG.getBoolean(RootNode.SILVERFISH_DROP_COBBLE, world.getName());

        // FEATURE: silverfish drop cobblestone
        if (dropCobble && entity.getType() == EntityType.SILVERFISH)
        {
            event.getDrops().add(new ItemStack(Material.COBBLESTONE));
        }
    }
}
