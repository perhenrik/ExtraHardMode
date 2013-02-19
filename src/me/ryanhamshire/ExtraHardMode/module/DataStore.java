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

package me.ryanhamshire.ExtraHardMode.module;

import java.util.List;
import java.util.Map;
import java.util.AbstractMap.SimpleEntry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import me.ryanhamshire.ExtraHardMode.ExtraHardMode;
import me.ryanhamshire.ExtraHardMode.service.EHMModule;

/**
 * Manages miscellaneous data.
 */
public class DataStore extends EHMModule {

   /**
    * in-memory cache for player data
    */
   private final Map<String, PlayerData> playerNameToPlayerDataMap = new ConcurrentHashMap<String, PlayerData>();

   private final List<SimpleEntry<Player, Location>> previousLocations = new CopyOnWriteArrayList<>();

   public DataStore(ExtraHardMode plugin) {
      super(plugin);
   }

   // retrieves player data from memory
   public PlayerData getPlayerData(String playerName) {
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

   public List<SimpleEntry<Player, Location>> getPreviousLocations() {
      return previousLocations;
   }

   @Override
   public void starting() {
   }

   @Override
   public void closing() {
      playerNameToPlayerDataMap.clear();
   }

   /**
    * holds all of ExtraHardMode's player-tied data
    */
   public class PlayerData {
      public String lastMessageSent = "";
      public long lastMessageTimestamp = 0;
      public Boolean cachedWeightStatus = null;
   }
}
