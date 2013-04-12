/*
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package me.ryanhamshire.ExtraHardMode.config.messages;

import me.ryanhamshire.ExtraHardMode.service.ConfigNode;

/**
 * Configuration nodes for messages.yml configuration file.
 */
public enum MessageNode implements ConfigNode
{
    NO_TORCHES_HERE("Messages.NoTorchesHere", "There's not enough air flow down here for permanent flames. Use another method to light your way."),
    STONE_MINING_HELP(
            "Messages.StoneMiningHelp",
            "You'll need an iron or diamond pickaxe to break stone.  Try exploring natural formations for exposed ore like coal, which softens stone around it when broken."),
    NO_PLACING_ORE_AGAINST_STONE("Messages.NoPlacingOreAgainstStone", "Sorry, you can't place ore next to stone."),
    REALISTIC_BUILDING("Messages.RealisticBuilding", "You can't build while in the air."),
    LIMITED_TORCH_PLACEMENTS("Messages.LimitedTorchPlacements", "It's too soft there to fasten a torch."),
    NO_CRAFTING_MELON_SEEDS("Messages.NoCraftingMelonSeeds", "That appears to be seedless!"),
    LIMITED_END_BUILDING("Messages.LimitedEndBuilding", "Sorry, building here is very limited.  You may only break blocks to reach ground level."),
    DRAGON_FOUNTAIN_TIP("Messages.DragonFountainTip",
            "Congratulations on defeating the dragon!  If you can't reach the fountain to jump into the portal, throw an ender pearl at it."),
    NO_SWIMMING_IN_ARMOR("Message.NoSwimmingInArmor", "You're carrying too much weight to swim!");

    /**
     * Configuration path.
     */
    private final String path;
    /**
     * Messages are always strings.
     */
    private final VarType type = VarType.STRING;
    /**
     * Default value.
     */
    private final Object defaultValue;

    /**
     * Constructor.
     *
     * @param path - Configuration path.
     * @param def  - Default value.
     */
    private MessageNode(String path, Object def)
    {
        this.path = path;
        this.defaultValue = def;
    }

    @Override
    public String getPath()
    {
        return path;
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
    public Object getDefaultValue()
    {
        return defaultValue;
    }
}
