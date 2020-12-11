package me.william278.huskhomes2.Objects;

import me.william278.huskhomes2.Main;
import me.william278.huskhomes2.dataManager;
import org.bukkit.entity.Player;

public class SetHomeConditions {

    boolean conditionsMet;
    String conditionsNotMetReason;

    public SetHomeConditions(Player player, String homeName) {
        conditionsMet = false;
        if (dataManager.getPlayerHomeCount(player) > (Main.settings.getMaximumHomes()-1)) {
            conditionsNotMetReason = "error_set_home_maximum_homes";
            return;
        }
        if (dataManager.homeExists(player, homeName)) {
            conditionsNotMetReason = "error_set_home_name_taken";
            return;
        }
        if (homeName.length() > 16) {
            conditionsNotMetReason = "error_set_home_invalid_length";
            return;
        }
        if (!homeName.matches("[A-Za-z0-9_\\-]+")) {
            conditionsNotMetReason = "error_set_home_invalid_characters";
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
