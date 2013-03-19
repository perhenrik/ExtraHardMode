package me.ryanhamshire.ExtraHardMode.features;

import me.ryanhamshire.ExtraHardMode.ExtraHardMode;
import me.ryanhamshire.ExtraHardMode.config.DynamicConfig;
import me.ryanhamshire.ExtraHardMode.config.RootNode;
import me.ryanhamshire.ExtraHardMode.config.messages.MessageConfig;
import me.ryanhamshire.ExtraHardMode.config.messages.MessageNode;
import me.ryanhamshire.ExtraHardMode.module.BlockModule;
import me.ryanhamshire.ExtraHardMode.module.UtilityModule;
import me.ryanhamshire.ExtraHardMode.service.PermissionNode;
import me.ryanhamshire.ExtraHardMode.task.EvaporateWaterTask;
import org.bukkit.DyeColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.SheepRegrowWoolEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class AntiFarming implements Listener
{
    ExtraHardMode plugin;
    DynamicConfig dynC;
    UtilityModule utils;

    public AntiFarming (ExtraHardMode plugin)
    {
        this.plugin = plugin;
        dynC = plugin.getModuleForClass(DynamicConfig.class);
        utils = plugin.getModuleForClass(UtilityModule.class);
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
        Action action = event.getAction();

        final boolean noBonemealOnMushrooms = dynC.getBoolean(RootNode.NO_BONEMEAL_ON_MUSHROOMS, world.getName())
                                        &! player.hasPermission(PermissionNode.BYPASS.getNode());
        final boolean weakFoodCrops = dynC.getBoolean(RootNode.WEAK_FOOD_CROPS, world.getName())
                                &! player.hasPermission(PermissionNode.BYPASS.getNode());

        // FEATURE: bonemeal doesn't work on mushrooms
        if (noBonemealOnMushrooms && action == Action.RIGHT_CLICK_BLOCK)
        {
            Block block = event.getClickedBlock();
            if (block.getType() == Material.RED_MUSHROOM || block.getType() == Material.BROWN_MUSHROOM)
            {
                // what's the player holding?
                Material materialInHand = player.getItemInHand().getType();

                // if bonemeal, cancel the event
                if (materialInHand == Material.INK_SACK) // bukkit labels bonemeal as ink sack
                {
                    event.setCancelled(true);
                }
            }
        }

        // FEATURE: seed reduction. some plants die even when a player uses bonemeal.
        if (weakFoodCrops && action.equals(Action.RIGHT_CLICK_BLOCK))
        {
            Block block = event.getClickedBlock();
            if (utils.isPlant(block.getType()))
            {
                Material materialInHand = player.getItemInHand().getType();
                if (materialInHand == Material.INK_SACK && plugin.getModuleForClass(BlockModule.class).plantDies(block, Byte.MAX_VALUE))
                {
                    event.setCancelled(true);
                    block.setType(Material.LONG_GRASS); // dead shrub
                }
            }
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
        Player player = breakEvent.getPlayer();
        Block block = breakEvent.getBlock();
        World world = block.getWorld();

        final boolean noFarmingNetherWart = dynC.getBoolean(RootNode.NO_FARMING_NETHER_WART, world.getName());
        final boolean permAntiFarming = player != null ? player.hasPermission(PermissionNode.BYPASS.getNode()) : true; //true = has bypass won't run if no player

        // FEATURE: no nether wart farming (always drops exactly 1 nether wart when broken)
        if (!permAntiFarming && noFarmingNetherWart)
        {
            if (block.getType() == Material.NETHER_WARTS)
            {
                block.getDrops().clear();
                block.getDrops().add(new ItemStack(Material.NETHER_STALK));
            }
        }
    }

    /**
     * When a player places a block...
     * no farming nether wart
     * @param placeEvent
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onBlockPlace(BlockPlaceEvent placeEvent)
    {
        Player player = placeEvent.getPlayer();
        Block block = placeEvent.getBlock();
        World world = block.getWorld();

        final boolean noFarmingNetherWart = dynC.getBoolean(RootNode.NO_FARMING_NETHER_WART, world.getName());
        final boolean permAntiFarming = player != null ? player.hasPermission(PermissionNode.BYPASS.getNode()) : true; //true = has bypass won't run if no player

        // FEATURE: no farming/placing nether wart
        if (!permAntiFarming && noFarmingNetherWart && block.getType() == Material.NETHER_WARTS)
        {
            placeEvent.setCancelled(true);
            return;
        }
    }

    /**
     * When a block grows...
     * fewer seeds = shrinking crops. when a plant grows to its full size, it may be replaced by a dead shrub
     *
     * @param event - Event that occurred.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onBlockGrow(BlockGrowEvent event)
    {
        World world = event.getBlock().getWorld();

        final boolean weakCropsEnabled = dynC.getBoolean(RootNode.WEAK_FOOD_CROPS, world.getName());

        // FEATURE:
        if (weakCropsEnabled && plugin.getModuleForClass(BlockModule.class).plantDies(event.getBlock(), event.getNewState().getData().getData()))
        {
            event.setCancelled(true);
            event.getBlock().setType(Material.LONG_GRASS); // dead shrub
        }
    }

    /**
     * when a tree or mushroom grows...
     * no big plant growth in deserts
     *
     * @param event - Event that occurred.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onStructureGrow(StructureGrowEvent event)
    {
        World world = event.getWorld();
        Block block = event.getLocation().getBlock();

        boolean aridDesertsEnabled = dynC.getBoolean(RootNode.ARID_DESSERTS, world.getName());


        if (aridDesertsEnabled)
        {
            Biome biome = block.getBiome();
            if (biome == Biome.DESERT || biome == Biome.DESERT_HILLS)
            {
                event.setCancelled(true);
            }
        }
    }

    /**
     * when a dispenser dispenses...
     *
     * @param event - Event that occurred.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    void onBlockDispense(BlockDispenseEvent event)
    {
        World world = event.getBlock().getWorld();

        final boolean dontMoveWaterEnabled = dynC.getBoolean(RootNode.DONT_MOVE_WATER_SOURCE_BLOCKS, world.getName());

        // FEATURE: can't move water source blocks
        if (dontMoveWaterEnabled)
        {
            // only care about water
            if (event.getItem().getType() == Material.WATER_BUCKET)
            {
                // plan to evaporate the water next tick
                Block block;
                Vector velocity = event.getVelocity();
                if (velocity.getX() > 0)
                {
                    block = event.getBlock().getLocation().add(1, 0, 0).getBlock();
                }
                else if (velocity.getX() < 0)
                {
                    block = event.getBlock().getLocation().add(-1, 0, 0).getBlock();
                }
                else if (velocity.getZ() > 0)
                {
                    block = event.getBlock().getLocation().add(0, 0, 1).getBlock();
                }
                else
                {
                    block = event.getBlock().getLocation().add(0, 0, -1).getBlock();
                }

                EvaporateWaterTask task = new EvaporateWaterTask(block);
                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, task, 1L);
            }
        }
    }

    /**
     * when a sheep regrows its wool...
     *
     * @param event - Event that occurred.
     */
    @EventHandler
    public void onSheepRegrowWool(SheepRegrowWoolEvent event)
    {
        World world = event.getEntity().getWorld();

        boolean sheepRegrowWhiteEnabled = dynC.getBoolean(RootNode.SHEEP_REGROW_WHITE_WOOL, world.getName());

        // FEATURE: sheep are all white, and may be dyed only temporarily
        if (sheepRegrowWhiteEnabled)
        {
            Sheep sheep = event.getEntity();
            if (sheep.isSheared())
                sheep.setColor(DyeColor.WHITE);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onEntitySpawn(CreatureSpawnEvent event)
    {
        LivingEntity entity = event.getEntity();
        CreatureSpawnEvent.SpawnReason reason = event.getSpawnReason();
        EntityType entityType = entity.getType();
        World world = event.getLocation().getWorld();

        final boolean sheepRegrowWhiteEnabled = dynC.getBoolean(RootNode.SHEEP_REGROW_WHITE_WOOL, world.getName());

        //Breed Sheep spawn white
        if (sheepRegrowWhiteEnabled && entityType == EntityType.SHEEP)
        {
            Sheep sheep = (Sheep) entity;
            if (reason.equals(CreatureSpawnEvent.SpawnReason.BREEDING))
            {
                sheep.setColor(DyeColor.WHITE);
                return;
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event)
    {
        LivingEntity entity = event.getEntity();
        World world = entity.getWorld();

        final boolean animalExpNerfEnabled = dynC.getBoolean(RootNode.ANIMAL_EXP_NERF, world.getName());

        // FEATURE: animals don't drop experience (because they're easy to "farm")
        if (animalExpNerfEnabled && entity instanceof Animals)
        {
            event.setDroppedExp(0);
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
        World world = null;
        Material result = event.getRecipe().getResult().getType();
        InventoryHolder human = event.getInventory().getHolder();
        Player player = null;
        if (human instanceof Player)
        {
            player = (Player)human;
            world = player.getWorld();
        }

        final boolean cantCraftMelons = world != null && dynC.getBoolean(RootNode.CANT_CRAFT_MELONSEEDS, world.getName());
        final boolean permAntiFarming = player != null ? player.hasPermission(PermissionNode.BYPASS.getNode()) : true; //true = has bypass won't run if no player

        MessageConfig messages = plugin.getModuleForClass(MessageConfig.class);


        if (!permAntiFarming && cantCraftMelons)
        {
            // FEATURE: no crafting melon seeds
            if (cantCraftMelons && (result == Material.MELON_SEEDS || result == Material.PUMPKIN_SEEDS))
            {
                event.setCancelled(true);
                plugin.sendMessage(player, messages.getString(MessageNode.NO_CRAFTING_MELON_SEEDS));
                return;
            }
        }
    }
    /**
     * when a player fills a bucket...
     *
     * @param event - Event that occurred.
     */
    @EventHandler(priority = EventPriority.LOW)
    void onPlayerFillBucket(PlayerBucketFillEvent event)
    {
        Player player = event.getPlayer();
        World world = player.getWorld();
        Block block = event.getBlockClicked();

        final boolean dontMoveWaterEnabled = dynC.getBoolean(RootNode.DONT_MOVE_WATER_SOURCE_BLOCKS, world.getName())
                                             &! player.hasPermission(PermissionNode.BYPASS.getNode()); //PlayerEvent so Player won't be null, right? right?....

        // FEATURE: can't move water source blocks
        if (dontMoveWaterEnabled)
        {
            // only care about stationary (source) water
            if (block.getType() == Material.STATIONARY_WATER)
            {
                // cancel the event so that the water doesn't get removed
                event.setCancelled(true);

                // fill the player's bucket anyway
                // (beware, player may have a stack of empty buckets, and filled
                // buckets DON'T stack)
                int extraBuckets = player.getItemInHand().getAmount() - 1;
                player.getItemInHand().setType(Material.WATER_BUCKET);
                player.getItemInHand().setAmount(1);
                if (extraBuckets > 0)
                {
                    player.getInventory().addItem(new ItemStack(Material.BUCKET, extraBuckets));
                }

                // send the player data so that his client doesn't incorrectly show
                // the water as missing
                player.sendBlockChange(block.getLocation(), block.getTypeId(), block.getData());
            }
        }
    }

    /**
     * when a player empties a bucket...
     *
     * @param event - Event that occurred.
     */
    @EventHandler(priority = EventPriority.NORMAL)
    void onPlayerEmptyBucket(PlayerBucketEmptyEvent event)
    {
        Player player = event.getPlayer();
        World world = player.getWorld();

        final boolean dontMoveWaterEnabled = dynC.getBoolean(RootNode.DONT_MOVE_WATER_SOURCE_BLOCKS, world.getName());
        final boolean permAntiFarming = player != null ? player.hasPermission(PermissionNode.BYPASS.getNode())
                                        || player.getGameMode().equals(GameMode.CREATIVE) : true; //true = has bypass won't run if no player

        // FEATURE: can't move water source blocks
        if (!permAntiFarming && dontMoveWaterEnabled && player.getItemInHand().getType().equals(Material.WATER_BUCKET))
        {
            // plan to change this block into a non-source block on the next tick
            Block block = event.getBlockClicked().getRelative(event.getBlockFace());
            EvaporateWaterTask task = new EvaporateWaterTask(block);
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, task, 15L);
        }
    }
}
