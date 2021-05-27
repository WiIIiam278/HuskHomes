package me.william278.huskhomes2.commands;

import me.william278.huskhomes2.MessageManager;
import me.william278.huskhomes2.teleport.TeleportRequestHandler;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

public class TpaHereCommand extends CommandBase {

    @Override
    protected void onCommand(Player p, Command command, String label, String[] args) {
        if (args.length == 1) {
            String targetPlayer = args[0];
            TeleportRequestHandler.sendTeleportHereRequest(p, targetPlayer);
        } else {
            MessageManager.sendMessage(p, "error_invalid_syntax", command.getUsage());
        }
    }
}
