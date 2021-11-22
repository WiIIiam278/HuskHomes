package me.william278.huskhomes2.data.sql;

import com.zaxxer.hikari.HikariDataSource;
import me.william278.huskhomes2.HuskHomes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.logging.Level;

public class SQLite extends Database {

    final static String[] SQL_SETUP_STATEMENTS = {
            "PRAGMA foreign_keys = ON;",
            "PRAGMA encoding = 'UTF-8';",

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

            "CREATE TABLE IF NOT EXISTS " + HuskHomes.getSettings().getPlayerDataTable() + " (" +
                    "`player_id` integer NOT NULL," +
                    "`user_uuid` char(36) NOT NULL UNIQUE," +
                    "`username` varchar(16) NOT NULL," +
                    "`home_slots` integer NOT NULL," +
                    "`rtp_cooldown` integer NOT NULL DEFAULT 0," +
                    "`is_teleporting` boolean NOT NULL DEFAULT 0," +
                    "`dest_location_id` integer NULL," +
                    "`last_location_id` integer NULL," +
                    "`offline_location_id` integer NULL," +
                    "`is_ignoring_requests` boolean NOT NULL DEFAULT 0," +
                    "PRIMARY KEY (`player_id`)," +
                    "FOREIGN KEY (`offline_location_id`) REFERENCES " + HuskHomes.getSettings().getLocationsDataTable() + " (`location_id`) ON DELETE SET NULL ON UPDATE NO ACTION," +
                    "FOREIGN KEY (`dest_location_id`) REFERENCES " + HuskHomes.getSettings().getLocationsDataTable() + " (`location_id`) ON DELETE SET NULL ON UPDATE NO ACTION," +
                    "FOREIGN KEY (`last_location_id`) REFERENCES " + HuskHomes.getSettings().getLocationsDataTable() + " (`location_id`) ON DELETE SET NULL ON UPDATE NO ACTION" +
                    ");",

            "CREATE TABLE IF NOT EXISTS " + HuskHomes.getSettings().getHomesDataTable() + " (" +
                    "`player_id` integer NOT NULL," +
                    "`location_id` integer NOT NULL," +
                    "`name` varchar(16) NOT NULL," +
                    "`description` varchar(255) NOT NULL," +
                    "`public` boolean NOT NULL," +
                    "`creation_time` timestamp NULL," +
                    "PRIMARY KEY (`player_id`, `name`)," +
                    "FOREIGN KEY (`player_id`) REFERENCES " + HuskHomes.getSettings().getPlayerDataTable() + " (`player_id`) ON DELETE CASCADE ON UPDATE NO ACTION," +
                    "FOREIGN KEY (`location_id`) REFERENCES " + HuskHomes.getSettings().getLocationsDataTable() + " (`location_id`) ON DELETE CASCADE ON UPDATE NO ACTION" +
                    ");",

            "CREATE TABLE IF NOT EXISTS " + HuskHomes.getSettings().getWarpsDataTable() + " (" +
                    "`location_id` integer NOT NULL," +
                    "`name` varchar(16) NOT NULL UNIQUE," +
                    "`description` varchar(255) NOT NULL," +
                    "`creation_time` timestamp NULL," +
                    "PRIMARY KEY (`location_id`)," +
                    "FOREIGN KEY (`location_id`) REFERENCES " + HuskHomes.getSettings().getLocationsDataTable() + " (`location_id`) ON DELETE CASCADE ON UPDATE NO ACTION" +
                    ");"
    };

    private static final String DATABASE_NAME = "HuskHomesData";

    private HikariDataSource dataSource;

    public SQLite(HuskHomes instance) {
        super(instance);
    }

    // Create the database file if it does not exist yet
    private void createDatabaseFileIfNotExist() {
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
    }

    @Override
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void load() {
        // Make SQLite database file
        createDatabaseFileIfNotExist();

        // Create new HikariCP data source
        final String jdbcUrl = "jdbc:sqlite:" + plugin.getDataFolder().getAbsolutePath() + "/" + DATABASE_NAME + ".db";
        dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(jdbcUrl);

        // Set various additional parameters
        dataSource.setMaximumPoolSize(hikariMaximumPoolSize);
        dataSource.setMinimumIdle(hikariMinimumIdle);
        dataSource.setMaxLifetime(hikariMaximumLifetime);
        dataSource.setKeepaliveTime(hikariKeepAliveTime);
        dataSource.setConnectionTimeout(hikariConnectionTimeOut);
        dataSource.setPoolName(DATA_POOL_NAME);

        // Create tables
        try (Connection connection = dataSource.getConnection()) {
            try (Statement statement = connection.createStatement()) {
                for (String tableCreationStatement : SQL_SETUP_STATEMENTS) {
                    statement.execute(tableCreationStatement);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "An error occurred creating tables on the SQLite database: ", e);
        }
    }

    @Override
    public void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    @Override
    public void backup() {
        final String BACKUPS_FOLDER_NAME = "database-backups";
        final String backupFileName = DATABASE_NAME + "Backup_" + DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss-SS")
                .withLocale(Locale.getDefault())
                .withZone(ZoneId.systemDefault())
                .format(Instant.now()).replaceAll(" ", "-") + ".db";
        final File databaseFile = new File(plugin.getDataFolder(), DATABASE_NAME + ".db");
        if (new File(plugin.getDataFolder(), BACKUPS_FOLDER_NAME).mkdirs()) {
            plugin.getLogger().info("Created backups directory in HuskHomes plugin data folder.");
        }
        final File backUpFile = new File(plugin.getDataFolder(), BACKUPS_FOLDER_NAME + File.separator + backupFileName);
        try {
            Files.copy(databaseFile.toPath(), backUpFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            plugin.getLogger().info("Created a backup of your database.");
        } catch (IOException iox) {
            plugin.getLogger().log(Level.WARNING, "An error occurred making a database backup", iox);
        }
    }
}
