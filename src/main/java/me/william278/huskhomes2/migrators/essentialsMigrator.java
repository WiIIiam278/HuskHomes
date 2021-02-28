package me.william278.huskhomes2.migrators;

import me.william278.huskhomes2.HuskHomes;
import me.william278.huskhomes2.objects.Home;
import me.william278.huskhomes2.objects.TeleportationPoint;
import me.william278.huskhomes2.objects.Warp;
import me.william278.huskhomes2.dataManager;
import me.william278.huskhomes2.settingHandler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.Set;
import java.util.UUID;

import static org.bukkit.configuration.file.YamlConfiguration.loadConfiguration;

public class essentialsMigrator {

    // Migrate data from EssentialsX
    public static void migrate() {
        String serverID = HuskHomes.settings.getServerID();
        File essentialsDataFolder = new File(Bukkit.getWorldContainer() + File.separator + "plugins" + File.separator + "Essentials");
        if (essentialsDataFolder.exists()) {
            Bukkit.getLogger().info("Essentials plugin data found!");
            Bukkit.getLogger().info("-- Migration started --");

            // Migrate user data
            File essentialsPlayerDataFolder = new File(Bukkit.getWorldContainer() + File.separator + "plugins" + File.separator + "Essentials" + File.separator + "userdata");
            if (essentialsPlayerDataFolder.exists()) {
                Bukkit.getLogger().info("- Migrating user data and homes -");

                File[] playerFiles = essentialsPlayerDataFolder.listFiles();
                for (File playerFile : playerFiles) {
                    String uuidS = playerFile.getName().replaceFirst("[.][^.]+$", "");
                    UUID uuid = UUID.fromString(uuidS);
                    FileConfiguration playerFileConfig = loadConfiguration(playerFile);
                    String playerName = playerFileConfig.getString("lastAccountName");
                    Bukkit.getLogger().info("Migrating user data for " + playerName);
                    try {
                        Set<String> essentialsHomes = playerFileConfig.getConfigurationSection("homes").getKeys(false);
                        dataManager.createPlayer(uuid, playerName);
                        for (String home : essentialsHomes) {

                            // Get
                            String worldName = playerFileConfig.getString("homes." + home + ".world");
                            double x = playerFileConfig.getDouble("homes." + home + ".x");
                            double y = playerFileConfig.getDouble("homes." + home + ".y");
                            double z = playerFileConfig.getDouble("homes." + home + ".z");
                            float pitch = (float) playerFileConfig.getDouble("homes." + home + ".pitch");
                            float yaw = (float) playerFileConfig.getDouble("homes." + home + ".yaw");

                            Home playerHome = new Home(new TeleportationPoint(worldName, x, y, z, yaw, pitch, serverID), playerName, uuidS, home, playerName + "'s home", false);

                            Bukkit.getLogger().info("> Migrated home " + home);
                            dataManager.addHome(playerHome, uuid);
                        }
                    } catch (NullPointerException e) {
                        Bukkit.getLogger().info("No home data found for " + playerName);
                    }
                }
                Bukkit.getLogger().info("- All homes migrated! -");
            }

            // Migrate warps
            File essentialsWarpsFolder = new File(Bukkit.getWorldContainer() + File.separator + "plugins" + File.separator + "Essentials" + File.separator + "warps");
            if (essentialsWarpsFolder.exists()) {
                Bukkit.getLogger().info("- Migrating warps -");

                File[] warpFiles = essentialsWarpsFolder.listFiles();
                for (File warpFile : warpFiles) {
                    FileConfiguration warpFileConfig = loadConfiguration(warpFile);
                    try {
                        String warpName = warpFileConfig.getString("name");
                        String worldName = warpFileConfig.getString("world");
                        double x = warpFileConfig.getDouble("x");
                        double y = warpFileConfig.getDouble("y");
                        double z = warpFileConfig.getDouble("z");
                        float yaw = (float) warpFileConfig.getDouble("yaw");
                        float pitch = (float) warpFileConfig.getDouble("pitch");

                        Warp warp = new Warp(new TeleportationPoint(worldName, x, y, z, yaw, pitch, serverID), warpName, "A publicly accessible warp");
                        dataManager.addWarp(warp);

                        Bukkit.getLogger().info("Migrated warp \"" + warpName + "\"");
                    } catch (NullPointerException e) {
                        Bukkit.getLogger().warning("Error obtaining a warp's data!");
                    }
                }
                Bukkit.getLogger().info("- All warps migrated! -");
            }

            // Migrate /spawn position
            File essentialsSpawnData = new File(Bukkit.getWorldContainer() + File.separator + "plugins" + File.separator + "Essentials" + File.separator + "spawn.yml");
            if (essentialsSpawnData.exists()) {
                Bukkit.getLogger().info("Migrating /spawn position");

                FileConfiguration spawnConfig = loadConfiguration(essentialsSpawnData);
                try {
                    String worldName = spawnConfig.getString("spawns.all.world");
                    double x = spawnConfig.getDouble("spawns.all.x");
                    double y = spawnConfig.getDouble("spawns.all.y");
                    double z = spawnConfig.getDouble("spawns.all.z");
                    float yaw = (float) spawnConfig.getDouble("spawns.all.yaw");
                    float pitch = (float) spawnConfig.getDouble("spawns.all.pitch");

                    Location spawnLocation = new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
                    settingHandler.setSpawnLocation(spawnLocation);
                    Bukkit.getLogger().info("/spawn position has been migrated!");
                } catch (Exception e) {
                    Bukkit.getLogger().warning("Error obtaining /spawn position!");
                }
            }

            Bukkit.getLogger().info("-- Migration completed! --");

        } else {
            Bukkit.getLogger().severe("-- Migration failed! --");
            Bukkit.getLogger().warning("Could not find Essentials plugin data to migrate!");
        }
    }
}
