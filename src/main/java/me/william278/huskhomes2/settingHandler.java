package me.william278.huskhomes2;

import me.william278.huskhomes2.Objects.Home;
import me.william278.huskhomes2.Objects.SetHomeConditions;
import me.william278.huskhomes2.Objects.Warp;
import me.william278.huskhomes2.Objects.SetWarpConditions;
import org.bukkit.Location;
import org.bukkit.entity.Player;

// This class handles setting homes, warps and the spawn location.
public class settingHandler {

    // Set a home at the specified position
    public static void setHome(Location location, Player player, String name) {
        SetHomeConditions setHomeConditions = new SetHomeConditions(player, name);
        if (setHomeConditions.areConditionsMet()) {
            dataManager.addHome(new Home(location, Main.settings.getServerID(), player, name, false), player);
            messageManager.sendMessage(player, "set_home_success", name);
        } else {
            if (setHomeConditions.getConditionsNotMetReason().equals("error_set_home_maximum_homes")) {
                messageManager.sendMessage(player, "error_set_home_maximum_homes", Integer.toString(Main.settings.getMaximumHomes()));
            } else {
                messageManager.sendMessage(player, setHomeConditions.getConditionsNotMetReason());
            }
        }
    }

    // Set a home at the specified position
    public static void setWarp(Location location, Player player, String name) {
        SetWarpConditions setWarpConditions = new SetWarpConditions(name);
        if (setWarpConditions.areConditionsMet()) {
            dataManager.addWarp(new Warp(location, Main.settings.getServerID(), name));
            messageManager.sendMessage(player, "set_warp_success", name);
        } else {
            messageManager.sendMessage(player, setWarpConditions.getConditionsNotMetReason());
        }
    }

    // Delete a home
    public static void deleteHome(Player player, String homeName) {
        if (dataManager.homeExists(player, homeName)) {
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
        } else {
            messageManager.sendMessage(player, "error_warp_invalid");
        }
    }
}
