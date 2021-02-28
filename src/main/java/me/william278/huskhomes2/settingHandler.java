package me.william278.huskhomes2;

import me.william278.huskhomes2.api.events.PlayerDeleteHomeEvent;
import me.william278.huskhomes2.api.events.PlayerDeleteWarpEvent;
import me.william278.huskhomes2.api.events.PlayerSetHomeEvent;
import me.william278.huskhomes2.api.events.PlayerSetWarpEvent;
import me.william278.huskhomes2.commands.tab.homeTabCompleter;
import me.william278.huskhomes2.commands.tab.publicHomeTabCompleter;
import me.william278.huskhomes2.commands.tab.warpTabCompleter;
import me.william278.huskhomes2.integrations.dynamicMap;
import me.william278.huskhomes2.integrations.economy;
import me.william278.huskhomes2.objects.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

// This class handles setting homes, warps and the spawn location.
public class settingHandler {

    private static final HuskHomes plugin = HuskHomes.getInstance();

    // Set a home at the specified position
    public static void setHome(Location location, Player player, String name) {
        SetHomeConditions setHomeConditions = new SetHomeConditions(player, name);
        if (setHomeConditions.areConditionsMet()) {
            Home home = new Home(location, HuskHomes.settings.getServerID(), player, name, false);
            PlayerSetHomeEvent event = new PlayerSetHomeEvent(player, home);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return;
            }
            dataManager.addHome(home, player);
            messageManager.sendMessage(player, "set_home_success", name);
            homeTabCompleter.updatePlayerHomeCache(player);
        } else {
            switch (setHomeConditions.getConditionsNotMetReason()) {
                case "error_set_home_maximum_homes":
                    messageManager.sendMessage(player, "error_set_home_maximum_homes", Integer.toString(HuskHomes.settings.getMaximumHomes()));
                    return;
                case "error_insufficient_funds":
                    messageManager.sendMessage(player, "error_insufficient_funds", economy.format(HuskHomes.settings.getSetHomeCost()));
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
            Warp warp = new Warp(location, HuskHomes.settings.getServerID(), name);
            PlayerSetWarpEvent event = new PlayerSetWarpEvent(player, warp);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return;
            }
            dataManager.addWarp(warp);
            messageManager.sendMessage(player, "set_warp_success", name);
            if (HuskHomes.settings.doDynmap() && HuskHomes.settings.showWarpsOnDynmap()) {
                dynamicMap.addDynamicMapMarker(warp);
            }
            warpTabCompleter.updateWarpsTabCache();
        } else {
            messageManager.sendMessage(player, setWarpConditions.getConditionsNotMetReason());
        }
    }

    // Delete a home
    public static void deleteHome(Player player, String homeName) {
        if (dataManager.homeExists(player, homeName)) {
            Home home = dataManager.getHome(player.getName(), homeName);
            if (home != null) {
                if (home.isPublic()) {
                    // Delete Dynmap marker if it exists & if the home is public
                    if (HuskHomes.settings.doDynmap() && HuskHomes.settings.showPublicHomesOnDynmap()) {
                        dynamicMap.removeDynamicMapMarker(homeName, player.getName());
                    }
                    PlayerDeleteHomeEvent event = new PlayerDeleteHomeEvent(player, home);
                    Bukkit.getPluginManager().callEvent(event);
                    if (event.isCancelled()) {
                        return;
                    }
                    dataManager.deleteHome(homeName, player);
                    publicHomeTabCompleter.updatePublicHomeTabCache();
                } else {
                    dataManager.deleteHome(homeName, player);
                }
                homeTabCompleter.updatePlayerHomeCache(player);
                messageManager.sendMessage(player, "home_deleted", homeName);
            } else {
                messageManager.sendMessage(player, "error_home_invalid", homeName);
            }
        } else {
            messageManager.sendMessage(player, "error_home_invalid", homeName);
        }
    }

    // Delete a warp
    public static void deleteWarp(Player player, String warpName) {
        if (dataManager.warpExists(warpName)) {
            Warp warp = dataManager.getWarp(warpName);
            PlayerDeleteWarpEvent event = new PlayerDeleteWarpEvent(player, warp);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return;
            }
            dataManager.deleteWarp(warpName);
            messageManager.sendMessage(player, "warp_deleted", warpName);
            if (HuskHomes.settings.doDynmap() && HuskHomes.settings.showWarpsOnDynmap()) {
                dynamicMap.removeDynamicMapMarker(warpName);
            }
            warpTabCompleter.updateWarpsTabCache();
        } else {
            messageManager.sendMessage(player, "error_warp_invalid", warpName);
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
        teleportManager.spawnLocation = new TeleportationPoint(location, HuskHomes.settings.getServerID());
    }

    // Update current spawn location from config
    public static void fetchSpawnLocation() {
        teleportManager.spawnLocation = getSpawnLocation();
    }

    // Get spawn location from config
    private static TeleportationPoint getSpawnLocation() {
        String server = HuskHomes.settings.getServerID();
        try {
            FileConfiguration config = plugin.getConfig();
            String worldName = (config.getString("spawn_command.position.world"));
            if (worldName == null || worldName.equals("")) {
                return null;
            }
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
