package me.william278.huskhomes2.commands;

import me.william278.huskhomes2.HuskHomes;
import me.william278.huskhomes2.MessageManager;
import me.william278.huskhomes2.data.DataManager;
import me.william278.huskhomes2.teleport.TeleportManager;
import me.william278.huskhomes2.teleport.points.Home;
import me.william278.huskhomes2.util.RegexUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class HomeCommand extends CommandBase {

    private static final HuskHomes plugin = HuskHomes.getInstance();

    @Override
    protected void onCommand(Player p, Command command, String label, String[] args) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try(Connection connection = HuskHomes.getConnection()) {
                if (args.length == 1) {
                    String homeArgument = args[0];
                    if (RegexUtil.OWNER_NAME_PATTERN.matcher(homeArgument).matches()) {
                        PublicHomeCommand.queueHomeTeleport(p, homeArgument);
                    } else if (DataManager.homeExists(p, homeArgument, connection)) {
                        Home home = DataManager.getHome(p.getName(), homeArgument, connection);
                        TeleportManager.queueTimedTeleport(p, home);
                    } else {
                        MessageManager.sendMessage(p, "error_home_invalid", homeArgument);
                    }
                } else {
                    List<Home> homes = DataManager.getPlayerHomes(p.getName(), connection);
                    if (homes.size() == 1) {
                        // Teleport the player if they only have one home
                        TeleportManager.queueTimedTeleport(p, homes.get(0));
                        return;
                    }
                    HomeListCommand.displayPlayerHomeList(p, p.getName(), 1);
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred teleporting home.", e);
            }
        });
    }

    public static class Tab implements TabCompleter {

        // Owner UUID, Home Name
        private static final Map<UUID, List<String>> homeTabCache = new HashMap<>();

        // This method updates a player's home cache.
        public static void updatePlayerHomeCache(Player p) {
            UUID uuid = p.getUniqueId();
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try(Connection connection = HuskHomes.getConnection()) {
                    List<Home> playerHomes = DataManager.getPlayerHomes(p.getName(), connection);
                    List<String> homeNames = new ArrayList<>();
                    for (Home home : playerHomes) {
                        homeNames.add(home.getName());
                    }
                    homeTabCache.put(uuid, homeNames);
                } catch (SQLException e) {
                    plugin.getLogger().log(Level.WARNING, "An SQL exception occurred updating the player home cache", e);
                }
            });
        }

        @Override
        public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
            Player p = (Player) sender;
            if (!sender.hasPermission("huskhomes.home")) {
                return Collections.emptyList();
            }
            if (args.length == 1) {
                final List<String> tabCompletions = new ArrayList<>();

                List<String> homes = homeTabCache.get(p.getUniqueId());
                if (homes == null) {
                    updatePlayerHomeCache(p);
                    return Collections.emptyList();
                }
                StringUtil.copyPartialMatches(args[0], homes, tabCompletions);

                Collections.sort(tabCompletions);

                return tabCompletions;
            } else {
                return Collections.emptyList();
            }
        }

        public static Map<UUID, List<String>> getHomeTabCache() {
            return homeTabCache;
        }
    }
}