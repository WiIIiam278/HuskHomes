package me.william278.huskhomes2;

import me.william278.huskhomes2.Data.SQLite.Database;
import me.william278.huskhomes2.Data.SQLite.SQLite;
import me.william278.huskhomes2.Objects.Home;
import me.william278.huskhomes2.Objects.TeleportationPoint;
import me.william278.huskhomes2.Objects.Warp;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

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

    private static Database sqliteDatabase;

    private static void initializeSQLite() {
        sqliteDatabase = new SQLite(Main.getInstance());
        sqliteDatabase.load();
    }

    private static void initializeMySQL() {

    }

    public static ResultSet queryDatabase(String query) {
        if (Main.settings.getStorageType().equalsIgnoreCase("mysql")) {
            return null;
        } else {
            return sqliteDatabase.queryDatabase(query);
        }
    }

    // Return an integer from the player table
    private static Integer getPlayerInteger(String playerName, String column) {
        try {
            Integer playerID = getPlayerId(playerName);
            String query = "SELECT * FROM " + Main.settings.getHomesTable() + " WHERE `player_id`=?;";
            query = query.replace("?", playerID.toString());
            ResultSet rs = queryDatabase(query);
            if (rs != null) {
                if (rs.next()) {
                    return rs.getInt(column);
                } else {
                    return null;
                }
            } else {
                Bukkit.getLogger().severe("Could not return integer for " + playerName + " because the player ID was null");
                return null;
            }
        } catch (SQLException e) {
            Bukkit.getLogger().severe("An SQL exception occurred returning a value for " + playerName);
            return null;
        }
    }

    // Return an integer from the player table
    public static Integer getPlayerInteger(Player p, String column) {
        try {
            Integer playerID = getPlayerId(p.getName());
            String query = "SELECT * FROM " + Main.settings.getHomesTable() + " WHERE `player_id`=?;";
            query = query.replace("?", playerID.toString());
            ResultSet rs = queryDatabase(query);
            if (rs != null) {
                if (rs.next()) {
                    return rs.getInt(column);
                } else {
                    return null;
                }
            } else {
                Bukkit.getLogger().severe("Could not return integer for " + p.getName() + " because the player ID was null");
                return null;
            }
        } catch (SQLException e) {
            Bukkit.getLogger().severe("An SQL exception occurred returning a value for " + p.getName());
            return null;
        }
    }

    public static void createPlayer(Player p) {
        if (Main.settings.getStorageType().equalsIgnoreCase("mysql")) {

        } else {
            sqliteDatabase.addPlayer(p);
        }
    }

    // Get a string value from a player's data row
    public static String getPlayerString(Player p, String column) {
        Integer playerID = getPlayerId(p.getName());
        if (playerID != null) {
            return getPlayerString(playerID, column);
        } else {
            Bukkit.getLogger().severe("Could not return string for " + p.getName() + " because the player ID was null");
            return null;
        }
    }

    // Get a string value from a player with given ID's data row
    public static String getPlayerString(int playerID, String column) {
        try {
            String query = "SELECT * FROM " + Main.settings.getPlayerTable() + " WHERE `player_id`=?;";
            query = query.replace("?", Integer.toString(playerID));
            ResultSet rs = queryDatabase(query);
            if (rs != null) {
                if (rs.next()) {
                    return rs.getString(column);
                } else {
                    return null;
                }
            } else {
                Bukkit.getLogger().severe("An error occurred retrieving a string");
                return null;
            }
        } catch (SQLException e) {
            Bukkit.getLogger().severe("An SQL exception occurred returning a player UUID!");
            return null;
        }
    }

    // Return how many home slots a player has
    public static Boolean getPlayerTeleporting(Player p) {
        try {
            Integer playerID = getPlayerId(p.getName());
            String query = "SELECT * FROM " + Main.settings.getHomesTable() + " WHERE `player_id`=?;";
            query = query.replace("?", playerID.toString());
            ResultSet rs = queryDatabase(query);
            if (rs != null) {
                if (rs.next()) {
                    return rs.getBoolean("is_teleporting");
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } catch (SQLException e) {
            Bukkit.getLogger().severe("An SQL exception occurred checking " + p.getName() + " is teleporting");
            return null;
        }
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

    // Return an array of a player's homes
    public static ArrayList<Home> getPlayerHomes(String playerName) {
        try {
            Integer playerID = getPlayerId(playerName);
            if (playerID == null) {
                Bukkit.getLogger().severe("Player ID returned null!!");
                return new ArrayList<>();
            }
            String query = "SELECT * FROM " + Main.settings.getHomesTable() + " WHERE `player_id`=?;";
            query = query.replace("?", Integer.toString(playerID));
            ResultSet rs = queryDatabase(query);
            ArrayList<Home> playerHomes = new ArrayList<>();
            if (rs != null) {
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
                return null;
            }
        } catch (SQLException e) {
            Bukkit.getLogger().severe("An SQL exception occurred returning a home!");
            return null;
        }
    }

    // Return an array of all the public homes
    public static ArrayList<Home> getPublicHomes() {
        try {
            String query = "SELECT * FROM " + Main.settings.getHomesTable() + " WHERE `public`;";
            ResultSet rs = queryDatabase(query);
            ArrayList<Home> publicHomes = new ArrayList<>();
            if (rs != null) {
                while (rs.next()) {
                    int playerID = rs.getInt("player_id");
                    int locationID = rs.getInt("location_id");
                    TeleportationPoint teleportationPoint = getTeleportationPoint(locationID);
                    if (teleportationPoint != null) {
                        publicHomes.add(new Home(teleportationPoint, getPlayerUsername(playerID), getPlayerUUID(playerID), rs.getString("name"), rs.getString("description"), true));
                    }
                }
            } else {
                return null;
            }
            return publicHomes;
        } catch (SQLException e) {
            Bukkit.getLogger().severe("An SQL exception occurred returning a home!");
            return null;
        }
    }

    // Return an array of all the warps
    public static ArrayList<Warp> getWarps() {
        try {
            String query = "SELECT * FROM " + Main.settings.getWarpsTable() + ";";
            ResultSet rs = queryDatabase(query);
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
            }
            return warps;
        } catch (SQLException e) {
            Bukkit.getLogger().severe("An SQL exception occurred returning a list of warps!");
            return null;
        }
    }

    // Return a warp with a given name (warp names are unique)
    public static Warp getWarp(String name) {
        try {
            String query = "SELECT * FROM " + Main.settings.getWarpsTable() + " WHERE `name`=?;";
            query = query.replace("?", "'" + name + "'");
            ResultSet rs = queryDatabase(query);
            if (rs != null) {
                if (rs.next()) {
                    int locationID = rs.getInt("location_id");
                    TeleportationPoint teleportationPoint = getTeleportationPoint(locationID);
                    if (teleportationPoint != null) {
                        return new Warp(teleportationPoint,
                                rs.getString("name"),
                                rs.getString("description"));
                    } else {
                        Bukkit.getLogger().severe("An error occurred returning a warp!");
                        return null;
                    }
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } catch (SQLException e) {
            Bukkit.getLogger().severe("An SQL exception occurred returning a warp");
            e.printStackTrace();
            return null;
        }
    }

    // Return a home with a given owner username and home name
    public static Home getHome(String ownerUsername, String homeName) {
        try {
            Integer playerID = getPlayerId(ownerUsername);
            String query = "SELECT * FROM " + Main.settings.getHomesTable() + " WHERE `player_id`=%1% AND `name`=%2%;";
            query = query.replace("%1%", Integer.toString(playerID));
            query = query.replace("%2%", "'" + homeName + "'");            ResultSet rs = queryDatabase(query);
            if (rs != null) {
                if (rs.next()) {
                    int locationID = rs.getInt("location_id");
                    TeleportationPoint teleportationPoint = getTeleportationPoint(locationID);
                    if (teleportationPoint != null && playerID != null) {
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
                    return null;
                }
            } else {
                Bukkit.getLogger().severe("An error occurred retrieving a home from the table!");
                return null;
            }
        } catch (SQLException e) {
            Bukkit.getLogger().severe("An SQL exception occurred returning a home!");
            e.printStackTrace();
            return null;
        }
    }

    public static boolean warpExists(String warpName) {
        try {
            String query = "SELECT * FROM " + Main.settings.getWarpsTable() + " WHERE `name`=?;";
            query = query.replace("?", "'" + warpName + "'");
            ResultSet rs = queryDatabase(query);
            if (rs != null) {
                return rs.next();
            } else {
                Bukkit.getLogger().severe("An SQL exception occurred in retrieving warp data from the table.");
                return false;
            }
        } catch (SQLException e) {
            Bukkit.getLogger().severe("An SQL exception occurred in checking if warp data exists in the table.");
            e.printStackTrace();
            return false;
        }
    }

    public static boolean homeExists(Player p, String homeName) {
        Integer playerID = getPlayerId(p.getName());
        try {
            String query = "SELECT * FROM " + Main.settings.getHomesTable() + " WHERE `player_id`=%1% AND `name`=%2%;";
            query = query.replace("%1%", Integer.toString(playerID));
            query = query.replace("%2%", "'" + homeName + "'");
            ResultSet rs = queryDatabase(query);
            if (rs != null) {
                return rs.next();
            } else {
                Bukkit.getLogger().severe("An SQL exception occurred in retrieving player data from the table.");
                return false;
            }
        } catch (SQLException e) {
            Bukkit.getLogger().severe("An SQL exception occurred in checking if player data exists in the table.");
            e.printStackTrace();
            return false;
        }
    }

    public static boolean playerExists(Player p) {
        try {
            UUID uuid = p.getUniqueId();
            String uuidString = uuid.toString();
            String query = "SELECT * FROM " + Main.settings.getPlayerTable() + " WHERE `user_uuid`=?;";
            query = query.replace("?", "'" + uuidString + "'");
            ResultSet rs = queryDatabase(query);
            if (rs != null) {
                return rs.next();
            } else {
                Bukkit.getLogger().severe("An SQL exception retrieving SQL data from the player table in determining if they exist.");
                return false;
            }
        } catch (SQLException e) {
            Bukkit.getLogger().severe("An SQL exception occurred in determining if a player exists in the player table.");
            e.printStackTrace();
            return false;
        }
    }

    private static Integer getPlayerId(String playerUsername) {
        try {
            String query = "SELECT * FROM " + Main.settings.getPlayerTable() + " WHERE `username`=?;";
            query = query.replace("?", "'" + playerUsername + "'");
            Bukkit.getLogger().info(query);
            ResultSet rs = queryDatabase(query);
            if (rs != null) {
                if (rs.next()) {
                    return rs.getInt("player_id");
                } else {
                    Bukkit.getLogger().info("Could not find player ID for " + playerUsername);
                    return null;
                }
            } else {
                Bukkit.getLogger().severe("An SQL exception occurred in retrieving a player ID.");
                return null;
            }
        } catch (SQLException e) {
            Bukkit.getLogger().severe("An SQL exception occurred returning a player ID!");
            return null;
        }
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

    public static TeleportationPoint getTeleportationPoint(int locationID) {
        try {
            String query = "SELECT * FROM " + Main.settings.getLocationsTable() + " WHERE `location_id`=" + (locationID) + ";";
            ResultSet rs = queryDatabase(query);
            if (rs != null) {
                if (rs.next()) {
                    return new TeleportationPoint(rs.getString("world"),
                            rs.getDouble("x"), rs.getDouble("y"),
                            rs.getDouble("z"), rs.getFloat("yaw"),
                            rs.getFloat("pitch"), rs.getString("server"));
                } else {
                    return null;
                }
            } else {
                Bukkit.getLogger().severe("An error occurred returning a transportation point!");
                return null;
            }
        } catch (SQLException e) {
            Bukkit.getLogger().severe("An SQL exception occurred returning a transportation point!");
            return null;
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

}