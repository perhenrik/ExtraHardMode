package com.extrahardmode.metrics;


import com.extrahardmode.config.RootConfig;
import com.extrahardmode.config.RootNode;
import com.extrahardmode.service.config.ConfigNode;
import org.bukkit.plugin.Plugin;
import org.mcstats.Metrics;

/**
 * Output all the choosen modules to mcstats in nice plots
 *
 * @author Diemex
 */
public class ConfigPlotter
{
    private final Plugin plugin;

    private final RootConfig CFG;


    public ConfigPlotter(Plugin plugin, RootConfig CFG)
    {
        this.plugin = plugin;
        this.CFG = CFG;
        createPlot();
    }


    private void createPlot()
    {

        // Enabled metrics
        Metrics metrics;
        try
        {
            metrics = new Metrics(plugin);

            metrics.createGraph("Enabled for % of worlds").addPlotter(
                    new Metrics.Plotter()
                    {

                        @Override
                        public int getValue()
                        {
                            return plugin.getServer().getWorlds().size() * 100 / CFG.getEnabledWorlds().length;
                        }

                    });

            for (final RootNode node : RootNode.values())
            {
                switch (node)
                {
                    case ALWAYS_ANGRY_PIG_ZOMBIES:
                    case ANIMAL_EXP_NERF:
                    case BETTER_TNT:
                    case BETTER_TREE_CHOPPING:
                    case BLAZES_EXPLODE_ON_DEATH:
                    case CANT_CRAFT_MELONSEEDS:
                    case CHARGED_CREEPERS_EXPLODE_ON_HIT:
                    case DONT_MOVE_WATER_SOURCE_BLOCKS:
                    case ENDER_DRAGON_ADDITIONAL_ATTACKS:
                    case ENHANCED_ENVIRONMENTAL_DAMAGE:
                    case EXTINGUISHING_FIRE_IGNITES_PLAYERS:
                    case FORTRESS_PIGS_DROP_WART:
                    case GHASTS_DEFLECT_ARROWS:
                    case IMPROVED_ENDERMAN_TELEPORTATION:
                    case INHIBIT_MONSTER_GRINDERS:
                    case LIMITED_BLOCK_PLACEMENT:
                    case LIMITED_TORCH_PLACEMENT:
                    case MAGMA_CUBES_BECOME_BLAZES_ON_DAMAGE:
                    //case MORE_MONSTERS_MULTIPLIER:
                    //case NETHER_PIGS_DROP_WART:
                    case NO_BONEMEAL_ON_MUSHROOMS:
                    case NO_FARMING_NETHER_WART:
                    case NO_SWIMMING_IN_ARMOR:
                    case RAIN_BREAKS_TORCHES:
                    //case SILVERFISH_CANT_ENTER_BLOCKS:
                    case SNOW_BREAKS_CROPS:
                    case SUPER_HARD_STONE:
                    case SUPER_HARD_STONE_PHYSICS:
                    case SPIDERS_DROP_WEB_ON_DEATH:
                    case WEAK_FOOD_CROPS:
                    case WITCHES_ADDITIONAL_ATTACKS:
                    case ZOMBIES_DEBILITATE_PLAYERS:
                    {
                        metrics.createGraph(getLastPart(node)).addPlotter(
                                new Metrics.Plotter()
                                {

                                    @Override
                                    public int getValue()
                                    {
                                        return CFG.getMetricsValue(node);
                                    }

                                });
                        break;
                    }
                }
            }

            metrics.start();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static String getLastPart(ConfigNode node)
    {
        String path = node.getPath();
        String[] split = path.split("\\."); //Durr it's a regex...
        return split.length > 0 ? split[split.length - 1] : "";
    }
}
