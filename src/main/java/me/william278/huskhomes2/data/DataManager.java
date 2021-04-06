package me.william278.huskhomes2.data;

import me.william278.huskhomes2.HuskHomes;
import me.william278.huskhomes2.teleport.points.Home;
import me.william278.huskhomes2.teleport.points.TeleportationPoint;
import me.william278.huskhomes2.teleport.points.Warp;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

/*
    TODO Should not be an utility class for SQL class
    Instead of doing everything here, all methods should be moved into corresponding classes

 */
// This class handles the saving of data; whether that be through SQLite or mySQL
public class DataManager {

    // Syntax for creating tables; changes based on storage medium
    public static String createPlayerTable = "";

    public static String createLocationsTable = "";

    public static final String createHomesTable = "CREATE TABLE IF NOT EXISTS " + HuskHomes.getSettings().getHomesDataTable() + " (" +
            "`player_id` integer NOT NULL," +
            "`location_id` integer NOT NULL," +
            "`name` varchar(16) NOT NULL," +
            "`description` varchar(255) NOT NULL," +
            "`public` boolean NOT NULL," +
            "PRIMARY KEY (`player_id`, `name`)," +
            "FOREIGN KEY (`player_id`) REFERENCES " + HuskHomes.getSettings().getPlayerDataTable() + " (`player_id`) ON DELETE CASCADE ON UPDATE NO ACTION," +
            "FOREIGN KEY (`location_id`) REFERENCES " + HuskHomes.getSettings().getLocationsDataTable() + " (`location_id`) ON DELETE CASCADE ON UPDATE NO ACTION" +
            ");";

    public static final String createWarpsTable = "CREATE TABLE IF NOT EXISTS " + HuskHomes.getSettings().getWarpsDataTable() + " (" +
            "`location_id` integer NOT NULL," +
            "`name` varchar(16) NOT NULL UNIQUE," +
            "`description` varchar(255) NOT NULL," +
            "PRIMARY KEY (`location_id`)," +
            "FOREIGN KEY (`location_id`) REFERENCES " + HuskHomes.getSettings().getLocationsDataTable() + " (`location_id`) ON DELETE CASCADE ON UPDATE NO ACTION" +
            ");";

    private static final HuskHomes plugin = HuskHomes.getInstance();

    private static Database database;

    private static Connection getConnection() {
        return database.getSQLConnection();
    }

    private static void initializeDatabase() {
        database = new SQL(HuskHomes.getInstance());
        database.load();
    }

    // Return a player's ID  from their UUID
    private static Integer getPlayerId(UUID playerUUID) {
        Connection conn;
        PreparedStatement ps;
        ResultSet rs;

        try {
            conn = getConnection();
            ps = conn.prepareStatement("SELECT * FROM " + HuskHomes.getSettings().getPlayerDataTable() + " WHERE `user_uuid`=?;");
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
            plugin.getLogger().log(Level.SEVERE, Error.SQL_CONNECTION_EXECUTE, ex);
        }
        return null;
    }

    // Return a player's ID  from their username
    private static Integer getPlayerId(String playerUsername) {
        Connection conn;
        PreparedStatement ps;
        ResultSet rs;

        try {
            conn = getConnection();
            ps = conn.prepareStatement("SELECT * FROM " + HuskHomes.getSettings().getPlayerDataTable() + " WHERE `username`=?;");
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
            plugin.getLogger().log(Level.SEVERE, Error.SQL_CONNECTION_EXECUTE, ex);
        }
        return null;
    }

    // Return an integer from the player table from a player ID
    public static Integer getPlayerInteger(Integer playerID, String column) {
        Connection conn;
        PreparedStatement ps;
        ResultSet rs;

        try {
            conn = getConnection();
            ps = conn.prepareStatement("SELECT * FROM " + HuskHomes.getSettings().getPlayerDataTable() + " WHERE `player_id`=?;");
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
            plugin.getLogger().log(Level.SEVERE, Error.SQL_CONNECTION_EXECUTE, ex);
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
        Connection conn;
        PreparedStatement ps;
        ResultSet rs;

        try {
            conn = getConnection();
            ps = conn.prepareStatement("SELECT * FROM " + HuskHomes.getSettings().getPlayerDataTable() + " WHERE `player_id`=?;");
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
            plugin.getLogger().log(Level.SEVERE, Error.SQL_CONNECTION_EXECUTE, ex);
        }
        return null;
    }

    // Return if the player is teleporting
    public static Boolean getPlayerTeleporting(Player p) {
        Connection conn;
        PreparedStatement ps;
        ResultSet rs;

        try {
            conn = getConnection();
            ps = conn.prepareStatement("SELECT * FROM " + HuskHomes.getSettings().getPlayerDataTable() + " WHERE `user_uuid`=?);");
            ps.setString(1, p.getUniqueId().toString());
            rs = ps.executeQuery();
            if (rs != null) {
                if (rs.next()) {
                    return rs.getBoolean("is_teleporting");
                } else {
                    return null;
                }
            } else {
                Bukkit.getLogger().severe("Failed to retrieve if player was teleporting");
                return null;
            }
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Error.SQL_CONNECTION_EXECUTE, ex);
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
        List<Home> playerHomes = getPlayerHomes(p.getName());
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
        database.setPlayerHomeSlots(p.getUniqueId(), slots);
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
    public static List<Home> getPlayerHomes(String playerName) {
        Connection conn;
        PreparedStatement ps;
        ResultSet rs;

        try {
            conn = getConnection();
            ps = conn.prepareStatement("SELECT * FROM " + HuskHomes.getSettings().getHomesDataTable() + " WHERE `player_id`=(SELECT `player_id` FROM " + HuskHomes.getSettings().getPlayerDataTable() + " WHERE `username`=?));");
            ps.setString(1, playerName);
            rs = ps.executeQuery();
            if (rs != null) {
                List<Home> playerHomes = new ArrayList<>();
                while (rs.next()) {
                    int locationID = rs.getInt("location_id");
                    TeleportationPoint teleportationPoint = getTeleportationPoint(locationID);
                    if (teleportationPoint != null) {
                        playerHomes.add(new Home(teleportationPoint,
                                playerName, getPlayerUUID(rs.getInt("player_id")),
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
            plugin.getLogger().log(Level.SEVERE, Error.SQL_CONNECTION_EXECUTE, ex);
        }
        return null;
    }

    // Return all the public homes
    public static List<Home> getPublicHomes() {
        Connection conn;
        PreparedStatement ps;
        ResultSet rs;

        try {
            conn = getConnection();
            ps = conn.prepareStatement("SELECT * FROM " + HuskHomes.getSettings().getHomesDataTable() + " WHERE `public`;");
            rs = ps.executeQuery();
            if (rs != null) {
                List<Home> publicHomes = new ArrayList<>();
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
            plugin.getLogger().log(Level.SEVERE, Error.SQL_CONNECTION_EXECUTE, ex);
        }
        return null;
    }

    // Return an array of all the warps
    public static List<Warp> getWarps() {
        Connection conn;
        PreparedStatement ps;
        ResultSet rs;

        try {
            conn = getConnection();
            ps = conn.prepareStatement("SELECT * FROM " + HuskHomes.getSettings().getWarpsDataTable() + ";");
            rs = ps.executeQuery();
            List<Warp> warps = new ArrayList<>();
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
            plugin.getLogger().log(Level.SEVERE, Error.SQL_CONNECTION_EXECUTE, ex);
        }
        return null;
    }

    // Return a warp with a given name (warp names are unique)
    public static Warp getWarp(String name) {
        Connection conn;
        PreparedStatement ps;
        ResultSet rs;

        try {
            conn = getConnection();
            ps = conn.prepareStatement("SELECT * FROM " + HuskHomes.getSettings().getWarpsDataTable() + " WHERE `name`=?;");
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
            plugin.getLogger().log(Level.SEVERE, Error.SQL_CONNECTION_EXECUTE, ex);
        }
        return null;
    }

    // Obtain the teleportation location ID from a home
    public static Integer getHomeLocationID(int ownerID, String homeName) {
        Connection conn;
        PreparedStatement ps;
        ResultSet rs;

        try {
            conn = getConnection();
            ps = conn.prepareStatement("SELECT * FROM " + HuskHomes.getSettings().getHomesDataTable() + " WHERE `player_id`=? AND `name`=?;");
            ps.setInt(1, ownerID);
            ps.setString(2, homeName);
            rs = ps.executeQuery();
            if (rs != null) {
                if (rs.next()) {
                    return rs.getInt("location_id");
                }
            } else {
                Bukkit.getLogger().severe("Failed to obtain home teleportation location ID");
            }
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Error.SQL_CONNECTION_EXECUTE, ex);
        }
        return null;
    }

    // Delete a home's corresponding home teleport location
    public static void deleteHomeTeleportLocation(int ownerID, String homeName) {
        Integer locationID = getHomeLocationID(ownerID, homeName);
        database.deleteTeleportationPoint(locationID);
    }

    // Update a home's teleport location (deletion of the old one is done afterward to prevent cascading deletion from wiping the home
    public static void updateHomeTeleportLocation(int ownerID, String homeName, TeleportationPoint teleportationPoint) {
        Integer oldLocationID = getHomeLocationID(ownerID, homeName);
        database.setHomeTeleportPoint(homeName, ownerID, teleportationPoint);
        database.deleteTeleportationPoint(oldLocationID);
    }

    // Get the ID of the teleportation point of the warp
    public static Integer getWarpLocationID(String warpName) {
        Connection conn;
        PreparedStatement ps;
        ResultSet rs;

        try {
            conn = getConnection();
            ps = conn.prepareStatement("SELECT * FROM " + HuskHomes.getSettings().getWarpsDataTable() + " WHERE `name`=?;");
            ps.setString(1, warpName);
            rs = ps.executeQuery();
            if (rs != null) {
                if (rs.next()) {
                    int locationID = rs.getInt("location_id");
                    return locationID;
                }
            } else {
                Bukkit.getLogger().severe("Failed to obtain warp teleportation location ID");
            }
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Error.SQL_CONNECTION_EXECUTE, ex);
        }
        return null;
    }

    // Update a warp's teleport location (deletion of the old one is done afterward to prevent cascading deletion from wiping the warp
    public static void updateWarpTeleportLocation(String warpName, TeleportationPoint teleportationPoint) {
        Integer oldLocationID = getWarpLocationID(warpName);
        database.setWarpTeleportPoint(warpName, teleportationPoint);
        database.deleteTeleportationPoint(oldLocationID);
    }

    public static void deleteWarpTeleportLocation(String warpName) {
        Integer locationID = getWarpLocationID(warpName);
        database.deleteTeleportationPoint(locationID);
    }

    public static void updateHomePrivacy(String ownerName, String homeName, boolean isPublic) {
        database.setHomePrivacy(homeName, ownerName, isPublic);
    }

    public static void updateHomeName(String ownerName, String homeName, String newName) {
        database.setHomeName(homeName, ownerName, newName);
    }

    public static void updateHomeDescription(String ownerName, String homeName, String newDescription) {
        database.setHomeDescription(homeName, ownerName, newDescription);
    }

    public static void updateHomeLocation(String ownerName, String homeName, Location newLocation) {
        Integer playerID = getPlayerId(ownerName);
        updateHomeTeleportLocation(playerID,homeName,
                new TeleportationPoint(newLocation, HuskHomes.getSettings().getServerID()));
    }

    public static void updateWarpName(String warpName, String newName) {
        database.setWarpName(warpName, newName);
    }

    public static void updateWarpDescription(String warpName, String newDescription) {
        database.setWarpDescription(warpName, newDescription);
    }

    public static void updateWarpLocation(String warpName, Location newLocation) {
        updateWarpTeleportLocation(warpName,
                new TeleportationPoint(newLocation, HuskHomes.getSettings().getServerID()));
    }

    // Return a home with a given owner username and home name
    public static Home getHome(String ownerUsername, String homeName) {
        Connection conn;
        PreparedStatement ps;
        ResultSet rs;

        try {
            conn = getConnection();
            ps = conn.prepareStatement("SELECT * FROM " + HuskHomes.getSettings().getHomesDataTable() + " WHERE `player_id`=(SELECT `player_id` FROM " + HuskHomes.getSettings().getPlayerDataTable() + " WHERE `username`=?) AND `name`=?;");
            ps.setString(1, ownerUsername);
            ps.setString(2, homeName);
            rs = ps.executeQuery();
            if (rs != null) {
                if (rs.next()) {
                    int locationID = rs.getInt("location_id");
                    TeleportationPoint teleportationPoint = getTeleportationPoint(locationID);
                    if (teleportationPoint != null) {
                        return new Home(teleportationPoint,
                                ownerUsername,
                                getPlayerUUID(rs.getInt("player_id")),
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
            plugin.getLogger().log(Level.SEVERE, Error.SQL_CONNECTION_EXECUTE, ex);
        }
        return null;
    }

    public static Boolean warpExists(String warpName) {
        Connection conn;
        PreparedStatement ps;
        ResultSet rs;

        try {
            conn = getConnection();
            ps = conn.prepareStatement("SELECT * FROM " + HuskHomes.getSettings().getWarpsDataTable() + " WHERE `name`=?;");
            ps.setString(1, warpName);
            rs = ps.executeQuery();
            if (rs != null) {
                return rs.next();
            } else {
                Bukkit.getLogger().severe("An SQL exception occurred in retrieving if a warp exists from the table.");
                return false;
            }
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Error.SQL_CONNECTION_EXECUTE, ex);
        }
        return null;
    }

    public static Boolean homeExists(Player owner, String homeName) {
        return homeExists(owner.getName(), homeName);
    }

    public static Boolean homeExists(String ownerName, String homeName) {
        Connection conn;
        PreparedStatement ps;
        ResultSet rs;

        try {
            conn = getConnection();
            ps = conn.prepareStatement("SELECT * FROM " + HuskHomes.getSettings().getHomesDataTable() + " WHERE `player_id`=(SELECT `player_id` FROM " + HuskHomes.getSettings().getPlayerDataTable() + " WHERE `username`=?) AND `name`=?;");
            ps.setString(1, ownerName);
            ps.setString(2, homeName);
            rs = ps.executeQuery();
            if (rs != null) {
                return rs.next();
            } else {
                Bukkit.getLogger().severe("An SQL exception occurred in retrieving if a home exists from the table.");
                return false;
            }
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Error.SQL_CONNECTION_EXECUTE, ex);
        }
        return null;
    }

    public static Boolean playerExists(Player player) {
        return playerExists(player.getUniqueId());
    }

    public static Boolean playerExists(UUID playerUUID) {
        Connection conn;
        PreparedStatement ps;
        ResultSet rs;
        String playerUUIDString = playerUUID.toString();

        try {
            conn = getConnection();
            ps = conn.prepareStatement("SELECT * FROM " + HuskHomes.getSettings().getPlayerDataTable() + " WHERE `user_uuid`=?;");
            ps.setString(1, playerUUIDString);
            rs = ps.executeQuery();
            if (rs != null) {
                return rs.next();
            } else {
                Bukkit.getLogger().severe("An SQL exception occurred in retrieving if a player exists from the table.");
                return false;
            }
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Error.SQL_CONNECTION_EXECUTE, ex);
        }
        return null;
    }

    public static TeleportationPoint getTeleportationPoint(Integer locationID) {
        Connection conn;
        PreparedStatement ps;
        ResultSet rs;

        try {
            conn = getConnection();
            ps = conn.prepareStatement("SELECT * FROM " + HuskHomes.getSettings().getLocationsDataTable() + " WHERE `location_id`=?;");
            ps.setInt(1, locationID);
            rs = ps.executeQuery();
            if (rs.next()) {
                return new TeleportationPoint(rs.getString("world"),
                        rs.getDouble("x"), rs.getDouble("y"),
                        rs.getDouble("z"), rs.getFloat("yaw"),
                        rs.getFloat("pitch"), rs.getString("server"));
            } else {
                throw new IllegalArgumentException("Could not return a teleportationPoint from the locations data table at (ID#" + locationID + ")");
            }
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Error.SQL_CONNECTION_EXECUTE, ex);
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
        deletePlayerLastPosition(p);
        database.setTeleportationLastPosition(p.getUniqueId(), point);
    }

    // Update a player's destination location on SQL
    public static void setPlayerDestinationLocation(String playerName, TeleportationPoint point) {
        deletePlayerDestination(playerName);
        database.setTeleportationDestination(playerName, point);
    }

    // Update a player's destination location on SQL
    public static void setPlayerDestinationLocation(Player p, TeleportationPoint point) {
        setPlayerDestinationLocation(p.getName(), point);
    }

    public static void setPlayerTeleporting(Player p, boolean value) {
        database.setPlayerTeleporting(p.getUniqueId(), value);
    }

    public static void clearPlayerDestination(String playerName) {
        deletePlayerDestination(playerName);
        database.clearPlayerDestination(playerName);
    }

    public static void updateRtpCooldown(Player p) {
        long currentTime = Instant.now().getEpochSecond();
        int newCooldownTime = (int) currentTime + (60 * HuskHomes.getSettings().getRtpCooldown());
        database.setRtpCooldown(p.getUniqueId(), newCooldownTime);
    }

    public static void deletePlayerDestination(String playerName) {
        Integer destinationID = getPlayerInteger(playerName, "dest_location_id");
        if (destinationID != null) {
            database.deleteTeleportationPoint(destinationID);
            database.clearPlayerDestination(playerName);
        }
    }

    public static void deletePlayerDestination(Player p) {
        deletePlayerDestination(p.getName());
    }

    public static void deletePlayerLastPosition(Player p) {
        Integer lastPositionID = getPlayerInteger(p, "last_location_id");
        if (lastPositionID != null) {
            database.deleteTeleportationPoint(lastPositionID);
            database.clearPlayerLastPosition(p.getUniqueId());
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
            database.deleteHome(homeName, p.getName());
        } else {
            Bukkit.getLogger().warning("Player ID returned null when deleting a home");
        }
    }

    public static void deleteWarp(String warpName) {
        deleteWarpTeleportLocation(warpName);
        database.deleteWarp(warpName);
    }

    public static void setupStorage() {
        if (HuskHomes.getSettings().getStorageType().equalsIgnoreCase("mysql")) {
            createPlayerTable = "CREATE TABLE IF NOT EXISTS " + HuskHomes.getSettings().getPlayerDataTable() + " (" +
                    "`player_id` integer AUTO_INCREMENT," +
                    "`user_uuid` char(36) NOT NULL UNIQUE," +
                    "`username` varchar(16) NOT NULL," +
                    "`home_slots` integer NOT NULL," +
                    "`rtp_cooldown` integer NOT NULL," +
                    "`is_teleporting` boolean NOT NULL," +
                    "`dest_location_id` integer NULL," +
                    "`last_location_id` integer NULL," +

                    "PRIMARY KEY (`player_id`)," +
                    "FOREIGN KEY (`dest_location_id`) REFERENCES " + HuskHomes.getSettings().getLocationsDataTable() + " (`location_id`) ON DELETE SET NULL ON UPDATE NO ACTION," +
                    "FOREIGN KEY (`last_location_id`) REFERENCES " + HuskHomes.getSettings().getLocationsDataTable() + " (`location_id`) ON DELETE SET NULL ON UPDATE NO ACTION" +
                    ");";
            createLocationsTable = "CREATE TABLE IF NOT EXISTS " + HuskHomes.getSettings().getLocationsDataTable() + " (" +
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
            createPlayerTable = "CREATE TABLE IF NOT EXISTS " + HuskHomes.getSettings().getPlayerDataTable() + " (" +
                    "`player_id` integer NOT NULL," +
                    "`user_uuid` char(36) NOT NULL UNIQUE," +
                    "`username` varchar(16) NOT NULL," +
                    "`home_slots` integer NOT NULL," +
                    "`rtp_cooldown` integer NOT NULL," +
                    "`is_teleporting` boolean NOT NULL," +
                    "`dest_location_id` integer NULL," +
                    "`last_location_id` integer NULL," +
                    "PRIMARY KEY (`player_id`)," +
                    "FOREIGN KEY (`dest_location_id`) REFERENCES " + HuskHomes.getSettings().getLocationsDataTable() + " (`location_id`) ON DELETE SET NULL ON UPDATE NO ACTION," +
                    "FOREIGN KEY (`last_location_id`) REFERENCES " + HuskHomes.getSettings().getLocationsDataTable() + " (`location_id`) ON DELETE SET NULL ON UPDATE NO ACTION" +
                    ");";
            createLocationsTable = "CREATE TABLE IF NOT EXISTS " + HuskHomes.getSettings().getLocationsDataTable() + " (" +
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

    public static void createPlayer(UUID playerUUID, String playerUsername, int homeSlots) {
        database.addPlayer(playerUUID, playerUsername, homeSlots);
    }

    public static void checkPlayerNameChange(Player p) {
        if (!getPlayerUsername(getPlayerId(p.getUniqueId())).equals(p.getName())) {
            database.updatePlayerUsername(p.getUniqueId(), p.getName());
        }
    }
}