package me.william278.huskhomes2.commands;

import me.william278.huskhomes2.HuskHomes;
import me.william278.huskhomes2.dataManager;
import me.william278.huskhomes2.listHandler;
import me.william278.huskhomes2.messageManager;
import me.william278.huskhomes2.objects.Warp;
import me.william278.huskhomes2.teleportManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class WarpCommand extends CommandBase {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if (HuskHomes.settings.doWarpCommand()) {
                if (args.length == 1) {
                    String warpName = args[0];
                    if (dataManager.warpExists(warpName)) {
                        Warp warp = dataManager.getWarp(warpName);
                        teleportManager.queueTimedTeleport(p, warp);
                    } else {
                        messageManager.sendMessage(p, "error_warp_invalid", warpName);
                    }
                } else {
                    listHandler.displayWarpList(p, 1);
                }
            } else {
                messageManager.sendMessage(p, "error_command_disabled");
            }
            return true;
        }
        return false;
    }

    public static class Tab implements TabCompleter {

        // TODO Remove
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
}
