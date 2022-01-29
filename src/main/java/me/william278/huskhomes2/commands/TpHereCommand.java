package me.william278.huskhomes2.commands;

import me.william278.huskhomes2.HuskHomes;
import me.william278.huskhomes2.util.MessageManager;
import me.william278.huskhomes2.teleport.TeleportManager;
import me.william278.huskhomes2.util.NameAutoCompleter;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.logging.Level;

public class TpHereCommand extends CommandBase {

    private static final HuskHomes plugin = HuskHomes.getInstance();

    @Override
    protected void onCommand(Player p, Command command, String label, String[] args) {
        if (args.length == 1) {
            String targetPlayer = args[0];
            executeTpHere(p, targetPlayer);
        } else {
            MessageManager.sendMessage(p, "error_invalid_syntax", command.getUsage());
        }
    }

    // Execute the tp here operation
    public static void executeTpHere(Player player, String targetPlayer) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                TeleportManager.teleportHere(player, NameAutoCompleter.getAutoCompletedName(targetPlayer));
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred using /tphere");
            }
        });
    }
}
