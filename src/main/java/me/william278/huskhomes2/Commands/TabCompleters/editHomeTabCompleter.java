package me.william278.huskhomes2.Commands.TabCompleters;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class editHomeTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        Player p = (Player) sender;
        if (!p.hasPermission("huskhomes.edithome")) {
            return new ArrayList<>();
        }
        if (args.length == 1) {
            final List<String> tabCompletions = new ArrayList<>();
            StringUtil.copyPartialMatches(args[0], homeTabCompleter.homeTabCache.get(p.getUniqueId()), tabCompletions);
            Collections.sort(tabCompletions);
            return tabCompletions;
        } else if (args.length == 2) {
            List<String> editHomeOptions = new ArrayList<>();
            editHomeOptions.add("rename");
            editHomeOptions.add("location");
            editHomeOptions.add("description");
            if (sender.hasPermission("huskhomes.edithome.public")) {
                editHomeOptions.add("public");
                editHomeOptions.add("private");
            }
            return editHomeOptions;
        } else {
            return new ArrayList<>();
        }
    }

}
