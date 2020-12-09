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

// This class handles the saving of data; whether that be through SQLite or SQL
public class dataManager {

    public static String createPlayerTable = "CREATE TABLE IF NOT EXISTS " + Main.settings.getPlayerTable() + " (" +
            "`player_id` integer NOT NULL," +
            "`uuid` text NOT NULL UNIQUE," +
            "`username` text NOT NULL," +
            "`home_count` integer NOT NULL," +
            "`home_slots` integer NOT NULL," +
            "`rtp_cooldown` integer NOT NULL," +
            "`is_teleporting` boolean NOT NULL," +
            "`dest_type` text," + // Destination can be PLAYER or LOCATION.
            "`dest_player` integer," + // These values below can be null if needed
            "`dest_location_id` integer," +
            "`last_location_id` integer," +

            "PRIMARY KEY (`player_id`)," +
            "FOREIGN KEY (`dest_location_id`) REFERENCES " + Main.settings.getLocationsTable() + " (`location_id`) ON DELETE CASCADE ON UPDATE NO ACTION," +
            "FOREIGN KEY (`last_location_id`) REFERENCES " + Main.settings.getLocationsTable() + " (`location_id`) ON DELETE CASCADE ON UPDATE NO ACTION" +
            ");";

    public static String createLocationsTable = "CREATE TABLE IF NOT EXISTS " + Main.settings.getLocationsTable() + " (" +
            "`location_id` integer PRIMARY KEY," +
            "`server` text NOT NULL," +
            "`world` text NOT NULL," +
            "`x` double NOT NULL," +
            "`y` double NOT NULL," +
            "`z` double NOT NULL," +
            "`yaw` float NOT NULL," +
            "`pitch` float NOT NULL" +
            ");";

    public static String createHomesTable = "CREATE TABLE IF NOT EXISTS " + Main.settings.getHomesTable() + " (" +
            "`player_id` integer NOT NULL," +
            "`location_id` integer NOT NULL," +
            "`name` text NOT NULL," +
            "`description` double NOT NULL," +
            "`public` boolean NOT NULL," +

            "PRIMARY KEY (`player_id`, `location_id`)," +
            "FOREIGN KEY (`player_id`) REFERENCES " + Main.settings.getPlayerTable() + " (`player_id`) ON DELETE CASCADE ON UPDATE NO ACTION," +
            "FOREIGN KEY (`location_id`) REFERENCES " + Main.settings.getLocationsTable() + " (`location_id`) ON DELETE CASCADE ON UPDATE NO ACTION" +
            ");";

    public static String createWarpsTable = "CREATE TABLE IF NOT EXISTS " + Main.settings.getWarpsTable() + " (" +
            "`location_id` integer NOT NULL," +
            "`name` text NOT NULL UNIQUE," +
            "`description` double NOT NULL," +

            "PRIMARY KEY (`location_id`)," +
            "FOREIGN KEY (`location_id`) REFERENCES " + Main.settings.getLocationsTable() + " (`location_id`) ON DELETE CASCADE ON UPDATE NO ACTION" +
            ");";

    private static Database sqliteDatabase;

    private static void initializeSQLite() {
        sqliteDatabase = new SQLite(Main.getInstance());
        sqliteDatabase.load();
    }

    private static void initializeMySQL() {

    }

    public ResultSet queryDatabase(String query) {
        if (Main.settings.getStorageType().equalsIgnoreCase("mysql")) {
            return null;
        } else {
            return sqliteDatabase.queryDatabase(query);
        }
    }

    // Return an array of a player's homes
    public ArrayList<Home> getPlayerHomes(String playerName) {
        try {
            Integer playerID = getPlayerId(playerName);
            ResultSet rs = queryDatabase("SELECT * FROM " + Main.settings.getHomesTable() + " WHERE `player_id`=" + playerID + ";");
            ArrayList<Home> publicHomes = new ArrayList<>();
            while (rs.next()) {
                int locationID = rs.getInt("location_id");
                publicHomes.add(new Home(getTeleportationPoint(locationID), playerName, getPlayerUUID(playerID), rs.getString("name"), rs.getString("description"), rs.getBoolean("public")));
            }
            return publicHomes;
        } catch (SQLException e) {
            Bukkit.getLogger().severe("An SQL exception occurred returning a home!");
            return null;
        }
    }

    // Return an array of all the public homes
    public ArrayList<Home> getPublicHomes() {
        try {
            ResultSet rs = queryDatabase("SELECT * FROM " + Main.settings.getHomesTable() + " WHERE `public`;");
            ArrayList<Home> publicHomes = new ArrayList<>();
            while (rs.next()) {
                int playerID = rs.getInt("player_id");
                int locationID = rs.getInt("location_id");
                publicHomes.add(new Home(getTeleportationPoint(locationID), getPlayerUsername(playerID), getPlayerUUID(playerID), rs.getString("name"), rs.getString("description"), true));
            }
            return publicHomes;
        } catch (SQLException e) {
            Bukkit.getLogger().severe("An SQL exception occurred returning a home!");
            return null;
        }
    }

    // Return an array of all the warps
    public ArrayList<Warp> getWarps() {
        try {
            ResultSet rs = queryDatabase("SELECT * FROM " + Main.settings.getWarpsTable() + ";");
            ArrayList<Warp> warps = new ArrayList<>();
            while (rs.next()) {
                int locationID = rs.getInt("location_id");
                warps.add(new Warp(getTeleportationPoint(locationID), rs.getString("name"), rs.getString("description")));
            }
            return warps;
        } catch (SQLException e) {
            Bukkit.getLogger().severe("An SQL exception occurred returning a list of warps!");
            return null;
        }
    }

    // Return a warp with a given name (warp names are unique)
    public Warp getWarp(String name) {
        try {
            ResultSet rs = queryDatabase("SELECT * FROM " + Main.settings.getWarpsTable() + " WHERE `name`=" + name + ";");
            if (rs.next()) {
                int locationID = rs.getInt("location_id");
                return new Warp(getTeleportationPoint(locationID), rs.getString("name"), rs.getString("description"));
            } else {
                return null;
            }
        } catch (SQLException e) {
            Bukkit.getLogger().severe("An SQL exception occurred returning a list of warps!");
            return null;
        }
    }

    // Return a home with a given owner username and home name
    public Home getHome(String ownerUsername, String homeName) {
        try {
            Integer playerID = getPlayerId(ownerUsername);
            ResultSet rs = queryDatabase("SELECT * FROM " + Main.settings.getHomesTable() + " WHERE `player_id`=" + (playerID) + " AND `name`=" + homeName +";");
            if (rs.next()) {
                int locationID = rs.getInt("location_id");
                return new Home(getTeleportationPoint(locationID), ownerUsername, getPlayerUUID(playerID), rs.getString("name"), rs.getString("description"), rs.getBoolean("public"));
            } else {
                return null;
            }
        } catch (SQLException e) {
            Bukkit.getLogger().severe("An SQL exception occurred returning a home!");
            return null;
        }
    }

    public boolean playerExists(Player p) {
        try {
            ResultSet rs = queryDatabase("SELECT * FROM " + Main.settings.getPlayerTable() + " WHERE `uuid`=" + p.getUniqueId().toString() + ";");
            return rs.next();
        } catch (SQLException e) {
            Bukkit.getLogger().severe("An SQL exception occurred in determining if a player exists in the player table.");
            e.printStackTrace();
            return false;
        }
    }

    private Integer getPlayerId(String playerUsername) {
        try {
            ResultSet rs = queryDatabase("SELECT * FROM " + Main.settings.getPlayerTable() + " WHERE `username`=" + (playerUsername) + ";");
            if (rs.next()) {
                return rs.getInt("player_id");
            } else {
                return null;
            }
        } catch (SQLException e) {
            Bukkit.getLogger().severe("An SQL exception occurred returning a player ID!");
            return null;
        }
    }

    public String getPlayerUUID(int playerID) {
        try {
            ResultSet rs = queryDatabase("SELECT * FROM " + Main.settings.getPlayerTable() + " WHERE `player_id`=" + (playerID) + ";");
            if (rs.next()) {
                return rs.getString("uuid");
            } else {
                return null;
            }
        } catch (SQLException e) {
            Bukkit.getLogger().severe("An SQL exception occurred returning a player UUID!");
            return null;
        }
    }

    public void deletePlayerDestination(Player p) {
        int destinationID = getPlayerPositionID(p, "dest");
        if (Main.settings.getStorageType().equalsIgnoreCase("mySQL")) {

        } else {
            sqliteDatabase.deleteTeleportationPoint(destinationID);
        }
    }

    public void deletePlayerLastPosition(Player p) {
        int lastPositionID = getPlayerPositionID(p, "last");
        if (Main.settings.getStorageType().equalsIgnoreCase("mySQL")) {

        } else {
            sqliteDatabase.deleteTeleportationPoint(lastPositionID);
        }
    }

    public TeleportationPoint getPlayerDestination(Player p) {
        int locationID = getPlayerPositionID(p, "dest");
        return getTeleportationPoint(locationID);
    }

    public TeleportationPoint getPlayerLastPosition(Player p) {
        int locationID = getPlayerPositionID(p, "last");
        return getTeleportationPoint(locationID);
    }

    public Integer getPlayerPositionID(Player p, String type) {
        try {
            String playerName = p.getName();
            Integer playerID = getPlayerId(playerName);
            ResultSet rs = queryDatabase("SELECT * FROM " + Main.settings.getPlayerTable() + " WHERE `player_id`=" + playerID + ";");
            if (rs.next()) {
                return rs.getInt(type + "_location_id");
            } else {
                return null;
            }
        } catch (SQLException e) {
            Bukkit.getLogger().severe("An SQL exception occurred returning a player's destination location ID!");
            return null;
        }
    }

    // Update a player's last position location on SQL
    public void setPlayerLastPosition(Player p, TeleportationPoint point) {
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
    public void setPlayerDestination(Player p, TeleportationPoint point) {
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

    public String getPlayerUsername(int playerID) {
        try {
            ResultSet rs = queryDatabase("SELECT * FROM " + Main.settings.getPlayerTable() + " WHERE `player_id`=" + (playerID) + ";");
            if (rs.next()) {
                return rs.getString("username");
            } else {
                return null;
            }
        } catch (SQLException e) {
            Bukkit.getLogger().severe("An SQL exception occurred returning a player username!");
            return null;
        }
    }

    public TeleportationPoint getTeleportationPoint(int locationID) {
        try {
            ResultSet rs = queryDatabase("SELECT * FROM " + Main.settings.getLocationsTable() + " WHERE `location_id`=" + (locationID) + ";");
            if (rs.next()) {
                return new TeleportationPoint(rs.getString("world"), rs.getDouble("x"), rs.getDouble("y"), rs.getDouble("z"), rs.getFloat("yaw"), rs.getFloat("pitch"), rs.getString("server"));
            } else {
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
