package me.william278.huskhomes2.Commands;

import me.william278.huskhomes2.HuskHomes;
import me.william278.huskhomes2.messageManager;
import me.william278.huskhomes2.settingHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class setSpawnCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if (HuskHomes.settings.doSpawnCommand()) {
                settingHandler.setSpawnLocation(p.getLocation());
                p.getLocation().getWorld().setSpawnLocation(p.getLocation());
                messageManager.sendMessage(p, "set_spawn_success");
            } else {
                messageManager.sendMessage(p, "error_command_disabled");
            }
            return true;
        }
        return false;
    }
}
