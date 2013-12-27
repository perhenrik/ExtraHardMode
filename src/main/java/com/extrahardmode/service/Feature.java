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

package com.extrahardmode.service;


/**
 * Holds some information about a given feature like the permission which will bypass it
 *
 * @author Max
 */
public enum Feature
{
    /**
     * MONSTER CLASSES
     */
    MONSTER_BITCHES,
    MONSTER_BLAZES,
    MONSTER_BOBS,
    MONSTER_BUMBUMBENS(PermissionNode.BYPASS_CREEPERS),
    MONSTER_GHASTS,
    MONSTER_GLYDIA,
    MONSTERRULES,
    MONSTER_PIGIES,
    MONSTER_SILVERFISH,
    MONSTER_SKELETONS,
    MONSTER_SPOIDER,
    MONSTER_ZOMBIES,

    /**
     * GENERAL CLASSES
     */
    ANTIFARMING,
    ANTIGRINDER,
    EXPLOSIONS,
    HARDENEDSTONE,
    LIMITED_BUILDING,
    PHYSICS,
    MORE_FALLING_BLOCKS,
    PLAYERS,
    RESPAWN_FOOD_HEALTH,
    DEATH_INV_LOSS(PermissionNode.BYPASS_INVENTORY),
    ENVIRONMENTAL_EFFECTS,
    DANGEROUS_FIRES,
    REALISTIC_CHOPPING,
    TORCHES,
    WATER;

    private final PermissionNode node;


    /**
     * Constructor
     * <p/>
     * for Features with no dedicated bypass permission
     */
    private Feature()
    {
        node = PermissionNode.BYPASS;
    }


    /**
     * Constructor
     * <p/>
     * for Features with a dedicated bypass permission
     *
     * @param node which will bypass
     */
    private Feature(PermissionNode node)
    {
        this.node = node;
    }


    /**
     * Get the node which will bypass this feature
     *
     * @return bypass node
     */
    public PermissionNode getBypassNode()
    {
        return node;
    }
}
