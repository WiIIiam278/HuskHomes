package me.william278.huskhomes2.Commands;

import me.william278.huskhomes2.HuskHomes;
import me.william278.huskhomes2.Objects.TeleportationPoint;
import me.william278.huskhomes2.messageManager;
import me.william278.huskhomes2.teleportManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class tpCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if (args.length >= 1) {
                switch (args.length) {
                    case 1:
                        String targetPlayer = args[0];
                        teleportManager.teleportPlayer(p, targetPlayer);
                        return true;
                    case 3:
                        try {
                            double x = Double.parseDouble(args[0]);
                            double y = Double.parseDouble(args[1]);
                            double z = Double.parseDouble(args[2]);
                            TeleportationPoint teleportationPoint = new TeleportationPoint(p.getWorld().getName(), x, y, z, 0F, 0F, HuskHomes.settings.getServerID());
                            teleportManager.teleportPlayer(p, teleportationPoint);
                        } catch (Exception e) {
                            messageManager.sendMessage(p, "error_invalid_syntax", "/tp <x> <y> <z> [world] [server]");
                        }
                        return true;
                    case 4:
                        try {
                            double x = Double.parseDouble(args[0]);
                            double y = Double.parseDouble(args[1]);
                            double z = Double.parseDouble(args[2]);
                            String worldName = args[3];
                            if (Bukkit.getWorld(worldName) == null) {
                                messageManager.sendMessage(p, "error_invalid_syntax", "/tp <x> <y> <z> [world] [server]");
                                return true;
                            }
                            TeleportationPoint teleportationPoint = new TeleportationPoint(worldName, x, y, z, 0F, 0F, HuskHomes.settings.getServerID());
                            teleportManager.teleportPlayer(p, teleportationPoint);
                        } catch (Exception e) {
                            messageManager.sendMessage(p, "error_invalid_syntax", "/tp <x> <y> <z> [world] [server]");
                        }
                        return true;
                    case 5:
                        if (HuskHomes.settings.doBungee()) {
                            try {
                                double x = Double.parseDouble(args[0]);
                                double y = Double.parseDouble(args[1]);
                                double z = Double.parseDouble(args[2]);
                                String worldName = args[3];
                                String serverName = args[4];
                                if (Bukkit.getWorld(worldName) == null) {
                                    messageManager.sendMessage(p, "error_invalid_syntax", "/tp <x> <y> <z> <world> <server>");
                                    return true;
                                }
                                TeleportationPoint teleportationPoint = new TeleportationPoint(worldName, x, y, z, 0F, 0F, serverName);
                                teleportManager.teleportPlayer(p, teleportationPoint);
                            } catch (Exception e) {
                                messageManager.sendMessage(p, "error_invalid_syntax", "/tp <x> <y> <z> <world> <server>");
                            }
                        } else {
                            messageManager.sendMessage(p, "error_invalid_syntax", command.getUsage());
                        }
                        return true;
                }
            } else {
                messageManager.sendMessage(p, "error_invalid_syntax", command.getUsage());
            }
            return true;
        }
        return false;
    }
}
