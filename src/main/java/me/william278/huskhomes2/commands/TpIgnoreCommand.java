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
        if (HuskHomes.isIgnoringTeleportRequests(p.getUniqueId())) {
            MessageManager.sendMessage(p, "tpignore_toggle_off");
            HuskHomes.setNotIgnoringTeleportRequests(p.getUniqueId());
            setIgnoringRequestsData(p, false);
        } else {
            MessageManager.sendMessage(p, "tpignore_toggle_on");
            HuskHomes.setIgnoringTeleportRequests(p.getUniqueId());
            setIgnoringRequestsData(p, true);
        }
    }

    private void setIgnoringRequestsData(Player player, boolean isIgnoring) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                DataManager.setPlayerIgnoringRequests(player, isIgnoring, HuskHomes.getConnection());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

}
