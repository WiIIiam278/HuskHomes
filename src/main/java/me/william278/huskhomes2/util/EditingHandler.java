package me.william278.huskhomes2.util;

import de.themoep.minedown.MineDown;
import me.william278.huskhomes2.HuskHomes;
import me.william278.huskhomes2.MessageManager;
import me.william278.huskhomes2.teleport.points.Home;
import me.william278.huskhomes2.teleport.points.SetPoint;
import me.william278.huskhomes2.teleport.points.Warp;
import org.bukkit.entity.Player;

public class EditingHandler {

    public static void showEditHomeOptions(Player p, Home home) {
        if (home.getOwnerUsername().equalsIgnoreCase(p.getName())) {
            MessageManager.sendMessage(p, "edit_home_title", home.getName());
        } else {
            MessageManager.sendMessage(p, "edit_home_title_other", home.getOwnerUsername(), home.getName());
        }
        showPointDetails(p, home);
        if (!home.isPublic()) {
            if (home.getOwnerUsername().equalsIgnoreCase(p.getName())) {
                MessageManager.sendMessage(p, "edit_home_privacy_private");
            } else {
                MessageManager.sendMessage(p, "edit_home_privacy_private_other", home.getOwnerUsername());
            }
        } else {
            MessageManager.sendMessage(p, "edit_home_privacy_public");
        }

        if (p.hasPermission("huskhomes.edithome.public")) {
            if (home.isPublic()) {
                MessageManager.sendMessage(p, "edit_home_options_make_private", home.getOwnerUsername(), home.getName());
            } else {
                MessageManager.sendMessage(p, "edit_home_options_make_public", home.getOwnerUsername(), home.getName());
            }
        } else {
            MessageManager.sendMessage(p, "edit_home_options", home.getOwnerUsername(), home.getName());
        }
    }

    public static void showEditWarpOptions(Player p, Warp warp) {
        MessageManager.sendMessage(p, "edit_warp_title", warp.getName());
        showPointDetails(p, warp);

        MessageManager.sendMessage(p, "edit_warp_options", warp.getName());
    }

    private static void showPointDetails(Player p, SetPoint point) {
        MessageManager.sendMessage(p, "edit_description", MineDown.escape(point.getDescription()
                .replace("]", "\\]")
                .replace("[", "\\[")
                .replace("(", "\\(")
                .replace(")", "\\)")));
        MessageManager.sendMessage(p, "edit_timestamp", point.getFormattedCreationTime());
        MessageManager.sendMessage(p, "edit_location",
                Integer.toString((int) point.getX()),
                Integer.toString((int) point.getY()),
                Integer.toString((int) point.getZ()));
        MessageManager.sendMessage(p, "edit_world", point.getWorldName());
        if (HuskHomes.getSettings().doBungee()) {
            MessageManager.sendMessage(p, "edit_server", point.getServer());
        }
    }

}