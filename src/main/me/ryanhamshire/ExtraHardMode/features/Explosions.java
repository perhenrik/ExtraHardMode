package me.ryanhamshire.ExtraHardMode.features;


import me.ryanhamshire.ExtraHardMode.ExtraHardMode;
import me.ryanhamshire.ExtraHardMode.config.DynamicConfig;
import me.ryanhamshire.ExtraHardMode.config.RootNode;
import me.ryanhamshire.ExtraHardMode.config.messages.MessageConfig;
import me.ryanhamshire.ExtraHardMode.module.BlockModule;
import me.ryanhamshire.ExtraHardMode.module.EntityModule;
import me.ryanhamshire.ExtraHardMode.module.UtilityModule;
import me.ryanhamshire.ExtraHardMode.service.PermissionNode;
import me.ryanhamshire.ExtraHardMode.task.CreateExplosionTask;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.*;

import java.util.List;

public class Explosions implements Listener
{
    ExtraHardMode plugin;
    DynamicConfig dynC;
    MessageConfig messages;
    UtilityModule utils;
    EntityModule entityModule;

    public Explosions (ExtraHardMode plugin)
    {
        this.plugin = plugin;
        dynC = plugin.getModuleForClass(DynamicConfig.class);
        messages = plugin.getModuleForClass(MessageConfig.class);
        utils = plugin.getModuleForClass(UtilityModule.class);
        entityModule = plugin.getModuleForClass(EntityModule.class);
    }

    /**
     * Handles all of EHM's custom explosions,
     * this includes bigger random tnt explosions,
     * bigger ghast explosion
     * turn stone into cobble in hardened stone mode
     *
     * @param event - Event that occurred.
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onExplosion(EntityExplodeEvent event)
    {
        World world = event.getLocation().getWorld();
        Entity entity = event.getEntity();

        final boolean betterTntEnabled = dynC.getBoolean(RootNode.BETTER_TNT, world.getName());
        final boolean hardStoneEnabled = dynC.getBoolean(RootNode.SUPER_HARD_STONE, world.getName());

        // FEATURE: bigger TNT booms, all explosions have 100% block yield
        if (betterTntEnabled)
        {
            event.setYield(1);

            if (entity != null && entity.getType() == EntityType.PRIMED_TNT)
            {
                // create more explosions nearby
                long serverTime = world.getFullTime();
                int random1 = (int) (serverTime + entity.getLocation().getBlockZ()) % 8;
                int random2 = (int) (serverTime + entity.getLocation().getBlockX()) % 8;

                Location[] locations = new Location[]
                {
                    entity.getLocation().add(random1, 1, random2),
                    entity.getLocation().add(-random2, 0, random1 / 2),
                    entity.getLocation().add(-random1 / 2, -1, -random2),
                    entity.getLocation().add(random1 / 2, 0, -random2 / 2)
                };

                for (int i = 0; i < locations.length; i++)
                {
                    CreateExplosionTask task = new CreateExplosionTask(locations[i], 6F);
                    plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, task, 3L * (i + 1));
                }
            }
        }



        // FEATURE: in hardened stone mode, TNT only softens stone to cobble
        if (hardStoneEnabled)
        {
            List<Block> blocks = event.blockList();
            for (int i = 0; i < blocks.size(); i++)
            {
                Block block = blocks.get(i);
                if (block.getType() == Material.STONE)
                {
                    block.setType(Material.COBBLESTONE);
                    blocks.remove(i--);
                }

                // FEATURE: more falling blocks
                BlockModule physics = plugin.getModuleForClass(BlockModule.class);
                physics.physicsCheck(block, 0, true);
            }
        }

        // FEATURE: more powerful ghast fireballs
        if (entity != null && entity instanceof Fireball)
        {
            Fireball fireball = (Fireball) entity;
            if (fireball.getShooter() != null && fireball.getShooter().getType() == EntityType.GHAST)
            {
                event.setCancelled(true);
                // same as vanilla TNT, plus fire
                entity.getWorld().createExplosion(entity.getLocation(), 4F, true);
            }
        }

        // FEATURE: bigger creeper explosions (for more-frequent cave-ins)
        if (entity != null && entity instanceof Creeper)
        {
            event.setCancelled(true);
            // same as vanilla TNT
            entity.getWorld().createExplosion(entity.getLocation(), 3F, false);
        }
    }

    /**
     * Gets called just when an ItemStack is about to be crafted
     * Sets the amount in the result slot to the appropriate number
     * @param event
     */
    @EventHandler
    public void beforeCraft (PrepareItemCraftEvent event)
    {
        InventoryHolder human = event.getInventory().getHolder();
        Player player = null;
        if (human instanceof Player)
        {
            player = (Player)human;
            World world = player.getWorld();

            final int multiplier = dynC.getInt(RootNode.MORE_TNT_NUMBER, world.getName());

            switch (multiplier)
            {
                case 0:
                    break;
                case 1:
                    break;
                default:
                    if (event.getRecipe().getResult().getType().equals(Material.TNT))
                    {
                        //Recipe in CraftingGrid
                        ShapedRecipe craftRecipe = (ShapedRecipe) event.getRecipe();
                        CraftingInventory craftInv = event.getInventory();

                        //The vanilla tnt recipe
                        ShapedRecipe vanillaTnt = new ShapedRecipe(new ItemStack(Material.TNT)).shape("gsg", "sgs", "gsg").setIngredient('g', Material.SULPHUR).setIngredient('s', Material.SAND);

                        //Multiply the amount of tnt in enabled worlds
                        if (utils.isSameRecipe(craftRecipe, vanillaTnt))
                        {
                            craftInv.setResult(new ItemStack(Material.TNT, multiplier));
                        }
                    }
                    break;
            }
        }
    }

    /**
     * when a player crafts something...
     * MoreTnt: Contains the logic for modifying the amount of tnt crafted on a per world basis.
     *
     * @param event - Event that occurred.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onItemCrafted(CraftItemEvent event)
    {
        InventoryHolder human = event.getInventory().getHolder();
        Player player = null;

        if (human instanceof Player)
        {
            player = (Player)human;

            World world = player.getWorld();

            final int multiplier = dynC.getInt(RootNode.MORE_TNT_NUMBER, world.getName());
            final boolean playerHasBypass = player.hasPermission(PermissionNode.BYPASS.getNode());

            if (!playerHasBypass)
            {
                //Are we crafting tnt and is more tnt enabled, from BeforeCraftEvent
                if (event.getRecipe().getResult().equals(new ItemStack (Material.TNT, multiplier)))
                {
                    switch (multiplier)
                    {
                        case 0:
                            event.setCancelled(true); //Feature disable tnt crafting
                            break;
                        default: //doesnt check for negative values
                            PlayerInventory inv = player.getInventory();
                            //ShiftClick only causes this event to be called once
                            if (event.isShiftClick())
                            {
                                int amountBefore = utils.countInvItem(inv, Material.TNT);
                                //Add the missing tnt 1 tick later, we count what has been added by shiftclicking and multiply it
                                UtilityModule.addExtraItemsLater task = new UtilityModule.addExtraItemsLater(inv, amountBefore, Material.TNT, multiplier -1);
                                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, task, 1L);
                            }
                            break;
                    }
                }
            }
        }
    }
}