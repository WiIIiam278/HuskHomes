package me.william278.huskhomes2.commands;

import me.william278.huskhomes2.HuskHomes;
import me.william278.huskhomes2.MessageManager;
import me.william278.huskhomes2.data.DataManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class TpIgnoreCommand extends CommandBase {

    private static final HuskHomes plugin = HuskHomes.getInstance();

    @Override
    protected void onCommand(Player p, Command command, String label, String[] args) {
        boolean ignoring;
        if (HuskHomes.isIgnoringTeleportRequests(p.getUniqueId())) {
            MessageManager.sendMessage(p, "tpignore_toggle_off");
            ignoring = false;
        } else {
            MessageManager.sendMessage(p, "tpignore_toggle_on");
            ignoring = true;
        }
        setIgnoringRequestsData(p, ignoring);
    }

    private void setIgnoringRequestsData(Player player, boolean ignoring) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                DataManager.setPlayerIgnoringRequests(player, ignoring, HuskHomes.getConnection());
                if (ignoring) {
                    HuskHomes.setIgnoringTeleportRequests(player.getUniqueId());
                } else {
                    HuskHomes.setNotIgnoringTeleportRequests(player.getUniqueId());
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

}
