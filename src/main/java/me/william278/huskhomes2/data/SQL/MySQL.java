package me.william278.huskhomes2.data.SQL;

import com.zaxxer.hikari.HikariDataSource;
import me.william278.huskhomes2.HuskHomes;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

public class MySQL extends Database {

    final static String[] SQL_SETUP_STATEMENTS = {
            "CREATE TABLE IF NOT EXISTS " + HuskHomes.getSettings().getLocationsDataTable() + " (" +
                    "`location_id` integer AUTO_INCREMENT," +
                    "`server` text NOT NULL," +
                    "`world` text NOT NULL," +
                    "`x` double NOT NULL," +
                    "`y` double NOT NULL," +
                    "`z` double NOT NULL," +
                    "`yaw` float NOT NULL," +
                    "`pitch` float NOT NULL," +

                    "PRIMARY KEY (`location_id`)" +
                    ");",

            "CREATE TABLE IF NOT EXISTS " + HuskHomes.getSettings().getPlayerDataTable() + " (" +
                    "`player_id` integer AUTO_INCREMENT," +
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

    final String host = HuskHomes.getSettings().getMySQLHost();
    final int port = HuskHomes.getSettings().getMySQLPort();
    final String database = HuskHomes.getSettings().getMySQLDatabase();
    final String username = HuskHomes.getSettings().getMySQLUsername();
    final String password = HuskHomes.getSettings().getMySQLPassword();
    final String params = HuskHomes.getSettings().getMySQLParams();

    private HikariDataSource dataSource;

    public MySQL(HuskHomes instance) {
        super(instance);
    }

    @Override
    public Connection getConnection() {
        Connection connection = null;
        try {
            if (dataSource == null || dataSource.isClosed()) {
                try {
                    // Create new HikariCP data source
                    final String jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + database + params;
                    HikariDataSource hikariDataSource = new HikariDataSource();
                    hikariDataSource.setJdbcUrl(jdbcUrl);

                    hikariDataSource.setUsername(username);
                    hikariDataSource.setPassword(password);

                    hikariDataSource.setMaximumPoolSize(hikariMaximumPoolSize);
                    hikariDataSource.setMinimumIdle(hikariMinimumIdle);
                    hikariDataSource.setMaxLifetime(hikariMaximumLifetime);
                    hikariDataSource.setKeepaliveTime(hikariKeepAliveTime);
                    hikariDataSource.setConnectionTimeout(hikariConnectionTimeOut);

                    this.dataSource = hikariDataSource;
                    connection = dataSource.getConnection();
                } catch (SQLException e ) {
                    plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred initialising a new connection via HikariCP", e);
                }
            } else {
                connection = dataSource.getConnection();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred getting the mySQL connection via the existing Hikari pool", e);
        }
        return connection;
    }

    @Override
    public void load() {
        Connection connection = getConnection();
        try(Statement statement = connection.createStatement()) {
            for (String tableCreationStatement : SQL_SETUP_STATEMENTS) {
                statement.execute(tableCreationStatement);
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "An error occurred creating tables on the MySQL database: ", e);
        }
        initialize();
    }

    @Override
    public void backup() {
        plugin.getLogger().info("Remember to make backups of your HuskHomes Database before updating HuskHomes!");
    }
}
