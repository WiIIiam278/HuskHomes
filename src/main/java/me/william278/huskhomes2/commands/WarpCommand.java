package me.william278.huskhomes2.commands;

import me.william278.huskhomes2.HuskHomes;
import me.william278.huskhomes2.MessageManager;
import me.william278.huskhomes2.data.DataManager;
import me.william278.huskhomes2.teleport.TeleportManager;
import me.william278.huskhomes2.teleport.points.Warp;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

public class WarpCommand extends CommandBase {

    private static final HuskHomes plugin = HuskHomes.getInstance();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player p)) {
            handleConsoleWarp(sender, args);
            return true;
        }
        if (!HuskHomes.getSettings().doWarpCommand()) {
            MessageManager.sendMessage(p, "error_command_disabled");
            return true;
        }
        if (args.length == 1) {
            String warpName = args[0];
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try (Connection connection = HuskHomes.getConnection()) {
                    if (DataManager.warpExists(warpName, connection)) {
                        if (Warp.getWarpCanUse(p, warpName)) {
                            Warp warp = DataManager.getWarp(warpName, connection);
                            TeleportManager.queueTimedTeleport(p, warp);
                        } else {
                            MessageManager.sendMessage(p, "error_permission_restricted_warp", warpName);
                        }
                    } else {
                        MessageManager.sendMessage(p, "error_warp_invalid", warpName);
                    }                } catch (SQLException e) {
                    plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred using /tphere");
                }
            });
        } else {
            WarpListCommand.displayWarpList(p, 1);
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

                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    try (Connection connection = HuskHomes.getConnection()) {
                        if (DataManager.warpExists(warpName, connection)) {
                            Warp warp = DataManager.getWarp(warpName, connection);
                            TeleportManager.teleportPlayer(targetPlayer, warp);
                            sender.sendMessage("Successfully warped player!");
                            MessageManager.sendMessage(targetPlayer, "teleporting_complete_console", warpName);
                        } else {
                            sender.sendMessage("Error: Invalid warp \"" + warpName + "\" specified");
                        }
                    } catch (SQLException e) {
                        plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred using /tphere");
                    }
                });
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
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try (Connection connection = HuskHomes.getConnection()) {
                    for (Warp warp : DataManager.getWarps(connection)) {
                        warpsTabCache.add(warp.getName());
                    }
                } catch (SQLException e) {
                    plugin.getLogger().log(Level.WARNING, "An SQL exception occurred updating the warp cache", e);
                }
            });
        }


        @Override
        public List<String> onTabComplete(CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
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
