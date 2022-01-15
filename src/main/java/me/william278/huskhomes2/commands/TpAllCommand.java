package me.william278.huskhomes2.commands;

import me.william278.huskhomes2.MessageManager;
import me.william278.huskhomes2.teleport.TeleportManager;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

public class TpAllCommand extends CommandBase {

    @Override
    protected void onCommand(Player p, Command command, String label, String[] args) {
        TeleportManager.teleportAllHere(p);
        MessageManager.sendMessage(p, "teleporting_all_players");
    }

}