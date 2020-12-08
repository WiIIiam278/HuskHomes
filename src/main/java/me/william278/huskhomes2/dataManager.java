package me.william278.huskhomes2;

import me.william278.huskhomes2.Data.SQLite.Database;
import me.william278.huskhomes2.Data.SQLite.SQLite;
import me.william278.huskhomes2.Objects.Home;
import me.william278.huskhomes2.Objects.TeleportationPoint;
import org.bukkit.Bukkit;

import java.sql.ResultSet;
import java.sql.SQLException;

// This class handles the saving of data; whether that be through SQLite or SQL
public class dataManager {

    public static String createPlayerTable = "CREATE TABLE IF NOT EXISTS " + Main.settings.getPlayerTable() + " (" +
            "`player_id` integer PRIMARY KEY," +
            "`uuid` text NOT NULL UNIQUE," +
            "`username` text NOT NULL," +
            "`home_count` integer NOT NULL," +
            "`home_slots` integer NOT NULL," +
            "`rtp_cooldown` integer NOT NULL," +
            "`home_count` integer NOT NULL" +
            ");";

    public static String createLocationsTable = "CREATE TABLE IF NOT EXISTS " + Main.settings.getLocationsTable() + " (" +
            "`location_id` integer PRIMARY KEY," +
            "`world` text NOT NULL," +
            "`server` text NOT NULL," +
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
            "FOREIGN KEY (`location_id`) REFERENCES " + Main.settings.getLocationsTable() + " (`location_id`) ON DELETE CASCADE ON UPDATE NO ACTION," +
            ");";

    public static String createWarpsTable = "CREATE TABLE IF NOT EXISTS " + Main.settings.getWarpsTable() + " (" +
            "`location_id` integer NOT NULL," +
            "`name` text NOT NULL," +
            "`description` double NOT NULL," +

            "PRIMARY KEY (`location_id`)," +
            "FOREIGN KEY (`location_id`) REFERENCES " + Main.settings.getLocationsTable() + " (`location_id`) ON DELETE CASCADE ON UPDATE NO ACTION," +
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

    public Home getHome(String ownerUsername, String homeName) {
        try {
            Integer playerID = getPlayerId(ownerUsername);
            ResultSet rs = queryDatabase("SELECT * FROM " + Main.settings.getPlayerTable() + " WHERE `player_id`=" + (playerID) + " AND `name`=" + homeName +";");
            if (rs.next()) {
                return new Home(getTransportationPoint(rs.getInt("location_id")), ownerUsername, getPlayerUUID(playerID), rs.getString("name"), rs.getString("description"), rs.getBoolean("public"));
            } else {
                return null;
            }
        } catch (SQLException e) {
            Bukkit.getLogger().severe("An SQL exception occurred returning a home!");
            return null;
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
            ResultSet rs = queryDatabase("SELECT * FROM " + Main.settings.getLocationsTable() + " WHERE `player_id`=" + (playerID) + ";");
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

    public TeleportationPoint getTransportationPoint(int locationID) {
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
