package me.william278.huskhomes2.Data.SQLite;

import me.william278.huskhomes2.Main;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

public class SQLite extends Database {

    final String databaseName = "HuskHomesData";

    public SQLite(Main instance) {
        super(instance);
    }

    public String createPlayerTable = "CREATE TABLE IF NOT EXISTS " + Main.settings.getPlayerTable() + " (" +
            "`player_id` integer PRIMARY KEY," +
            "`uuid` text NOT NULL UNIQUE," +
            "`username` text NOT NULL," +
            "`home_count` integer NOT NULL," +
            "`home_slots` integer NOT NULL," +
            "`rtp_cooldown` integer NOT NULL," +
            "`home_count` integer NOT NULL" +
            ");";

    public String createLocationsTable = "CREATE TABLE IF NOT EXISTS " + Main.settings.getLocationsTable() + " (" +
            "`location_id` integer PRIMARY KEY," +
            "`world` text NOT NULL," +
            "`server` text NOT NULL," +
            "`x` double NOT NULL," +
            "`y` double NOT NULL," +
            "`z` double NOT NULL," +
            "`yaw` float NOT NULL," +
            "`pitch` float NOT NULL" +
            ");";

    public String createHomesTable = "CREATE TABLE IF NOT EXISTS " + Main.settings.getHomesTable() + " (" +
            "`player_id` integer NOT NULL," +
            "`location_id` integer NOT NULL," +
            "`name` text NOT NULL," +
            "`description` double NOT NULL," +
            "`public` boolean NOT NULL," +

            "PRIMARY KEY (`player_id`, `location_id`)," +
            "FOREIGN KEY (`player_id`) REFERENCES " + Main.settings.getPlayerTable() + " (`player_id`) ON DELETE CASCADE ON UPDATE NO ACTION," +
            "FOREIGN KEY (`location_id`) REFERENCES " + Main.settings.getLocationsTable() + " (`location_id`) ON DELETE CASCADE ON UPDATE NO ACTION," +
            ");";

    public String createWarpsTable = "CREATE TABLE IF NOT EXISTS " + Main.settings.getWarpsTable() + " (" +
            "`location_id` integer NOT NULL," +
            "`name` text NOT NULL," +
            "`description` double NOT NULL," +

            "PRIMARY KEY (`location_id`)," +
            "FOREIGN KEY (`location_id`) REFERENCES " + Main.settings.getLocationsTable() + " (`location_id`) ON DELETE CASCADE ON UPDATE NO ACTION," +
            ");";

    // SQL creation stuff, You can leave the blow stuff untouched.
    public Connection getSQLConnection() {
        File dataFolder = new File(plugin.getDataFolder(), databaseName + ".db");
        if (!dataFolder.exists()) {
            try {
                dataFolder.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "File write error: " + databaseName + ".db");
            }
        }
        try {
            if (connection != null && !connection.isClosed()) {
                return connection;
            }
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dataFolder);
            return connection;
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, "An exception occurred initialising the SQLite database", ex);
        } catch (ClassNotFoundException ex) {
            plugin.getLogger().log(Level.SEVERE, "You need the SQLite JBDC library; please download and place this in the /lib folder.");
        }
        return null;
    }

    public void load() {
        connection = getSQLConnection();
        try {
            Statement s = connection.createStatement();
            s.executeUpdate(createPlayerTable);
            s.executeUpdate(createLocationsTable);
            s.executeUpdate(createHomesTable);
            s.executeUpdate(createWarpsTable);
            s.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        initialize();
    }
}