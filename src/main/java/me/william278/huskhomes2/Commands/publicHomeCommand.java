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

public class publicHomeCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if (args.length == 1) {
                String publicHome = args[0];
                if (publicHome.matches("[A-Za-z0-9_\\-]+\\.[A-Za-z0-9_\\-]+")) {
                    String ownerName = publicHome.split("\\.")[0];
                    String homeName = publicHome.split("\\.")[1];
                    if (dataManager.homeExists(ownerName, homeName)) {
                        Home home = dataManager.getHome(ownerName, homeName);
                        if (home.isPublic()) {
                            teleportManager.queueTimedTeleport(p, home);
                        } else {
                            messageManager.sendMessage(p, "error_public_home_invalid", ownerName, homeName);
                        }
                    } else {
                        messageManager.sendMessage(p, "error_public_home_invalid", ownerName, homeName);
                    }
                } else {
                    messageManager.sendMessage(p, "error_invalid_syntax", command.getUsage());
                }
            } else {
                listHandler.displayPublicHomeList(p, 1);
            }
            return true;
        }
        return false;
    }

}
