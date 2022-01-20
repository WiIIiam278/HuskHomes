package me.william278.huskhomes2.commands;

import de.themoep.minedown.MineDown;
import me.william278.huskhomes2.HuskHomes;
import me.william278.huskhomes2.util.MessageManager;
import me.william278.huskhomes2.data.DataManager;
import me.william278.huskhomes2.teleport.points.Warp;
import me.william278.huskhomes2.util.ChatList;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class WarpListCommand extends CommandBase {

    private static final HuskHomes plugin = HuskHomes.getInstance();

    @Override
    protected void onCommand(Player p, Command command, String label, String[] args) {
        int pageNo = 1;
        if (args.length == 1) {
            try {
                pageNo = Integer.parseInt(args[0]);
            } catch (Exception e) {
                MessageManager.sendMessage(p, "error_invalid_syntax", command.getUsage());
                return;
            }
        }
        displayWarpList(p, pageNo);
    }

    public static void displayWarpList(Player player, int pageNumber) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection connection = HuskHomes.getConnection()) {
                List<Warp> warps = DataManager.getWarps(connection);
                ArrayList<String> warpList = new ArrayList<>();
                for (Warp warp : warps) {
                    if (HuskHomes.getSettings().doPermissionRestrictedWarps()) {
                        if (HuskHomes.getSettings().doHideRestrictedWarps()) {
                            if (!warp.canUse(player)) {
                                continue;
                            }
                        }
                    }
                    warpList.add(MessageManager.getRawMessage("warp_list_item", warp.getName(),
                            MineDown.escape(warp.getDescription()).replace("]", "\\]")
                            .replace("[", "\\[")
                            .replace("(", "\\(")
                            .replace(")", "\\)")));
                }
                if (warpList.isEmpty()) {
                    MessageManager.sendMessage(player, "error_no_warps_set");
                    return;
                }

                final int itemsPerPage = HuskHomes.getSettings().getWarpsPerPage();
                final int homeLowerBound = (pageNumber - 1) * itemsPerPage;
                int warpsUpperBound = pageNumber * itemsPerPage;
                if (warpsUpperBound > warpList.size()) {
                    warpsUpperBound = warpList.size();
                }

                ChatList homeChatList = new ChatList(warpList, itemsPerPage, "/huskhomes:warplist", MessageManager.getRawMessage("list_item_divider"), true);
                if (homeChatList.doesNotContainPage(pageNumber)) {
                    MessageManager.sendMessage(player, "error_invalid_page_number");
                    return;
                }

                MessageManager.sendMessage(player, "warp_list_page_top", Integer.toString(homeLowerBound + 1), Integer.toString(warpsUpperBound), Integer.toString(warpList.size()));
                player.spigot().sendMessage(homeChatList.getPage(pageNumber));
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred!", e);
            }
        });
    }

}
