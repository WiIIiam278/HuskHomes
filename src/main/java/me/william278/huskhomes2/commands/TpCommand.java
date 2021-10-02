package me.william278.huskhomes2.commands;

import me.william278.huskhomes2.HuskHomes;
import me.william278.huskhomes2.MessageManager;
import me.william278.huskhomes2.teleport.TeleportManager;
import me.william278.huskhomes2.teleport.points.TeleportationPoint;
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
import java.util.List;
import java.util.logging.Level;

public class TpCommand extends CommandBase {

    private static final HuskHomes plugin = HuskHomes.getInstance();

    @Override
    protected void onCommand(Player p, Command command, String label, String[] args) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection connection = HuskHomes.getConnection()) {
                switch (args.length) {
                    default:
                    case 0:
                        MessageManager.sendMessage(p, "error_invalid_syntax", command.getUsage());
                        return;
                    case 1:
                        String targetPlayer = args[0];
                        TeleportManager.teleportPlayer(p, targetPlayer, connection);
                        return;
                    case 3:
                        try {
                            double x = Double.parseDouble(args[0]);
                            double y = Double.parseDouble(args[1]);
                            double z = Double.parseDouble(args[2]);
                            TeleportationPoint teleportationPoint = new TeleportationPoint(p.getWorld().getName(), x, y, z, 0F, 0F, HuskHomes.getSettings().getServerID());
                            TeleportManager.teleportPlayer(p, teleportationPoint, connection);
                        } catch (NumberFormatException e) {
                            MessageManager.sendMessage(p, "error_invalid_syntax", "/tp <x> <y> <z> [world] [server]");
                        }
                        return;
                    case 4:
                        try {
                            double x = Double.parseDouble(args[0]);
                            double y = Double.parseDouble(args[1]);
                            double z = Double.parseDouble(args[2]);
                            String worldName = args[3];
                            if (Bukkit.getWorld(worldName) == null) {
                                MessageManager.sendMessage(p, "error_invalid_syntax", "/tp <x> <y> <z> [world] [server]");
                                return;
                            }
                            TeleportationPoint teleportationPoint = new TeleportationPoint(worldName, x, y, z, 0F, 0F, HuskHomes.getSettings().getServerID());
                            TeleportManager.teleportPlayer(p, teleportationPoint, connection);
                        } catch (NumberFormatException e) {
                            MessageManager.sendMessage(p, "error_invalid_syntax", "/tp <x> <y> <z> [world] [server]");
                        }
                        return;
                    case 5:
                        if (HuskHomes.getSettings().doBungee()) {
                            try {
                                double x = Double.parseDouble(args[0]);
                                double y = Double.parseDouble(args[1]);
                                double z = Double.parseDouble(args[2]);
                                String worldName = args[3];
                                String serverName = args[4];
                                TeleportationPoint teleportationPoint = new TeleportationPoint(worldName, x, y, z, 0F, 0F, serverName);
                                TeleportManager.teleportPlayer(p, teleportationPoint, connection);
                            } catch (NumberFormatException e) {
                                MessageManager.sendMessage(p, "error_invalid_syntax", "/tp <x> <y> <z> <world> <server>");
                            }
                        } else {
                            MessageManager.sendMessage(p, "error_invalid_syntax", command.getUsage());
                        }
                        return;
                    case 6:
                        try {
                            double x = Double.parseDouble(args[0]);
                            double y = Double.parseDouble(args[1]);
                            double z = Double.parseDouble(args[2]);
                            String worldName = args[3];
                            float yaw = Float.parseFloat(args[4]);
                            float pitch = Float.parseFloat(args[5]);
                            TeleportationPoint teleportationPoint = new TeleportationPoint(worldName, x, y, z, yaw, pitch, HuskHomes.getSettings().getServerID());
                            TeleportManager.teleportPlayer(p, teleportationPoint, connection);
                        } catch (NumberFormatException e) {
                            MessageManager.sendMessage(p, "error_invalid_syntax", "/tp <x> <y> <z> <world> <yaw> <pitch>");
                        }
                    case 7:
                        if (HuskHomes.getSettings().doBungee()) {
                            try {
                                double x = Double.parseDouble(args[0]);
                                double y = Double.parseDouble(args[1]);
                                double z = Double.parseDouble(args[2]);
                                String worldName = args[3];
                                String serverName = args[4];
                                float yaw = Float.parseFloat(args[5]);
                                float pitch = Float.parseFloat(args[6]);
                                TeleportationPoint teleportationPoint = new TeleportationPoint(worldName, x, y, z, yaw, pitch, serverName);
                                TeleportManager.teleportPlayer(p, teleportationPoint, connection);
                            } catch (NumberFormatException e) {
                                MessageManager.sendMessage(p, "error_invalid_syntax", "/tp <x> <y> <z> <world> <server> <yaw> <pitch>");
                            }
                        } else {
                            MessageManager.sendMessage(p, "error_invalid_syntax", command.getUsage());
                        }
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred while using /tp", e);
            }
        });
    }

    public static class Tab implements TabCompleter {
        @Override
        public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
            List<String> players = new ArrayList<>();
            if (args.length == 0 || args.length == 1) {
                players.addAll(HuskHomes.getPlayerList().getPlayers());
            }
            players.remove(sender.getName());
            final List<String> tabCompletions = new ArrayList<>();
            StringUtil.copyPartialMatches(args[0], players, tabCompletions);
            Collections.sort(tabCompletions);
            return tabCompletions;
        }

    }
}
