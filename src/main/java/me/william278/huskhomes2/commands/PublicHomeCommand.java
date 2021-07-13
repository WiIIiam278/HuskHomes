package me.william278.huskhomes2.commands;

import me.william278.huskhomes2.HuskHomes;
import me.william278.huskhomes2.MessageManager;
import me.william278.huskhomes2.data.DataManager;
import me.william278.huskhomes2.teleport.TeleportManager;
import me.william278.huskhomes2.teleport.points.Home;
import me.william278.huskhomes2.util.RegexUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class PublicHomeCommand extends CommandBase implements TabCompleter {

    // TODO Remove it
    // Cached HashMap of public homes
    // HomeName, OwnerName
    private static final Map<String, String> publicHomeTabCache = new HashMap<>();

    private static final HuskHomes plugin = HuskHomes.getInstance();

    // Updates the public home cache
    public static void updatePublicHomeTabCache() {
        publicHomeTabCache.clear();
        Connection connection = HuskHomes.getConnection();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                for (Home home : DataManager.getPublicHomes(connection)) {
                    publicHomeTabCache.put(home.getName(), home.getOwnerUsername());
                }
            } catch (SQLException exception) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred updating the public home tab cache");
            }
        });
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            handleConsolePublicHomeTeleport(sender, args);
            return true;
        }
        if (args.length == 0) {
            PublicHomeListCommand.displayPublicHomeList(p, 1);
            return true;
        }
        String publicHome = args[0];
        if (RegexUtil.OWNER_NAME_PATTERN.matcher(publicHome).matches()) {
            Connection connection = HuskHomes.getConnection();
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    String[] split = publicHome.split("\\.");
                    String ownerName = split[0];
                    String homeName = split[1];
                    if (DataManager.homeExists(ownerName, homeName, connection)) {
                        Home home = DataManager.getHome(ownerName, homeName, connection);
                        if (home.isPublic()) {
                            TeleportManager.queueTimedTeleport(p, home, connection);
                        } else {
                            MessageManager.sendMessage(p, "error_public_home_invalid", ownerName, homeName);
                        }
                    } else {
                        MessageManager.sendMessage(p, "error_public_home_invalid", ownerName, homeName);
                    }
                } catch (SQLException e) {
                    plugin.getLogger().log(Level.SEVERE, "An exception occurred teleporting to a public home.");
                }
            });
        } else {
            MessageManager.sendMessage(p, "error_invalid_syntax", command.getUsage());
        }
        return true;
    }

    // Handle a warp from console
    public static void handleConsolePublicHomeTeleport(CommandSender sender, String[] args) {
        String publicHomeName;
        Player targetPlayer;
        if (args.length == 2) {
            publicHomeName = args[0];
            targetPlayer = Bukkit.getPlayer(args[1]);
            if (targetPlayer != null) {
                if (RegexUtil.OWNER_NAME_PATTERN.matcher(publicHomeName).matches()) {
                    String[] split = publicHomeName.split("\\.");
                    String ownerName = split[0];
                    String homeName = split[1];
                    Connection connection = HuskHomes.getConnection();
                    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                        try {
                            if (DataManager.homeExists(ownerName, homeName, connection)) {
                                Home home = DataManager.getHome(ownerName, homeName, connection);
                                if (!home.isPublic()) {
                                    sender.sendMessage("Warning: Bypassed home security (" + home.getName() + " was not set to public by " + home.getOwnerUsername() + ")");
                                }
                                TeleportManager.teleportPlayer(targetPlayer, home, connection);
                                sender.sendMessage("Successfully teleported player!");
                                MessageManager.sendMessage(targetPlayer, "teleporting_complete_console", (home.getOwnerUsername() + "." + homeName));
                            } else {
                                sender.sendMessage("Error: Invalid home \"" + publicHomeName + "\" specified");
                            }
                        } catch (SQLException e) {
                            plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred handling a consome phome teleport");
                        }
                    });
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
