package me.william278.huskhomes2.commands;

import me.william278.huskhomes2.MessageManager;
import me.william278.huskhomes2.data.DataManager;
import me.william278.huskhomes2.teleport.ListHandler;
import me.william278.huskhomes2.teleport.TeleportManager;
import me.william278.huskhomes2.teleport.points.Home;
import me.william278.huskhomes2.utils.RegexUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PublichomeCommand extends CommandBase implements TabCompleter {

    // TODO Remove it
    // Cached HashMap of public homes
    // HomeName, OwnerName
    private static final Map<String,String> publicHomeTabCache = new HashMap<>();

    // Updates the public home cache
    public static void updatePublicHomeTabCache() {
        publicHomeTabCache.clear();
        for (Home home : DataManager.getPublicHomes()) {
            publicHomeTabCache.put(home.getName(), home.getOwnerUsername());
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            handleConsolePublicHomeTeleport(sender, args);
            return true;
        }
        Player p = (Player) sender;
        if (args.length == 0) {
            ListHandler.displayPublicHomeList(p, 1);
            return true;
        }
        String publicHome = args[0];
        if (RegexUtil.OWNER_NAME_PATTERN.matcher(publicHome).matches()) {
            String[] split = publicHome.split("\\.");
            String ownerName = split[0];
            String homeName = split[1];
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
        return true;
    }

    // Handle a warp from console
    public static void handleConsolePublicHomeTeleport(CommandSender sender, String[] args) {
        String phomeName;
        Player targetPlayer;
        if (args.length == 2) {
            phomeName = args[0];
            targetPlayer = Bukkit.getPlayer(args[1]);
            if (targetPlayer != null) {
                if (RegexUtil.OWNER_NAME_PATTERN.matcher(phomeName).matches()) {
                    String[] split = phomeName.split("\\.");
                    String ownerName = split[0];
                    String homeName = split[1];
                    if (DataManager.homeExists(ownerName, homeName)) {
                        Home home = DataManager.getHome(ownerName, homeName);
                        if (!home.isPublic()) {
                            sender.sendMessage("Warning: Bypassed home security (" + home.getName() + " was not set to public by " + home.getOwnerUsername() + ")");
                        }
                        TeleportManager.teleportPlayer(targetPlayer, home);
                        sender.sendMessage("Successfully teleported player!");
                        MessageManager.sendMessage(targetPlayer, "teleporting_complete_console", (home.getOwnerUsername() + "." + homeName));
                    } else {
                        sender.sendMessage("Error: Invalid home \"" + phomeName + "\" specified");
                    }
                } else {
                    sender.sendMessage("Error: Invalid public home format: /warp <ownerUsername.homeName> <player>");
                }
            } else {
                sender.sendMessage("Error: Invalid player specified (are they online?)");
            }
        } else {
            sender.sendMessage("Console Public Home Usage: /phome <owner_name.home_name> <player>");
        }
    }

    @Override
    protected void onCommand(Player player, Command command, String label, String[] args) {
        // Console is fine too
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("huskhomes.publichome")) {
            return Collections.emptyList();
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
            return Collections.emptyList();
        }
    }
}
