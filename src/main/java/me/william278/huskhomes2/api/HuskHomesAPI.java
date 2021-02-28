package me.william278.huskhomes2.api;

import me.william278.huskhomes2.objects.Home;
import me.william278.huskhomes2.objects.TeleportationPoint;
import me.william278.huskhomes2.objects.Warp;
import me.william278.huskhomes2.dataManager;
import me.william278.huskhomes2.teleportManager;
import org.bukkit.entity.Player;

import java.util.ArrayList;

/**
 * The HuskHomes API
 * Only use methods/events found within the API package
 */
public class HuskHomesAPI {

    public HuskHomesAPI() { }

    /**
     Returns a Home set by a player, or NULL if it does not exist
     @param player A player
     @param homeName Name of the player's home you wish to retrieve
     @return the player's Home, or NULL if it does not exist
     @see Home
     */
    public Home getHome(Player player, String homeName) {
        return getHome(player.getName(), homeName);
    }

    /**
     Returns a Home set by a player, or NULL if it does not exist
     @param ownerUsername Username of the player
     @param homeName Name of the player's home you wish to retrieve
     @return the player's Home, or NULL if it does not exist
     @see Home
     */
    public Home getHome(String ownerUsername, String homeName)  {
        return dataManager.getHome(ownerUsername, homeName);
    }

    /**
     Returns a Warp set on the server, or NULL if it does not exist
     @param warpName Name of the warp
     @return a Warp, or NULL if it does not exist
     @see Warp
     */
    public Warp getWarp(String warpName)  {
        return dataManager.getWarp(warpName);
    }

    /**
     Returns an ArrayList of homes set by a Player
     @param player A Player
     @return an ArrayList of Homes
     @see Home
     */
    public ArrayList<Home> getHomes(Player player) {
        return dataManager.getPlayerHomes(player.getName());
    }

    /**
     Returns an ArrayList of homes set by a player
     @param ownerUsername Username of the player who's homes you want to get
     @return an ArrayList of Homes
     @see Home
     */
    public ArrayList<Home> getHomes(String ownerUsername) {
        return dataManager.getPlayerHomes(ownerUsername);
    }

    /**
     * Returns how many Homes a given Player has set
     * @param player the Player being checked
     * @return an integer of how many homes a player has set
     * @see Home
     */
    public int getHomeCount(Player player) {
        return getHomes(player).size();
    }

    /**
     Returns an ArrayList of all the public homes set
     @return an ArrayList of Homes
     @see Home
     */
    public ArrayList<Home> getPublicHomes() {
        return dataManager.getPublicHomes();
    }

    /**
     * Returns how many public Homes there are
     * @return an integer of how many public homes there are
     * @see Home
     */
    public int getPublicHomeCount() {
        return getPublicHomes().size();
    }

    /**
     Returns an ArrayList of all the warps set
     @return an ArrayList of Warps
     @see Warp
     */
    public ArrayList<Warp> getWarps() {
        return dataManager.getWarps();
    }

    /**
     * Returns how many Warps have been set
     * @return an integer of how many warps have been set
     * @see Warp
     */
    public int getWarpCount() {
        return getWarps().size();
    }

    /**
     * Returns the current spawn position as a TeleportationPoint
     * @return the spawn position as a TeleportationPoint
     */
    public TeleportationPoint getSpawnPosition() {
        return teleportManager.spawnLocation;
    }
}
