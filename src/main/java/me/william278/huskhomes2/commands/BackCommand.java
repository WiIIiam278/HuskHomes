package me.william278.huskhomes2.commands;

import me.william278.huskhomes2.HuskHomes;
import me.william278.huskhomes2.util.MessageManager;
import me.william278.huskhomes2.teleport.TeleportManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;

public class BackCommand extends CommandBase {

    private static final HuskHomes plugin = HuskHomes.getInstance();

    @Override
    protected void onCommand(Player p, Command command, String label, String[] args) {
        if (HuskHomes.getSettings().getDoBackCommand()) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try (Connection connection = HuskHomes.getConnection()) {
                    TeleportManager.queueBackTeleport(p, connection);
                } catch (SQLException e) {
                    plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred teleporting you back");
                }
            });
        } else {
            MessageManager.sendMessage(p, "error_command_disabled");
        }
    }
}
