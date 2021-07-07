package me.william278.huskhomes2.migrators;

import me.william278.huskhomes2.HuskHomes;
import org.bukkit.Bukkit;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;

public class UpgradeDatabase {

    private static final HuskHomes plugin = HuskHomes.getInstance();

    // Upgrade the database system if the config file version is not high enough
    public static void upgradeDatabase() {
        plugin.reloadConfig();
        if (plugin.getConfig().getInt("config_file_version", 1) > 5) {
            plugin.getLogger().info("Detected that the database needs updating. Running database upgrade...");
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try (PreparedStatement statement = HuskHomes.getConnection().prepareStatement("ALTER TABLE " + HuskHomes.getSettings().getPlayerDataTable() + " ADD `is_ignoring_requests` boolean NOT NULL DEFAULT 0")) {
                    statement.executeUpdate();
                } catch (SQLException e) {
                    plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred upgrading the database format!", e);
                    plugin.getLogger().info("Database update complete!");
                } finally {
                    // Update the config file version
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        plugin.getConfig().set("config_file_version", 6);
                        plugin.saveConfig();
                    });
                }
            });
        }
    }

}
