package me.william278.huskhomes2;

import me.william278.huskhomes2.teleport.points.Home;
import me.william278.huskhomes2.teleport.points.Warp;
import org.bukkit.entity.Player;

public class EditingHandler {

    public static void showEditHomeOptions(Player p, Home home) {
        MessageManager.sendMessage(p, "edit_home_title", home.getName());
        MessageManager.sendMessage(p, "edit_description", home.getDescription());
        MessageManager.sendMessage(p, "edit_location",
                Integer.toString((int) home.getX()),
                Integer.toString((int) home.getY()),
                Integer.toString((int) home.getZ()));
        MessageManager.sendMessage(p, "edit_world", home.getWorldName());
        if (HuskHomes.getSettings().doBungee()) {
            MessageManager.sendMessage(p, "edit_server", home.getServer());
        }
        if (!home.isPublic()) {
            MessageManager.sendMessage(p, "edit_home_privacy_private");
        } else {
            MessageManager.sendMessage(p, "edit_home_privacy_public");
        }

        if (p.hasPermission("huskhomes.edithome.public")) {
            if (home.isPublic()) {
                MessageManager.sendMessage(p, "edit_home_options_make_private", home.getName());
            } else {
                MessageManager.sendMessage(p, "edit_home_options_make_public", home.getName());
            }
        } else {
            MessageManager.sendMessage(p, "edit_home_options", home.getName());
        }
    }

    public static void showEditWarpOptions(Player p, Warp warp) {
        MessageManager.sendMessage(p, "edit_warp_title", warp.getName());
        MessageManager.sendMessage(p, "edit_description", warp.getDescription());
        MessageManager.sendMessage(p, "edit_location",
                Integer.toString((int) warp.getX()),
                Integer.toString((int) warp.getY()),
                Integer.toString((int) warp.getZ()));
        MessageManager.sendMessage(p, "edit_world", warp.getWorldName());
        if (HuskHomes.getSettings().doBungee()) {
            MessageManager.sendMessage(p, "edit_server", warp.getServer());
        }

        MessageManager.sendMessage(p, "edit_warp_options", warp.getName());
    }

}