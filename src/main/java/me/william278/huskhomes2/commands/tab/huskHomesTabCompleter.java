package me.william278.huskhomes2.commands.tab;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class huskHomesTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            final List<String> tabCompletions = new ArrayList<>();
            ArrayList<String> tabOptions = new ArrayList<>();
            tabOptions.add("about");
            tabOptions.add("reload");
            tabOptions.add("update");

            StringUtil.copyPartialMatches(args[0], tabOptions, tabCompletions);

            Collections.sort(tabCompletions);

            return tabCompletions;
        } else {
            return new ArrayList<>();
        }
    }
}
