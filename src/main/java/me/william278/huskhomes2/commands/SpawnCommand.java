package me.william278.huskhomes2.commands;

import me.william278.huskhomes2.HuskHomes;
import me.william278.huskhomes2.MessageManager;
import me.william278.huskhomes2.teleport.TeleportManager;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

public class SpawnCommand extends CommandBase {

    @Override
    protected boolean onCommand(Player p, Command command, String label, String[] args) {
        if (HuskHomes.getSettings().doSpawnCommand()) {
            if (TeleportManager.getSpawnLocation() != null) {
                TeleportManager.queueTimedTeleport(p, TeleportManager.getSpawnLocation());
            } else {
                MessageManager.sendMessage(p, "error_spawn_undefined");
            }
        } else {
            MessageManager.sendMessage(p, "error_command_disabled");
        }
        return true;
    }
}
