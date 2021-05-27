package me.william278.huskhomes2.commands;

import me.william278.huskhomes2.MessageManager;
import me.william278.huskhomes2.teleport.SettingHandler;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

public class DelHomeCommand extends CommandBase {

    @Override
    protected void onCommand(Player p, Command command, String label, String[] args) {
        if (args.length == 1) {
            String homeName = args[0];
            SettingHandler.deleteHome(p, homeName);
            return;
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("all") && args[1].equalsIgnoreCase("confirm")) {
                SettingHandler.deleteAllHomes(p);
                return;
            }
        }
        MessageManager.sendMessage(p, "error_invalid_syntax", command.getUsage());
    }

}
