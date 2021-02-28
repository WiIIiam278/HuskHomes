package me.william278.huskhomes2.commands;

import me.william278.huskhomes2.HuskHomes;
import me.william278.huskhomes2.MessageManager;
import me.william278.huskhomes2.teleport.TeleportManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpawnCommand extends CommandBase {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if (HuskHomes.settings.doSpawnCommand()) {
                if (TeleportManager.spawnLocation != null) {
                    TeleportManager.queueTimedTeleport(p, TeleportManager.spawnLocation);
                } else {
                    MessageManager.sendMessage(p, "error_spawn_undefined");
                }
            } else {
                MessageManager.sendMessage(p, "error_command_disabled");
            }
            return true;
        }
        return false;
    }
}
