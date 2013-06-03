/*
 * This file is part of
 * ExtraHardMode Server Plugin for Minecraft
 *
 * Copyright (C) 2012 Ryan Hamshire
 * Copyright (C) 2013 Diemex
 *
 * ExtraHardMode is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ExtraHardMode is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero Public License
 * along with ExtraHardMode.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.extrahardmode.config;

import com.extrahardmode.service.config.ConfigNode;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Collections;

/**
* Configuration options of the root config.yml file.
*/
//Please keep the codestyle, it makes it easier to grasp the structure of the config
public enum RootNode implements ConfigNode
{
    /**
     * How this ConfigFile is going to be handled by the plugin
     */
    MODE
            ("Config Type", VarType.STRING, "MAIN"),
    /**
     * list of worlds where extra hard mode rules apply
     */
    WORLDS
            ("Enabled Worlds", VarType.LIST, new ArrayList<String>()),

    /**
     * If we should check for the bypass permission
     */
    BYPASS_PERMISSION
            ("Bypassing.Check For Permission", VarType.BOOLEAN, true),
    /**
     * If players in creative auto bypass (useful for building)
     */
    BYPASS_CREATIVE
            ("Bypassing.Creative Mode Bypasses", VarType.BOOLEAN, true),
    /**
     * If players with op should bypass by default
     */
    BYPASS_OPS
            ("Bypassing.Operators Bypass", VarType.BOOLEAN, true),

    /**
     * whether stone is hardened to encourage cave exploration over tunneling
     */
    SUPER_HARD_STONE
            ("World Rules.Mining.Inhibit Tunneling.Enable", VarType.BOOLEAN, true),
    /**
     * Number of blocks player can mine with an iron pick and hard stone enabled
     */
    IRON_DURABILITY_PENALTY
            ("World Rules.Mining.Inhibit Tunneling.Number of Stone Iron Pickaxe Can Mine", VarType.INTEGER, SubType.NATURAL_NUMBER, 32),
    /**
     * Number of blocks player can mine with an diamond pick and hard stone enabled
     */
    DIAMOND_DURABILITY_PENALTY
            ("World Rules.Mining.Inhibit Tunneling.Number of Stone Diamond Pickaxe Can Mine", VarType.INTEGER, SubType.NATURAL_NUMBER, 64),
    /**
     * Breaking an ore will cause surrounding stone to turn to cobble and fall
     */
    SUPER_HARD_STONE_PHYSICS
            ("World Rules.Mining.Breaking Ore Softens Adjacent Stone", VarType.BOOLEAN, true),
    /**
     * maximum y for placing standard torches
     */
    STANDARD_TORCH_MIN_Y
            ("World Rules.Torches.No Placement Under Y", VarType.INTEGER, SubType.Y_VALUE, Disable.ZERO, 30),
    /**
     * whether players are limited to placing torches against specific materials
     */
    LIMITED_TORCH_PLACEMENT
            ("World Rules.Torches.No Placement On Soft Materials", VarType.BOOLEAN, true),
    /**
     * whether rain should break torches
     */
    RAIN_BREAKS_TORCHES
            ("World Rules.Torches.Rain Breaks Torches", VarType.BOOLEAN, true),

    /**
     * Sound when torch placing fails
     */
    SOUNDS_TORCH_FIZZ
            ("World Rules.Play Sounds.Torch Fizzing", VarType.BOOLEAN, true),
    /**
     * Warning Sound when a creeper drops tnt
     */
    SOUND_CREEPER_TNT
            ("World Rules.Play Sounds.Creeper Tnt Warning", VarType.BOOLEAN, true),
    /**
     * percent chance for broken netherrack to start a fire
     */
    BROKEN_NETHERRACK_CATCHES_FIRE_PERCENT
            ("World Rules.Breaking Netherrack Starts Fire Percent", VarType.INTEGER, SubType.PERCENTAGE, 20),
    /**
     * whether players may place blocks directly underneath themselves
     */
    LIMITED_BLOCK_PLACEMENT
            ("World Rules.Limited Block Placement", VarType.BOOLEAN, true),
    /**
     * whether tree logs respect gravity
     */
    BETTER_TREE_CHOPPING
            ("World Rules.Better Tree Felling", VarType.BOOLEAN, true),
    /**
     * whether players take additional damage and/or debuffs from environmental injuries
     */
    ENHANCED_ENVIRONMENTAL_DAMAGE
            ("Player.Enhanced Environmental Injuries", VarType.BOOLEAN, true),
    /**
     * whether players catch fire when extinguishing a fire up close
     */
    EXTINGUISHING_FIRE_IGNITES_PLAYERS
            ("Player.Extinguishing Fires Ignites Player", VarType.BOOLEAN, true),
    /**
     * percentage of item stacks lost on death
     */
    PLAYER_DEATH_ITEM_STACKS_FORFEIT_PERCENT
            ("Player.Death.Item Stacks Forfeit Percent", VarType.INTEGER, SubType.PERCENTAGE, 10),
    /**
     * how much health after respawn
     */
    PLAYER_RESPAWN_HEALTH
            ("Player.Death.Respawn Health", VarType.INTEGER, SubType.HEALTH, 15),
    /**
     * how much food bar after respawn
     */
    PLAYER_RESPAWN_FOOD_LEVEL
            ("Player.Death.Respawn Foodlevel", VarType.INTEGER, SubType.HEALTH, 15),
    /**
     * whether players may swim while wearing armor
     */
    NO_SWIMMING_IN_ARMOR
            ("Player.No Swimming When Too Heavy.Enable", VarType.BOOLEAN, true),
    /**
     * Block Swimming Up WaterFalls/WaterElevators
     */
    NO_SWIMMING_IN_ARMOR_BLOCK_ELEVATORS
            ("Player.No Swimming When Too Heavy.Block Elevators/Waterfalls", VarType.BOOLEAN, true),
    /**
     * The maximum amount of points you can have before being too heavy
     */
    NO_SWIMMING_IN_ARMOR_MAX_POINTS
            ("Player.No Swimming When Too Heavy.Max Points", VarType.DOUBLE, 18.0),
    /**
     * The amount of points a piece of armor adds to the max
     */
    NO_SWIMMING_IN_ARMOR_ARMOR_POINTS
            ("Player.No Swimming When Too Heavy.One Piece Of Worn Armor Adds", VarType.DOUBLE, 2.0),
    /**
     * The amount of points that stuff in your inventory adds to the max
     */
    NO_SWIMMING_IN_ARMOR_INV_POINTS
            ("Player.No Swimming When Too Heavy.One Stack Adds", VarType.DOUBLE, 1.0),
    /**
     * How much a tool or item which doesn't stack adds to the max
     */
    NO_SWIMMING_IN_ARMOR_TOOL_POINTS
            ("Player.No Swimming When Too Heavy.One Tool Adds", VarType.DOUBLE, 0.5),
    /**
     * How fast do you drown, 100 (percent) = you drown no chance, 25 there is a chance you'll drown
     */
    NO_SWIMMING_IN_ARMOR_DROWN_RATE
            ("Player.No Swimming When Too Heavy.Drown Rate", VarType.INTEGER, SubType.NATURAL_NUMBER, 35),
    /**
     * How much do you drown faster per weight over the max
     */
    NO_SWIMMING_IN_ARMOR_ENCUMBRANCE_EXTRA
            ("Player.No Swimming When Too Heavy.Overencumbrance Adds To Drown Rate", VarType.INTEGER, SubType.NATURAL_NUMBER, 2),

    /**
     * whether monster grinders (or "farms") should be inhibited
     */
    INHIBIT_MONSTER_GRINDERS
            ("General Monster Rules.Inhibit Monster Grinders", VarType.BOOLEAN, true),
    /**
     * max y value for extra monster spawns
     */
    MORE_MONSTERS_MAX_Y
            ("General Monster Rules.More Monsters.Max Y", VarType.INTEGER, SubType.Y_VALUE, Disable.ZERO, 55),
    /**
     * what to multiply monster spawns by
     */
    MORE_MONSTERS_MULTIPLIER
            ("General Monster Rules.More Monsters.Multiplier", VarType.INTEGER, SubType.NATURAL_NUMBER, Disable.ONE, 2),
    /**
     * max y value for monsters to spawn in the light
     */
    MONSTER_SPAWNS_IN_LIGHT_MAX_Y
            ("General Monster Rules.Monsters Spawn In Light Max Y", VarType.INTEGER, SubType.Y_VALUE, Disable.ZERO, 50),

    /**
     * whether zombies apply a debuff to players on hit
     */
    ZOMBIES_DEBILITATE_PLAYERS
            ("Zombies.Slow Players", VarType.BOOLEAN, true),
    /**
     * percent chance for a zombie to reanimate after death
     */
    ZOMBIES_REANIMATE_PERCENT
            ("Zombies.Reanimate Percent", VarType.INTEGER, SubType.PERCENTAGE, 50),

    /**
     * percent chance skeletons have a chance to knock back targets with arrows
     */
    SKELETONS_KNOCK_BACK_PERCENT
            ("Skeletons.Arrows Knockback Percent", VarType.INTEGER, SubType.PERCENTAGE, 30),
    /**
     * percent chance skeletons will release silverfish instead of firing arrows
     */
    SKELETONS_RELEASE_SILVERFISH
            ("Skeletons.Shoot Silverfish Percent", VarType.INTEGER, SubType.PERCENTAGE, 30),
    /**
     * whether or not arrows will pass harmlessly through skeletons
     */
    SKELETONS_DEFLECT_ARROWS
            ("Skeletons.Deflect Arrows Percent", VarType.INTEGER, SubType.PERCENTAGE, 100),
    /**
     * If Silverfish cant enter stone etc and turn it into a silverfish block
     */
    SILVERFISH_CANT_ENTER_BLOCKS
            ("Silverfish.Cant enter blocks", VarType.BOOLEAN, true),
    /**
     * If spawned silverfish drop cobble on death
     */
    SILVERFISH_DROP_COBBLE
            ("Silverfish.Drop Cobble", VarType.BOOLEAN, true),

    /**
     * percentage of zombies which will be replaced with spiders under sea level
     */
    BONUS_UNDERGROUND_SPIDER_SPAWN_PERCENT
            ("Spiders.Bonus Underground Spawn Percent", VarType.INTEGER, SubType.PERCENTAGE, 20),
    /**
     * whether spiders drop webbing when they die
     */
    SPIDERS_DROP_WEB_ON_DEATH
            ("Spiders.Drop Web On Death", VarType.BOOLEAN, true),

    /**
     * percentage of creepers which will spawn charged
     */
    CHARGED_CREEPER_SPAWN_PERCENT
            ("Creepers.Charged Creeper Spawn Percent", VarType.INTEGER, SubType.PERCENTAGE, 10),
    /**
     * percentage of creepers which spawn activated TNT on death
     */
    CREEPERS_DROP_TNT_ON_DEATH_PERCENT
            ("Creepers.Drop Tnt On Death.Percent", VarType.INTEGER, SubType.PERCENTAGE, 20),
    /**
     * max y for creepers to drop tnt, to restrict them to caves
     */
    CREEPERS_DROP_TNT_ON_DEATH_MAX_Y
            ("Creepers.Drop Tnt On Death.Max Y", VarType.INTEGER, SubType.Y_VALUE, Disable.ZERO, 50),
    /**
     * whether charged creepers explode when damaged
     */
    CHARGED_CREEPERS_EXPLODE_ON_HIT
            ("Creepers.Charged Creepers Explode On Damage", VarType.BOOLEAN, true),
    /**
     * whether creepers explode when caught on fire
     */
    FLAMING_CREEPERS_EXPLODE
            ("Creepers.Fire Triggers Explosion.Enable", VarType.BOOLEAN, true),
    /**
     * Number of Fireworks to show when creeper launches
     */
    FLAMING_CREEPERS_FIREWORK
            ("Creepers.Fire Triggers Explosion.Firework Count", VarType.INTEGER, 3),
    /**
     * Speed at which a creeper ascends
     */
    FLAMING_CREEPERS_ROCKET
            ("Creepers.Fire Triggers Explosion.Launch In Air Speed", VarType.DOUBLE, 0.5),

    /**
     * percentage of skeletons near bedrock which will be replaced with blazes
     */
    NEAR_BEDROCK_BLAZE_SPAWN_PERCENT
            ("Blazes.Near Bedrock Spawn Percent", VarType.INTEGER, SubType.PERCENTAGE, 50),
    /**
     * percentage of pig zombies which will be replaced with blazes
     */
    BONUS_NETHER_BLAZE_SPAWN_PERCENT
            ("Blazes.Bonus Nether Spawn Percent", VarType.INTEGER, SubType.PERCENTAGE, 20),
    /**
     * whether blazes drop fire when damaged
     */
    BLAZES_DROP_FIRE_ON_DAMAGE
            ("Blazes.Drop Fire On Damage", VarType.BOOLEAN, true),
    /**
     * whether blazes drop extra loot
     */
    BLAZES_DROP_BONUS_LOOT
            ("Blazes.Bonus Loot", VarType.BOOLEAN, true),
    /**
     * percentage chance that a blaze slain in the nether will split into two blazes
     */
    NETHER_BLAZES_SPLIT_ON_DEATH_PERCENT
            ("Blazes.Nether Split On Death Percent", VarType.INTEGER, SubType.PERCENTAGE, 25),

    /**
     * percentage chance that a blaze spawn will trigger a flame slime spawn as well
     */
    FLAME_SLIMES_SPAWN_WITH_NETHER_BLAZE_PERCENT
            ("MagmaCubes.Spawn With Nether Blaze Percent", VarType.INTEGER, SubType.PERCENTAGE, 100),
    /**
     * whether damaging a magma cube turns it into a blaze
     */
    MAGMA_CUBES_BECOME_BLAZES_ON_DAMAGE
            ("MagmaCubes.Grow Into Blazes On Damage", VarType.BOOLEAN, true),


    /**
     * whether pig zombies are always hostile
     */
    ALWAYS_ANGRY_PIG_ZOMBIES
            ("PigZombies.Always Angry", VarType.BOOLEAN, true),
    /**
     * whether pig zombies always drop nether wart in nether fortresses
     */
    FORTRESS_PIGS_DROP_WART
            ("PigZombies.Always Drop Netherwart In Fortresses", VarType.BOOLEAN, true),
    /**
     * Whether pig zombies should drop netherwart occasionally elsewhere in Nether
     */
    NETHER_PIGS_DROP_WART
            ("PigZombies.Percent Chance to Drop Netherwart Elsewhere In Nether", VarType.INTEGER, SubType.PERCENTAGE, 0),
    /**
     * PigMen get spawned when lighting strikes
     */
    LIGHTNING_SPAWNS_PIGMEN
            ("PigZombies.Spawn on Lighting Strikes.Enable", VarType.BOOLEAN, true),
    /**
     * whether ghasts should deflect arrows and drop extra loot
     * percentage like skeleton deflect
     */
    GHASTS_DEFLECT_ARROWS
            ("Ghasts.Immune To Arrows", VarType.BOOLEAN, true),
    /**
     * whether endermen may teleport players
     */
    IMPROVED_ENDERMAN_TELEPORTATION
            ("Endermen.May Teleport Players", VarType.BOOLEAN, true),

    /**
     * Do Witches have additional attacks
     */
    WITCHES_ADDITIONAL_ATTACKS
            ("Witches.Additional Attacks", VarType.BOOLEAN, true),
    /**
     * percentage of surface zombies which spawn as witches
     */
    BONUS_WITCH_SPAWN_PERCENT
            ("Witches.Bonus Spawn Percent", VarType.INTEGER, SubType.PERCENTAGE, 5),

    /**
     * whether the ender dragon respawns
     */
    RESPAWN_ENDER_DRAGON
            ("EnderDragon.Respawns", VarType.BOOLEAN, true),
    /**
     * whether it drops an egg when slain
     */
    ENDER_DRAGON_DROPS_EGG
            ("EnderDragon.Drops Dragonegg", VarType.BOOLEAN, true),
    /**
     * whether it drops a pair of villager eggs when slain
     */
    ENDER_DRAGON_DROPS_VILLAGER_EGGS
            ("EnderDragon.Drops 2 Villager Eggs", VarType.BOOLEAN, true),
    /**
     * whether the dragon spits fireballs and summons minions
     */
    ENDER_DRAGON_ADDITIONAL_ATTACKS
            ("EnderDragon.Harder Battle", VarType.BOOLEAN, true),
    /**
     * whether server wide messages will broadcast player victories and defeats
     */
    ENDER_DRAGON_COMBAT_ANNOUNCEMENTS
            ("EnderDragon.Battle Announcements", VarType.BOOLEAN, true),
    /**
     * whether players will be allowed to build in the end
     */
    ENDER_DRAGON_NO_BUILDING
            ("EnderDragon.No Building Allowed", VarType.BOOLEAN, true),

    /**
     * whether food crops die more easily
     */
    WEAK_FOOD_CROPS
            ("Farming.Weak Crops.Enable", VarType.BOOLEAN, true),
    /**
     * How much percent of plants you loose
     */
    WEAK_FOOD_CROPS_LOSS_RATE
            ("Farming.Weak Crops.Loss Rate", VarType.INTEGER, SubType.PERCENTAGE, 25),
    /**
     * Should desserts be really empty and hostile towards plants
     */
    ARID_DESSERTS
            ("Farming.Weak Crops.Infertile Deserts", VarType.BOOLEAN, true),
    /**
     * Weather Snow should break crops
     */
    SNOW_BREAKS_CROPS
            ("Farming.Weak Crops.Snow Breaks Crops", VarType.BOOLEAN, true),
    /**
     * Should you be able to craft melonseeds
     */
    CANT_CRAFT_MELONSEEDS
            ("Farming.Cant Craft Melonseeds", VarType.BOOLEAN, true),
    /**
     * whether bonemeal may be used on mushrooms
     */
    NO_BONEMEAL_ON_MUSHROOMS
            ("Farming.No Bonemeal On Mushrooms", VarType.BOOLEAN, true),
    /**
     * whether nether wart will ever drop more than 1 wart when broken
     */
    NO_FARMING_NETHER_WART
            ("Farming.No Farming Nether Wart", VarType.BOOLEAN, true),
    /**
     * whether sheep will always regrow white wool
     */
    SHEEP_REGROW_WHITE_WOOL
            ("Farming.Sheep Grow Only White Wool", VarType.BOOLEAN, true),
    /**
     * whether players may move water source blocks
     */
    DONT_MOVE_WATER_SOURCE_BLOCKS
            ("Farming.Buckets Dont Move Water Sources", VarType.BOOLEAN, true),
    /**
     * wheter animals should drop exp
     */
    ANIMAL_EXP_NERF
            ("Farming.Animal Experience Nerf", VarType.BOOLEAN, true),
    /**
     * Wheter More Falling blocks should be enabled
     */
    MORE_FALLING_BLOCKS_ENABLE
            ("Additional Falling Blocks.Enable", VarType.BOOLEAN, true),
    /**
     * How much damage loose Falling Logs do to Players and Animals
     */
    MORE_FALLING_BLOCKS_DMG_AMOUNT
            ("Additional Falling Blocks.Dmg Amount When Hitting Players" , VarType.INTEGER, 2),
    /**
     * wheter falling grass/mycel turns into dirt
     */
    MORE_FALLING_BLOCKS_TURN_TO_DIRT
            ("Additional Falling Blocks.Turn Mycel/Grass To Dirt", VarType.BOOLEAN, true),
    /**
     * which materials beyond sand and gravel should be subject to gravity
     */
    MORE_FALLING_BLOCKS
            ("Additional Falling Blocks.Enabled Blocks", VarType.LIST, new DefaultFallingBlocks()),

    /**
     * Should Stone be turned to cobblestone
     */
    EXPLOSIONS_TURN_STONE_TO_COBLE
            ("Explosions.Turn Stone To Cobble", VarType.BOOLEAN, true),
    /**
     * This determines if the explosion is categorized as under or above
     */
    EXPLOSIONS_Y
            ("Explosions.Border Y", VarType.INTEGER, 55),

    //WHEN ADDING NEW EXPLOSIONTYPES YOU HAVE TO ADD THE NODES TO EXPLOSIONTYPE AND ALSO UPDATE THE EXPLOSIONTASK
    /**CREEPER
     * Enable this custom explosion
     */
    EXPLOSIONS_CREEPERS_ENABLE
            ("Explosions.Creeper.Enable Custom Explosion", VarType.BOOLEAN, true),
    /**
     * Size of Explosion below border
     */
    EXPLOSIONS_CREEPERS_BELOW_POWER
            ("Explosions.Creeper.Below Border.Explosion Power", VarType.INTEGER, SubType.NATURAL_NUMBER, 3),
    /**
     * Set Fire on Explosion below border
     */
    EXPLOSIONS_CREEPERS_BELOW_FIRE
            ("Explosions.Creeper.Below Border.Set Fire", VarType.BOOLEAN, false),
    /**
     * Damage the world below border
     */
    EXPLOSIONS_CREEPERS_BELOW_WORLD_GRIEF
            ("Explosions.Creeper.Below Border.World Damage", VarType.BOOLEAN, true),
    /**
     * Size of Explosion below border
     */
    EXPLOSIONS_CREEPERS_ABOVE_POWER
            ("Explosions.Creeper.Above Border.Explosion Power", VarType.INTEGER,SubType.NATURAL_NUMBER, 3),
    /**
     * Set Fire on Explosion below border
     */
    EXPLOSIONS_CREEPERS_ABOVE_FIRE
            ("Explosions.Creeper.Above Border.Set Fire", VarType.BOOLEAN, false),
    /**
     * Damage the world below border
     */
    EXPLOSIONS_CREEPERS_ABOVE_WORLD_GRIEF
            ("Explosions.Creeper.Above Border.World Damage", VarType.BOOLEAN, true),

    /** Charged CREEPER
     * Enable?
     */
    EXPLOSIONS_CHARGED_CREEPERS_ENABLE
            ("Explosions.Charged Creeper.Enable Custom Explosion", VarType.BOOLEAN, true),
    /**
     * Size of Explosion below the border
     */
    EXPLOSIONS_CHARGED_CREEPERS_BELOW_POWER
            ("Explosions.Charged Creeper.Below Border.Explosion Power", VarType.INTEGER, SubType.NATURAL_NUMBER, 4),
    /**
     * Set Fire on Explosion below border
     */
    EXPLOSIONS_CHARGED_CREEPERS_BELOW_FIRE
            ("Explosions.Charged Creeper.Below Border.Set Fire", VarType.BOOLEAN, false),
    /**
     * Damage the world below border
     */
    EXPLOSIONS_CHARGED_CREEPERS_BELOW_WORLD_GRIEF
            ("Explosions.Charged Creeper.Below Border.World Damage", VarType.BOOLEAN, true),
    /**
     * Size of Explosion below border
     */
    EXPLOSIONS_CHARGED_CREEPERS_ABOVE_POWER
            ("Explosions.Charged Creeper.Above Border.Explosion Power", VarType.INTEGER, SubType.NATURAL_NUMBER, 4),
    /**
     * Set Fire on Explosion below border
     */
    EXPLOSIONS_CHARGED_CREEPERS_ABOVE_FIRE
            ("Explosions.Charged Creeper.Above Border.Set Fire", VarType.BOOLEAN, false),
    /**
     * Damage the world below border
     */
    EXPLOSIONS_CHARGED_CREEPERS_ABOVE_WORLD_GRIEF
            ("Explosions.Charged Creeper.Above Border.World Damage", VarType.BOOLEAN, true),

    /** TNT
     * Enable Custom Explosion?
     */
    EXPLOSIONS_TNT_ENABLE
            ("Explosions.Tnt.Enable Custom Explosion", VarType.BOOLEAN, true),
    /**
     * whether TNT should explode multiple times
     */
    BETTER_TNT
            ("Explosions.Tnt.Enable Multiple Explosions", VarType.BOOLEAN, true),
    /**
     * wheter the crafting recipe should give more tnt
     */
    MORE_TNT_NUMBER
            ("Explosions.Tnt.Tnt Per Recipe", VarType.INTEGER, SubType.NATURAL_NUMBER, Disable.ONE, 3),
    /**
     * Size of Explosion below the border
     */
    EXPLOSIONS_TNT_BELOW_POWER
            ("Explosions.Tnt.Below Border.Explosion Power", VarType.INTEGER, SubType.NATURAL_NUMBER, 6),
    /**
     * Set Fire on Explosion below border
     */
    EXPLOSIONS_TNT_BELOW_FIRE
            ("Explosions.Tnt.Below Border.Set Fire", VarType.BOOLEAN, false),
    /**
     * Damage the world below border
     */
    EXPLOSIONS_TNT_BELOW_WORLD_GRIEF
            ("Explosions.Tnt.Below Border.World Damage", VarType.BOOLEAN, true),
    /**
     * Size of Explosion below border
     */
    EXPLOSIONS_TNT_ABOVE_POWER
            ("Explosions.Tnt.Above Border.Explosion Power", VarType.INTEGER, SubType.NATURAL_NUMBER, 6),
    /**
     * Set Fire on Explosion below border
     */
    EXPLOSIONS_TNT_ABOVE_FIRE
            ("Explosions.Tnt.Above Border.Set Fire", VarType.BOOLEAN, false),
    /**
     * Damage the world below border
     */
    EXPLOSIONS_TNT_ABOVE_WORLD_GRIEF
            ("Explosions.Tnt.Above Border.World Damage", VarType.BOOLEAN, true),

    /** BLAZE
    * whether blazes explode and spread fire when they die
    */
    BLAZES_EXPLODE_ON_DEATH
            ("Explosions.Blazes Explode On Death.Enable", VarType.BOOLEAN, true),
    /**
     * Size of Explosion below the border
     */
    EXPLOSIONS_BLAZE_BELOW_POWER
            ("Explosions.Blazes Explode On Death.Below Border.Explosion Power", VarType.INTEGER, SubType.NATURAL_NUMBER, 4),
    /**
     * Set Fire on Explosion below border
     */
    EXPLOSIONS_BLAZE_BELOW_FIRE
            ("Explosions.Blazes Explode On Death.Below Border.Set Fire", VarType.BOOLEAN, true),
    /**
     * Damage the world below border
     */
    EXPLOSIONS_BLAZE_BELOW_WORLD_GRIEF
            ("Explosions.Blazes Explode On Death.Below Border.World Damage", VarType.BOOLEAN, true),
    /**
     * Size of Explosion below border
     */
    EXPLOSIONS_BLAZE_ABOVE_POWER
            ("Explosions.Blazes Explode On Death.Above Border.Explosion Power", VarType.INTEGER, SubType.NATURAL_NUMBER, 4),
    /**
     * Set Fire on Explosion below border
     */
    EXPLOSIONS_BLAZE_ABOVE_FIRE
            ("Explosions.Blazes Explode On Death.Above Border.Set Fire", VarType.BOOLEAN, true),
    /**
     * Damage the world below border
     */
    EXPLOSIONS_BLAZE_ABOVE_WORLD_GRIEF
            ("Explosions.Blazes Explode On Death.Above Border.World Damage", VarType.BOOLEAN, true),

    /** Ghast
     * Enable custom Explosion?
     */
    EXPLOSIONS_GHASTS_ENABLE
            ("Explosions.Ghasts.Enable Custom Explosion", VarType.BOOLEAN, true),
    /**
     * Size of Explosion below the border
     */
    EXPLOSIONS_GHAST_BELOW_POWER
            ("Explosions.Ghasts.Below Border.Explosion Power", VarType.INTEGER, SubType.NATURAL_NUMBER, 3),
    /**
     * Set Fire on Explosion below border
     */
    EXPLOSIONS_GHAST_BELOW_FIRE
            ("Explosions.Ghasts.Below Border.Set Fire", VarType.BOOLEAN, true),
    /**
     * Damage the world below border
     */
    EXPLOSIONS_GHAST_BELOW_WORLD_GRIEF
            ("Explosions.Ghasts.Below Border.World Damage", VarType.BOOLEAN, true),
    /**
     * Size of Explosion below border
     */
    EXPLOSIONS_GHAST_ABOVE_POWER
            ("Explosions.Ghasts.Above Border.Explosion Power", VarType.INTEGER, SubType.NATURAL_NUMBER, 3),
    /**
     * Set Fire on Explosion below border
     */
    EXPLOSIONS_GHAST_ABOVE_FIRE
            ("Explosions.Ghasts.Above Border.Set Fire", VarType.BOOLEAN, true),
    /**
     * Damage the world below border
     */
    EXPLOSIONS_GHAST_ABOVE_WORLD_GRIEF
            ("Explosions.Ghasts.Above Border.World Damage", VarType.BOOLEAN, true),
    ;

    /**
     * Path.
     */
    private final String path;
    /**
     * Variable type.
     */
    private final VarType type;
    /**
     * Subtype like percentage, y-value, health
     */
    private SubType subType = null;
    /**
     * Default value.
     */
    private final Object defaultValue;
    /**
     * The value that will disable this option
     */
    private Disable disableValue = null;

    /**
     * Constructor.
     *
     * @param path - Configuration path.
     * @param type - Variable type.
     * @param def  - Default value.
     */
    private RootNode(String path, VarType type, Object def)
    {
        this.path = path;
        this.type = type;
        this.defaultValue = def;
    }

    private RootNode(String path, VarType type, SubType subType, Object def)
    {
        this.path = path;
        this.type = type;
        this.defaultValue = def;
        this.subType = subType;
    }

    private RootNode(String path, VarType type, SubType subType, Disable disable, Object def)
    {
        this.path = path;
        this.type = type;
        this.defaultValue = def;
        this.subType = subType;
        this.disableValue = disable;
    }

    @Override
    public String getPath()
    {
        return baseNode() + "." + path;
    }

    @Override
    public VarType getVarType()
    {
        return type;
    }

    @Override
    public Object getDefaultValue()
    {
        return defaultValue;
    }

    @Override
    public SubType getSubType()
    {
        return subType;
    }

    public static String baseNode()
    {
        return "ExtraHardMode";
    }

    /**
     * Get the Object that will disable this option
     * @return Object that will disable this option in the plugin
     */
    @Override
    public Object getValueToDisable ()
    {
        Object obj;
        switch (type)
        {
            case BOOLEAN:
            {
                obj = false;
                break;
            }
            case INTEGER:
            {
                obj = 0;
                if (subType != null)
                {
                    switch (subType)
                    {
                        case NATURAL_NUMBER:
                        case Y_VALUE:
                        {
                            if (disableValue != null)
                                obj = (Integer) disableValue.get();
                            break;
                        }
                        case HEALTH:
                        {
                            obj = 20;
                            break;
                        }
                        case PERCENTAGE:
                        {
                            obj = 0;
                            break;
                        }
                        default:
                        {
                            obj = defaultValue;
                            throw new UnsupportedOperationException("SubType hasn't been specified for " + path);
                        }
                    }
                }
                break;
            }
            case DOUBLE:
            {
                obj = 0.0;
                break;
            }
            case STRING:
            {
                obj = "";
                break;
            }
            case LIST:
            {
                obj = Collections.emptyList();
                break;
            }
            default:
            {
                throw new UnsupportedOperationException("Type of " + type + " doesn't have a default value to be disabled");
            }
        }
        return obj;
    }

    /**
     * Contains values for some Nodes which require a special value, which differs from other Nodes with the same type
     */
    private enum Disable
    {
        /**
         * A value of 0 will disable this feature in the plugin
         */
        ZERO (0),
        /**
         * A value of 1 will disable this feature in the plugin
         */
        ONE (1);

        private Disable (Object obj)
        {
            disable = obj;
        }

        final Object disable;

        public Object get()
        {
            return disable;
        }
    }

    /**
     * Default list of falling blocks.
     */
    private static class DefaultFallingBlocks extends ArrayList<String>
    {

        /**
         * Serial Version UID.
         */
        private static final long serialVersionUID = 1L;

        /**
         * Constructor.
         */
        public DefaultFallingBlocks()
        {
            super();
            this.add(Material.DIRT.toString());
            this.add(Material.GRASS.toString());
            this.add(Material.COBBLESTONE.toString());
            this.add(Material.MOSSY_COBBLESTONE.toString());
            this.add(Material.DOUBLE_STEP.toString() + "@3"); //cobble double halfslabs
            this.add(Material.MYCEL.toString());
        }
    }
}
