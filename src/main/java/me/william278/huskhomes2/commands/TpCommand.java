package me.william278.huskhomes2.commands;

import me.william278.huskhomes2.HuskHomes;
import me.william278.huskhomes2.MessageManager;
import me.william278.huskhomes2.integrations.VanishChecker;
import me.william278.huskhomes2.teleport.TeleportManager;
import me.william278.huskhomes2.teleport.points.TeleportationPoint;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TpCommand extends CommandBase {

    @Override
    protected boolean onCommand(Player p, Command command, String label, String[] args) {
        switch (args.length) {
            default: case 0:
                MessageManager.sendMessage(p, "error_invalid_syntax", command.getUsage());
                return true;
            case 1:
                String targetPlayer = args[0];
                TeleportManager.teleportPlayer(p, targetPlayer);
                return true;
            case 3:
                try {
                    double x = Double.parseDouble(args[0]);
                    double y = Double.parseDouble(args[1]);
                    double z = Double.parseDouble(args[2]);
                    TeleportationPoint teleportationPoint = new TeleportationPoint(p.getWorld().getName(), x, y, z, 0F, 0F, HuskHomes.getSettings().getServerID());
                    TeleportManager.teleportPlayer(p, teleportationPoint);
                } catch (Exception e) {
                    MessageManager.sendMessage(p, "error_invalid_syntax", "/tp <x> <y> <z> [world] [server]");
                }
                return true;
            case 4:
                try {
                    double x = Double.parseDouble(args[0]);
                    double y = Double.parseDouble(args[1]);
                    double z = Double.parseDouble(args[2]);
                    String worldName = args[3];
                    if (Bukkit.getWorld(worldName) == null) {
                        MessageManager.sendMessage(p, "error_invalid_syntax", "/tp <x> <y> <z> [world] [server]");
                        return true;
                    }
                    TeleportationPoint teleportationPoint = new TeleportationPoint(worldName, x, y, z, 0F, 0F, HuskHomes.getSettings().getServerID());
                    TeleportManager.teleportPlayer(p, teleportationPoint);
                } catch (Exception e) {
                    MessageManager.sendMessage(p, "error_invalid_syntax", "/tp <x> <y> <z> [world] [server]");
                }
                return true;
            case 5:
                if (HuskHomes.getSettings().doBungee()) {
                    try {
                        double x = Double.parseDouble(args[0]);
                        double y = Double.parseDouble(args[1]);
                        double z = Double.parseDouble(args[2]);
                        String worldName = args[3];
                        String serverName = args[4];
                        if (Bukkit.getWorld(worldName) == null) {
                            MessageManager.sendMessage(p, "error_invalid_syntax", "/tp <x> <y> <z> <world> <server>");
                            return true;
                        }
                        TeleportationPoint teleportationPoint = new TeleportationPoint(worldName, x, y, z, 0F, 0F, serverName);
                        TeleportManager.teleportPlayer(p, teleportationPoint);
                    } catch (Exception e) {
                        MessageManager.sendMessage(p, "error_invalid_syntax", "/tp <x> <y> <z> <world> <server>");
                    }
                } else {
                    MessageManager.sendMessage(p, "error_invalid_syntax", command.getUsage());
                }
                return true;
        }
    }

    public static class Tab implements TabCompleter {
        @Override
        public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
            List<String> players = new ArrayList<>();
            if (args.length == 0 || args.length == 1) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (!VanishChecker.isVanished(p) && !(p.getName().equals(sender.getName()))) {
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
}
