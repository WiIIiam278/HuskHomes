package me.william278.huskhomes2.Commands;

import me.william278.huskhomes2.HuskHomes;
import me.william278.huskhomes2.messageManager;
import me.william278.huskhomes2.teleportManager;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class rtpCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if (HuskHomes.settings.doRtpCommand()) {
                if (p.getWorld().getEnvironment() == World.Environment.NORMAL) {
                    teleportManager.queueRandomTeleport(p);
                } else {
                    messageManager.sendMessage(p, "error_rtp_invalid_dimension");
                }
            } else {
                messageManager.sendMessage(p, "error_command_disabled");
            }
            return true;
        }
        return false;
    }
}
