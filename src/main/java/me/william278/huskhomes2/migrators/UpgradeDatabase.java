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
        if (plugin.getConfig().getInt("config_file_version", 1) <= 5) {
            plugin.getLogger().info("Detected that the database needs updating. Running database upgrade...");
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try (PreparedStatement tableUpdateStatement = HuskHomes.getConnection().prepareStatement(
                        "ALTER TABLE " + HuskHomes.getSettings().getPlayerDataTable()
                                + " ADD `is_ignoring_requests` boolean NOT NULL DEFAULT 0, "
                                + "`offline_location_id` integer NULL DEFAULT NULL, "
                                + "FOREIGN KEY (`offline_location_id`) REFERENCES " + HuskHomes.getSettings().getLocationsDataTable() + " (`location_id`) ON DELETE SET NULL ON UPDATE NO ACTION;")) {
                    tableUpdateStatement.executeUpdate();
                    plugin.getLogger().info("Database update complete!");
                } catch (SQLException e) {
                    plugin.getLogger().warning("Failed to perform the database upgrade: " + e.getCause() + ". This might be because another server on your HuskHomes network already carried out the upgrade - in which case you can safely ignore this warning.");
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
