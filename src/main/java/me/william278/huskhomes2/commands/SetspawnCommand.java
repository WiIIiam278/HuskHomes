package me.william278.huskhomes2.commands;

import me.william278.huskhomes2.HuskHomes;
import me.william278.huskhomes2.MessageManager;
import me.william278.huskhomes2.teleport.SettingHandler;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

public class SetspawnCommand extends CommandBase {

    @Override
    protected boolean onCommand(Player p, Command command, String label, String[] args) {
        if (HuskHomes.settings.doSpawnCommand()) {
            SettingHandler.setSpawnLocation(p.getLocation());
            p.getLocation().getWorld().setSpawnLocation(p.getLocation());
            MessageManager.sendMessage(p, "set_spawn_success");
        } else {
            MessageManager.sendMessage(p, "error_command_disabled");
        }
        return true;
    }
}
