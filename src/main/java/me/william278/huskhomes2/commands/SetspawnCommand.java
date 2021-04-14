package me.william278.huskhomes2.commands;

import me.william278.huskhomes2.HuskHomes;
import me.william278.huskhomes2.MessageManager;
import me.william278.huskhomes2.teleport.SettingHandler;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

public class SetspawnCommand extends CommandBase {

    @Override
    protected void onCommand(Player p, Command command, String label, String[] args) {
        if (HuskHomes.getSettings().doSpawnCommand()) {
            if (HuskHomes.getSettings().doCrossServerSpawn()) {
                if (SettingHandler.setCrossServerSpawnWarp(p.getLocation(), p)) {
                    updateSpawnLocation(p);
                }
                return;
            }
            updateSpawnLocation(p);
        } else {
            MessageManager.sendMessage(p, "error_command_disabled");
        }
    }

    // Set the spawn position on the world to the player's location
    private void updateSpawnLocation(Player p) {
        SettingHandler.setSpawnLocation(p.getLocation());
        p.getLocation().getWorld().setSpawnLocation(p.getLocation());
        MessageManager.sendMessage(p, "set_spawn_success");
    }
}
