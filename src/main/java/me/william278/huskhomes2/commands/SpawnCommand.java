package me.william278.huskhomes2.commands;

import me.william278.huskhomes2.HuskHomes;
import me.william278.huskhomes2.MessageManager;
import me.william278.huskhomes2.data.DataManager;
import me.william278.huskhomes2.teleport.TeleportManager;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

public class SpawnCommand extends CommandBase {

    @Override
    protected void onCommand(Player p, Command command, String label, String[] args) {
        if (HuskHomes.getSettings().doSpawnCommand()) {
            String spawnWarpName = HuskHomes.getSettings().getSpawnWarpName();

            // Cross server spawn teleport
            if (HuskHomes.getSettings().doCrossServerSpawn()) {
                if (DataManager.warpExists(spawnWarpName)) {
                    TeleportManager.queueTimedTeleport(p, DataManager.getWarp(spawnWarpName));
                } else {
                    MessageManager.sendMessage(p, "error_spawn_undefined");
                }
                return;
            }

            // Server-based spawn teleport
            if (TeleportManager.getSpawnLocation() != null) {
                TeleportManager.queueTimedTeleport(p, TeleportManager.getSpawnLocation());
            } else {
                MessageManager.sendMessage(p, "error_spawn_undefined");
            }
        } else {
            MessageManager.sendMessage(p, "error_command_disabled");
        }
    }
}
