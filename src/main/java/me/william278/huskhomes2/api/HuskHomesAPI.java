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
import java.util.Optional;
import java.util.logging.Level;

/**
 * The HuskHomes API
 * Only use methods/events found within the API package
 */
public class HuskHomesAPI {
    private static HuskHomesAPI instance;
    private final HuskHomes huskHomes;

    private HuskHomesAPI() {
        huskHomes = HuskHomes.getInstance();
    }

    public static HuskHomesAPI getInstance() {
        if (instance == null) {
            instance = new HuskHomesAPI();
        }
        return instance;
    }

    /**
     * Returns a Home set by a player, or NULL if it does not exist
     *
     * @param player   A player
     * @param homeName Name of the player's home you wish to retrieve
     * @return the player's Home, or NULL if it does not exist
     * @see Home
     */
    public Home getHome(Player player, String homeName) {
        return getHome(player.getName(), homeName);
    }

    /**
     * Returns a Home set by a player, or NULL if it does not exist
     *
     * @param ownerUsername Username of the player
     * @param homeName      Name of the player's home you wish to retrieve
     * @return the player's Home, or NULL if it does not exist
     * @see Home
     */
    public Home getHome(String ownerUsername, String homeName) {
        try (Connection connection = HuskHomes.getConnection()) {
            return DataManager.getHome(ownerUsername, homeName, connection);
        } catch (SQLException e) {
            huskHomes.getLogger().log(Level.WARNING, "An SQL exception occurred retrieving data for API access", e);
            return null;
        }
    }

    /**
     * Returns a Warp set on the server, or NULL if it does not exist
     *
     * @param warpName Name of the warp
     * @return a Warp, or NULL if it does not exist
     * @see Warp
     */
    public Warp getWarp(String warpName) {
        try (Connection connection = HuskHomes.getConnection()) {
            return DataManager.getWarp(warpName, connection);
        } catch (SQLException e) {
            huskHomes.getLogger().log(Level.WARNING, "An SQL exception occurred retrieving data for API access", e);
            return null;
        }
    }

    /**
     * Returns an ArrayList of homes set by a Player
     *
     * @param player A Player
     * @return an ArrayList of Homes
     * @see Home
     */
    public List<Home> getHomes(Player player) {
        return getHomes(player.getName());
    }

    /**
     * Returns an ArrayList of homes set by a player
     *
     * @param ownerUsername Username of the player who's homes you want to get
     * @return an ArrayList of Homes
     * @see Home
     */
    public List<Home> getHomes(String ownerUsername) {
        try (Connection connection = HuskHomes.getConnection()) {
            return DataManager.getPlayerHomes(ownerUsername, connection);
        } catch (SQLException e) {
            huskHomes.getLogger().log(Level.WARNING, "An SQL exception occurred retrieving data for API access", e);
            return null;
        }
    }

    /**
     * Returns how many Homes a given Player has set
     *
     * @param player the Player being checked
     * @return an integer of how many homes a player has set
     * @see Home
     */
    public int getHomeCount(Player player) {
        return getHomes(player).size();
    }

    /**
     * Returns the maximum number of homes a player can set
     *
     * @param player the Player being checked
     * @return the maximum homes a player can set
     */
    public int getMaxSetHomes(Player player) {
        return Home.getSetHomeLimit(player);
    }

    /**
     * Returns the number of homes a player can set for free
     *
     * @param player the Player being checked
     * @return the number of free homes a player can set
     */
    public int getFreeSetHomes(Player player) {
        return Home.getFreeHomes(player);
    }

    /**
     * Returns an ArrayList of all the public homes set
     *
     * @return an ArrayList of Homes
     * @see Home
     */
    public List<Home> getPublicHomes() {
        try (Connection connection = HuskHomes.getConnection()) {
            return DataManager.getPublicHomes(connection);
        } catch (SQLException e) {
            huskHomes.getLogger().log(Level.WARNING, "An SQL exception occurred retrieving data for API access", e);
            return null;
        }
    }

    /**
     * Returns how many public Homes there are
     *
     * @return an integer of how many public homes there are
     * @see Home
     */
    public int getPublicHomeCount() {
        return getPublicHomes().size();
    }

    /**
     * Returns an ArrayList of all the warps set
     *
     * @return an ArrayList of Warps
     * @see Warp
     */
    public List<Warp> getWarps() {
        try (Connection connection = HuskHomes.getConnection()) {
            return DataManager.getWarps(connection);
        } catch (SQLException e) {
            huskHomes.getLogger().log(Level.WARNING, "An SQL exception occurred retrieving data for API access", e);
            return null;
        }
    }

    /**
     * Returns how many Warps have been set
     *
     * @return an integer of how many warps have been set
     * @see Warp
     */
    public int getWarpCount() {
        return getWarps().size();
    }

    /**
     * Returns an {@link Optional} that will contain the spawn position set on the server, as a {@link TeleportationPoint}.
     * <p>
     * <ul>
     *      <li>To check if the spawn position is present: {@code #getServerSpawnPosition().isPresent()}</li>
     *      <li>To get the spawn position, if it is present: {@code #getServerSpawnPosition().get()}</li>
     *      <li>To get a runnable, if it is present: {@code #getServerSpawnPosition().get()}</li>
     * </ul>
     *
     * @return an {@link Optional} that will contain the server's {@link TeleportationPoint} spawn position if it has been set (empty otherwise)
     */
    public Optional<TeleportationPoint> getServerSpawnPosition() {
        return TeleportManager.getSpawnLocation();
    }

    /**
     * Returns the current spawn position as a TeleportationPoint
     *
     * @return the spawn position as a {@link TeleportationPoint}; {@code null otherwise}
     * @deprecated Use {@link #getServerSpawnPosition()} that returns an {@code Optional<TeleportationPoint>} instead
     */
    @Deprecated
    public TeleportationPoint getSpawnPosition() {
        return TeleportManager.getSpawnLocation().isPresent() ? TeleportManager.getSpawnLocation().get() : null;
    }

    /**
     * Updates a player's last position on the server
     *
     * @param player The {@link Player} who's last position you wish to update
     * @param point  the {@link TeleportationPoint} to set as their new last position
     */
    public void updatePlayerLastPosition(Player player, TeleportationPoint point) {
        Bukkit.getScheduler().runTaskAsynchronously(huskHomes, () -> {
            try (Connection connection = HuskHomes.getConnection()) {
                DataManager.setPlayerLastPosition(player, point, connection);
            } catch (SQLException e) {
                huskHomes.getLogger().severe("An SQL exception occurred updating a player's last position via the API.");
            }
        });
    }

    /**
     * Teleport a {@link Player} to a specific {@link TeleportationPoint}
     *
     * @param player The {@link Player} to be teleported
     * @param point  The target {@link TeleportationPoint}
     * @param timed  Whether the teleport should be timed; have a warmup countdown
     */
    public void teleportPlayer(Player player, TeleportationPoint point, boolean timed) {
        Bukkit.getScheduler().runTask(huskHomes, () -> {
            if (timed) {
                TeleportManager.queueTimedTeleport(player, point);
            } else {
                TeleportManager.teleportPlayer(player, point);
            }
        });
    }

    /**
     * Teleport a {@link Player} to a specific {@link TeleportationPoint}
     *
     * @param player           The {@link Player} to be teleported
     * @param targetPlayerName The target {@link Player}'s username
     * @param timed            Whether the teleport should be timed; have a warmup countdown
     */
    public void teleportPlayer(Player player, String targetPlayerName, boolean timed) {
        Bukkit.getScheduler().runTask(huskHomes, () -> {
            if (timed) {
                TeleportManager.queueTimedTeleport(player, targetPlayerName);
            } else {
                TeleportManager.teleportPlayer(player, targetPlayerName);
            }
        });
    }

}