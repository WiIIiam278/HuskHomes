package me.william278.huskhomes2;

import de.themoep.minedown.MineDown;
import me.william278.huskhomes2.teleport.points.Home;
import me.william278.huskhomes2.teleport.points.SetPoint;
import me.william278.huskhomes2.teleport.points.Warp;
import org.bukkit.entity.Player;

public class EditingHandler {

    public static void showEditHomeOptions(Player p, Home home) {
        MessageManager.sendMessage(p, "edit_home_title", home.getName());
        showPointDetails(p, home);
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
        showPointDetails(p, warp);

        MessageManager.sendMessage(p, "edit_warp_options", warp.getName());
    }

    private static void showPointDetails(Player p, SetPoint point) {
        MessageManager.sendMessage(p, "edit_description", MineDown.escape(point.getDescription().replace("]", "］").replace("[", "［").replace("(", "❲").replace(")", "❳")));
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