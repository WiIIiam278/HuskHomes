package me.william278.huskhomes2.Commands.TabCompleters;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class editWarpTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        Player p = (Player) sender;
        if (!p.hasPermission("huskhomes.editwarp")) {
            return new ArrayList<>();
        }
        if (args.length == 1) {
            final List<String> tabCompletions = new ArrayList<>();
            StringUtil.copyPartialMatches(args[0], warpTabCompleter.warpsTabCache, tabCompletions);
            Collections.sort(tabCompletions);
            return tabCompletions;
        } else if (args.length == 2) {
            List<String> editWarpOptions = new ArrayList<>();
            editWarpOptions.add("rename");
            editWarpOptions.add("location");
            editWarpOptions.add("description");
            return editWarpOptions;
        } else {
            return new ArrayList<>();
        }
    }

}
