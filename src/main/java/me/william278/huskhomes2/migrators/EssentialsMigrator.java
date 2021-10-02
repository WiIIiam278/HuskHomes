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
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
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
    public static void migrate(final String worldFilter, final String targetServer) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            HuskHomes.backupDatabase();
            final File essentialsDataFolder = new File(Bukkit.getWorldContainer() + File.separator + "plugins" + File.separator + "Essentials");

            if (essentialsDataFolder.exists()) {
                plugin.getLogger().info("Essentials plugin data found!");
                if (worldFilter != null) {
                    plugin.getLogger().info("Started Filtered Migration from EssentialsX:\n• World to migrate from: " + worldFilter + "\n• Target server: " + targetServer);
                } else {
                    plugin.getLogger().info("Started Migration from EssentialsX...\n");
                }

                // Migrate user data
                File essentialsPlayerDataFolder = new File(Bukkit.getWorldContainer() + File.separator + "plugins" + File.separator + "Essentials" + File.separator + "userdata");
                if (essentialsPlayerDataFolder.exists()) {
                    File[] playerFiles = essentialsPlayerDataFolder.listFiles();
                    if (playerFiles != null) {
                        for (File playerFile : playerFiles) {
                            try {
                                final String fileName = playerFile.getName();
                                final UUID playerUUID = UUID.fromString(fileName.substring(0, fileName.lastIndexOf(".")));
                                FileConfiguration playerFileConfig = loadConfiguration(playerFile);
                                final String playerName = playerFileConfig.getString("last-account-name", playerFileConfig.getString("lastAccountName"));
                                try {
                                    try (Connection connection = HuskHomes.getConnection()) {
                                        if (!DataManager.playerExists(playerUUID, connection)) {
                                            DataManager.createPlayer(playerUUID, playerName, connection);
                                        }
                                        final ConfigurationSection homesSection = playerFileConfig.getConfigurationSection("homes");
                                        if (homesSection != null) {
                                            Set<String> essentialsHomes = homesSection.getKeys(false);
                                            for (String homeName : essentialsHomes) {
                                                try {
                                                    String worldID = playerFileConfig.getString("homes." + homeName + ".world");
                                                    try {
                                                        final UUID worldUUID = UUID.fromString(worldID);
                                                        final World world = Bukkit.getWorld(worldUUID);
                                                        if (world != null) {
                                                            worldID = world.getName();
                                                        } else {
                                                            plugin.getLogger().warning("✖ Could not migrate " + homeName + " as the world value was a UUID and is not loaded on this server.");
                                                            continue;
                                                        }
                                                    } catch (IllegalArgumentException ignored) { }
                                                    if (worldFilter != null) {
                                                        if (!worldFilter.equalsIgnoreCase(worldID)) {
                                                            continue;
                                                        }
                                                    }
                                                    final double x = playerFileConfig.getDouble("homes." + homeName + ".x");
                                                    final double y = playerFileConfig.getDouble("homes." + homeName + ".y");
                                                    final double z = playerFileConfig.getDouble("homes." + homeName + ".z");
                                                    final float pitch = (float) playerFileConfig.getDouble("homes." + homeName + ".pitch");
                                                    final float yaw = (float) playerFileConfig.getDouble("homes." + homeName + ".yaw");
                                                    final String homeDescription = MessageManager.getRawMessage("home_default_description", playerName);

                                                    if (DataManager.homeExists(playerName, homeName, connection)) {
                                                        plugin.getLogger().warning("✖ Failed to migrate home " + homeName + " (Already exists!)");
                                                        continue;
                                                    }
                                                    DataManager.addHome(new Home(new TeleportationPoint(worldID, x, y, z, yaw, pitch, targetServer),
                                                            playerName, playerUUID, homeName, homeDescription, false, Instant.now().getEpochSecond()), playerUUID, connection);

                                                    plugin.getLogger().info("→ Migrated home " + homeName);
                                                } catch (NullPointerException | IllegalArgumentException e) {
                                                    plugin.getLogger().warning("✖ Failed to migrate home " + homeName + "!");
                                                    e.printStackTrace();
                                                }
                                            }
                                        }
                                    } catch (SQLException e) {
                                        plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred migrating Essentials home data.", e);
                                    }

                                } catch (NullPointerException ignored) { }
                            } catch (IllegalArgumentException ignored) { }
                        }
                    }
                }

                // Migrate warps
                File essentialsWarpsFolder = new File(Bukkit.getWorldContainer() + File.separator + "plugins" + File.separator + "Essentials" + File.separator + "warps");
                if (essentialsWarpsFolder.exists()) {
                    final File[] warpFiles = essentialsWarpsFolder.listFiles();

                    if (warpFiles != null) {
                        for (File warpFile : warpFiles) {
                            try (Connection connection = HuskHomes.getConnection()) {
                                final FileConfiguration warpFileConfig = loadConfiguration(warpFile);
                                final String warpName = warpFileConfig.getString("name");
                                String worldID = warpFileConfig.getString("world");
                                try {
                                    final UUID worldUUID = UUID.fromString(worldID);
                                    final World world = Bukkit.getWorld(worldUUID);
                                    if (world != null) {
                                        worldID = world.getName();
                                    } else {
                                        plugin.getLogger().warning("✖ Could not migrate " + warpName + " as the world value was a UUID and is not loaded on this server.");
                                        continue;
                                    }
                                } catch (IllegalArgumentException ignored) { }
                                if (worldFilter != null) {
                                    if (!worldFilter.equalsIgnoreCase(worldID)) {
                                        continue;
                                    }
                                }
                                final double x = warpFileConfig.getDouble("x");
                                final double y = warpFileConfig.getDouble("y");
                                final double z = warpFileConfig.getDouble("z");
                                final float yaw = (float) warpFileConfig.getDouble("yaw");
                                final float pitch = (float) warpFileConfig.getDouble("pitch");

                                if (DataManager.warpExists(warpName, connection)) {
                                    plugin.getLogger().warning("✖ Failed to migrate warp " + warpName + " (Already exists!)");
                                    continue;
                                }

                                DataManager.addWarp(new Warp(new TeleportationPoint(worldID, x, y, z, yaw, pitch, targetServer),
                                        warpName, MessageManager.getRawMessage("warp_default_description"), Instant.now().getEpochSecond()), connection);
                                plugin.getLogger().info("→ Migrated warp " + warpName);
                            } catch (NullPointerException | IllegalArgumentException e) {
                                plugin.getLogger().warning("✖ Failed to migrate warp " + warpFile.getName());
                            } catch (SQLException e) {
                                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred migrating Essentials warp data!", e);
                                return;
                            }
                        }
                    }
                }

                // Migrate /spawn position
                if (TeleportManager.getSpawnLocation() == null && HuskHomes.getSettings().doCrossServerSpawn()) {
                    File essentialsSpawnData = new File(Bukkit.getWorldContainer() + File.separator + "plugins" + File.separator + "Essentials" + File.separator + "spawn.yml");
                    if (essentialsSpawnData.exists()) {
                        plugin.getLogger().info("Migrating /spawn position from Essentials...");

                        FileConfiguration spawnConfig = loadConfiguration(essentialsSpawnData);
                        try {
                            String worldID = spawnConfig.getString("spawns.all.world");
                            try {
                                final UUID worldUUID = UUID.fromString(worldID);
                                final World world = Bukkit.getWorld(worldUUID);
                                if (world != null) {
                                    worldID = world.getName();
                                } else {
                                    plugin.getLogger().warning("✖ Could not migrate the spawn position as the world value was a UUID and is not loaded on this server.");
                                    return;
                                }
                            } catch (IllegalArgumentException ignored) { }
                            if (worldFilter != null) {
                                if (!worldFilter.equalsIgnoreCase(worldID)) {
                                    plugin.getLogger().warning("✖ Did not migrate the /spawn position as the world filter did not match.");
                                    return;
                                }
                            }
                            final double x = spawnConfig.getDouble("spawns.all.x");
                            final double y = spawnConfig.getDouble("spawns.all.y");
                            final double z = spawnConfig.getDouble("spawns.all.z");
                            final float yaw = (float) spawnConfig.getDouble("spawns.all.yaw");
                            final float pitch = (float) spawnConfig.getDouble("spawns.all.pitch");

                            Location spawnLocation = new Location(Bukkit.getWorld(worldID), x, y, z, yaw, pitch);
                            SettingHandler.setSpawnLocation(spawnLocation);
                            plugin.getLogger().info("→ /spawn position has been migrated!\n");
                        } catch (NullPointerException | IllegalArgumentException e) {
                            plugin.getLogger().warning("✖ Failed to migrate the /spawn position.\n");
                        }
                    }
                } else {
                    plugin.getLogger().info("✖ /spawn position migration from EssentialsX has been skipped as it has already been set or Cross Server Spawn has been enabled. Please re-set it again manually if you want.");
                }
            } else {
                plugin.getLogger().warning("Failed to Migrate from Essentials!");
                Bukkit.getLogger().warning("Could not find Essentials plugin data to migrate!");
            }
        });
    }
}
