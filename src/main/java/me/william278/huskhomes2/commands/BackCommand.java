package me.william278.huskhomes2.commands;

import me.william278.huskhomes2.MessageManager;
import me.william278.huskhomes2.data.DataManager;
import me.william278.huskhomes2.teleport.TeleportManager;
import me.william278.huskhomes2.teleport.points.TeleportationPoint;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BackCommand extends CommandBase {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            TeleportationPoint lastPosition = DataManager.getPlayerLastPosition(p);
            if (lastPosition != null) {
                TeleportManager.queueTimedTeleport(p, lastPosition);
            } else {
                MessageManager.sendMessage(p, "error_no_last_position");
            }
            return true;
        }
        return false;
    }
}
