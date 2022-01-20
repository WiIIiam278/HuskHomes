package me.william278.huskhomes2.commands;

import me.william278.huskhomes2.HuskHomes;
import me.william278.huskhomes2.util.MessageManager;
import me.william278.huskhomes2.data.DataManager;
import me.william278.huskhomes2.teleport.TeleportManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;

public class SpawnCommand extends CommandBase {

    private final static HuskHomes plugin = HuskHomes.getInstance();

    @Override
    protected void onCommand(Player p, Command command, String label, String[] args) {
        if (HuskHomes.getSettings().doSpawnCommand()) {
            String spawnWarpName = HuskHomes.getSettings().getSpawnWarpName();
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try (Connection connection = HuskHomes.getConnection()) {
                    // Cross server spawn teleport
                    if (HuskHomes.getSettings().doCrossServerSpawn()) {
                        if (DataManager.warpExists(spawnWarpName, connection)) {
                            TeleportManager.queueTimedTeleport(p, DataManager.getWarp(spawnWarpName, connection));
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
                } catch (SQLException e) {
                    plugin.getLogger().log(Level.SEVERE, "An exception occurred returning back to spawn");
                }
            });
        } else {
            MessageManager.sendMessage(p, "error_command_disabled");
        }
    }
}
