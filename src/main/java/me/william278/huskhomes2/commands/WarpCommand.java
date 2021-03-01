package me.william278.huskhomes2.commands;

import me.william278.huskhomes2.HuskHomes;
import me.william278.huskhomes2.MessageManager;
import me.william278.huskhomes2.data.DataManager;
import me.william278.huskhomes2.teleport.ListHandler;
import me.william278.huskhomes2.teleport.TeleportManager;
import me.william278.huskhomes2.teleport.points.Warp;
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
    protected boolean onCommand(Player p, Command command, String label, String[] args) {
        if (!HuskHomes.getSettings().doWarpCommand()) {
            MessageManager.sendMessage(p, "error_command_disabled");
            return true;
        }
        if (args.length == 1) {
            String warpName = args[0];
            if (DataManager.warpExists(warpName)) {
                Warp warp = DataManager.getWarp(warpName);
                TeleportManager.queueTimedTeleport(p, warp);
            } else {
                MessageManager.sendMessage(p, "error_warp_invalid", warpName);
            }
        } else {
            ListHandler.displayWarpList(p, 1);
        }
        return true;
    }

    public static class Tab implements TabCompleter {

        // TODO Remove
        // Cached HashMap of warps
        public static final HashSet<String> warpsTabCache = new HashSet<>();

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
