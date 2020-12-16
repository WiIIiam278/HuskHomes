package me.william278.huskhomes2.Commands;

import me.william278.huskhomes2.*;
import me.william278.huskhomes2.Objects.Warp;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class warpCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if (HuskHomes.settings.doWarpCommand()) {
                if (args.length == 1) {
                    String warpName = args[0];
                    if (dataManager.warpExists(warpName)) {
                        Warp warp = dataManager.getWarp(warpName);
                        teleportManager.queueTimedTeleport(p, warp);
                    } else {
                        messageManager.sendMessage(p, "error_warp_invalid", warpName);
                    }
                } else {
                    listHandler.displayWarpList(p, 1);
                }
            } else {
                messageManager.sendMessage(p, "error_command_disabled");
            }
            return true;
        }
        return false;
    }

}
