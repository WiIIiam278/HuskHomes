package me.william278.huskhomes2.commands;

import me.william278.huskhomes2.MessageManager;
import me.william278.huskhomes2.teleport.ListHandler;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

public class HomelistCommand extends CommandBase {

    @Override
    protected boolean onCommand(Player p, Command command, String label, String[] args) {
        int pageNo = 1;
        if (args.length == 1) {
            try {
                pageNo = Integer.parseInt(args[0]);
            } catch (Exception e) {
                MessageManager.sendMessage(p, "error_invalid_syntax", command.getUsage());
                return true;
            }
        }
        ListHandler.displayPlayerHomeList(p, pageNo);
        return true;
    }

}
