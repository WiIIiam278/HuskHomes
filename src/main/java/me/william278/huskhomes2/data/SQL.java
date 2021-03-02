package me.william278.huskhomes2.data;

import me.william278.huskhomes2.HuskHomes;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

/*
    TODO Split into SQLite, MySQL and abstract SQL classes
    Even tho both are using pretty much the same syntax and stuff, their initializations are too different and should
    not co-exist in the same class.
 */
public class SQL extends Database {

    private static final String SQLITE_DATABASE_NAME = "HuskHomesData";

    private Connection connection;

    private void setConnection(Connection connection) {
        this.connection = connection;
    }

    public SQL(HuskHomes instance) {
        super(instance);
    }

    // Initialise connection via mySQL
    private void getMySQLConnection() {
        String host = HuskHomes.getSettings().getMySQLhost();
        int port = HuskHomes.getSettings().getMySQLport();
        String database = HuskHomes.getSettings().getMySQLdatabase();
        String username = HuskHomes.getSettings().getMySQLusername();
        String password = HuskHomes.getSettings().getMySQLpassword();

        try {
            synchronized (HuskHomes.getInstance()) {
                Class.forName("com.mysql.jdbc.Driver");
                setConnection(DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database + "?autoReconnect=true&useSSL=false", username, password));
            }
        } catch (SQLException e) {
            HuskHomes.disablePlugin("[!] Could not connect to the mySQL Database with the credentials provided!\n[!] Check that your host IP address and port are valid and that your username and password are valid and that the user has the correct access permissions on the database.");
        } catch (ClassNotFoundException e) {
            HuskHomes.disablePlugin("[!] A critical exception occurred when attempting to establish a connection to the mySQL database!");
            e.printStackTrace();
        }
    }

    // Initialise connection via SQLite
    private void getSQLiteConnection() {
        File dataFolder = new File(plugin.getDataFolder(), SQLITE_DATABASE_NAME + ".db");
        if (!dataFolder.exists()) {
            try {
                if (!dataFolder.createNewFile()) {
                    plugin.getLogger().log(Level.SEVERE, "Failed to write new file: " + SQLITE_DATABASE_NAME + ".db (file already exists)");
                }
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "An error occurred writing a file: " + SQLITE_DATABASE_NAME + ".db (" + e.getCause() + ")");
            }
        }
        try {
            Class.forName("org.sqlite.JDBC");
            setConnection(DriverManager.getConnection("jdbc:sqlite:" + plugin.getDataFolder().getAbsolutePath() + "/" + SQLITE_DATABASE_NAME + ".db"));
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, "An exception occurred initialising the SQLite database", ex);
        } catch (ClassNotFoundException ex) {
            plugin.getLogger().log(Level.SEVERE, "You need the SQLite JBDC library; please download and place this in the /lib folder.");
        }
    }

    public Connection getSQLConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                String dataStorageType = HuskHomes.getSettings().getStorageType().toLowerCase();
                switch (dataStorageType) {
                    case "mysql":
                        getMySQLConnection();
                        break;
                    case "sqlite":
                        getSQLiteConnection();
                        break;
                    default:
                        plugin.getLogger().log(Level.WARNING, "An invalid data storage type was specified in config.yml; defaulting to SQLite");
                        getSQLiteConnection();
                        break;
                }
            }
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.WARNING, "An error occurred checking the status of the SQL connection");
        }
        return connection;
    }

    public void load() {
        connection = getSQLConnection();

        try {
            Statement s = connection.createStatement();
            s.execute(DataManager.createLocationsTable);
            s.execute(DataManager.createPlayerTable);
            s.execute(DataManager.createHomesTable);
            s.execute(DataManager.createWarpsTable);
            s.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        initialize();
    }
}