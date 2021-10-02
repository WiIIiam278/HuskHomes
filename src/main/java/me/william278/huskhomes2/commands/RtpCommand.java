package me.william278.huskhomes2.commands;

import me.william278.huskhomes2.HuskHomes;
import me.william278.huskhomes2.MessageManager;
import me.william278.huskhomes2.teleport.TeleportManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.StringJoiner;
import java.util.logging.Level;

public class RtpCommand extends CommandBase {

    private static final HuskHomes plugin = HuskHomes.getInstance();

    @Override
    protected void onCommand(Player p, Command command, String label, String[] args) {
        if (HuskHomes.getSettings().doRtpCommand()) {
            if (p.getWorld().getEnvironment() == World.Environment.NORMAL) {
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    try (Connection connection = HuskHomes.getConnection()) {
                        TeleportManager.queueRandomTeleport(p, connection);
                    } catch (SQLException e) {
                        plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred queuing a Random teleport", e);
                    }
                });
            } else {
                MessageManager.sendMessage(p, "error_rtp_invalid_dimension");
            }
        } else if (Bukkit.getPluginManager().getPlugin("HuskBungeeRTP") != null) {
            StringJoiner commandForward = new StringJoiner(" ").add("huskbungeertp:rtp");
            for (String argument : args) {
                commandForward.add(argument);
            }
            p.performCommand(commandForward.toString());
        } else {
            MessageManager.sendMessage(p, "error_command_disabled");
        }
    }
}


