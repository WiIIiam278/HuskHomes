package me.william278.huskhomes2.Data.SQLite;

import me.william278.huskhomes2.Main;
import me.william278.huskhomes2.dataManager;

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

    // SQL creation stuff, You can leave the blow stuff untouched.
    public Connection getSQLConnection() {
        File dataFolder = new File(plugin.getDataFolder(), databaseName + ".db");
        if (!dataFolder.exists()) {
            try {
                if (!dataFolder.createNewFile()) {
                    plugin.getLogger().log(Level.SEVERE, "File write error: " + databaseName + ".db");
                }
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "File write error: " + databaseName + ".db");
            }
        }
        try {
            if (connection != null && !connection.isClosed()) {
                return connection;
            }
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + plugin.getDataFolder().getAbsolutePath() + "/" + databaseName + ".db");
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
            s.execute(dataManager.createPlayerTable);
            s.execute(dataManager.createLocationsTable);
            s.execute(dataManager.createHomesTable);
            s.execute(dataManager.createWarpsTable);
            s.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        initialize();
    }
}