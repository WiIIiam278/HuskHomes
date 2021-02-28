package me.william278.huskhomes2.commands;

import me.william278.huskhomes2.MessageManager;
import me.william278.huskhomes2.teleport.ListHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PublichomelistCommand extends CommandBase {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            int pageNo = 1;
            if (args.length == 1) {
                try {
                    pageNo = Integer.parseInt(args[0]);
                } catch (Exception e) {
                    MessageManager.sendMessage(p, "error_invalid_syntax", command.getUsage());
                    return true;
                }
            }
            ListHandler.displayPublicHomeList(p, pageNo);
            return true;
        }
        return false;
    }

}
