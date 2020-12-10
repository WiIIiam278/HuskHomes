package me.william278.huskhomes2;

import me.william278.huskhomes2.Objects.Home;
import me.william278.huskhomes2.Objects.HomeConditionsMet;
import org.bukkit.Location;
import org.bukkit.entity.Player;

// This class handles setting homes, warps and the spawn location.
public class settingHandler {

    // Set a home at the specified position
    public static void setHome(Location location, Player player, String name) {
        HomeConditionsMet homeConditionsMet = new HomeConditionsMet(player);
        if (homeConditionsMet.areConditionsMet()) {
            dataManager.addHome(new Home(location, Main.settings.getServerID(), player, name, false), player);
            messageManager.sendMessage(player, "set_home_success", name);
        } else {
            if (homeConditionsMet.getConditionsNotMetReason().equals("error_set_home_maximum_homes")) {
                messageManager.sendMessage(player, "error_set_home_maximum_homes", Integer.toString(Main.settings.getMaximumHomes()));
            } else {
                messageManager.sendMessage(player, homeConditionsMet.getConditionsNotMetReason());
            }
        }
    }



}
