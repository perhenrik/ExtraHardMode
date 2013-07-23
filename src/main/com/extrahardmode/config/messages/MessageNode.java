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


package com.extrahardmode.config.messages;


import com.extrahardmode.service.config.ConfigNode;

/**
 * Configuration nodes for messages.yml configuration file.
 */
public enum MessageNode implements ConfigNode
{
    NO_TORCHES_HERE
            ("NoTorchesHere", MsgType.NOTIFICATION, "torches_underground",
                    "There's not enough air flow down here for permanent flames. Use another method to light your way."),
    STONE_MINING_HELP
            ("StoneMiningHelp", MsgType.NOTIFICATION, "hardened_stone",
                    "You'll need an iron or diamond pickaxe to break stone.  Try exploring natural formations for exposed ore like coal, which softens stone around it when broken."),
    NO_PLACING_ORE_AGAINST_STONE
            ("NoPlacingOreAgainstStone", MsgType.NOTIFICATION, "hardened_stone_ore",
                    "Sorry, you can't place ore next to stone."),
    REALISTIC_BUILDING
            ("RealisticBuilding", MsgType.NOTIFICATION, "realistic_building",
                    "You can't build while in the air."),
    REALISTIC_BUILDING_BENEATH
            ("RealisticBuildingBeneath", MsgType.NOTIFICATION, "realistic_building_beneath",
                    "You can't place a block directly beneath you."),
    LIMITED_TORCH_PLACEMENTS
            ("LimitedTorchPlacements", MsgType.NOTIFICATION, "torches_soft_blocks",
                    "It's too soft there to fasten a torch."),
    NO_CRAFTING_MELON_SEEDS
            ("NoCraftingMelonSeeds", MsgType.NOTIFICATION, "no_crafting_melon_seeds",
                    "That appears to be seedless!"),
    LIMITED_END_BUILDING
            ("LimitedEndBuilding", MsgType.NOTIFICATION, "limited_building_end",
                    "Sorry, building here is very limited.  You may only break blocks to reach ground level."),
    DRAGON_FOUNTAIN_TIP
            ("DragonFountainTip", MsgType.NOTIFICATION, "dragon_fountain_tip",
                    "Congratulations on defeating the dragon!  If you can't reach the fountain to jump into the portal, throw an ender pearl at it."),
    NO_SWIMMING_IN_ARMOR
            ("NoSwimmingInArmor", MsgType.NOTIFICATION, "no_swimming_in_armor_warning",
                    "You're carrying too much weight to swim!"),
    END_DRAGON_KILLED
            ("DragonDefeated", MsgType.NOTIFICATION, "end_dragon_killed",
                    "The dragon has been defeated!  ( By: " + variables.PLAYERS.getVarName() + " )"),
    END_DRAGON_PLAYER_KILLED
            ("PlayerKilledByDragon", MsgType.NOTIFICATION, "player_killed_dragon",
                    variables.PLAYER.getVarName() + " was killed while fighting the dragon!"),
    END_DRAGON_PLAYER_CHALLENGING
            ("PlayerChallengingDragon", MsgType.NOTIFICATION, "player_challenging_dragon",
                    variables.PLAYER.getVarName() + " is challenging the dragon!"),

    //Horses
    HORSE_FEED_LOW
            ("HorseFeedLow", MsgType.NOTIFICATION, "horse_feed_low",
                    "You have saved your horse from starving, but it\'s still very hungry."),
    HORSE_FEED_MIDDLE
            ("HorseFeedMiddle", MsgType.NOTIFICATION, "horse_feed_middle",
                    "Your horse has ate well."),
    HORSE_FEED_HIGH
            ("HorseFeedHigh", MsgType.NOTIFICATION, "horse_feed_high",
                    "Your horse is satiated."),

    HORSE_STARVING_LOW
            ("HorseStarveLow", MsgType.NOTIFICATION, "horse_starve_low",
                    "Your horse is very tired and needs some food."),
    HORSE_STARVING_DANGEROUS
            ("HorseStarveMiddle", MsgType.NOTIFICATION, "horse_starve_dangerous",
                    "Your horse is about to starve."),
    HORSE_STARVING_IMMINENT_DEATH
            ("HorseStarveHigh", MsgType.NOTIFICATION, "horse_starve_imminent_death",
                    "Your horse is starving, feed it or it will die."),


    //Target Events
    CHARGED_CREEPER_TARGET
            ("ChargedCreeper", MsgType.TUTORIAL, "charged_creeper",
                    "&cCharged Creepers explode instantly when hit. Run!"),
    BLAZE_TARGET_NORMAL
            ("BlazeOverworld", MsgType.TUTORIAL, "blaze_overworld",
                    "&cBlazes spawn near lava and their fiery breath causes a big explosion on death!"),
    BLAZE_TARGET_NETHER
            ("BlazeNether", MsgType.TUTORIAL, "blaze_nether",
                    "&cBlazes spawn everywhere in the Nether and may split on death!"),
    MAGMACUBE_TARGET
            ("Magmacube", MsgType.TUTORIAL, "magmacube",
                    "&cThese small buggers actually are just disguised blazes!"),
    GHAST_TARGET
            ("Ghast", MsgType.TUTORIAL, "ghast_warning",
                    "&cThese fearsome Ghasts wear invisible arrow deflective armor! Ghasts drop a lot more loot as well."),
    PIGZOMBIE_TARGET
            ("PigZombieAlwaysAngry", MsgType.TUTORIAL, "pigzombie",
                    "&cRUN! Pig Zombies are always angry and hungry!"),
    PIGZOMBIE_TARGET_WART
            ("PigZombieDropNetherwart", MsgType.TUTORIAL, "pigzombie_wart",
                    "&cYou can get netherwart from slaying Pig Zombies"),

    //Various Own Events
    ZOMBIE_RESPAWN
            ("ZombieRespawn", MsgType.TUTORIAL, "zombie_respawn",
                    "&cZombies might resurrect if not on fire!"),
    ZOMBIE_SLOW_PLAYERS
            ("ZombieSlowPlayer", MsgType.TUTORIAL, "zombie_slow",
                    "&cZombies slow you down when hit!"),
    SKELETON_DEFLECT
            ("SkeletonDeflectArrows", MsgType.TUTORIAL, "skeleton_deflect_arrow",
                    "&cArrows just pass through Skeletons, you gotta go close combat!"),
    ENDERMAN_GENERAL
            ("EndermanGeneral", MsgType.TUTORIAL, "enderman_teleport",
                    "&cEnderman can teleport you too!"),
    ENDERMAN_SUICIDAL
            ("EndermanSuicidal", MsgType.ONE_TIME, "enderman_deaggro",
                    "&cGet better gear if you want to fight an enderman or die trying!"),
    CREEPER_DROP_TNT
            ("CreeperDropTnt", MsgType.TUTORIAL, "creeper_drop_tnt",
                    "&cCreepers may drop activated tnt on death!"),
    EXTINGUISH_FIRE
            ("ExtinguishFire", MsgType.TUTORIAL, "extinguish_fire",
                    "&cPutting out fire with your hand will catch you on fire."),
    LOST_ITEMS
            ("ListLostItemsOnDeath", MsgType.NOTIFICATION, "lost_items_broadcast",
                    variables.DEATH_MSG + " and managed to loose " + variables.ITEMS.getVarName()),
    LOST_ITEMS_PLAYER
            ("InformPlayerOnLostItems", MsgType.TUTORIAL, "lost_items",
                    "On death there is a a chance you might loose some of your items!"),

    //Farming
    BUCKET_FILL
            ("BucketFill", MsgType.TUTORIAL, "bucket_fill",
                    "&cYou can pick up water, but once you place it, it evaporates. Get some ice if you want to farm!"),
    ANTIFARMING_UNWATERD
            ("AntifarmingUnwatered", MsgType.TUTORIAL, "antifarm_unwatered",
                    "&cYour crops need sufficient water, otherwise they\'ll dry out!"),
    ANTIFARMING_NO_LIGHT
            ("AntifarmingNotEnoughLight", MsgType.TUTORIAL, "antifarm_natural_light",
                    "&cYour crops require natural light to grow!"),
    ANTIFARMING_DESSERT_WARNING
            ("AntifarmingDesert", MsgType.TUTORIAL, "antifarm_desert",
                    "&cDeserts are really dry and nothing grows here!"),

    //General Advice
    NETHER_WARNING
            ("NetherWarning", MsgType.TUTORIAL, "nether_warn",
                    "&cThis is a dangerous place. Make sure you come prepared with arrows and good gear.");

    /**
     * Path in the Config
     */
    private final String path;

    /**
     * Default value
     */
    private final String value;

    /**
     * Messages are always strings.
     */
    private final VarType varType = VarType.STRING;

    /**
     * Name of the column to be used for persistence
     */
    private final String column;

    /**
     * How often this msg should be displayed to a Player (0 = off, -1 = infinite)
     */
    private final MsgType msgType;


    /**
     * Constructor.
     *
     * @param value
     *         - Default Value
     */
    private MessageNode(String path, MsgType msgType, String column, String value)
    {
        this.path = path;
        this.msgType = msgType;
        this.column = column;
        this.value = value;
    }


    @Override
    public String getPath()
    {
        return "Messages." + path;
    }


    @Override
    public VarType getVarType()
    {
        return varType;
    }


    @Override
    public SubType getSubType()
    {/*ignored*/
        return null;
    }


    @Override
    public Object getValueToDisable()
    {   /*ignored*/
        return null;
    }


    @Override
    public Object getDefaultValue()
    {
        return value;
    }


    /**
     * Get the name of the column. Used by our sqllite db for persistence.
     *
     * @return column name. Won't be changed.
     */
    public String getColumnName()
    {
        return column;
    }


    /**
     * Get the type of this message
     *
     * @return type
     */
    public MsgType getMsgType()
    {
        return msgType;
    }


    /**
     * Get how often a message is supposed to be displayed
     *
     * @return how often this message will be displayed. -1 = no limit
     */
    public int getMsgCount()
    {
        switch (msgType)
        {
            case TUTORIAL:
                return 3;
            case NOTIFICATION:
                return -1;
            case BROADCAST:
                return -1;
            case ONE_TIME:
                return 1;
            default:
                throw new UnsupportedOperationException("Not Implemented MsgType");
        }
    }


    public enum MsgType
    {
        /**
         * A message which will only be shown a few times and then won't show anymore at all
         */
        TUTORIAL,
        /**
         * An informative message which will show always but with a timeout
         */
        NOTIFICATION,
        /**
         * A message which get's broadcast to the whole server
         */
        BROADCAST,
        /**
         * Will only be shown once
         */
        ONE_TIME,
        /**
         * Currently disabled
         */
        DISABLED,
    }


    /**
     * Variables that will be filled in by the plugin
     */
    public enum variables
    {
        DEATH_MSG("DEATH_MSG"),
        PLAYER("$PLAYER"),
        PLAYERS("$PLAYERS"),
        ITEMS("ITEMS");

        private final String variable;


        private variables(String variable)
        {
            this.variable = variable;
        }


        /**
         * Get the identifier which represents this variable in the String
         */
        public String getVarName()
        {
            return variable;
        }
    }
}
