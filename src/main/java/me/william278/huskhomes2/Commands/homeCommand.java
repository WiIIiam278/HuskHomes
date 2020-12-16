package me.william278.huskhomes2.Commands;

import me.william278.huskhomes2.Objects.Home;
import me.william278.huskhomes2.dataManager;
import me.william278.huskhomes2.listHandler;
import me.william278.huskhomes2.messageManager;
import me.william278.huskhomes2.teleportManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

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
                ArrayList<Home> homes = dataManager.getPlayerHomes(p.getName());
                if (homes != null) {
                    if (homes.size() == 1) {
                        // Teleport the player if they only have one home
                        teleportManager.queueTimedTeleport(p, homes.get(0));
                        return true;
                    }
                }
                listHandler.displayPlayerHomeList(p, 1);
            }
            return true;
        }
        return false;
    }

}