package com.extrahardmode.features;

import com.extrahardmode.service.PermissionNode;

/**
 * Holds some information about a given feature
 * like the permission which will bypass it
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
    MONSTER_BUMBUMBENS,
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
        DEATH_INV_LOSS,
        ENVIRONMENTAL_EFFECTS,
        DANGEROUS_FIRES,
    REALISTIC_CHOPPING,
    TORCHES,
    WATER;

    private final PermissionNode node;

    /**
     * Constructor
     *
     * for Features with no dedicated bypass permission
     */
    private Feature ()
    {
        node = PermissionNode.BYPASS;
    }

    /**
     * Constructor
     *
     * for Features with a dedicated bypass permission
     * @param node which will bypass
     */
    private Feature (PermissionNode node)
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
