package me.william278.huskhomes2.data.SQL;

import me.william278.huskhomes2.HuskHomes;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;

public abstract class Database {
    protected HuskHomes plugin;
    private Connection connection;

    public Database(HuskHomes instance) {
        plugin = instance;
    }

    public abstract Connection getConnection();

    public abstract void load();

    public void initialize() {
        connection = getConnection();

        // Test the retrieved connection; throw an error if it fails
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM " + HuskHomes.getSettings().getPlayerDataTable() + ";");
            ResultSet rs = ps.executeQuery();
            close(ps, rs);
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, "Failed to retrieve Database connection: ", ex);
        }
    }

    // Close the mySQL connection
    public void close(PreparedStatement ps, ResultSet rs) {
        try {
            if (ps != null)
                ps.close();
            if (rs != null)
                rs.close();
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, "Failed to close the Database connection: ", ex);
        }
    }


}
