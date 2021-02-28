package me.william278.huskhomes2.commands;

import me.william278.huskhomes2.MessageManager;
import me.william278.huskhomes2.data.DataManager;
import me.william278.huskhomes2.teleport.ListHandler;
import me.william278.huskhomes2.teleport.TeleportManager;
import me.william278.huskhomes2.teleport.points.Home;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class PublichomeCommand extends CommandBase implements TabCompleter {

    // TODO Remove it
    // Cached HashMap of public homes
    // HomeName, OwnerName
    public static final HashMap<String,String> publicHomeTabCache = new HashMap<>();

    // Updates the public home cache
    public static void updatePublicHomeTabCache() {
        publicHomeTabCache.clear();
        for (Home home : DataManager.getPublicHomes()) {
            publicHomeTabCache.put(home.getName(), home.getOwnerUsername());
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if (args.length == 1) {
                String publicHome = args[0];
                if (publicHome.matches("[A-Za-z0-9_\\-]+\\.[A-Za-z0-9_\\-]+")) {
                    String ownerName = publicHome.split("\\.")[0];
                    String homeName = publicHome.split("\\.")[1];
                    if (DataManager.homeExists(ownerName, homeName)) {
                        Home home = DataManager.getHome(ownerName, homeName);
                        if (home.isPublic()) {
                            TeleportManager.queueTimedTeleport(p, home);
                        } else {
                            MessageManager.sendMessage(p, "error_public_home_invalid", ownerName, homeName);
                        }
                    } else {
                        MessageManager.sendMessage(p, "error_public_home_invalid", ownerName, homeName);
                    }
                } else {
                    MessageManager.sendMessage(p, "error_invalid_syntax", command.getUsage());
                }
            } else {
                ListHandler.displayPublicHomeList(p, 1);
            }
            return true;
        }
        return false;
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
