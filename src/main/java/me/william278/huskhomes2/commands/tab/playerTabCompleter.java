package me.william278.huskhomes2.commands.tab;

import me.william278.huskhomes2.integrations.vanishChecker;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class playerTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        ArrayList<String> players = new ArrayList<>();
        if (args.length == 0 || args.length == 1) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!vanishChecker.isVanished(p) && !(p.getName().equals(sender.getName()))) {
                    players.add(p.getName());
                }
            }
        }
        final List<String> tabCompletions = new ArrayList<>();
        StringUtil.copyPartialMatches(args[0], players, tabCompletions);
        Collections.sort(tabCompletions);
        return tabCompletions;
    }

}
