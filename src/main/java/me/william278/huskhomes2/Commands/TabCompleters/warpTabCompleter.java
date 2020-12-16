package me.william278.huskhomes2.Commands.TabCompleters;

import me.william278.huskhomes2.Objects.Home;
import me.william278.huskhomes2.Objects.Warp;
import me.william278.huskhomes2.dataManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import java.util.*;

public class warpTabCompleter implements TabCompleter {

    // Cached HashMap of warps
    public static final HashSet<String> warpsTabCache = new HashSet<>();

    // Updates the public home cache
    public static void updateWarpsTabCache() {
        warpsTabCache.clear();
        for (Warp warp : dataManager.getWarps()) {
            warpsTabCache.add(warp.getName());
        }
    }


    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("huskhomes.warp")) {
            return new ArrayList<>();
        }
        if (args.length == 1) {
            final List<String> tabCompletions = new ArrayList<>();

            StringUtil.copyPartialMatches(args[0], warpsTabCache, tabCompletions);

            Collections.sort(tabCompletions);

            return tabCompletions;

        } else {
            return new ArrayList<>();
        }
    }

}
