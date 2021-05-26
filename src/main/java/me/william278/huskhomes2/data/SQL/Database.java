package me.william278.huskhomes2.data.SQL;

import me.william278.huskhomes2.HuskHomes;
import me.william278.huskhomes2.data.Error;
import me.william278.huskhomes2.teleport.points.Home;
import me.william278.huskhomes2.teleport.points.TeleportationPoint;
import me.william278.huskhomes2.teleport.points.Warp;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import java.util.logging.Level;

/*
    TODO Should not relay on SQL
    Instead should contain abstract getters and setters etc, so some alternative way of storing data may exist
    Current method should possibly go into the SQL class
 */
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
