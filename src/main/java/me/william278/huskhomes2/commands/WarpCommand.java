package me.william278.huskhomes2.commands;

import me.william278.huskhomes2.HuskHomes;
import me.william278.huskhomes2.MessageManager;
import me.william278.huskhomes2.data.DataManager;
import me.william278.huskhomes2.teleport.ListHandler;
import me.william278.huskhomes2.teleport.TeleportManager;
import me.william278.huskhomes2.teleport.points.Warp;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WarpCommand extends CommandBase {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            handleConsoleWarp(sender, args);
            return true;
        }
        Player p = (Player) sender;
        if (!HuskHomes.getSettings().doWarpCommand()) {
            MessageManager.sendMessage(p, "error_command_disabled");
            return true;
        }
        if (args.length == 1) {
            String warpName = args[0];
            if (DataManager.warpExists(warpName)) {
                if (Warp.getWarpCanUse(p, warpName)) {
                    Warp warp = DataManager.getWarp(warpName);
                    TeleportManager.queueTimedTeleport(p, warp);
                } else {
                    MessageManager.sendMessage(p, "error_permission_restricted_warp", warpName);
                }
            } else {
                MessageManager.sendMessage(p, "error_warp_invalid", warpName);
            }
        } else {
            ListHandler.displayWarpList(p, 1);
        }
        return true;
    }

    @Override
    protected void onCommand(Player player, Command command, String label, String[] args) {
        // Console is fine too
    }

    // Handle a warp from console
    public static void handleConsoleWarp(CommandSender sender, String[] args) {
        String warpName;
        Player targetPlayer;
        if (args.length == 2) {
            warpName = args[0];
            targetPlayer = Bukkit.getPlayer(args[1]);
            if (targetPlayer != null) {
                if (DataManager.warpExists(warpName)) {
                    Warp warp = DataManager.getWarp(warpName);
                    TeleportManager.teleportPlayer(targetPlayer, warp);
                    sender.sendMessage("Successfully warped player!");
                    MessageManager.sendMessage(targetPlayer, "teleporting_complete_console", warpName);
                } else {
                    sender.sendMessage("Error: Invalid warp \"" + warpName + "\" specified");
                }
            } else {
                sender.sendMessage("Error: Invalid player specified (are they online?)");
            }
        } else {
            sender.sendMessage("Console Warp Usage: /warp <warp> <player>");
        }
    }

    public static class Tab implements TabCompleter {

        // TODO Remove
        // Cached HashMap of warps
        public static final Set<String> warpsTabCache = new HashSet<>();

        // Updates the public home cache
        public static void updateWarpsTabCache() {
            warpsTabCache.clear();
            for (Warp warp : DataManager.getWarps()) {
                warpsTabCache.add(warp.getName());
            }
        }


        @Override
        public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
            if (!sender.hasPermission("huskhomes.warp")) {
                return Collections.emptyList();
            }
            if (args.length == 1) {
                final List<String> tabCompletions = new ArrayList<>();

                StringUtil.copyPartialMatches(args[0], warpsTabCache, tabCompletions);

                Collections.sort(tabCompletions);

                return tabCompletions;

            } else {
                return Collections.emptyList();
            }
        }

    }
}
