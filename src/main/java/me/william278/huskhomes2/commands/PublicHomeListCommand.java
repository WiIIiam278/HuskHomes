package me.william278.huskhomes2.commands;

import me.william278.huskhomes2.HuskHomes;
import me.william278.huskhomes2.MessageManager;
import me.william278.huskhomes2.data.DataManager;
import me.william278.huskhomes2.teleport.points.Home;
import me.william278.huskhomes2.util.ChatList;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class PublicHomeListCommand extends CommandBase {

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
        displayPublicHomeList(p, pageNo);
    }

    public static void displayPublicHomeList(Player player, int pageNumber) {
        Connection connection = HuskHomes.getConnection();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                List<Home> homes = DataManager.getPublicHomes(connection);
                if (homes.isEmpty()) {
                    MessageManager.sendMessage(player, "error_no_public_homes_set");
                    return;
                }
                ArrayList<String> publicHomeList = new ArrayList<>();
                for (Home home : homes) {
                    publicHomeList.add(MessageManager.getRawMessage("public_home_list_item", home.getName(), home.getOwnerUsername(), home.getDescription()));
                }

                final int itemsPerPage = HuskHomes.getSettings().getPublicHomesPerPage();
                final int homeLowerBound = (pageNumber - 1) * itemsPerPage;
                int homeUpperBound = pageNumber * itemsPerPage;
                if (homeUpperBound > publicHomeList.size()) {
                    homeUpperBound = publicHomeList.size();
                }

                ChatList homeChatList = new ChatList(publicHomeList, itemsPerPage, "/huskhomes:publichomelist", MessageManager.getRawMessage("list_item_divider"), true);
                if (homeChatList.doesNotContainPage(pageNumber)) {
                    MessageManager.sendMessage(player, "error_invalid_page_number");
                    return;
                }

                MessageManager.sendMessage(player, "public_home_list_page_top", Integer.toString(homeLowerBound + 1), Integer.toString(homeUpperBound), Integer.toString(publicHomeList.size()));
                player.spigot().sendMessage(homeChatList.getPage(pageNumber));
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred!", e);
            }
        });
    }

}
