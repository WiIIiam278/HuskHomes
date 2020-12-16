package me.william278.huskhomes2.Objects;

import me.william278.huskhomes2.Integrations.economy;
import me.william278.huskhomes2.HuskHomes;
import me.william278.huskhomes2.dataManager;
import me.william278.huskhomes2.messageManager;
import me.william278.huskhomes2.permissionHomeLimits;
import org.bukkit.entity.Player;

public class SetHomeConditions {

    boolean conditionsMet;
    String conditionsNotMetReason;

    public SetHomeConditions(Player player, String homeName) {
        conditionsMet = false;
        int currentHomeCount = dataManager.getPlayerHomeCount(player);
        if (currentHomeCount > (permissionHomeLimits.getSetHomeLimit(player) - 1)) {
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
        if (HuskHomes.settings.doEconomy()) {
            double setHomeCost = HuskHomes.settings.setHomeCost;
            if (setHomeCost > 0) {
                int currentPlayerHomeSlots = dataManager.getPlayerHomeSlots(player);
                if (currentHomeCount > (currentPlayerHomeSlots - 1)) {
                    if (!economy.takeMoney(player, setHomeCost)) {
                        conditionsNotMetReason = "error_insufficient_funds";
                        return;
                    } else {
                        dataManager.incrementPlayerHomeSlots(player);
                        messageManager.sendMessage(player, "set_home_spent_money", economy.format(setHomeCost));
                    }
                } else if (currentHomeCount == (currentPlayerHomeSlots - 1)) {
                    messageManager.sendMessage(player, "set_home_used_free_slots", Integer.toString(permissionHomeLimits.getFreeHomes(player)), economy.format(setHomeCost));
                }
            }
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
