package me.william278.huskhomes2.Commands;

import me.william278.huskhomes2.listHandler;
import me.william278.huskhomes2.messageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class publicHomeListCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            int pageNo = 1;
            if (args.length == 1) {
                try {
                    pageNo = Integer.parseInt(args[0]);
                } catch (Exception e) {
                    messageManager.sendMessage(p, "error_invalid_syntax", command.getUsage());
                    return true;
                }
            }
            listHandler.displayPublicHomeList(p, pageNo);
            return true;
        }
        return false;
    }

}
