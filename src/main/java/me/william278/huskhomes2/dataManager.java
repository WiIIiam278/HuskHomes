package me.william278.huskhomes2;

import me.william278.huskhomes2.Data.SQLite.Database;
import me.william278.huskhomes2.Data.SQLite.Errors.Errors;
import me.william278.huskhomes2.Data.SQLite.SQLite;
import me.william278.huskhomes2.Objects.Home;
import me.william278.huskhomes2.Objects.TeleportationPoint;
import me.william278.huskhomes2.Objects.Warp;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Level;

// This class handles the saving of data; whether that be through SQLite or SQL
public class dataManager {

    public static String createPlayerTable = "CREATE TABLE IF NOT EXISTS huskhomes_player_data (" +
            "`player_id` integer NOT NULL," +
            "`user_uuid` char(36) NOT NULL UNIQUE," +
            "`username` varchar(16) NOT NULL," +
            "`home_slots` integer NOT NULL," +
            "`rtp_cooldown` integer NOT NULL," +
            "`is_teleporting` boolean NOT NULL," +
            "`dest_location_id` integer NULL," +
            "`last_location_id` integer NULL," +
            "PRIMARY KEY (`player_id`)," +
            "FOREIGN KEY (`dest_location_id`) REFERENCES huskhomes_location_data (`location_id`) ON DELETE CASCADE ON UPDATE NO ACTION," +
            "FOREIGN KEY (`last_location_id`) REFERENCES huskhomes_location_data (`location_id`) ON DELETE CASCADE ON UPDATE NO ACTION" +
            ");";

    public static String createLocationsTable = "CREATE TABLE IF NOT EXISTS huskhomes_location_data (" +
            "`location_id` integer PRIMARY KEY," +
            "`server` text NOT NULL," +
            "`world` text NOT NULL," +
            "`x` double NOT NULL," +
            "`y` double NOT NULL," +
            "`z` double NOT NULL," +
            "`yaw` float NOT NULL," +
            "`pitch` float NOT NULL" +
            ");";

    public static String createHomesTable = "CREATE TABLE IF NOT EXISTS huskhomes_home_data (" +
            "`player_id` integer NOT NULL," +
            "`location_id` integer NOT NULL," +
            "`name` double NOT NULL," +
            "`description` varchar NOT NULL," +
            "`public` boolean NOT NULL," +
            "PRIMARY KEY (`player_id`, `location_id`)," +
            "FOREIGN KEY (`player_id`) REFERENCES huskhomes_player_data (`player_id`) ON DELETE CASCADE ON UPDATE NO ACTION," +
            "FOREIGN KEY (`location_id`) REFERENCES huskhomes_location_data (`location_id`) ON DELETE CASCADE ON UPDATE NO ACTION" +
            ");";

    public static String createWarpsTable = "CREATE TABLE IF NOT EXISTS huskhomes_warp_data (" +
            "`location_id` integer NOT NULL," +
            "`name` varchar NOT NULL UNIQUE," +
            "`description` varchar NOT NULL," +

            "PRIMARY KEY (`location_id`)," +
            "FOREIGN KEY (`location_id`) REFERENCES huskhomes_location_data (`location_id`) ON DELETE CASCADE ON UPDATE NO ACTION" +
            ");";

    private static Main plugin = Main.getInstance();

    private static Database sqliteDatabase;

    private static Connection getConnection() {
        if (Main.settings.getStorageType().equalsIgnoreCase("mysql")) {
            return null; //TODO: Add mySQL support
        } else {
            initializeSQLite();
            return sqliteDatabase.getSQLConnection();
        }
    }

    private static void initializeSQLite() {
        sqliteDatabase = new SQLite(Main.getInstance());
        sqliteDatabase.load();
    }

    private static void initializeMySQL() {

    }

    // Return a player's ID  from their username
    private static Integer getPlayerId(String playerUsername) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs;

        try {
            conn = getConnection();
            ps = conn.prepareStatement("SELECT * FROM huskhomes_player_data WHERE `username`=?;");
            ps.setString(1, playerUsername);
            rs = ps.executeQuery();
            if (rs != null) {
                if (rs.next()) {
                    return rs.getInt("player_id");
                } else {
                    return null;
                }
            } else {
                Bukkit.getLogger().severe("Result set for a player returned null while fetching player ID");
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
    public static Integer getPlayerInteger(Integer playerID, String column) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs;

        try {
            conn = getConnection();
            ps = conn.prepareStatement("SELECT * FROM huskhomes_player_data WHERE `player_id`=?;");
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
        return getPlayerInteger(getPlayerId(p.getName()), column);
    }

    // Return an integer from the player table from a player ID
    public static String getPlayerString(Integer playerID, String column) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs;

        try {
            conn = getConnection();
            ps = conn.prepareStatement("SELECT * FROM huskhomes_player_data WHERE `player_id`=?;");
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

    // Return an integer from the player table from a player name
    public static String getPlayerString(String playerName, String column) {
        return getPlayerString(getPlayerId(playerName), column);
    }

    // Return an integer from the player table from a player object
    public static String getPlayerString(Player p, String column) {
        return getPlayerString(getPlayerId(p.getName()), column);
    }

    // Return an integer from the player table from a player ID
    public static Boolean getPlayerTeleporting(Player p) {
        Connection conn = null;
        PreparedStatement ps = null;
        Integer playerID = getPlayerId(p.getName());
        ResultSet rs;

        try {
            conn = getConnection();
            ps = conn.prepareStatement("SELECT * FROM huskhomes_player_data WHERE `player_id`=?;");
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

    // Return how many home slots a player has
    public static Integer getPlayerHomeSlots(Player p) {
        return getPlayerInteger(p, "home_slots");
    }

    // Return how many home slots a player has
    public static Integer getPlayerRtpCooldown(Player p) {
        return getPlayerInteger(p, "rtp_cooldown");
    }

    // Return a player's homes.
    public static ArrayList<Home> getPlayerHomes(String playerName) {
        Connection conn = null;
        PreparedStatement ps = null;
        Integer playerID = getPlayerId(playerName);
        ResultSet rs;

        try {
            conn = getConnection();
            ps = conn.prepareStatement("SELECT * FROM huskhomes_home_data WHERE `player_id`=?;");
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
            ps = conn.prepareStatement("SELECT * FROM huskhomes_home_data WHERE `public`;");
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
            ps = conn.prepareStatement("SELECT * FROM huskhomes_warp_data;");
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
            ps = conn.prepareStatement("SELECT * FROM huskhomes_warp_data WHERE `name`=?;");
            ps.setString(1, name);
            rs = ps.executeQuery();
            if (rs != null) {
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

    // Return a home with a given owner username and home name
    public static Home getHome(String ownerUsername, String homeName) {
        Connection conn = null;
        PreparedStatement ps = null;
        Integer playerID = getPlayerId(ownerUsername);
        ResultSet rs;

        try {
            conn = getConnection();
            ps = conn.prepareStatement("SELECT * FROM huskhomes_home_data WHERE `player_id`=? AND `name`=?;");
            ps.setInt(1, playerID);
            ps.setString(2, homeName);
            rs = ps.executeQuery();
            if (rs != null) {
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
            ps = conn.prepareStatement("SELECT * FROM huskhomes_warp_data WHERE `name`=?;");
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
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs;
        Integer playerID = getPlayerId(owner.getName());

        try {
            conn = getConnection();
            ps = conn.prepareStatement("SELECT * FROM huskhomes_home_data WHERE `player_id`=? AND `name`=?;");
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
            ps = conn.prepareStatement("SELECT * FROM huskhomes_player_data WHERE `user_uuid`=?;");
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
            ps = conn.prepareStatement("SELECT * FROM huskhomes_location_data WHERE `location_id`=?;");
            ps.setInt(1, locationID);
            rs = ps.executeQuery();
            if (rs.next()) {
                return new TeleportationPoint(rs.getString("world"),
                        rs.getDouble("x"), rs.getDouble("y"),
                        rs.getDouble("z"), rs.getFloat("yaw"),
                        rs.getFloat("pitch"), rs.getString("server"));
            } else {
                Bukkit.getLogger().severe("An SQL exception occurred in retrieving a teleportation location.");
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
        Integer playerID = getPlayerId(p.getName());
        if (playerID != null) {
            deletePlayerLastPosition(p);
            if (Main.settings.getStorageType().equalsIgnoreCase("mySQL")) {

            } else {
                sqliteDatabase.setTeleportationLastOrDest(playerID, point, "last");
            }
        } else {
            Bukkit.getLogger().warning("Failed to update player last position records for " + p.getName());
        }
    }

    // Update a player's destination location on SQL
    public static void setPlayerDestinationLocation(String playerName, TeleportationPoint point) {
        Integer playerID = getPlayerId(playerName);
        if (playerID != null) {
            deletePlayerDestination(playerName);
            if (Main.settings.getStorageType().equalsIgnoreCase("mySQL")) {

            } else {
                sqliteDatabase.setTeleportationLastOrDest(playerID, point, "dest");
            }
        } else {
            Bukkit.getLogger().warning("Failed to update player destination records for " + playerName);
        }
    }

    // Update a player's destination location on SQL
    public static void setPlayerDestinationLocation(Player p, TeleportationPoint point) {
        Integer playerID = getPlayerId(p.getName());
        if (playerID != null) {
            deletePlayerDestination(p);
            if (Main.settings.getStorageType().equalsIgnoreCase("mySQL")) {

            } else {
                sqliteDatabase.setTeleportationLastOrDest(playerID, point, "dest");
            }
        } else {
            Bukkit.getLogger().warning("Failed to update player destination records for " + p.getName());
        }
    }

    public static void setPlayerTeleporting(Player p, boolean value) {
        Integer playerID = getPlayerId(p.getName());
        if (playerID != null) {
            deletePlayerDestination(p);
            if (Main.settings.getStorageType().equalsIgnoreCase("mySQL")) {

            } else {
                sqliteDatabase.setPlayerTeleporting(playerID, value);
            }
        } else {
            Bukkit.getLogger().warning("Failed to update player destination records for " + p.getName());
        }
    }

    public static void deletePlayerDestination(String playerName) {
        Integer destinationID = getPlayerInteger(playerName, "dest_location_id");
        if (destinationID != null) {
            if (Main.settings.getStorageType().equalsIgnoreCase("mySQL")) {

            } else {
                sqliteDatabase.deleteTeleportationPoint(destinationID);
            }
        }
    }

    public static void deletePlayerDestination(Player p) {
        Integer destinationID = getPlayerInteger(p, "dest_location_id");
        if (destinationID != null) {
            if (Main.settings.getStorageType().equalsIgnoreCase("mySQL")) {

            } else {
                sqliteDatabase.deleteTeleportationPoint(destinationID);
            }
        }
    }

    public static void deletePlayerLastPosition(Player p) {
        Integer lastPositionID = getPlayerInteger(p, "last_location_id");
        if (lastPositionID != null) {
            if (Main.settings.getStorageType().equalsIgnoreCase("mySQL")) {

            } else {
                sqliteDatabase.deleteTeleportationPoint(lastPositionID);
            }
        }
    }

    public static void addWarp(Warp warp) {
        if (Main.settings.getStorageType().equalsIgnoreCase("mySQL")) {

        } else {
            sqliteDatabase.addWarp(warp);
        }
    }

    public static void addHome(Home home, Player p) {
        Integer playerID = getPlayerId(p.getName());
        if (playerID != null) {
            if (Main.settings.getStorageType().equalsIgnoreCase("mySQL")) {

            } else {
                sqliteDatabase.addHome(home, playerID);
            }
        } else {
            Bukkit.getLogger().warning("Failed to add a home for " + p.getName());
        }
    }

    public static void deleteHome(String homeName, Player p) {
        if (Main.settings.getStorageType().equalsIgnoreCase("mySQL")) {

        } else {
            Integer playerID = getPlayerId(p.getName());
            if (playerID != null) {
                sqliteDatabase.deleteHome(homeName, playerID);
            }
        }
    }

    public static void deleteWarp(String warpName) {
        if (Main.settings.getStorageType().equalsIgnoreCase("mySQL")) {

        } else {
            sqliteDatabase.deleteWarp(warpName);
        }
    }

    public static void setupStorage(String storageType) {
        if (storageType.equalsIgnoreCase("sqlite")) {
            initializeSQLite();
        } else if (storageType.equalsIgnoreCase("mysql")) {
            initializeMySQL();
        } else {
            Bukkit.getLogger().warning("Invalid storage method set in config.yml; defaulting to SQLite");
            initializeSQLite();
        }
    }

    public static void createPlayer(Player p) {
        if (Main.settings.getStorageType().equalsIgnoreCase("mySQL")) {

        } else {
            sqliteDatabase.addPlayer(p);
        }
    }
}