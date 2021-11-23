package me.william278.huskhomes2.commands;

import me.william278.huskhomes2.teleport.TeleportRequestHandler;
import me.william278.huskhomes2.util.NameAutoCompleter;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

public class TpAcceptCommand extends CommandBase {

    @Override
    protected void onCommand(Player p, Command command, String label, String[] args) {
        if (args.length == 1) {
            String targetPlayer = args[0];
            TeleportRequestHandler.replyTpRequest(p, NameAutoCompleter.getAutoCompletedName(targetPlayer), true);
        } else {
            TeleportRequestHandler.replyTpRequest(p, true);
        }
    }
}