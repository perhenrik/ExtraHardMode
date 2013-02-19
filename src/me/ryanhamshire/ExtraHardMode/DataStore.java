/*
    ExtraHardMode Server Plugin for Minecraft
    Copyright (C) 2012 Ryan Hamshire

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

package me.ryanhamshire.ExtraHardMode;

import java.util.concurrent.ConcurrentHashMap;

/**
 * class which manages all ExtraHardMode data (except for config options)
 * 
 * TODO complete redo to clean up the main class and make things easier. Convert
 * this to a module.
 */
public class DataStore {
   /**
    * in-memory cache for player data
    */
   private ConcurrentHashMap<String, PlayerData> playerNameToPlayerDataMap = new ConcurrentHashMap<String, PlayerData>();

   // retrieves player data from memory
   synchronized public PlayerData getPlayerData(String playerName) {
      // first, look in memory
      PlayerData playerData = this.playerNameToPlayerDataMap.get(playerName);

      // if not there, create a fresh entry
      if(playerData == null) {
         playerData = new PlayerData();
         this.playerNameToPlayerDataMap.put(playerName, playerData);
      }

      // try the hash map again. if it's STILL not there, we have a bug to fix
      return this.playerNameToPlayerDataMap.get(playerName);
   }
}
