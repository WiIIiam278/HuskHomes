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

public class TpOfflineCommand extends CommandBase {

    private static final HuskHomes plugin = HuskHomes.getInstance();

    @Override
    protected void onCommand(Player p, Command command, String label, String[] args) {
        if (args.length == 1) {
            final String targetPlayer = args[0];
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try (Connection connection = HuskHomes.getConnection()) {
                    TeleportManager.teleportToOfflinePlayer(p, targetPlayer, connection);
                } catch (SQLException e) {
                    plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred teleporting you to a player's offline position");
                }
            });
        } else {
            MessageManager.sendMessage(p, "error_invalid_syntax", command.getUsage());
        }
    }
}
