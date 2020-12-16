package me.william278.huskhomes2;

import me.william278.huskhomes2.Integrations.dynamicMap;
import me.william278.huskhomes2.Integrations.economy;
import me.william278.huskhomes2.Objects.*;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

// This class handles setting homes, warps and the spawn location.
public class settingHandler {

    private static final Main plugin = Main.getInstance();

    // Set a home at the specified position
    public static void setHome(Location location, Player player, String name) {
        SetHomeConditions setHomeConditions = new SetHomeConditions(player, name);
        if (setHomeConditions.areConditionsMet()) {
            Home home = new Home(location, Main.settings.getServerID(), player, name, false);
            dataManager.addHome(home, player);
            messageManager.sendMessage(player, "set_home_success", name);
            if (Main.settings.doDynmap() && Main.settings.showPublicHomesOnDynmap()) {
                dynamicMap.addDynamicMapMarker(home);
            }
        } else {
            switch (setHomeConditions.getConditionsNotMetReason()) {
                case "error_set_home_maximum_homes":
                    messageManager.sendMessage(player, "error_set_home_maximum_homes", Integer.toString(Main.settings.getMaximumHomes()));
                    return;
                case "error_insufficient_funds":
                    messageManager.sendMessage(player, "error_insufficient_funds", economy.format(Main.settings.getSetHomeCost()));
                    return;
                default:
                    messageManager.sendMessage(player, setHomeConditions.getConditionsNotMetReason());
            }
        }
    }

    // Set a home at the specified position
    public static void setWarp(Location location, Player player, String name) {
        SetWarpConditions setWarpConditions = new SetWarpConditions(name);
        if (setWarpConditions.areConditionsMet()) {
            Warp warp = new Warp(location, Main.settings.getServerID(), name);
            dataManager.addWarp(warp);
            messageManager.sendMessage(player, "set_warp_success", name);
            if (Main.settings.doDynmap() && Main.settings.showWarpsOnDynmap()) {
                dynamicMap.addDynamicMapMarker(warp);
            }
        } else {
            messageManager.sendMessage(player, setWarpConditions.getConditionsNotMetReason());
        }
    }

    // Delete a home
    public static void deleteHome(Player player, String homeName) {
        if (dataManager.homeExists(player, homeName)) {
            // Delete dynmap marker if it exists & if the home is public
            if (Main.settings.doDynmap() && Main.settings.showPublicHomesOnDynmap()) {
                if (dataManager.getHome(player.getName(), homeName).isPublic()) {
                    dynamicMap.removeDynamicMapMarker(homeName, player.getName());
                }
            }
            dataManager.deleteHome(homeName, player);
            messageManager.sendMessage(player, "home_deleted", homeName);
        } else {
            messageManager.sendMessage(player, "error_home_invalid", homeName);
        }
    }

    // Delete a warp
    public static void deleteWarp(Player player, String warpName) {
        if (dataManager.warpExists(warpName)) {
            dataManager.deleteWarp(warpName);
            messageManager.sendMessage(player, "warp_deleted");
            if (Main.settings.doDynmap() && Main.settings.showWarpsOnDynmap()) {
                dynamicMap.removeDynamicMapMarker(warpName);
            }
        } else {
            messageManager.sendMessage(player, "error_warp_invalid");
        }
    }

    // Set spawn location
    public static void setSpawnLocation(Location location) {
        // Write the new location to config
        FileConfiguration config = plugin.getConfig();
        config.set("spawn_command.position.world", location.getWorld().getName());
        config.set("spawn_command.position.x", location.getX());
        config.set("spawn_command.position.y", location.getY());
        config.set("spawn_command.position.z", location.getZ());
        config.set("spawn_command.position.yaw", (double) location.getYaw());
        config.set("spawn_command.position.pitch", (double) location.getPitch());
        plugin.saveConfig();

        // Update the current spawn location
        teleportManager.spawnLocation = new TeleportationPoint(location, Main.settings.getServerID());
    }

    // Update current spawn location from config
    public static void fetchSpawnLocation() {
        teleportManager.spawnLocation = getSpawnLocation();
    }

    // Get spawn location from config
    private static TeleportationPoint getSpawnLocation() {
        String server = Main.settings.getServerID();
        try {
            FileConfiguration config = plugin.getConfig();
            String worldName = (config.getString("spawn_command.position.world"));
            double x = (config.getDouble("spawn_command.position.x"));
            double y = (config.getDouble("spawn_command.position.y"));
            double z = (config.getDouble("spawn_command.position.z"));
            float yaw = (float) (config.getDouble("spawn_command.position.yaw"));
            float pitch = (float) (config.getDouble("spawn_command.position.pitch"));
            return new TeleportationPoint(worldName, x, y, z, yaw, pitch, server);
        } catch (Exception e) {
            return null;
        }
    }
}
