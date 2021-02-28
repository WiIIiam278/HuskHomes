package me.william278.huskhomes2.commands;

import me.william278.huskhomes2.dataManager;
import me.william278.huskhomes2.messageManager;
import me.william278.huskhomes2.settingHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SethomeCommand extends CommandBase {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if (args.length == 1) {
                String homeName = args[0];
                settingHandler.setHome(p.getLocation(), p, homeName);
            } else {
                if (dataManager.getPlayerHomes(p.getName()).size() == 0) {
                    // If the player hasn't set a home yet, set one called "home"
                    settingHandler.setHome(p.getLocation(), p, "home");
                } else {
                    messageManager.sendMessage(p, "error_invalid_syntax", command.getUsage());
                }
            }
            return true;
        }
        return false;
    }

}
