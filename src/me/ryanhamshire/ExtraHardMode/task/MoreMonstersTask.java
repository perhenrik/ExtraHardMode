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

package me.ryanhamshire.ExtraHardMode.task;

import me.ryanhamshire.ExtraHardMode.ExtraHardMode;
import me.ryanhamshire.ExtraHardMode.config.Config;
import me.ryanhamshire.ExtraHardMode.module.DataStoreModule;
import me.ryanhamshire.ExtraHardMode.service.PermissionNode;
import org.bukkit.*;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.AbstractMap.SimpleEntry;

/**
 * Task to spawn more monsters.
 */
public class
        MoreMonstersTask implements Runnable
{
    //TODO Fix weird bug
    //TODO if block not valid check random block nearby
    //TODO check for nearby players, test the distance

    /**
     * Plugin instance.
     */
    private ExtraHardMode plugin;

    /**
     * Constructor.
     *
     * @param plugin - Plugin instance.
     */
    public MoreMonstersTask(ExtraHardMode plugin)
    {
        this.plugin = plugin;
    }

    @Override
    public void run()
    {
        DataStoreModule dataStore = plugin.getModuleForClass(DataStoreModule.class);
        // spawn monsters from the last pass
        for (SimpleEntry<Player, Location> entry : dataStore.getPreviousLocations())
        {
            Player player = entry.getKey();
            Location location = entry.getValue();
            Chunk chunk = location.getChunk();
            World world = location.getWorld();

            try
            {
                // chunk must be loaded, player must not be close, and there must be
                // no other players in the chunk
                //TODO CHECK DISTANCE
                if (location.getChunk().isLoaded() && player.isOnline() && location.distanceSquared(player.getLocation()) > 150)
                {
                    boolean playerInChunk = false;
                    for (Entity entity : chunk.getEntities())
                    {
                        if (entity.getType() == EntityType.PLAYER)
                        {
                            playerInChunk = true;
                            break;
                        }
                    }

                    if (!playerInChunk)
                    {
                        // spawn random monster(s)
                        if (world.getEnvironment() == Environment.NORMAL)
                        {
                            int randomMonster = plugin.getRandom().nextInt(95);
                            EntityType monsterType;
                            int typeMultiplier = 1;

                            //Higher chance of more monsters when deeper down
                            if (Config.General_Monster_Rules__Monsters_Spawn_In_Light__More_Monsters_When_Y_Lower__Enable)
                                typeMultiplier = dynamicMonsterCount((int) location.getY(), Config.General_Monster_Rules__More_Monsters__Max_Y, Config.General_Monster_Rules__Monsters_Spawn_In_Light__More_Monsters_When_Y_Lower__Min_Multiplier, Config.General_Monster_Rules__Monsters_Spawn_In_Light__More_Monsters_When_Y_Lower__Max_Multiplier);

                            // decide which kind and how many
                            // monsters are more or less evenly distributed
                            if (randomMonster < 10)
                            {
                                monsterType = EntityType.SILVERFISH; /*10%*/
                            }
                            else if (randomMonster < 30)
                            {
                                monsterType = EntityType.SKELETON;   /*20%*/
                            }
                            else if (randomMonster < 50)
                            {
                                monsterType = EntityType.ZOMBIE;     /*20%*/
                            }
                            else if (randomMonster < 70)
                            {
                                monsterType = EntityType.CREEPER;    /*20%*/
                            }
                            else
                            {
                                monsterType = EntityType.SPIDER;     /*25%*/
                            }

                            int totalToSpawn = typeMultiplier;
                            for (int j = 0; j < totalToSpawn; j++)
                            {
                                world.spawnEntity(location, monsterType);
                            }
                        }
                        else if (world.getEnvironment() == Environment.NETHER)
                        {
                            int random = plugin.getRandom().nextInt();

                            if (random < 80)
                            {
                                world.spawnEntity(location, EntityType.PIG_ZOMBIE);
                            }
                            else
                            {
                                world.spawnEntity(location, EntityType.BLAZE);
                            }
                        }
                    }
                }
            } catch (IllegalArgumentException ignored)
            {
            } // in case the player is in a different world from the saved location
        }

        // plan for the next pass
        dataStore.getPreviousLocations().clear();
        for (Player player : plugin.getServer().getOnlinePlayers())
        {
            test_dynamicMonsterCount();
            /*TODO uncomment
            Location testLoc = player.getLocation();
            testLoc.setX(936);testLoc.setY(4);testLoc.setZ(-1344);
            test_single_verifyLocation(testLoc,player);*/
            //test_verifyLocation(player);
            //END TESTS
            Location verifiedLocation = null;
            //only if player hasn't got bypass and is in survival check location
            if (!player.hasPermission(PermissionNode.BYPASS.getNode()) && player.getGameMode() == GameMode.SURVIVAL)
                verifiedLocation = verifyLocation(player.getLocation());
            if (verifiedLocation != null)
                dataStore.getPreviousLocations().add(new SimpleEntry<Player, Location>(player, verifiedLocation));
        }
    }

    //TODO move this into a utility class

    /**
     * Tests if a a given location is elligible to be spawned on
     *
     * @return a valid Location or null if the location is invalid
     */
    private Location verifyLocation(Location location)
    {
        Block playerBlock = location.getBlock();
        World world = location.getWorld();
        Environment debug_env = world.getEnvironment();

        if (Config.Enabled_Worlds.contains(world.getName()))
        {
            // Only spawn monsters in normal world. End is crowded with endermen
            // and nether is too extreme anyway, add config later
            int lightLvl = location.getBlock().getLightFromSky();
            if (world.getEnvironment() == Environment.NORMAL
                    && (location.getY() < Config.General_Monster_Rules__Monsters_Spawn_In_Light__Max_Y && lightLvl < 3))
            {
                // the playerBlock should always be air, but if the player stands
                // on a slab he actually is in the slab, checking a few blocks under because player could have jumped etc..
                if (playerBlock.getType().equals(Material.AIR))
                {
                    for (int i = 0; i <= 3; i++)
                    {
                        playerBlock = location.getBlock().getRelative(BlockFace.DOWN, 1);
                        Material debug_Mat = playerBlock.getType();

                        if (playerBlock.getType().equals(Material.AIR))
                        {
                            location.subtract(0, 1, 0);
                            playerBlock = location.getBlock();
                            // the playerBlock is now the block where the monster
                            // should spawn on, next up: verify block
                        }
                        else
                        {
                            break;
                        }
                    }
                }
                // no spawning on steps, stairs and transparent blocks
                if (playerBlock.getType().name().endsWith("STEP") || playerBlock.getType().name().endsWith("STAIRS")
                        || playerBlock.getType().isTransparent() || !playerBlock.getType().isOccluding() || playerBlock.getType().equals(Material.AIR))
                {
                    Material debug_Mat = playerBlock.getType();
                    // don't spawn here
                    return null;
                }
                //Now test if there is enough space above the block for the monster to spawn, for simplicty we are going to check a 3x3x3 cube above the block. Any monster could potentially spawn in there.
                /* [(j=1|k=-1)] [(j=1|k=0)] [(j=1|k=1)]   i=height
                   [(j=0|k=-1)] [(0|0)]     [(j=0|k=1)]
                   [(j=-1|k=-1)][(j=-1|k=0)][(j=-1|k=1)] */
                boolean isClear = true; // if there is a single block which obstructs the spawn we will abort
                Location checkCube = location;
                cube_loop:
                for (int i = 0; i <= 2; i++)
                {
                    for (int j = -1; j <= 1; j++)
                    {
                        for (int k = -1; k <= 1; k++)
                        {
                            //Location.add() performs the action on the object! We have to subtract it again
                            //i is height, j/k can be swapped
                            checkCube.add(j, i, k);
                            Block test_blc = checkCube.getBlock();
                            Material debug_mat = test_blc.getType();
                            //if anything obstructs abort. Could possibly add more non obstructing Materials
                            if (!checkCube.getBlock().getType().equals(Material.AIR) & !checkCube.getBlock().getType().equals(Material.TORCH))
                            {
                                isClear = false;
                            }
                            checkCube.subtract(j, i, k);
                            break cube_loop;
                        }
                    }
                }
                //Once we get here the block should be eligible, just check if area above is clear
                if (isClear)
                {
                    return location;
                }
                else
                {
                    return null;
                }
            }

        }
        return null;
    }

    /**
     * Checks a few random locations if they are valid, when the locations before where invalid.
     */
    private void searchValidLocations(Location location)
    {
        //search for 3-5 locations before letting the spawn fail completely
        for (int i = 0; i < 5; i++)
        {
            int offsetX = plugin.getRandom().nextInt(11) + 5; //5-15 +/- boolean
            int offsetY = plugin.getRandom().nextInt(9) - 3; //-3 to +5
            int offsetZ = plugin.getRandom().nextInt(11) + 5; //5-15 +/- boolean
            int pmX = (plugin.getRandom().nextInt(4) - 1); //even number so same chance
            pmX = pmX == 0 ? -1 : pmX; //we want to either have -1 or 1, so we have am even number so its equal
            pmX = pmX == 3 ? 1 : pmX;
            int pmZ = plugin.getRandom().nextInt(3) - 1;
            pmZ = pmZ == 0 ? -1 : pmZ;
            pmZ = pmZ == 3 ? 1 : pmZ;
            Location randomLoc = location.add(offsetX * pmX, offsetY, offsetZ * pmZ);
            if (quickVerify(randomLoc))
            {
                Location verifiedLoc = verifyLocation(randomLoc);
                if (verifiedLoc != null)
                    break;
            }
        }
    }

    private boolean quickVerify(Location loc)
    {
        //quickly check if 2 blocks above this is clear
        Block oneAbove = loc.getBlock();
        Block twoAbove = oneAbove.getRelative(BlockFace.UP, 1);
        return oneAbove.getType().equals(Material.AIR) && twoAbove.getType().equals(Material.AIR);
    }

    /**
     * Returns the amount of monsters to spawn. ranges from 25% to 75% at the lowest value.
     *
     * @param yLevel   current y-level
     * @param maxY     maximum y for the feature
     * @param minCount minimum number of monsters
     * @param maxCount maximum number of monsters
     * @return the amount of monsters to spawn
     */
    private int dynamicMonsterCount(int yLevel, int maxY, int minCount, int maxCount)
    {
        if (maxY < 1 || yLevel < 1 || maxY < yLevel || minCount > maxCount)
            return minCount;
        /**The ratio that determines how deep a player is in a cave depending on the max worldheight**/
        float ratio = (((float) yLevel / (float) maxY - 1) * -1) * 100;
        /**The higher the difference is between these two the lower the chance is that a number will be choosen at the first try**/
        float countRatio = (float) minCount / (float) maxCount;
        float counter = 0;
        float percent = 0;
        for (int mobCount = minCount; mobCount <= maxCount; mobCount++)
        {
            counter += countRatio; //the further we progress the more likely it is to succeed
            percent = counter * ratio;
            int rdmPercent = plugin.getRandom().nextInt(101);
            if (rdmPercent < percent)
            {
                //success!
                return mobCount;
            }

        }
        return minCount;
    }

    /**
     * Tests the logic of the method by using random input
     */
    void test_dynamicMonsterCount()
    {
        for (int i = 0; i < 2000; i++)
        {
            int typeOfTest = 1;
            switch (typeOfTest)
            {
                //Completely Random
                case 0:
                    int dyn1 = plugin.getRandom().nextInt(255);
                    int dyn2 = plugin.getRandom().nextInt(255);
                    int countMin = plugin.getRandom().nextInt(10);
                    int countMax = plugin.getRandom().nextInt(15);
                    dynamicMonsterCount(dyn1, dyn2, countMin, countMax);
                    break;
                //Fixed Settings, height random
                case 1:
                    int rdmHeight = plugin.getRandom().nextInt(50);
                    dynamicMonsterCount(rdmHeight, 50, 1, 6);
                    break;
            }
        }
    }

    /**
     * This tests the method verifyLocation. It requires a player to be ingame. It will generate 20 random Locations
     * around the player, notify if a verified location has been found and will place a torch for visualisation at the given location.
     *
     * @param testPlayer player to test monsters with
     */
    private void test_verifyLocation(Player testPlayer)
    {
        for (int i = 0; i < 20; i++)
        {
            //Generate positive and negative randoms
            int offsetX = plugin.getRandom().nextInt(100) - 50;
            int offsetY = plugin.getRandom().nextInt(20) - 10;
            int offsetZ = plugin.getRandom().nextInt(100) - 50;
            Location randomLoc = testPlayer.getLocation().add(offsetX, offsetY, offsetZ);
            test_single_verifyLocation(randomLoc, testPlayer);
        }
    }

    private void test_single_verifyLocation(Location loc, Player testPlayer)
    {
        Location verifiedRandomLoc = verifyLocation(loc);
        if (verifiedRandomLoc != null)
        {
            plugin.sendMessage(testPlayer, "Location (" + ((int) verifiedRandomLoc.getX()) + "|" + ((int) verifiedRandomLoc.getY()) + "|" + ((int) verifiedRandomLoc.getZ()) + ") verified");
            verifiedRandomLoc.getBlock().setType(Material.DIAMOND_BLOCK);
        }
    }
}
