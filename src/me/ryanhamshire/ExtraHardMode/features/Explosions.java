package me.ryanhamshire.ExtraHardMode.features;


import me.ryanhamshire.ExtraHardMode.ExtraHardMode;
import me.ryanhamshire.ExtraHardMode.config.RootConfig;
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
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.*;
import org.bukkit.util.Vector;

import java.util.List;

public class Explosions implements Listener
{
    ExtraHardMode plugin = null;
    RootConfig rootC = null;
    MessageConfig messages;
    UtilityModule utils = null;

    public Explosions (ExtraHardMode plugin)
    {
        this.plugin = plugin;
        rootC = plugin.getModuleForClass(RootConfig.class);
        messages = plugin.getModuleForClass(MessageConfig.class);
        utils = plugin.getModuleForClass(UtilityModule.class);
    }

    /**
     * Handles all of EHM's custom explosions,
     * this includes bigger random tnt explosions,
     * TODO fix too extreme ghasts
     * bigger ghast explosion
     * turn stone into cobble in hardened stone mode
     * the fireball-event of the dragon is used to spawn monsters
     *
     * @param event - Event that occurred.
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onExplosion(EntityExplodeEvent event)
    {
        World world = event.getLocation().getWorld();
        if (!rootC.getStringList(RootNode.WORLDS).contains(world.getName()))
            return;

        EntityModule module = plugin.getModuleForClass(EntityModule.class);

        Entity entity = event.getEntity();

        // FEATURE: bigger TNT booms, all explosions have 100% block yield
        if (rootC.getBoolean(RootNode.BETTER_TNT))
        {
            event.setYield(1);

            if (entity != null && entity.getType() == EntityType.PRIMED_TNT && !rootC.getBoolean(RootNode.DISABLE_EXPLOSIONS))
            {
                // create more explosions nearby
                long serverTime = world.getFullTime();
                int random1 = (int) (serverTime + entity.getLocation().getBlockZ()) % 8;
                int random2 = (int) (serverTime + entity.getLocation().getBlockX()) % 8;

                Location[] locations = new Location[4];

                locations[0] = entity.getLocation().add(random1, 1, random2);
                locations[1] = entity.getLocation().add(-random2, 0, random1 / 2);
                locations[2] = entity.getLocation().add(-random1 / 2, -1, -random2);
                locations[3] = entity.getLocation().add(random1 / 2, 0, -random2 / 2);

                for (int i = 0; i < locations.length; i++)
                {
                    CreateExplosionTask task = new CreateExplosionTask(locations[i], 6F);
                    plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, task, 3L * (i + 1));
                }
            }
        }

        // FEATURE: ender dragon fireballs may summon minions and/or set fires
        if (entity != null && entity.getType() == EntityType.FIREBALL)
        {
            Fireball fireball = (Fireball) entity;
            Entity spawnedMonster = null;
            if (fireball.getShooter() != null && fireball.getShooter().getType() == EntityType.ENDER_DRAGON)
            {
                int random = plugin.getRandom().nextInt(100);
                if (random < 40)
                {
                    spawnedMonster = entity.getWorld().spawnEntity(entity.getLocation(), EntityType.BLAZE);

                    for (int x1 = -2; x1 <= 2; x1++)
                    {
                        for (int z1 = -2; z1 <= 2; z1++)
                        {
                            for (int y1 = 2; y1 >= -2; y1--)
                            {
                                Block block = fireball.getLocation().add(x1, y1, z1).getBlock();
                                Material underType = block.getRelative(BlockFace.DOWN).getType();
                                if (block.getType() == Material.AIR && underType != Material.AIR && underType != Material.FIRE)
                                {
                                    block.setType(Material.FIRE);
                                }
                            }
                        }
                    }

                    Location location = fireball.getLocation().add(0, 1, 0);
                    for (int i = 0; i < 10; i++)
                    {
                        FallingBlock fire = world.spawnFallingBlock(location, Material.FIRE, (byte) 0);
                        Vector velocity = Vector.getRandom();
                        if (velocity.getY() < 0)
                        {
                            velocity.setY(velocity.getY() * -1);
                        }
                        if (plugin.getRandom().nextBoolean())
                        {
                            velocity.setZ(velocity.getZ() * -1);
                        }
                        if (plugin.getRandom().nextBoolean())
                        {
                            velocity.setX(velocity.getX() * -1);
                        }
                        fire.setVelocity(velocity);
                    }
                }
                else if (random < 70)
                {
                    for (int i = 0; i < 2; i++)
                    {
                        spawnedMonster = entity.getWorld().spawnEntity(entity.getLocation(), EntityType.ZOMBIE);
                        module.markLootLess((LivingEntity) spawnedMonster);
                        Zombie zombie = (Zombie) spawnedMonster;
                        zombie.setVillager(true);
                    }
                }
                else
                {
                    spawnedMonster = entity.getWorld().spawnEntity(entity.getLocation(), EntityType.ENDERMAN);
                }
            }

            if (spawnedMonster != null)
            {
                module.markLootLess((LivingEntity) spawnedMonster);
            }
        }

        // FEATURE: in hardened stone mode, TNT only softens stone to cobble
        if (rootC.getBoolean(RootNode.SUPER_HARD_STONE))
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
        if (entity != null && entity instanceof Fireball && !rootC.getBoolean(RootNode.DISABLE_EXPLOSIONS))
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
        if (entity != null && entity instanceof Creeper && !rootC.getBoolean(RootNode.DISABLE_EXPLOSIONS))
        {
            event.setCancelled(true);
            // same as vanilla TNT
            entity.getWorld().createExplosion(entity.getLocation(), 3F, false);
        }
    }

    @EventHandler
    public void beforeCraft (PrepareItemCraftEvent event)
    {
        List<String> worlds = rootC.getStringList(RootNode.WORLDS);
        int multiplier = rootC.getInt(RootNode.MORE_TNT_NUMBER);

        InventoryHolder human = event.getInventory().getHolder();
        Player player = null;
        if (human instanceof Player) player = (Player)human;

        if (event.getRecipe().getResult().getType().equals(Material.TNT) && player != null)
        {
            //Recipe in CraftingGrid
            ShapedRecipe craftRecipe = (ShapedRecipe) event.getRecipe();
            CraftingInventory craftInv = event.getInventory();

            //The vanilla tnt recipe
            ShapedRecipe vanillaTnt = new ShapedRecipe(new ItemStack(Material.TNT)).shape("gsg", "sgs", "gsg").setIngredient('g', Material.SULPHUR).setIngredient('s', Material.SAND);

            //Multiply the amount of tnt in enabled worlds
            if (worlds.contains(player.getWorld().getName()) && utils.isSameRecipe(craftRecipe, vanillaTnt))
            {
                craftInv.setResult(new ItemStack(Material.TNT, multiplier));
            }
        }
    }

    /**
     * when a player crafts something...
     *
     * @param event - Event that occurred.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onItemCrafted(CraftItemEvent event)
    {
        List <String> worlds = rootC.getStringList(RootNode.WORLDS);
        int multiplier = rootC.getInt(RootNode.MORE_TNT_NUMBER);

        InventoryHolder human = event.getInventory().getHolder();
        Player player = null;

        if (human instanceof Player) player = (Player)human;
        World world = player.getWorld();

        if (worlds.contains(world.getName()) &! player.hasPermission(PermissionNode.BYPASS.getNode()))
        {
            //Are we crafting tnt and is more tnt enabled, from BeforeCraftEvent
            if (event.getRecipe().getResult().equals(new ItemStack (Material.TNT, multiplier)) && player != null)
            {
                if (multiplier == 0) event.setCancelled(true);//Feature disable tnt crafting
                if (multiplier > 1)
                {
                    PlayerInventory inv = player.getInventory();
                    //ShiftClick only causes this event to be called once
                    if (event.isShiftClick())
                    {
                        int amountBefore = utils.countInvItem(inv, Material.TNT);
                        //Add the missing tnt 1 tick later, we count what has been added by shiftclicking and multiply it
                        UtilityModule.addExtraItemsLater task = new UtilityModule.addExtraItemsLater(inv, amountBefore, Material.TNT, multiplier -1);
                        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, task, 1L);
                    }
                }
            }
        }
    }
}