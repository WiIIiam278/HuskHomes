package me.william278.huskhomes2.commands;

import me.william278.huskhomes2.HuskHomes;
import me.william278.huskhomes2.messageManager;
import me.william278.huskhomes2.teleportManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class spawnCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if (HuskHomes.settings.doSpawnCommand()) {
                if (teleportManager.spawnLocation != null) {
                    teleportManager.queueTimedTeleport(p, teleportManager.spawnLocation);
                } else {
                    messageManager.sendMessage(p, "error_spawn_undefined");
                }
            } else {
                messageManager.sendMessage(p, "error_command_disabled");
            }
            return true;
        }
        return false;
    }
}
