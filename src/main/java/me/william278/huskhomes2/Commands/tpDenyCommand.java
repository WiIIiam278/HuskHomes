package me.william278.huskhomes2.Commands;

import me.william278.huskhomes2.teleportRequestHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class tpDenyCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            teleportRequestHandler.replyTpRequest(p, false);
            return true;
        }
        return false;
    }
}
