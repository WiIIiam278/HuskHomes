package me.william278.huskhomes2.commands;

import me.william278.huskhomes2.teleport.TeleportRequestHandler;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

public class TpdenyCommand extends CommandBase {

    @Override
    protected boolean onCommand(Player p, Command command, String label, String[] args) {
        TeleportRequestHandler.replyTpRequest(p, false);
        return true;
    }
}
