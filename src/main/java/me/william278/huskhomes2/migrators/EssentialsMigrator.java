package me.william278.huskhomes2.migrators;

import me.william278.huskhomes2.HuskHomes;
import me.william278.huskhomes2.MessageManager;
import me.william278.huskhomes2.data.DataManager;
import me.william278.huskhomes2.teleport.SettingHandler;
import me.william278.huskhomes2.teleport.TeleportManager;
import me.william278.huskhomes2.teleport.points.Home;
import me.william278.huskhomes2.teleport.points.TeleportationPoint;
import me.william278.huskhomes2.teleport.points.Warp;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import static org.bukkit.configuration.file.YamlConfiguration.loadConfiguration;

public class EssentialsMigrator {

    private static final HuskHomes plugin = HuskHomes.getInstance();

    public static void migrate() {
        migrate(null, HuskHomes.getSettings().getServerID());
    }

    // Migrate data from EssentialsX
    public static void migrate(String targetWorld, String targetServer) {
        final String serverID = targetServer;
        final File essentialsDataFolder = new File(Bukkit.getWorldContainer() + File.separator + "plugins" + File.separator + "Essentials");

        if (essentialsDataFolder.exists()) {
            plugin.getLogger().info("Essentials plugin data found!");
            plugin.getLogger().info("Started Migration from EssentialsX...\n");

            // Migrate user data
            File essentialsPlayerDataFolder = new File(Bukkit.getWorldContainer() + File.separator + "plugins" + File.separator + "Essentials" + File.separator + "userdata");
            if (essentialsPlayerDataFolder.exists()) {
                plugin.getLogger().info("Migrating user and home data from Essentials...\n");

                File[] playerFiles = essentialsPlayerDataFolder.listFiles();
                if (playerFiles != null) {

                    for (File playerFile : playerFiles) {
                        String uuidS = playerFile.getName().split("\\.")[0];
                        UUID uuid = UUID.fromString(uuidS);
                        FileConfiguration playerFileConfig = loadConfiguration(playerFile);
                        String playerName = playerFileConfig.getString("lastAccountName");
                        plugin.getLogger().info("Migrating user data for " + playerName);
                        try {
                            Connection connection = HuskHomes.getConnection();
                            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                                try {
                                    if (!DataManager.playerExists(uuid, connection)) {
                                        DataManager.createPlayer(uuid, playerName, connection);
                                    }
                                    ConfigurationSection homesSection = playerFileConfig.getConfigurationSection("homes");
                                    if (homesSection != null) {
                                        Set<String> essentialsHomes = homesSection.getKeys(false);
                                        for (String homeName : essentialsHomes) {
                                            try {
                                                final String worldName = playerFileConfig.getString("homes." + homeName + ".world");
                                                if (targetWorld != null) {
                                                    if (!targetWorld.equalsIgnoreCase(worldName)) {
                                                        continue;
                                                    }
                                                }
                                                final double x = playerFileConfig.getDouble("homes." + homeName + ".x");
                                                final double y = playerFileConfig.getDouble("homes." + homeName + ".y");
                                                final double z = playerFileConfig.getDouble("homes." + homeName + ".z");
                                                final float pitch = (float) playerFileConfig.getDouble("homes." + homeName + ".pitch");
                                                final float yaw = (float) playerFileConfig.getDouble("homes." + homeName + ".yaw");
                                                final String homeDescription = MessageManager.getRawMessage("home_default_description", playerName);

                                                DataManager.addHome(new Home(new TeleportationPoint(worldName, x, y, z, yaw, pitch, serverID),
                                                        playerName, uuidS, homeName, homeDescription, false), uuid, connection);

                                                plugin.getLogger().info("→ Migrated home " + homeName);
                                            } catch (NullPointerException | IllegalArgumentException e) {
                                                plugin.getLogger().warning("✖ Failed to migrate home " + homeName);
                                            }
                                        }
                                    }
                                } catch (SQLException e) {
                                    plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred migrating Essentials home data.", e);
                                }
                            });

                        } catch (NullPointerException e) {
                            plugin.getLogger().info("→ No Essentials home data was found for " + playerName);
                        }
                    }
                }
                plugin.getLogger().info("Finished migrating homes from Essentials!\n");
            }

            // Migrate warps
            File essentialsWarpsFolder = new File(Bukkit.getWorldContainer() + File.separator + "plugins" + File.separator + "Essentials" + File.separator + "warps");
            if (essentialsWarpsFolder.exists()) {
                plugin.getLogger().info("Migrating warps from Essentials...\n");
                final File[] warpFiles = essentialsWarpsFolder.listFiles();

                if (warpFiles != null) {
                    Connection connection = HuskHomes.getConnection();
                    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                        for (File warpFile : warpFiles) {
                            try {
                                final FileConfiguration warpFileConfig = loadConfiguration(warpFile);
                                final String warpName = warpFileConfig.getString("name");
                                final String worldName = warpFileConfig.getString("world");
                                if (targetWorld != null) {
                                    if (!targetWorld.equalsIgnoreCase(worldName)) {
                                        continue;
                                    }
                                }
                                final double x = warpFileConfig.getDouble("x");
                                final double y = warpFileConfig.getDouble("y");
                                final double z = warpFileConfig.getDouble("z");
                                final float yaw = (float) warpFileConfig.getDouble("yaw");
                                final float pitch = (float) warpFileConfig.getDouble("pitch");

                                DataManager.addWarp(new Warp(new TeleportationPoint(worldName, x, y, z, yaw, pitch, serverID),
                                        warpName, MessageManager.getRawMessage("warp_default_description")), connection);
                                plugin.getLogger().info("→ Migrated warp " + warpName);
                            } catch (NullPointerException | IllegalArgumentException e) {
                                plugin.getLogger().warning("✖ Failed to migrate warp " + warpFile.getName());
                            } catch (SQLException e) {
                                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred migrating Essentials warp data!", e);
                                return;
                            }
                        }
                    });
                }
                plugin.getLogger().info("Finished migrating warps from Essentials!\n");
            }

            // Migrate /spawn position
            if (TeleportManager.getSpawnLocation() == null && HuskHomes.getSettings().doCrossServerSpawn()) {
                File essentialsSpawnData = new File(Bukkit.getWorldContainer() + File.separator + "plugins" + File.separator + "Essentials" + File.separator + "spawn.yml");
                if (essentialsSpawnData.exists()) {
                    plugin.getLogger().info("Migrating /spawn position from Essentials...");

                    FileConfiguration spawnConfig = loadConfiguration(essentialsSpawnData);
                    try {
                        final String worldName = spawnConfig.getString("spawns.all.world");
                        final double x = spawnConfig.getDouble("spawns.all.x");
                        final double y = spawnConfig.getDouble("spawns.all.y");
                        final double z = spawnConfig.getDouble("spawns.all.z");
                        final float yaw = (float) spawnConfig.getDouble("spawns.all.yaw");
                        final float pitch = (float) spawnConfig.getDouble("spawns.all.pitch");

                        Location spawnLocation = new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
                        SettingHandler.setSpawnLocation(spawnLocation);
                        plugin.getLogger().info("→ /spawn position has been migrated!\n");
                    } catch (NullPointerException | IllegalArgumentException e) {
                        plugin.getLogger().warning("✖ Failed to migrate the /spawn position.\n");
                    }
                }
            } else {
                plugin.getLogger().info("/spawn position migration from EssentialsX has been skipped as it has already been set or Cross Server Spawn has been enabled. Please re-set it again manually if you want.");
            }

            plugin.getLogger().info("Finished migrating data from Essentials!\n");

        } else {
            plugin.getLogger().warning("Failed to Migrate from Essentials!");
            Bukkit.getLogger().warning("Could not find Essentials plugin data to migrate!");
        }
    }
}
