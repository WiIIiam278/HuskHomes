package me.william278.huskhomes2.commands;

import me.william278.huskhomes2.HuskHomes;
import me.william278.huskhomes2.util.MessageManager;
import me.william278.huskhomes2.data.DataManager;
import me.william278.huskhomes2.teleport.SettingHandler;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;

public class SetHomeCommand extends CommandBase {

    private static final HuskHomes plugin = HuskHomes.getInstance();

    @Override
    protected void onCommand(Player p, Command command, String label, String[] args) {
        if (args.length == 1) {
            String homeName = args[0];
            SettingHandler.setHome(p.getLocation(), p, homeName);
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
               try (Connection connection = HuskHomes.getConnection()) {
                   if (DataManager.getPlayerHomes(p.getName(), connection).size() == 0) {
                       // If the player hasn't set a home yet, set one called "home"
                       SettingHandler.setHome(p.getLocation(), p, "home");
                   } else {
                       MessageManager.sendMessage(p, "error_invalid_syntax", command.getUsage());
                   }
               } catch (SQLException e) {
                   plugin.getLogger().log(Level.SEVERE, "An exception occurred setting your first home");
               }
            });
        }
    }

}
