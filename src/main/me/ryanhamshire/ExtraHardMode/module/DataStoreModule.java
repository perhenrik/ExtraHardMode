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

package me.ryanhamshire.ExtraHardMode.module;

import me.ryanhamshire.ExtraHardMode.ExtraHardMode;
import me.ryanhamshire.ExtraHardMode.service.EHMModule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.AbstractMap.SimpleEntry;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Manages miscellaneous data.
 */
public class DataStoreModule extends EHMModule
{

    /**
     * In-memory cache for player data
     */
    private final Map<String, PlayerData> playerNameToPlayerDataMap = new ConcurrentHashMap<String, PlayerData>();

    /**
     * List of previous locations.
     */
    private final List<SimpleEntry<Player, Location>> previousLocations = new CopyOnWriteArrayList<SimpleEntry<Player, Location>>();

    /**
     * TaskClasses that are running with their Ids
     */
    private Map<Class<?>, Integer/*taskid*/> currentTasks = new HashMap<Class<?>, Integer>();

    /**
     * List of FallingBlocks that need custom handling
     */
    private Map <UUID, FallingBlockData> fallingBlocksData = new HashMap<UUID, FallingBlockData>();

    /**
     * Constructor.
     *
     * @param plugin - Plugin instance.
     */
    public DataStoreModule(ExtraHardMode plugin)
    {
        super(plugin);
    }

    /**
     * Retrieves player data from memory
     *
     * @param playerName - Name of player.
     * @return PlayerData associated with it.
     */
    public PlayerData getPlayerData(String playerName)
    {
        // first, look in memory
        PlayerData playerData = this.playerNameToPlayerDataMap.get(playerName);

        // if not there, create a fresh entry
        if (playerData == null)
        {
            playerData = new PlayerData();
            this.playerNameToPlayerDataMap.put(playerName, playerData);
        }

        // try the hash map again. if it's STILL not there, we have a bug to fix
        return this.playerNameToPlayerDataMap.get(playerName);
    }

    /**
     * Get the list of previous locations of players.
     *
     * @return List of players to location entries.
     */
    public List<SimpleEntry<Player, Location>> getPreviousLocations()
    {
        return previousLocations;
    }


    /**
     * Add a Task with it's id to the list of running tasks
     * @param clazz Reference to the class of the task
     * @param id of the Task
     */
    public void addRunningTask (Class<?> clazz, int id)
    {
        if (clazz == null)
            throw new IllegalArgumentException("Class cannot be null");
        currentTasks.put(clazz, id);
    }

    /**
     * Remove the Task from the List of running Tasks
     * @param clazz reference of the task
     */
    public void rmRunningTask (Class <?> clazz)
    {
        currentTasks.remove(clazz);
    }

    /**
     * For Tasks that don't need multiple instances
     * @param clazz the Task to check
     * @return if there is an instance running already of the given task
     */
    public boolean isTaskRunning (Class<?> clazz)
    {
        if (clazz == null)
            throw new IllegalArgumentException("Class cannot be null");
        return currentTasks.containsKey(clazz);
    }

    /**
     * Get the id of the task
     * @param clazz to get the id for
     * @return the id of the running task or -1 if not running
     */
    public int getTaskId (Class<?> clazz)
    {
        return currentTasks.containsKey(clazz) ? currentTasks.get(clazz) : -1;
    }

    /**
     * Add a Block to be scheduled to fall later
     * @param loc where Block is at
     * @param damagePlayer damage Player when he is hit by this Block
     */
    public void addLog (Location loc, boolean damagePlayer)
    {
        FallingBlockData fbData = new F
        fallingBlocksData.
    }

    /**
     * If we are tracking this instance of a FallingBlock
     * @param id of the FallingLog
     * @return true if found
     */
    public boolean isMarkedForProcessing (UUID id)
    {
        return fallingBlocksData.containsKey(id);
    }

    /**
     * Does the given Location match the x and z coordinate given in the Location
     * Idea is that we are just iterested in the x and z
     * @param loc to check
     */
    public boolean isBlockFallingAtLoc (Location loc)
    {
        boolean contains = false;
        for (Map.Entry<UUID, FallingBlockData> data : fallingBlocksData.entrySet())
        {
            Location startLoc = data.getValue().getStartingLoc();
            if (loc.getBlockX() == startLoc.getBlockX() && loc.getBlockZ() == startLoc.getBlockX())
            {
                contains = true;
                break;
            }
        }
        return contains;
    }

    /**
     * Add a FallingBlock Reference to the List
     * @param id to add
     * @param loc starting point of the FallingBlock
     * @param damagePlayer should this Block damage the Player on hit
     */
    public void addFallLog (UUID id, Location loc, boolean damagePlayer)
    {
        FallingBlockData fbData = new FallingBlockData(loc);
        fbData.setDamagesPlayer(damagePlayer);
        fallingBlocksData.put(id, fbData);
    }

    /**
     * Remove the Block with the given UUID from the List of currently falling blocks
     * @param id to remove
     */
    public void rmFallLogById (UUID id)
    {
        fallingBlocksData.remove(id);
    }

    /**
     * Remove the Block(s) with the given Location from the List of falling blocks
     * @param loc to check for FallingBlocks
     */
    public void rmFallLogsByLoc (Location loc)
    {
        Iterator<Map.Entry<UUID,FallingBlockData>> iter = fallingBlocksData.entrySet().iterator();
        while (iter.hasNext())
        {
            Map.Entry<UUID, FallingBlockData> entry = iter.next();
            Location startLoc = entry.getValue().startingLoc;
            if (loc.getBlockX() == startLoc.getBlockX() && loc.getBlockZ() == startLoc.getBlockX())
                iter.remove();
        }
    }


    /**
     * Get a random Log from the List of availile ones and remove it
     * @return Block of Type LOG, or null if no blocks availible
     */
    public Block getRdmLog ()
    {
        if (looseLogs.size() > 0)
        {
            int rdmIndex = plugin.getRandom().nextInt(looseLogs.size());
            return looseLogs.get(rdmIndex);
        }
        else
            return null;
    }

    /**
     * Remove the given Log if it exists
     * @param log to remove
     */
    public void rmLog(Block log)
    {
        if (log == null)
            throw new IllegalArgumentException("Block can't be null!");
        looseLogs.remove(log);
    }


    /**
     * Data for FallingBlocks if they require special processing
     */
    private class FallingBlockData
    {
        /**
         * Location where the Block was in it's original Form
         */
        private final  Location startingLoc;
        /**
         * In which state is the FallingBlock
         */
        private FallState state;
        /**
         * Will the Block damage a Player if it lands on them?
         */
        private boolean damagesPlayer;

        /**
         * Constructor
         * @param startLoc
         */
        public FallingBlockData (Location startLoc)
        {
            this.startingLoc = startLoc;
        }

        private boolean isDamagesPlayer ()
        {
            return damagesPlayer;
        }

        private void setDamagesPlayer (boolean damagesPlayer)
        {
            this.damagesPlayer = damagesPlayer;
        }

        private FallState getState ()
        {
            return state;
        }

        private void setState (FallState state)
        {
            this.state = state;
        }

        private Location getStartingLoc ()
        {
            return startingLoc;
        }
    }

    /**
     * State of the FallingBlock
     */
    private enum FallState
    {
        /**
         * The Block has been scheduled to fall
         */
        SCHEDULED,
        /**
         * The Block is falling and hasnt landed yet
         */
        FALLING,
        /**
         * The Block has landed and processing is complete
         */
        LANDED
    }

    @Override
    public void starting()
    {
    }

    @Override
    public void closing()
    {
        playerNameToPlayerDataMap.clear();
        previousLocations.clear();
    }

    /**
     * Holds all of ExtraHardMode's player-tied data
     */
    public class PlayerData
    {
        /**
         * Last message sent.
         */
        public String lastMessageSent = "";
        /**
         * Last message timestamp.
         */
        public long lastMessageTimestamp = 0;
        /**
         * Cached weight
         */
        public float cachedWeightStatus = -1.0F; //player can't have negative invetory....
    }
}
