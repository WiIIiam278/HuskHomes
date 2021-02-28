package me.william278.huskhomes2.commands.tab;

import me.william278.huskhomes2.objects.Home;
import me.william278.huskhomes2.dataManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class publicHomeTabCompleter implements TabCompleter {

    // Cached HashMap of public homes
    // HomeName, OwnerName
    public static final HashMap<String,String> publicHomeTabCache = new HashMap<>();

    // Updates the public home cache
    public static void updatePublicHomeTabCache() {
        publicHomeTabCache.clear();
        for (Home home : dataManager.getPublicHomes()) {
            publicHomeTabCache.put(home.getName(), home.getOwnerUsername());
        }
    }


    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("huskhomes.publichome")) {
            return new ArrayList<>();
        }
        if (args.length == 1) {
            final List<String> sortedHomeNames = new ArrayList<>();
            StringUtil.copyPartialMatches(args[0], publicHomeTabCache.keySet(), sortedHomeNames);
            Collections.sort(sortedHomeNames);

            final List<String> finalCompletions = new ArrayList<>();
            for (String homeName : sortedHomeNames) {
                finalCompletions.add(publicHomeTabCache.get(homeName) + "." + homeName);
            }
            return finalCompletions;

        } else {
            return new ArrayList<>();
        }
    }
}
