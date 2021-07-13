package me.william278.huskhomes2.commands;

import de.themoep.minedown.MineDown;
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

public class HomeListCommand extends CommandBase {

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
        displayPlayerHomeList(p, pageNo);
    }

    public static void displayPlayerHomeList(Player player, int pageNumber) {
        Connection connection = HuskHomes.getConnection();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                List<Home> homes = DataManager.getPlayerHomes(player.getName(), connection);
                if (homes == null || homes.isEmpty()) {
                    MessageManager.sendMessage(player, "error_no_homes_set");
                    return;
                }

                ArrayList<String> homeList = new ArrayList<>();
                for (Home home : homes) {
                    homeList.add(MessageManager.getRawMessage("home_list_item", home.getName(), MineDown.escape(home.getDescription())));
                }

                final int itemsPerPage = HuskHomes.getSettings().getPrivateHomesPerPage();
                final int homeLowerBound = (pageNumber - 1) * itemsPerPage;
                int homeUpperBound = pageNumber * itemsPerPage;
                if (homeUpperBound > homeList.size()) {
                    homeUpperBound = homeList.size();
                }

                ChatList homeChatList = new ChatList(homeList, itemsPerPage, "/huskhomes:homelist", MessageManager.getRawMessage("list_item_divider"));
                if (homeChatList.doesNotContainPage(pageNumber)) {
                    MessageManager.sendMessage(player, "error_invalid_page_number");
                    return;
                }

                MessageManager.sendMessage(player, "private_home_list_page_top", player.getName(), Integer.toString(homeLowerBound + 1), Integer.toString(homeUpperBound), Integer.toString(homeList.size()));
                player.spigot().sendMessage(homeChatList.getPage(pageNumber));
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred!", e);
            }
        });
    }

}
