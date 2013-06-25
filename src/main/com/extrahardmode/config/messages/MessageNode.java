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
            ("NoTorchesHere", -1, "torches_underground",
                    "There's not enough air flow down here for permanent flames. Use another method to light your way."),
    STONE_MINING_HELP
            ("StoneMiningHelp", -1, "hardened_stone",
                    "You'll need an iron or diamond pickaxe to break stone.  Try exploring natural formations for exposed ore like coal, which softens stone around it when broken."),
    NO_PLACING_ORE_AGAINST_STONE
            ("NoPlacingOreAgainstStone", -1, "hardened_stone_ore",
                    "Sorry, you can't place ore next to stone."),
    REALISTIC_BUILDING
            ("RealisticBuilding", -1, "realistic_building",
                    "You can't build while in the air."),
    LIMITED_TORCH_PLACEMENTS
            ("LimitedTorchPlacements", -1, "torches_soft_blocks",
                    "It's too soft there to fasten a torch."),
    NO_CRAFTING_MELON_SEEDS
            ("NoCraftingMelonSeeds", -1, "no_crafting_melon_seeds",
                    "That appears to be seedless!"),
    LIMITED_END_BUILDING
            ("LimitedEndBuilding", -1, "limited_building_end",
                    "Sorry, building here is very limited.  You may only break blocks to reach ground level."),
    DRAGON_FOUNTAIN_TIP
            ("DragonFountainTip", -1, "dragon_fountain_tip",
                    "Congratulations on defeating the dragon!  If you can't reach the fountain to jump into the portal, throw an ender pearl at it."),
    NO_SWIMMING_IN_ARMOR
            ("NoSwimmingInArmor", -1, "no_swimming_in_armor_warning",
                    "You're carrying too much weight to swim!"),
    END_DRAGON_KILLED
            ("DragonDefeated", -1, "end_dragon_killed",
                    "The dragon has been defeated!  ( By: " + variables.PLAYERS.getVarName() + " )"),
    END_DRAGON_PLAYER_KILLED
            ("PlayerKilledByDragon", -1, "player_killed_dragon",
                    variables.PLAYER.getVarName() + " was killed while fighting the dragon!"),
    END_DRAGON_PLAYER_CHALLENGING
            ("PlayerChallengingDragon", -1, "player_challenging_dragon",
                    variables.PLAYER.getVarName() + " is challenging the dragon!"),

    //Target Events
    CHARGED_CREEPER_TARGET
            ("Charged Creeper", 3, "charged_creeper",
                    "&cCharged Creepers explode instantly when hit. Run!"),
    BLAZE_TARGET_NORMAL
            ("Blaze Overworld", 5, "blaze_overworld",
                    "&cBlazes spawn near lava and their fiery breath causes a big explosion on death!"),
    BLAZE_TARGET_NETHER
            ("Blaze Nether", 5, "blaze_nether",
                    "&cBlazes spawn everywhere in the Nether and may split on death!"),
    MAGMACUBE_TARGET
            ("Magmacube", 3, "magmacube",
                    "&cThese small buggers actually are just disguised blazes!"),
    GHAST_TARGET
            ("Ghast", 3, "ghast_warning",
                    "&cThese fearsome Ghasts wear invisible arrow deflective armor! Ghasts drop a lot more loot as well."),
    PIGZOMBIE_TARGET
            ("Pig Zombie Always Angry", 3, "pigzombie",
                    "&cRUN! Pig Zombies are always angry and hungry!"),
    PIGZOMBIE_TARGET_WART
            ("Pig Zombie Drop Netherwart", 3, "pigzombie_wart",
                    "&cYou can get netherwart from slaying Pig Zombies"),

    //Various Own Events
    ZOMBIE_RESPAWN
            ("Zombie Respawn", 5, "zombie_respawn",
                    "&cZombies might resurrect if not on fire!"),
    SKELETON_DEFLECT
            ("Skeleton Deflect Arrows", 3, "skeleton_deflect_arrow",
                    "&cArrows just pass through Skeletons, you gotta go close combat!"),
    ENDERMAN_GENERAL
            ("Enderman General", 3, "enderman_teleport",
                    "&cEnderman can teleport you too!"),
    ENDERMAN_SUICIDAL
            ("Enderman Suicidal", 1, "enderman_deaggro",
                    "&cGet better gear if you want to fight an enderman or die trying!"),
    CREEPER_DROP_TNT
            ("Creeper Drop Tnt", 2, "creeper_drop_tnt",
                    "&cCreepers may drop activated tnt on death!"),
    EXTINGUISH_FIRE
            ("Extinguish Fire", 3, "extinguish_fire",
                    "&cPutting out fire with your hand will catch you on fire."),

    //Farming
    BUCKET_FILL
            ("Bucket Fill", 3, "bucket_fill",
                    "&cYou can pick up water, but once you place it, it evaporates. Get some ice if you want to farm!"),
    ANTIFARMING_UNWATERD
            ("Antifarming Unwatered", 3, "antifarm_unwatered",
                    "&cYour crops need sufficient water, otherwise they'll dry out!"),
    ANTIFARMING_NO_LIGHT
            ("Antifarming Not Enough Light", 3, "antifarm_natural_light",
                    "&cYour crops require natural light to grow!"),
    ANTIFARMING_DESSERT_WARNING
            ("Antifarming Desert", 3, "antifarm_desert",
                    "&cDeserts are really dry and nothing grows here!"),

    //General Advice
    NETHER_WARNING
            ("Nether Warning", 2 ,"nether_warn",
                    "&cThis is a dangerous place. Make sure you come prepared with arrows and good gear.")
    ;

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
    private final VarType type = VarType.STRING;
    /**
     * Name of the column to be used for persistence
     */
    private final String column;
    /**
     * How often this msg should be displayed to a Player (0 = off, -1 = infinite)
     */
    private final int msgCount;

    /**
     * Constructor.
     *
     * @param value - Default Value
     */
    private MessageNode(String path, int msgCount, String column, String value)
    {
        this.path = path;
        this.msgCount = msgCount;
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
        return type;
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
     * Get the name of the column.
     * Used by our sqllite db for persistence.
     *
     * @return column name. Won't be changed.
     */
    public String getColumnName()
    {
        return column;
    }

    public int getMsgCount ()
    {
        return msgCount;
    }

    /**
     * Variables that will be filled in by the plugin
     */
    public enum variables
    {
        PLAYER ("$PLAYER"),
        PLAYERS ("$PLAYERS");

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
