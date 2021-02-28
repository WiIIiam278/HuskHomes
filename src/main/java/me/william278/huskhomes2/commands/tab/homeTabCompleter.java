package me.william278.huskhomes2.commands.tab;

import me.william278.huskhomes2.objects.Home;
import me.william278.huskhomes2.dataManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.*;

public class homeTabCompleter implements TabCompleter {

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
