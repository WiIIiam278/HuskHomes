package me.william278.huskhomes2.commands;

import me.william278.huskhomes2.util.MessageManager;
import me.william278.huskhomes2.teleport.SettingHandler;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

public class DelWarpCommand extends CommandBase {

    @Override
    protected void onCommand(Player p, Command command, String label, String[] args) {
        if (args.length == 1) {
            String warpName = args[0];
            SettingHandler.deleteWarp(p, warpName);
            return;
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("all") && args[1].equalsIgnoreCase("confirm")) {
                if (p.hasPermission("huskhomes.delwarp.all")) {
                    SettingHandler.deleteAllWarps(p);
                } else {
                    MessageManager.sendMessage(p, "error_no_permission");
                }
                return;
            }
        }
        MessageManager.sendMessage(p, "error_invalid_syntax", command.getUsage());
    }

}
