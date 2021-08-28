package me.william278.huskhomes2.migrators;

import me.william278.huskhomes2.HuskHomes;
import org.bukkit.Bukkit;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;

public class UpgradeDatabase {

    private static final HuskHomes plugin = HuskHomes.getInstance();

    private static final String[] mySQLUpgradeStatements = {
            "ALTER TABLE " + HuskHomes.getSettings().getPlayerDataTable()
                    + " ADD `is_ignoring_requests` boolean NOT NULL DEFAULT 0, "
                    + "ADD `offline_location_id` integer NULL DEFAULT NULL, "
                    + "ADD FOREIGN KEY (`offline_location_id`) REFERENCES " + HuskHomes.getSettings().getLocationsDataTable() + " (`location_id`) ON DELETE SET NULL ON UPDATE NO ACTION;"};

    private static final String[] SQLiteUpgradeStatements = {
            "ALTER TABLE " + HuskHomes.getSettings().getPlayerDataTable() + " ADD `is_ignoring_requests` boolean NOT NULL DEFAULT 0;",

            "ALTER TABLE " + HuskHomes.getSettings().getPlayerDataTable() + " ADD `offline_location_id` integer NULL DEFAULT NULL;",

            "CREATE TABLE " + HuskHomes.getSettings().getPlayerDataTable() + "_dg_tmp (" +
                    "`player_id` integer NOT NULL PRIMARY KEY," +
                    "`user_uuid` char(36) NOT NULL UNIQUE," +
                    "`username` varchar(16) NOT NULL," +
                    "`home_slots` integer NOT NULL," +
                    "`rtp_cooldown` integer NOT NULL," +
                    "`is_teleporting` boolean NOT NULL," +
                    "`dest_location_id` integer REFERENCES " + HuskHomes.getSettings().getLocationsDataTable() + " ON DELETE SET NULL," +
                    "`last_location_id` integer REFERENCES " + HuskHomes.getSettings().getLocationsDataTable() + " ON DELETE SET NULL," +
                    "`offline_location_id` integer DEFAULT NULL CONSTRAINT " + HuskHomes.getSettings().getPlayerDataTable() + "_" + HuskHomes.getSettings().getLocationsDataTable() + "_location_id_fk REFERENCES " + HuskHomes.getSettings().getLocationsDataTable() + "," +
                    "`is_ignoring_requests` boolean DEFAULT false NOT NULL" +
                    ");",

            "INSERT INTO " + HuskHomes.getSettings().getPlayerDataTable() + "_dg_tmp(player_id, user_uuid, username, home_slots, rtp_cooldown, is_teleporting, dest_location_id, last_location_id, offline_location_id, is_ignoring_requests) select player_id, user_uuid, username, home_slots, rtp_cooldown, is_teleporting, dest_location_id, last_location_id, offline_location_id, is_ignoring_requests FROM " + HuskHomes.getSettings().getPlayerDataTable() + ";",

            "DROP TABLE " + HuskHomes.getSettings().getPlayerDataTable() + ";",

            "ALTER TABLE " + HuskHomes.getSettings().getPlayerDataTable() + "_dg_tmp RENAME TO " + HuskHomes.getSettings().getPlayerDataTable() + ";"
    };

    // Upgrade the database system if the config file version is not high enough
    public static void upgradeDatabase() {
        plugin.reloadConfig();
        if (plugin.getConfig().getInt("config_file_version", 1) <= 6) {
            plugin.getLogger().info("Database upgrade needed: Adding logout position tracking and ignoring request data...");
            HuskHomes.backupDatabase(); // Backup database before upgrades are carried out!
            String[] statements = SQLiteUpgradeStatements;
            if (HuskHomes.getSettings().getDatabaseType().equalsIgnoreCase("mysql")) {
                statements = mySQLUpgradeStatements;
            }
            int i = 1;
            for (String statement : statements) {
                try (PreparedStatement tableUpdateStatement = HuskHomes.getConnection().prepareStatement(statement)) {
                    tableUpdateStatement.execute();
                    plugin.getLogger().info("Database upgrade in progress... (" + i + "/" + statements.length + ")");
                    i++;
                } catch (SQLException e) {
                    plugin.getLogger().info("Skipped performing the database upgrade: " + e.getCause() + ". This might be because another server on your HuskHomes network already carried out the upgrade - in which case you can safely ignore this warning.");
                    e.printStackTrace();
                }
            }

            // Update the config file version
            plugin.getLogger().info("Logout position and ignoring request database upgrade complete! (v7)");
            plugin.getConfig().set("config_file_version", 7);
            plugin.saveConfig();
        }

        if (plugin.getConfig().getInt("config_file_version", 1) <= 7) {
            plugin.getLogger().info("Database upgrade needed: Adding creation timestamps to homes and warps...");
            HuskHomes.backupDatabase(); // Backup database before upgrades are carried out!
            try (PreparedStatement tableUpdateStatement = HuskHomes.getConnection().prepareStatement(
                    "ALTER TABLE " + HuskHomes.getSettings().getHomesDataTable()
                            + " ADD `creation_time` timestamp NULL DEFAULT NULL;")) {
                tableUpdateStatement.execute();
                plugin.getLogger().info("Database upgrade in progress... (1/2)");
            } catch (SQLException e) {
                plugin.getLogger().info("Skipped performing the database upgrade: " + e.getCause() + ". This might be because another server on your HuskHomes network already carried out the upgrade - in which case you can safely ignore this warning.");
                e.printStackTrace();
            }
            try (PreparedStatement tableUpdateStatement = HuskHomes.getConnection().prepareStatement(
                    "ALTER TABLE " + HuskHomes.getSettings().getWarpsDataTable()
                            + " ADD `creation_time` timestamp NULL DEFAULT NULL;")) {
                tableUpdateStatement.execute();
                plugin.getLogger().info("Database upgrade in progress... (2/2)");
            } catch (SQLException e) {
                plugin.getLogger().info("Skipped performing the database upgrade: " + e.getCause() + ". This might be because another server on your HuskHomes network already carried out the upgrade - in which case you can safely ignore this warning.");
                e.printStackTrace();
            }
            plugin.getLogger().info("Creation timestamp upgrade complete! (v8)");

            // Update the config file version
            plugin.getLogger().info("All database upgrades have been completed!");
            plugin.getConfig().set("config_file_version", 8);
            plugin.saveConfig();
        }
    }

}
