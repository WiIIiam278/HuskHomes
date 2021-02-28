package me.william278.huskhomes2.commands;

import me.william278.huskhomes2.teleportRequestHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class tpAcceptCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            teleportRequestHandler.replyTpRequest(p, true);
            return true;
        }
        return false;
    }
}
