package me.william278.huskhomes2.data.SQL;

import me.william278.huskhomes2.HuskHomes;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

public class SQLite extends Database {

    final static String[] SQL_SETUP_STATEMENTS = {
            "PRAGMA foreign_keys = ON;",

            "CREATE TABLE IF NOT EXISTS " + HuskHomes.getSettings().getPlayerDataTable() + " (" +
                    "`player_id` integer NOT NULL," +
                    "`user_uuid` char(36) NOT NULL UNIQUE," +
                    "`username` varchar(16) NOT NULL," +
                    "`home_slots` integer NOT NULL," +
                    "`rtp_cooldown` integer NOT NULL DEFAULT 0," +
                    "`is_teleporting` boolean NOT NULL DEFAULT 0," +
                    "`dest_location_id` integer NULL," +
                    "`last_location_id` integer NULL," +
                    "PRIMARY KEY (`player_id`)," +
                    "FOREIGN KEY (`dest_location_id`) REFERENCES " + HuskHomes.getSettings().getLocationsDataTable() + " (`location_id`) ON DELETE SET NULL ON UPDATE NO ACTION," +
                    "FOREIGN KEY (`last_location_id`) REFERENCES " + HuskHomes.getSettings().getLocationsDataTable() + " (`location_id`) ON DELETE SET NULL ON UPDATE NO ACTION" +
                    ");",

            "CREATE TABLE IF NOT EXISTS " + HuskHomes.getSettings().getLocationsDataTable() + " (" +
                    "`location_id` integer PRIMARY KEY," +
                    "`server` text NOT NULL," +
                    "`world` text NOT NULL," +
                    "`x` double NOT NULL," +
                    "`y` double NOT NULL," +
                    "`z` double NOT NULL," +
                    "`yaw` float NOT NULL," +
                    "`pitch` float NOT NULL" +
                    ");",

            "CREATE TABLE IF NOT EXISTS " + HuskHomes.getSettings().getHomesDataTable() + " (" +
                    "`player_id` integer NOT NULL," +
                    "`location_id` integer NOT NULL," +
                    "`name` varchar(16) NOT NULL," +
                    "`description` varchar(255) NOT NULL," +
                    "`public` boolean NOT NULL," +
                    "PRIMARY KEY (`player_id`, `name`)," +
                    "FOREIGN KEY (`player_id`) REFERENCES " + HuskHomes.getSettings().getPlayerDataTable() + " (`player_id`) ON DELETE CASCADE ON UPDATE NO ACTION," +
                    "FOREIGN KEY (`location_id`) REFERENCES " + HuskHomes.getSettings().getLocationsDataTable() + " (`location_id`) ON DELETE CASCADE ON UPDATE NO ACTION" +
                    ");",

            "CREATE TABLE IF NOT EXISTS " + HuskHomes.getSettings().getWarpsDataTable() + " (" +
                    "`location_id` integer NOT NULL," +
                    "`name` varchar(16) NOT NULL UNIQUE," +
                    "`description` varchar(255) NOT NULL," +
                    "PRIMARY KEY (`location_id`)," +
                    "FOREIGN KEY (`location_id`) REFERENCES " + HuskHomes.getSettings().getLocationsDataTable() + " (`location_id`) ON DELETE CASCADE ON UPDATE NO ACTION" +
                    ");"
    };

    private static final String DATABASE_NAME = "HuskHomesData";

    private Connection connection;

    public SQLite(HuskHomes instance) {
        super(instance);
    }

    @Override
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                File databaseFile = new File(plugin.getDataFolder(), DATABASE_NAME + ".db");
                if (!databaseFile.exists()) {
                    try {
                        if (!databaseFile.createNewFile()) {
                            plugin.getLogger().log(Level.SEVERE, "Failed to write new file: " + DATABASE_NAME + ".db (file already exists)");
                        }
                    } catch (IOException e) {
                        plugin.getLogger().log(Level.SEVERE, "An error occurred writing a file: " + DATABASE_NAME + ".db (" + e.getCause() + ")");
                    }
                }
                try {
                    Class.forName("org.sqlite.JDBC");
                    connection = (DriverManager.getConnection("jdbc:sqlite:" + plugin.getDataFolder().getAbsolutePath() + "/" + DATABASE_NAME + ".db"));
                } catch (SQLException ex) {
                    plugin.getLogger().log(Level.SEVERE, "An exception occurred initialising the SQLite database", ex);
                } catch (ClassNotFoundException ex) {
                    plugin.getLogger().log(Level.SEVERE, "The SQLite JBDC library is missing! Please download and place this in the /lib folder.");
                }
            }
        } catch (SQLException exception) {
            plugin.getLogger().log(Level.WARNING, "An error occurred checking the status of the SQL connection: ", exception);
        }
        return connection;
    }

    @Override
    public void load() {
        connection = getConnection();
        try {
            Statement statement = connection.createStatement();
            for (String tableCreationStatement : SQL_SETUP_STATEMENTS) {
                statement.execute(tableCreationStatement);
            }
            statement.close();
        } catch (SQLException exception) {
            plugin.getLogger().log(Level.SEVERE, "An error occurred creating tables: ", exception);
            exception.printStackTrace();
        }

        initialize();
    }
}
