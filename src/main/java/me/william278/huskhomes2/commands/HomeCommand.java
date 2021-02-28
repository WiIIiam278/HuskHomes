package me.william278.huskhomes2.commands;

import me.william278.huskhomes2.dataManager;
import me.william278.huskhomes2.listHandler;
import me.william278.huskhomes2.messageManager;
import me.william278.huskhomes2.objects.Home;
import me.william278.huskhomes2.teleportManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class HomeCommand extends CommandBase {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if (args.length == 1) {
                String homeName = args[0];
                if (dataManager.homeExists(p, homeName)) {
                    Home home = dataManager.getHome(p.getName(), homeName);
                    teleportManager.queueTimedTeleport(p, home);
                } else {
                    messageManager.sendMessage(p, "error_home_invalid", homeName);
                }
            } else {
                ArrayList<Home> homes = dataManager.getPlayerHomes(p.getName());
                if (homes != null) {
                    if (homes.size() == 1) {
                        // Teleport the player if they only have one home
                        teleportManager.queueTimedTeleport(p, homes.get(0));
                        return true;
                    }
                }
                listHandler.displayPlayerHomeList(p, 1);
            }
            return true;
        }
        return false;
    }

    public static class Tab implements TabCompleter {

        // TODO Remove
        // This HashMap stores a cache of a player's homes that is displayed when a user presses TAB.
        // Owner UUID, Home Name
        public static HashMap<UUID, ArrayList<String>> homeTabCache = new HashMap<>();

        // This method updates a player's home cache.
        public static void updatePlayerHomeCache(Player p) {
            UUID uuid = p.getUniqueId();
            ArrayList<Home> playerHomes = dataManager.getPlayerHomes(p.getName());
            ArrayList<String> homeNames = new ArrayList<>();
            for (Home home : playerHomes) {
                homeNames.add(home.getName());
            }
            homeTabCache.put(uuid, homeNames);
        }

        @Override
        public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
            Player p = (Player) sender;
            if (!sender.hasPermission("huskhomes.home")) {
                return new ArrayList<>();
            }
            if (args.length == 1) {
                final List<String> tabCompletions = new ArrayList<>();

                ArrayList<String> homes = homeTabCache.get(p.getUniqueId());
                if (homes == null) {
                    updatePlayerHomeCache(p);
                    return new ArrayList<>();
                }
                StringUtil.copyPartialMatches(args[0], homes, tabCompletions);

                Collections.sort(tabCompletions);

                return tabCompletions;
            } else {
                return new ArrayList<>();
            }
        }
    }
}