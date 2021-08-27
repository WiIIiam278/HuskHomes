package me.william278.huskhomes2.commands;

import me.william278.huskhomes2.MessageManager;
import me.william278.huskhomes2.teleport.SettingHandler;
import me.william278.huskhomes2.util.RegexUtil;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

public class DelHomeCommand extends CommandBase {

    @Override
    protected void onCommand(Player p, Command command, String label, String[] args) {
        if (args.length == 1) {
            String homeArgument = args[0];
            if (RegexUtil.OWNER_NAME_PATTERN.matcher(homeArgument).matches()) {
                String[] split = homeArgument.split("\\.");
                final String ownerName = split[0];
                final String homeName = split[1];
                SettingHandler.deleteHome(p, ownerName, homeName);
            } else {
                SettingHandler.deleteHome(p, homeArgument);
            }
            return;
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("all") && args[1].equalsIgnoreCase("confirm")) {
                if (p.hasPermission("huskhomes.delhome.all")) {
                    SettingHandler.deleteAllHomes(p);
                } else {
                    MessageManager.sendMessage(p, "error_no_permission");
                }
                return;
            }
        }
        MessageManager.sendMessage(p, "error_invalid_syntax", command.getUsage());
    }

}
