package me.william278.huskhomes2.api;

import me.william278.huskhomes2.HuskHomes;
import me.william278.huskhomes2.data.DataManager;
import me.william278.huskhomes2.teleport.TeleportManager;
import me.william278.huskhomes2.teleport.points.Home;
import me.william278.huskhomes2.teleport.points.TeleportationPoint;
import me.william278.huskhomes2.teleport.points.Warp;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;

/**
 * The HuskHomes API
 * Only use methods/events found within the API package
 */
public class HuskHomesAPI {

    private static final HuskHomes plugin = HuskHomes.getInstance();
    private Connection getConnection() {
        return HuskHomes.getConnection();
    }

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
    public Home getHome(String ownerUsername, String homeName) {
        try {
            return DataManager.getHome(ownerUsername, homeName, getConnection());
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "An SQL exception occurred retrieving data for API access", e);
            return null;
        }
    }

    /**
     Returns a Warp set on the server, or NULL if it does not exist
     @param warpName Name of the warp
     @return a Warp, or NULL if it does not exist
     @see Warp
     */
    public Warp getWarp(String warpName)  {
        try {
            return DataManager.getWarp(warpName, getConnection());
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "An SQL exception occurred retrieving data for API access", e);
            return null;
        }
    }

    /**
     Returns an ArrayList of homes set by a Player
     @param player A Player
     @return an ArrayList of Homes
     @see Home
     */
    public List<Home> getHomes(Player player) {
        return getHomes(player.getName());
    }

    /**
     Returns an ArrayList of homes set by a player
     @param ownerUsername Username of the player who's homes you want to get
     @return an ArrayList of Homes
     @see Home
     */
    public List<Home> getHomes(String ownerUsername) {
        try {
            return DataManager.getPlayerHomes(ownerUsername, getConnection());
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "An SQL exception occurred retrieving data for API access", e);
            return null;
        }
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
     * Returns the maximum number of homes a player can set
     * @param player the Player being checked
     * @return the maximum homes a player can set
     */
    public int getMaxSethomes(Player player) {
        return Home.getSetHomeLimit(player);
    }

    /**
     * Returns the number of homes a player can set for free
     * @param player the Player being checked
     * @return the number of free homes a player can set
     */
    public int getFreeSethomes(Player player) {
        return Home.getFreeHomes(player);
    }

    /**
     Returns an ArrayList of all the public homes set
     @return an ArrayList of Homes
     @see Home
     */
    public List<Home> getPublicHomes() {
        try {
            return DataManager.getPublicHomes(getConnection());
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "An SQL exception occurred retrieving data for API access", e);
            return null;
        }
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
    public List<Warp> getWarps() {
        try {
            return DataManager.getWarps(getConnection());
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "An SQL exception occurred retrieving data for API access", e);
            return null;
        }
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
        return TeleportManager.getSpawnLocation();
    }

    /**
     * Teleport a player to a specific TeleportationPoint
     * @param player The player to be teleported
     * @param point The target teleportationPoint
     * @param timed Whether or not to do a warmup countdown
     * @see TeleportationPoint
     */
    public void teleportPlayer(Player player, TeleportationPoint point, boolean timed) {
        Connection connection = HuskHomes.getConnection();
        Bukkit.getScheduler().runTask(plugin, () -> {
            try {
                if (timed) {
                    TeleportManager.queueTimedTeleport(player, point, connection);
                } else {
                    TeleportManager.teleportPlayer(player, point, connection);
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.WARNING, "An SQLException occurred timed-teleporting a player via API");
            }
        });
    }

    /**
     * Teleport a player to a specific TeleportationPoint
     * @param player The player to be teleported
     * @param targetPlayerName The target player's name
     * @param timed Whether or not to do a warmup countdown
     * @see TeleportationPoint
     */
    public void teleportPlayer(Player player, String targetPlayerName, boolean timed) {
        Connection connection = HuskHomes.getConnection();
        Bukkit.getScheduler().runTask(plugin, () -> {
            try {
                if (timed) {
                    TeleportManager.queueTimedTeleport(player, targetPlayerName, connection);
                } else {
                    TeleportManager.teleportPlayer(player, targetPlayerName, connection);
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.WARNING, "An SQLException occurred teleporting a player via API");
            }
        });
    }

}