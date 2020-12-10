package me.william278.huskhomes2.Objects;

import me.william278.huskhomes2.Main;
import me.william278.huskhomes2.dataManager;
import org.bukkit.entity.Player;

public class HomeConditionsMet {

    boolean conditionsMet;
    String conditionsNotMetReason;

    public HomeConditionsMet (Player player) {
        if (dataManager.getPlayerHomeCount(player) > (Main.settings.getMaximumHomes()-1)) {
            conditionsMet = false;
            conditionsNotMetReason = "error_set_home_maximum_homes";
            return;
        }
        conditionsMet = true;
    }

    public boolean areConditionsMet() {
        return conditionsMet;
    }

    public String getConditionsNotMetReason() {
        return conditionsNotMetReason;
    }
}
