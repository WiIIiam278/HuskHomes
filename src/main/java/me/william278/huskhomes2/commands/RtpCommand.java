package me.william278.huskhomes2.commands;

import me.william278.huskhomes2.HuskHomes;
import me.william278.huskhomes2.MessageManager;
import me.william278.huskhomes2.api.events.PlayerRtpEvent;
import me.william278.huskhomes2.teleport.TeleportManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

public class RtpCommand extends CommandBase {

    @Override
    protected void onCommand(Player p, Command command, String label, String[] args) {
        Bukkit.getPluginManager().callEvent(new PlayerRtpEvent(p,args));

    }
}
