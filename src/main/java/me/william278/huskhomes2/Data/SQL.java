package me.william278.huskhomes2.Data;

import me.william278.huskhomes2.Main;
import me.william278.huskhomes2.dataManager;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

public class SQL extends Database {

    final String SQLiteDatabaseName = "HuskHomesData";

    public SQL(Main instance) {
        super(instance);
    }

    // Initialise connection via mySQL
    private Connection getMySQLConnection() {
        String host = Main.settings.getMySQLhost();
        int port = Main.settings.getMySQLport();
        String database = Main.settings.getMySQLdatabase();
        String username = Main.settings.getMySQLusername();
        String password = Main.settings.getMySQLpassword();

        try {
            synchronized (Main.getInstance()) {
                Class.forName("com.mysql.jdbc.Driver");
                return DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database + "?autoReconnect=true&useSSL=false", username, password);
            }
        } catch (SQLException e) {
            Main.disablePlugin("[!] Could not connect to the mySQL Database with the credentials provided!\n[!] Check that your host IP address and port are valid and that your username and password are valid and that the user has the correct access permissions on the database.");
        } catch (ClassNotFoundException e) {
            Main.disablePlugin("[!] A critical exception occurred when attempting to establish a connection to the mySQL database!");
            e.printStackTrace();
        }
        return null;
    }

    // Initialise connection via SQLite
    private Connection getSQLiteConnection() {
        File dataFolder = new File(plugin.getDataFolder(), SQLiteDatabaseName + ".db");
        if (!dataFolder.exists()) {
            try {
                if (!dataFolder.createNewFile()) {
                    plugin.getLogger().log(Level.SEVERE, "File write error: " + SQLiteDatabaseName + ".db");
                }
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "File write error: " + SQLiteDatabaseName + ".db");
            }
        }
        try {
            if (connection != null && !connection.isClosed()) {
                return connection;
            }
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + plugin.getDataFolder().getAbsolutePath() + "/" + SQLiteDatabaseName + ".db");
            return connection;
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, "An exception occurred initialising the SQLite database", ex);
        } catch (ClassNotFoundException ex) {
            plugin.getLogger().log(Level.SEVERE, "You need the SQLite JBDC library; please download and place this in the /lib folder.");
        }
        return null;
    }

    public Connection getSQLConnection() {
        String dataStorageType = Main.settings.getStorageType().toLowerCase();
        switch (dataStorageType) {
            case "mysql":
                return getMySQLConnection();
            case "sqlite":
                return getSQLiteConnection();
            default:
                Bukkit.getLogger().warning("An invalid data storage type was specified in config.yml; defaulting to SQLite");
                return getSQLiteConnection();
        }
    }

    public void load() {
        connection = getSQLConnection();

        try {
            Statement s = connection.createStatement();
            s.execute(dataManager.createLocationsTable);
            s.execute(dataManager.createPlayerTable);
            s.execute(dataManager.createHomesTable);
            s.execute(dataManager.createWarpsTable);
            s.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        initialize();
    }
}