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
            ("NoTorchesHere",
                    "There's not enough air flow down here for permanent flames. Use another method to light your way."),
    STONE_MINING_HELP
            ("StoneMiningHelp",
                    "You'll need an iron or diamond pickaxe to break stone.  Try exploring natural formations for exposed ore like coal, which softens stone around it when broken."),
    NO_PLACING_ORE_AGAINST_STONE
            ("NoPlacingOreAgainstStone",
                    "Sorry, you can't place ore next to stone."),
    REALISTIC_BUILDING
            ("RealisticBuilding",
                    "You can't build while in the air."),
    LIMITED_TORCH_PLACEMENTS
            ("LimitedTorchPlacements",
                    "It's too soft there to fasten a torch."),
    NO_CRAFTING_MELON_SEEDS
            ("NoCraftingMelonSeeds",
                    "That appears to be seedless!"),
    LIMITED_END_BUILDING
            ("LimitedEndBuilding",
                    "Sorry, building here is very limited.  You may only break blocks to reach ground level."),
    DRAGON_FOUNTAIN_TIP
            ("DragonFountainTip",
                    "Congratulations on defeating the dragon!  If you can't reach the fountain to jump into the portal, throw an ender pearl at it."),
    NO_SWIMMING_IN_ARMOR
            ("NoSwimmingInArmor",
                    "You're carrying too much weight to swim!"),
    END_DRAGON_KILLED
            ("DragonDefeated",
                    "The dragon has been defeated!  ( By: " + variables.PLAYERS.getVarName() + " )"),
    END_DRAGON_PLAYER_KILLED
            ("PlayerKilledByDragon",
                    variables.PLAYER.getVarName() + " was killed while fighting the dragon!"),
    END_DRAGON_PLAYER_CHALLENGING
            ("PlayerChallengingDragon",
                    variables.PLAYER.getVarName() + " is challenging the dragon!"),

    //Target Events
    CHARGED_CREEPER_TARGET
            ("Charged Creeper",
                    "&cCharged Creeper explode instantly... run!"),
    BLAZE_TARGET_NORMAL
            ("Blaze Overworld",
                    "&cBlazes spawn near lava and their fiery breath causes a big explosion on death!"),
    BLAZE_TARGET_NETHER
            ("Blaze Nether",
                    "&cBlazes spawn Everywhere in the Nether and may split on death!"),
    MAGMACUBE_TARGET
            ("Magmacube",
                    "&cThese small buggers actually are just disguised blazes!"),
    GHAST_TARGET
            ("Ghast",
                    "&cLucifer upgraded his Ghasts with arrow deflective armor! Ghasts drop a lot more loot as well."),
    PIGZOMBIE_TARGET
            ("Pig Zombie Always Angry",
                    "&cRUN! Pig Zombies are always angry and hungry!"),
    PIGZOMBIE_TARGET_WART
            ("Pig Zombie Drop Netherwart",
                    "&cYou can get Netherwart from slaying Pig Zombies"),

    //Various Own Events
    ZOMBIE_RESPAWN
            ("Zombie Respawn",
                    "&cZombies might resurrect if not on fire!"),
    SKELETON_DEFLECT
            ("Skeleton Deflect Arrows",
                    "&cArrows just pass through Skeletons, you gotta go close combat!"),
    ENDERMAN_GENERAL
            ("Enderman General",
                    "&cEnderman can teleport you too!"),
    ENDERMAN_SUICIDAL
            ("Enderman Suicidal",
                    "&cThe Enderman has been deaggroed, you would've died otherwise!"),

    //Farming
    BUCKET_FILL
            ("Bucket Fill",
                    "&cYou can pick up water, but once you place it, it evaporates. Get some ice if you want to farm!"),
    ANTIFARMING_UNWATERD
            ("Antifarming Unwatered",
                    "&cYour crops need sufficient water, otherwise they'll dry out!"),
    ANTIFARMING_NO_LIGHT
            ("Antifarming Not Enough Light",
                    "&cYour crops require natural light to grow!"),
    ANTIFARMING_DESSERT_WARNING
            ("Antifarming Desert",
                    "&cDeserts are really dry and nothing grows here!"),

    //General Advice
    NETHER_WARNING
            ("Nether Warning",
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
     * Constructor.
     *
     * @param value - Default Value
     */
    private MessageNode(String path, String value)
    {
        this.path = path;
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
