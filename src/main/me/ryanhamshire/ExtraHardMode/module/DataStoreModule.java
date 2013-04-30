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
import org.bukkit.entity.Player;

import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    /*private Map <UUID, FallingBlockData> looseLogs = new HashMap<UUID, FallingBlockData>();*/

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
    /*public void addLog (Location loc, boolean damagePlayer)
    {
        looseLogs.put(loc, damagePlayer);
    }*/

    /**
     * Get a random Log from the List of availile ones and remove it
     * @return Block of Type LOG, or null if no blocks availible
     */
    /*public Block getRdmLog ()
    {
        if (looseLogs.size() > 0)
        {
            int rdmIndex = plugin.getRandom().nextInt(looseLogs.size());
            return looseLogs.get(rdmIndex);
        }
        else
            return null;
    }*/

    /**
     * Remove the given Log if it exists
     * @param log to remove
     */
    /*public void rmLog(Block log)
    {
        if (log == null)
            throw new IllegalArgumentException("Block can't be null!");
        looseLogs.remove(log);
    }*/

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
