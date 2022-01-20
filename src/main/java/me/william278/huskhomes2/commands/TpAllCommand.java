package me.william278.huskhomes2.commands;

import me.william278.huskhomes2.HuskHomes;
import me.william278.huskhomes2.util.MessageManager;
import me.william278.huskhomes2.teleport.TeleportManager;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

public class TpAllCommand extends CommandBase {

    @Override
    protected void onCommand(Player p, Command command, String label, String[] args) {
        if (HuskHomes.getPlayerList().getPlayers().size() <= 1) {
            MessageManager.sendMessage(p, "error_no_other_online_players");
            return;
        }
        TeleportManager.teleportAllHere(p);
        MessageManager.sendMessage(p, "teleporting_all_players");
    }

}