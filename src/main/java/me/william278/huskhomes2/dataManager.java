package me.william278.huskhomes2;

import me.william278.huskhomes2.Data.Database;
import me.william278.huskhomes2.Data.ErrorLogging.Errors;
import me.william278.huskhomes2.Data.SQL;
import me.william278.huskhomes2.Objects.Home;
import me.william278.huskhomes2.Objects.TeleportationPoint;
import me.william278.huskhomes2.Objects.Warp;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Level;

// This class handles the saving of data; whether that be through SQLite or mySQL
public class dataManager {

    // Syntax for creating tables; changes based on storage medium
    public static String createPlayerTable = "";

    public static String createLocationsTable = "";

    public static String createHomesTable = "CREATE TABLE IF NOT EXISTS " + HuskHomes.settings.getHomesDataTable() + " (" +
            "`player_id` integer NOT NULL," +
            "`location_id` integer NOT NULL," +
            "`name` varchar(16) NOT NULL," +
            "`description` varchar(255) NOT NULL," +
            "`public` boolean NOT NULL," +
            "PRIMARY KEY (`player_id`, `name`)," +
            "FOREIGN KEY (`player_id`) REFERENCES " + HuskHomes.settings.getPlayerDataTable() + " (`player_id`) ON DELETE CASCADE ON UPDATE NO ACTION," +
            "FOREIGN KEY (`location_id`) REFERENCES " + HuskHomes.settings.getLocationsDataTable() + " (`location_id`) ON DELETE CASCADE ON UPDATE NO ACTION" +
            ");";

    public static String createWarpsTable = "CREATE TABLE IF NOT EXISTS " + HuskHomes.settings.getWarpsDataTable() + " (" +
            "`location_id` integer NOT NULL," +
            "`name` varchar(16) NOT NULL UNIQUE," +
            "`description` varchar(255) NOT NULL," +
            "PRIMARY KEY (`location_id`)," +
            "FOREIGN KEY (`location_id`) REFERENCES " + HuskHomes.settings.getLocationsDataTable() + " (`location_id`) ON DELETE CASCADE ON UPDATE NO ACTION" +
            ");";

    private static final HuskHomes plugin = HuskHomes.getInstance();

    private static Database database;

    private static Connection getConnection() {
        initializeDatabase();
        return database.getSQLConnection();
    }

    private static void initializeDatabase() {
        database = new SQL(HuskHomes.getInstance());
        database.load();
    }

    // Return a player's ID  from their UUID
    private static Integer getPlayerId(UUID playerUUID) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs;

        try {
            conn = getConnection();
            ps = conn.prepareStatement("SELECT * FROM " + HuskHomes.settings.getPlayerDataTable() + " WHERE `user_uuid`=?;");
            ps.setString(1, playerUUID.toString());
            rs = ps.executeQuery();
            if (rs != null) {
                if (rs.next()) {
                    return rs.getInt("player_id");
                } else {
                    return null;
                }
            } else {
                return null; // If the player doesn't exist, playerID will be null
            }
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return null;
    }

    // Return a player's ID  from their username
    private static Integer getPlayerId(String playerUsername) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs;

        try {
            conn = getConnection();
            ps = conn.prepareStatement("SELECT * FROM " + HuskHomes.settings.getPlayerDataTable() + " WHERE `username`=?;");
            ps.setString(1, playerUsername);
            rs = ps.executeQuery();
            if (rs != null) {
                if (rs.next()) {
                    return rs.getInt("player_id");
                } else {
                    return null;
                }
            } else {
                return null; // If the player doesn't exist, playerID will be null
            }
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return null;
    }

    // Return an integer from the player table from a player ID
    public static Integer getPlayerInteger(Integer playerID, String column) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs;

        try {
            conn = getConnection();
            ps = conn.prepareStatement("SELECT * FROM " + HuskHomes.settings.getPlayerDataTable() + " WHERE `player_id`=?;");
            ps.setInt(1, playerID);
            rs = ps.executeQuery();
            if (rs != null) {
                if (rs.next()) {
                    return rs.getInt(column);
                } else {
                    return null;
                }
            } else {
                Bukkit.getLogger().severe("Result set for a player returned null; perhaps player ID was null");
                return null;
            }
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return null;
    }

    // Return an integer from the player table from a player name
    public static Integer getPlayerInteger(String playerName, String column) {
        return getPlayerInteger(getPlayerId(playerName), column);
    }

    // Return an integer from the player table from a player object
    public static Integer getPlayerInteger(Player p, String column) {
        return getPlayerInteger(getPlayerId(p.getUniqueId()), column);
    }

    // Return an integer from the player table from a player ID
    public static String getPlayerString(Integer playerID, String column) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs;

        try {
            conn = getConnection();
            ps = conn.prepareStatement("SELECT * FROM " + HuskHomes.settings.getPlayerDataTable() + " WHERE `player_id`=?;");
            ps.setInt(1, playerID);
            rs = ps.executeQuery();
            if (rs != null) {
                if (rs.next()) {
                    return rs.getString(column);
                } else {
                    return null;
                }
            } else {
                Bukkit.getLogger().severe("Result set for a player returned null; perhaps player ID was null");
                return null;
            }
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return null;
    }

    // Return an integer from the player table from a player ID
    public static Boolean getPlayerTeleporting(Player p) {
        Connection conn = null;
        PreparedStatement ps = null;
        Integer playerID = getPlayerId(p.getUniqueId());
        ResultSet rs;

        try {
            conn = getConnection();
            ps = conn.prepareStatement("SELECT * FROM " + HuskHomes.settings.getPlayerDataTable() + " WHERE `player_id`=?;");
            ps.setInt(1, playerID);
            rs = ps.executeQuery();
            if (rs != null) {
                if (rs.next()) {
                    return rs.getBoolean("is_teleporting");
                } else {
                    return null;
                }
            } else {
                Bukkit.getLogger().severe("Result set for a player returned null; perhaps player ID was null");
                return null;
            }
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return null;
    }

    // Return a player's UUID
    public static String getPlayerUUID(int playerID) {
        return getPlayerString(playerID, "user_uuid");
    }

    // Return a player's username
    public static String getPlayerUsername(int playerID) {
        return getPlayerString(playerID, "username");
    }

    // Return how many homes the player has set
    public static int getPlayerHomeCount(Player p) {
        ArrayList<Home> playerHomes = getPlayerHomes(p.getName());
        if (playerHomes != null) {
            return playerHomes.size();
        } else {
            return 0;
        }
    }

    // Increment the number of home slots a player has
    public static void incrementPlayerHomeSlots(Player p) {
        setPlayerHomeSlots(p, (getPlayerHomeSlots(p)+1));
    }

    // Set how many home slots a player has
    private static void setPlayerHomeSlots(Player p, int slots) {
        database.setPlayerHomeSlots(getPlayerId(p.getUniqueId()), slots);
    }

    // Return how many home slots a player has
    public static Integer getPlayerHomeSlots(Player p) {
        return getPlayerInteger(p, "home_slots");
    }

    // Return how many home slots a player has
    public static Long getPlayerRtpCooldown(Player p) {
        return (long) getPlayerInteger(p, "rtp_cooldown");
    }

    // Return a player's homes.
    public static ArrayList<Home> getPlayerHomes(String playerName) {
        Connection conn = null;
        PreparedStatement ps = null;
        Integer playerID = getPlayerId(playerName);
        ResultSet rs;

        try {
            conn = getConnection();
            ps = conn.prepareStatement("SELECT * FROM " + HuskHomes.settings.getHomesDataTable() + " WHERE `player_id`=?;");
            ps.setInt(1, playerID);
            rs = ps.executeQuery();
            if (rs != null) {
                ArrayList<Home> playerHomes = new ArrayList<>();
                while (rs.next()) {
                    int locationID = rs.getInt("location_id");
                    TeleportationPoint teleportationPoint = getTeleportationPoint(locationID);
                    if (teleportationPoint != null) {
                        playerHomes.add(new Home(teleportationPoint,
                                playerName, getPlayerUUID(playerID),
                                rs.getString("name"),
                                rs.getString("description"),
                                rs.getBoolean("public")));
                    }
                }
                return playerHomes;
            } else {
                Bukkit.getLogger().severe("Result set of a player's homes returned null; perhaps player ID was null");
                return null;
            }
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return null;
    }

    // Return all the public homes
    public static ArrayList<Home> getPublicHomes() {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs;

        try {
            conn = getConnection();
            ps = conn.prepareStatement("SELECT * FROM " + HuskHomes.settings.getHomesDataTable() + " WHERE `public`;");
            rs = ps.executeQuery();
            if (rs != null) {
                ArrayList<Home> publicHomes = new ArrayList<>();
                while (rs.next()) {
                    int playerID = rs.getInt("player_id");
                    int locationID = rs.getInt("location_id");
                    TeleportationPoint teleportationPoint = getTeleportationPoint(locationID);
                    if (teleportationPoint != null) {
                        publicHomes.add(new Home(teleportationPoint, getPlayerUsername(playerID), getPlayerUUID(playerID), rs.getString("name"), rs.getString("description"), true));
                    }
                }
                return publicHomes;
            } else {
                Bukkit.getLogger().severe("Result set of public returns returned null!");
                return null;
            }
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return null;
    }

    // Return an array of all the warps
    public static ArrayList<Warp> getWarps() {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs;

        try {
            conn = getConnection();
            ps = conn.prepareStatement("SELECT * FROM " + HuskHomes.settings.getWarpsDataTable() + ";");
            rs = ps.executeQuery();
            ArrayList<Warp> warps = new ArrayList<>();
            if (rs != null) {
                while (rs.next()) {
                    int locationID = rs.getInt("location_id");
                    TeleportationPoint teleportationPoint = getTeleportationPoint(locationID);
                    if (teleportationPoint != null) {
                        warps.add(new Warp(teleportationPoint,
                                rs.getString("name"),
                                rs.getString("description")));
                    }
                }
            } else {
                Bukkit.getLogger().severe("Result set of warps returned null!");
            }
            return warps;
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return null;
    }

    // Return a warp with a given name (warp names are unique)
    public static Warp getWarp(String name) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs;

        try {
            conn = getConnection();
            ps = conn.prepareStatement("SELECT * FROM " + HuskHomes.settings.getWarpsDataTable() + " WHERE `name`=?;");
            ps.setString(1, name);
            rs = ps.executeQuery();
            if (rs != null) {
                if (rs.next()) {
                    int locationID = rs.getInt("location_id");
                    TeleportationPoint teleportationPoint = getTeleportationPoint(locationID);
                    if (teleportationPoint != null) {
                        return new Warp(teleportationPoint,
                                rs.getString("name"),
                                rs.getString("description"));
                    } else {
                        Bukkit.getLogger().severe("An error occurred returning a warp from the table!");
                        return null;
                    }
                }
            } else {
                Bukkit.getLogger().severe("Warp returned null!");
                return null;
            }
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return null;
    }

    public static void deleteHomeTeleportLocation(int ownerID, String homeName) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs;

        try {
            conn = getConnection();
            ps = conn.prepareStatement("SELECT * FROM " + HuskHomes.settings.getHomesDataTable() + " WHERE `player_id`=? AND `name`=?;");
            ps.setInt(1, ownerID);
            ps.setString(2, homeName);
            rs = ps.executeQuery();
            if (rs != null) {
                if (rs.next()) {
                    int locationID = rs.getInt("location_id");
                    database.deleteTeleportationPoint(locationID);
                }
            } else {
                Bukkit.getLogger().severe("Failed to delete home teleportation location");
            }
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
    }

    public static void deleteWarpTeleportLocation(String warpName) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs;

        try {
            conn = getConnection();
            ps = conn.prepareStatement("SELECT * FROM " + HuskHomes.settings.getWarpsDataTable() + " WHERE `name`=?;");
            ps.setString(1, warpName);
            rs = ps.executeQuery();
            if (rs != null) {
                if (rs.next()) {
                    int locationID = rs.getInt("location_id");
                    database.deleteTeleportationPoint(locationID);
                }
            } else {
                Bukkit.getLogger().severe("Failed to delete warp teleportation location");
            }
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
    }

    public static void updateHomePrivacy(String ownerName, String homeName, boolean isPublic) {
        Integer playerID = getPlayerId(ownerName);
        database.setHomePrivacy(homeName, playerID, isPublic);
    }

    public static void updateHomeName(String ownerName, String homeName, String newName) {
        Integer playerID = getPlayerId(ownerName);
        database.setHomeName(homeName, playerID, newName);
    }

    public static void updateHomeDescription(String ownerName, String homeName, String newDescription) {
        Integer playerID = getPlayerId(ownerName);
        database.setHomeDescription(homeName, playerID, newDescription);
    }

    public static void updateHomeLocation(String ownerName, String homeName, Location newLocation) {
        Integer playerID = getPlayerId(ownerName);
        deleteHomeTeleportLocation(playerID, homeName);
        database.setHomeTeleportPoint(homeName, playerID, new TeleportationPoint(newLocation, HuskHomes.settings.getServerID()));
    }

    public static void updateWarpName(String warpName, String newName) {
        database.setWarpName(warpName, newName);
    }

    public static void updateWarpDescription(String warpName, String newDescription) {
        database.setWarpDescription(warpName, newDescription);
    }

    public static void updateWarpLocation(String warpName, Location newLocation) {
        deleteWarpTeleportLocation(warpName);
        database.setWarpTeleportPoint(warpName, new TeleportationPoint(newLocation, HuskHomes.settings.getServerID()));
    }

    // Return a home with a given owner username and home name
    public static Home getHome(String ownerUsername, String homeName) {
        Connection conn = null;
        PreparedStatement ps = null;
        Integer playerID = getPlayerId(ownerUsername);
        ResultSet rs;

        try {
            conn = getConnection();
            ps = conn.prepareStatement("SELECT * FROM " + HuskHomes.settings.getHomesDataTable() + " WHERE `player_id`=? AND `name`=?;");
            ps.setInt(1, playerID);
            ps.setString(2, homeName);
            rs = ps.executeQuery();
            if (rs != null) {
                if (rs.next()) {
                    int locationID = rs.getInt("location_id");
                    TeleportationPoint teleportationPoint = getTeleportationPoint(locationID);
                    if (teleportationPoint != null) {
                        return new Home(teleportationPoint,
                                ownerUsername,
                                getPlayerUUID(playerID),
                                rs.getString("name"),
                                rs.getString("description"),
                                rs.getBoolean("public"));
                    } else {
                        Bukkit.getLogger().severe("An error occurred retrieving a home from the table!");
                        return null;
                    }
                }
            } else {
                Bukkit.getLogger().severe("Home returned null; perhaps player ID was null?");
                return null;
            }
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return null;
    }

    public static Boolean warpExists(String warpName) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs;

        try {
            conn = getConnection();
            ps = conn.prepareStatement("SELECT * FROM " + HuskHomes.settings.getWarpsDataTable() + " WHERE `name`=?;");
            ps.setString(1, warpName);
            rs = ps.executeQuery();
            if (rs != null) {
                return rs.next();
            } else {
                Bukkit.getLogger().severe("An SQL exception occurred in retrieving if a warp exists from the table.");
                return false;
            }
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return null;
    }

    public static Boolean homeExists(Player owner, String homeName) {
        return homeExists(owner.getName(), homeName);
    }

    public static Boolean homeExists(String ownerName, String homeName) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs;
        Integer playerID = getPlayerId(ownerName);

        // Return false if the player is invalid
        if (playerID == null) {
            return false;
        }

        try {
            conn = getConnection();
            ps = conn.prepareStatement("SELECT * FROM " + HuskHomes.settings.getHomesDataTable() + " WHERE `player_id`=? AND `name`=?;");
            ps.setInt(1, playerID);
            ps.setString(2, homeName);
            rs = ps.executeQuery();
            if (rs != null) {
                return rs.next();
            } else {
                Bukkit.getLogger().severe("An SQL exception occurred in retrieving if a home exists from the table.");
                return false;
            }
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return null;
    }

    public static Boolean playerExists(Player player) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs;
        String playerUUID = player.getUniqueId().toString();

        try {
            conn = getConnection();
            ps = conn.prepareStatement("SELECT * FROM " + HuskHomes.settings.getPlayerDataTable() + " WHERE `user_uuid`=?;");
            ps.setString(1, playerUUID);
            rs = ps.executeQuery();
            if (rs != null) {
                return rs.next();
            } else {
                Bukkit.getLogger().severe("An SQL exception occurred in retrieving if a player exists from the table.");
                return false;
            }
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return null;
    }

    public static TeleportationPoint getTeleportationPoint(Integer locationID) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs;

        try {
            conn = getConnection();
            ps = conn.prepareStatement("SELECT * FROM " + HuskHomes.settings.getLocationsDataTable() + " WHERE `location_id`=?;");
            ps.setInt(1, locationID);
            rs = ps.executeQuery();
            if (rs.next()) {
                return new TeleportationPoint(rs.getString("world"),
                        rs.getDouble("x"), rs.getDouble("y"),
                        rs.getDouble("z"), rs.getFloat("yaw"),
                        rs.getFloat("pitch"), rs.getString("server"));
            } else {
                Bukkit.getLogger().severe("An SQL exception occurred in retrieving a teleportation location.");
                Bukkit.getLogger().severe("location ID: " + locationID.toString());
                Bukkit.getLogger().severe("SELECT * FROM " + HuskHomes.settings.getLocationsDataTable() + " WHERE `location_id`=" + locationID.toString() + ";");
                return null;
            }
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return null;
    }

    public static TeleportationPoint getPlayerDestination(Player p) {
        Integer locationID = getPlayerInteger(p, "dest_location_id");
        if (locationID != null) {
            return getTeleportationPoint(locationID);
        } else {
            return null;
        }
    }

    public static TeleportationPoint getPlayerLastPosition(Player p) {
        Integer locationID = getPlayerInteger(p, "last_location_id");
        if (locationID != null) {
            return getTeleportationPoint(locationID);
        } else {
            return null;
        }
    }

    // Update a player's last position location on SQL
    public static void setPlayerLastPosition(Player p, TeleportationPoint point) {
        Integer playerID = getPlayerId(p.getUniqueId());
        if (playerID != null) {
            deletePlayerLastPosition(p);
            database.setTeleportationLastPosition(playerID, point);
        } else {
            Bukkit.getLogger().warning("Failed to update player last position records for " + p.getName());
        }
    }

    // Update a player's destination location on SQL
    public static void setPlayerDestinationLocation(String playerName, TeleportationPoint point) {
        Integer playerID = getPlayerId(playerName);
        if (playerID != null) {
            deletePlayerDestination(playerName);
            database.setTeleportationDestination(playerID, point);
        } else {
            Bukkit.getLogger().warning("Failed to update player destination records for " + playerName);
        }
    }

    // Update a player's destination location on SQL
    public static void setPlayerDestinationLocation(Player p, TeleportationPoint point) {
        setPlayerDestinationLocation(p.getName(), point);
    }

    public static void setPlayerTeleporting(Player p, boolean value) {
        Integer playerID = getPlayerId(p.getUniqueId());
        if (playerID != null) {
            database.setPlayerTeleporting(playerID, value);
        } else {
            Bukkit.getLogger().warning("Failed to set a player as teleporting (" + p.getName() + ")");
        }
    }

    public static void clearPlayerDestination(String playerName) {
        Integer playerID = getPlayerId(playerName);
        if (playerID != null) {
            deletePlayerDestination(playerName);
            database.clearPlayerDestination(playerID);
        }
    }

    public static void updateRtpCooldown(Player p) {
        Integer playerID = getPlayerId(p.getUniqueId());
        if (playerID != null) {
            long currentTime = Instant.now().getEpochSecond();
            int newCooldownTime = (int) currentTime + (60 * HuskHomes.settings.getRtpCooldown());
            database.setRtpCooldown(playerID, newCooldownTime);
        }
    }

    public static void deletePlayerDestination(String playerName) {
        Integer destinationID = getPlayerInteger(playerName, "dest_location_id");
        if (destinationID != null) {
            database.deleteTeleportationPoint(destinationID);
            database.clearPlayerDestination(getPlayerId(playerName));
        }
    }

    public static void deletePlayerDestination(Player p) {
        deletePlayerDestination(p.getName());
    }

    public static void deletePlayerLastPosition(Player p) {
        Integer lastPositionID = getPlayerInteger(p, "last_location_id");
        if (lastPositionID != null) {
            database.deleteTeleportationPoint(lastPositionID);
            database.clearPlayerLastPosition(getPlayerId(p.getUniqueId()));
        }
    }

    public static void addWarp(Warp warp) {
        database.addWarp(warp);
    }

    public static void addHome(Home home, UUID playerUUID) {
        Integer playerID = getPlayerId(playerUUID);
        if (playerID != null) {
            database.addHome(home, playerID);
        } else {
            Bukkit.getLogger().warning("Failed to add a home for a player!");
        }
    }
    public static void addHome(Home home, Player p) {
        addHome(home, p.getUniqueId());
    }

    public static void deleteHome(String homeName, Player p) {
        Integer playerID = getPlayerId(p.getUniqueId());
        if (playerID != null) {
            deleteHomeTeleportLocation(playerID, homeName);
            database.deleteHome(homeName, playerID);
        } else {
            Bukkit.getLogger().warning("Player ID returned null when deleting a home");
        }
    }

    public static void deleteWarp(String warpName) {
        deleteWarpTeleportLocation(warpName);
        database.deleteWarp(warpName);
    }

    public static void setupStorage() {
        if (HuskHomes.settings.getStorageType().equalsIgnoreCase("mysql")) {
            createPlayerTable = "CREATE TABLE IF NOT EXISTS " + HuskHomes.settings.getPlayerDataTable() + " (" +
                    "`player_id` integer AUTO_INCREMENT," +
                    "`user_uuid` char(36) NOT NULL UNIQUE," +
                    "`username` varchar(16) NOT NULL," +
                    "`home_slots` integer NOT NULL," +
                    "`rtp_cooldown` integer NOT NULL," +
                    "`is_teleporting` boolean NOT NULL," +
                    "`dest_location_id` integer NULL," +
                    "`last_location_id` integer NULL," +

                    "PRIMARY KEY (`player_id`)," +
                    "FOREIGN KEY (`dest_location_id`) REFERENCES " + HuskHomes.settings.getLocationsDataTable() + " (`location_id`) ON DELETE SET NULL ON UPDATE NO ACTION," +
                    "FOREIGN KEY (`last_location_id`) REFERENCES " + HuskHomes.settings.getLocationsDataTable() + " (`location_id`) ON DELETE SET NULL ON UPDATE NO ACTION" +
                    ");";
            createLocationsTable = "CREATE TABLE IF NOT EXISTS " + HuskHomes.settings.getLocationsDataTable() + " (" +
                    "`location_id` integer AUTO_INCREMENT," +
                    "`server` text NOT NULL," +
                    "`world` text NOT NULL," +
                    "`x` double NOT NULL," +
                    "`y` double NOT NULL," +
                    "`z` double NOT NULL," +
                    "`yaw` float NOT NULL," +
                    "`pitch` float NOT NULL," +

                    "PRIMARY KEY (`location_id`)" +
                    ");";
        } else {
            createPlayerTable = "CREATE TABLE IF NOT EXISTS " + HuskHomes.settings.getPlayerDataTable() + " (" +
                    "`player_id` integer NOT NULL," +
                    "`user_uuid` char(36) NOT NULL UNIQUE," +
                    "`username` varchar(16) NOT NULL," +
                    "`home_slots` integer NOT NULL," +
                    "`rtp_cooldown` integer NOT NULL," +
                    "`is_teleporting` boolean NOT NULL," +
                    "`dest_location_id` integer NULL," +
                    "`last_location_id` integer NULL," +
                    "PRIMARY KEY (`player_id`)," +
                    "FOREIGN KEY (`dest_location_id`) REFERENCES " + HuskHomes.settings.getLocationsDataTable() + " (`location_id`) ON DELETE SET NULL ON UPDATE NO ACTION," +
                    "FOREIGN KEY (`last_location_id`) REFERENCES " + HuskHomes.settings.getLocationsDataTable() + " (`location_id`) ON DELETE SET NULL ON UPDATE NO ACTION" +
                    ");";
            createLocationsTable = "CREATE TABLE IF NOT EXISTS " + HuskHomes.settings.getLocationsDataTable() + " (" +
                    "`location_id` integer PRIMARY KEY," +
                    "`server` text NOT NULL," +
                    "`world` text NOT NULL," +
                    "`x` double NOT NULL," +
                    "`y` double NOT NULL," +
                    "`z` double NOT NULL," +
                    "`yaw` float NOT NULL," +
                    "`pitch` float NOT NULL" +
                    ");";
        }
        initializeDatabase();
    }

    public static void createPlayer(Player p) {
        database.addPlayer(p);
    }

    public static void createPlayer(UUID playerUUID, String playerUsername) {
        database.addPlayer(playerUUID, playerUsername);
    }

    public static void checkPlayerNameChange(Player p) {
        Integer playerID = getPlayerId(p.getUniqueId());
        if (!getPlayerUsername(playerID).equals(p.getName())) {
            database.updatePlayerUsername(playerID, p.getName());
        }
    }
}