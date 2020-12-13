package me.william278.huskhomes2.Commands;

import me.william278.huskhomes2.*;
import me.william278.huskhomes2.Objects.Home;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class homeCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if (args.length == 1) {
                String homeName = args[0];
                if (dataManager.homeExists(p, homeName)) {
                    Home home = dataManager.getHome(p.getName(), homeName);
                    teleportManager.queueTimedTeleport(p, home);
                } else {
                    messageManager.sendMessage(p, "error_home_invalid", homeName);
                }
            } else {
                listHandler.displayPlayerHomeList(p, 1);
            }
            return true;
        }
        return false;
    }

}
