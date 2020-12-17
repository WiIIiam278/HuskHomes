package me.william278.huskhomes2.Migrators;

import me.william278.huskhomes2.HuskHomes;
import me.william278.huskhomes2.Objects.Home;
import me.william278.huskhomes2.Objects.TeleportationPoint;
import me.william278.huskhomes2.configManager;
import me.william278.huskhomes2.dataManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.sql.*;
import java.util.UUID;

import static org.bukkit.configuration.file.YamlConfiguration.loadConfiguration;

// This class migrates data from HuskHomes 1.5.x to HuskHomes 2.x
public class huskHomes1Migrator {

    public static boolean startupMigrate;
    private static String sourcePlayerTable;
    private static String sourceHomeTable;
    private static final HuskHomes plugin = HuskHomes.getInstance();

    private static void createMigrationSubdirectory() {
        File migrationDirectory = new File(plugin.getDataFolder() + File.separator + "MigratedData");
        if (!migrationDirectory.exists()) {
            if (!migrationDirectory.mkdir()) {
                Bukkit.getLogger().warning("Failed to create migration directory!");
            }
        }
    }

    public static void checkStartupMigration() {
        File configFile = new File(plugin.getDataFolder() + File.separator + "config.yml");
        if (configFile.exists()) {
            FileConfiguration config = loadConfiguration(configFile);
            if (!config.contains("config_file_version") && config.contains("host")) {
                Bukkit.getLogger().info("Detected HuskHomes 1.0 data to migrate!");
                Bukkit.getLogger().info("- Preparing Migration -");
                startupMigrate = true;
                createMigrationSubdirectory();
                if (!configFile.renameTo(new File(plugin.getDataFolder() + File.separator + "MigratedData" + File.separator + "OLD_config.yml"))) {
                    Bukkit.getLogger().warning("Failed to move old config.yml!");
                } else {
                    Bukkit.getLogger().info("Moved old config.yml --> HuskHomes/MigratedData/OLD_config.yml");
                }
                File messagesFile = new File(plugin.getDataFolder() + File.separator + "messages.yml");
                if (messagesFile.exists()) {
                    if (!messagesFile.renameTo(new File(plugin.getDataFolder() + File.separator + "MigratedData" + File.separator + "OLD_messages.yml"))) {
                        Bukkit.getLogger().warning("Failed to move old messages.yml!");
                    } else {
                        Bukkit.getLogger().info("Moved old messages.yml --> HuskHomes/MigratedData/OLD_messages.yml");
                    }
                }
            }
        }
    }

    public void migrateConfig() {
        FileConfiguration sourceConfig = loadConfiguration(new File(plugin.getDataFolder() + File.separator + "MigratedData" + File.separator + "OLD_config.yml"));
        Bukkit.getLogger().info("- Migrating config data -");
        try {
            // Transfer mySQL credentials
            plugin.getConfig().set("data_storage_options.storage_type", "mySQL");
            plugin.getConfig().set("data_storage_options.mysql_credentials.host", sourceConfig.getString("host"));
            plugin.getConfig().set("data_storage_options.mysql_credentials.port", sourceConfig.getInt("port"));
            plugin.getConfig().set("data_storage_options.mysql_credentials.database", sourceConfig.getString("database"));
            plugin.getConfig().set("data_storage_options.mysql_credentials.username", sourceConfig.getString("username"));
            plugin.getConfig().set("data_storage_options.mysql_credentials.password", sourceConfig.getString("password"));

            sourcePlayerTable = sourceConfig.getString("player_data_table");
            sourceHomeTable = sourceConfig.getString("home_data_table");

            // Transfer general settings
            plugin.getConfig().set("general.max_sethomes", sourceConfig.getInt("max_sethomes"));
            plugin.getConfig().set("general.teleport_warmup_time", sourceConfig.getInt("teleport_warmup"));

            // Transfer bungee settings
            plugin.getConfig().set("bungee_options.enable_bungee_mode", sourceConfig.getBoolean("bungee"));
            plugin.getConfig().set("bungee_options.server_id", sourceConfig.getString("server_name"));

            // Transfer rtp command settings
            plugin.getConfig().set("random_teleport_command.enabled", sourceConfig.getBoolean("do_rtp_command"));
            plugin.getConfig().set("random_teleport_command.range", sourceConfig.getInt("rtp_boundary"));

            // Transfer dynmap setting
            plugin.getConfig().set("dynmap_integration.enabled", sourceConfig.getBoolean("do_dynmap"));

            // Transfer economy settings
            plugin.getConfig().set("economy_integration.enabled", sourceConfig.getBoolean("do_economy"));
            plugin.getConfig().set("economy_integration.free_home_slots", sourceConfig.getInt("free_sethomes"));
            plugin.getConfig().set("economy_integration.costs.additional_home_slot", sourceConfig.getDouble("set_home_cost"));
            plugin.getConfig().set("economy_integration.costs.random_teleport", sourceConfig.getDouble("rtp_cost"));
            plugin.getConfig().set("economy_integration.costs.make_home_public", sourceConfig.getDouble("public_home_cost"));

            // Transfer spawn command settings (and location if set)
            plugin.getConfig().set("spawn_command.enabled", sourceConfig.getBoolean("do_spawn_command"));
            if (sourceConfig.contains("spawn_world")) {
                plugin.getConfig().set("spawn_command.position.world", sourceConfig.getBoolean("spawn_world"));
                plugin.getConfig().set("spawn_command.position.x", sourceConfig.getDouble("spawn_x"));
                plugin.getConfig().set("spawn_command.position.y", sourceConfig.getDouble("spawn_y"));
                plugin.getConfig().set("spawn_command.position.z", sourceConfig.getDouble("spawn_z"));
                plugin.getConfig().set("spawn_command.position.yaw", sourceConfig.getDouble("spawn_yaw"));
                plugin.getConfig().set("spawn_command.position.pitch", sourceConfig.getDouble("spawn_pitch"));
            }
            // Save the config at the end
            plugin.saveConfig();

            // Reload settings with new ones
            plugin.reloadConfig();
            configManager.loadConfig();
        } catch (Exception e) {
            Bukkit.getLogger().warning("Failed to migrate data; " + e.getCause() + " when trying to migrate config data");
            startupMigrate = false;
            return;
        }
        Bukkit.getLogger().info("- Config migrations complete! -");
    }

    private Connection getConnection() {
        try {
            synchronized (HuskHomes.getInstance()) {
                Class.forName("com.mysql.jdbc.Driver");
                return DriverManager.getConnection("jdbc:mysql://" + HuskHomes.settings.getMySQLhost() + ":" + HuskHomes.settings.getMySQLport()
                        + "/" + HuskHomes.settings.getMySQLdatabase() + "?autoReconnect=true&useSSL=false", HuskHomes.settings.getMySQLusername(), HuskHomes.settings.getMySQLpassword());
            }
        } catch (SQLException e) {
            return null;
        } catch (ClassNotFoundException e) {
            Bukkit.getLogger().info("A SQL exception occurred when attempting to migrate data! (" + e.getCause() + ")");
            return null;
        }
    }

    public void migratePlayerData() {
        // Migrate player data
        try {
            Connection connection = getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM " + sourcePlayerTable + ";");
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                UUID uuid = UUID.fromString(resultSet.getString("UUID"));
                String username = resultSet.getString("USERNAME");
                int homeSlots = resultSet.getInt("HOME_SLOTS");
                if (!dataManager.playerExists(uuid)) {
                    dataManager.createPlayer(uuid, username, homeSlots);
                }
            }
            connection.close();
        } catch (SQLException playerSQLException) {
            Bukkit.getLogger().info("A SQL exception occurred transferring player data! " + playerSQLException.getCause());
            playerSQLException.printStackTrace();
        }
    }

    public void migrateHomeData() {
        // Migrate player data
        try {
            Connection connection = getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM " + sourceHomeTable + ";");
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                // Get the home details
                String ownerUsername = resultSet.getString("OWNER_NAME");
                String ownerUUID = resultSet.getString("OWNER_UUID");
                String name = resultSet.getString("NAME");
                String description = resultSet.getString("DESCRIPTION");
                boolean isPublic = resultSet.getBoolean("PUBLIC");

                // Get the teleportation point
                String server = resultSet.getString("SERVER");
                String worldName = resultSet.getString("WORLD");
                double x = resultSet.getDouble("X");
                double y = resultSet.getDouble("Y");
                double z = resultSet.getDouble("Z");
                float yaw = resultSet.getFloat("YAW");
                float pitch = resultSet.getFloat("PITCH");
                TeleportationPoint teleportationPoint = new TeleportationPoint(worldName, x, y, z, yaw, pitch, server);

                // Add the home to the database
                if (!dataManager.homeExists(ownerUsername, name)) {
                    Home home = new Home(teleportationPoint, ownerUsername, ownerUUID, name, description, isPublic);
                    dataManager.addHome(home, UUID.fromString(ownerUUID));
                }
            }
            connection.close();
        } catch (SQLException homeSQLException) {
            Bukkit.getLogger().info("A SQL exception occurred transferring home data! " + homeSQLException.getCause());
            homeSQLException.printStackTrace();
        }
    }

}
