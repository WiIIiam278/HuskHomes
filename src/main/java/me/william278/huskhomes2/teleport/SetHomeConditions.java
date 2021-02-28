package me.william278.huskhomes2.teleport;

import me.william278.huskhomes2.integrations.VaultIntegration;
import me.william278.huskhomes2.HuskHomes;
import me.william278.huskhomes2.data.DataManager;
import me.william278.huskhomes2.MessageManager;
import me.william278.huskhomes2.PermissionHomeLimits;
import org.bukkit.entity.Player;

public class SetHomeConditions {

    private boolean conditionsMet;
    private String conditionsNotMetReason;

    public SetHomeConditions(Player player, String homeName) {
        conditionsMet = false;
        int currentHomeCount = DataManager.getPlayerHomeCount(player);
        if (currentHomeCount > (PermissionHomeLimits.getSetHomeLimit(player) - 1)) {
            conditionsNotMetReason = "error_set_home_maximum_homes";
            return;
        }
        if (DataManager.homeExists(player, homeName)) {
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
            double setHomeCost = HuskHomes.settings.getSetHomeCost();
            if (setHomeCost > 0) {
                int currentPlayerHomeSlots = DataManager.getPlayerHomeSlots(player);
                if (currentHomeCount > (currentPlayerHomeSlots - 1)) {
                    if (!VaultIntegration.takeMoney(player, setHomeCost)) {
                        conditionsNotMetReason = "error_insufficient_funds";
                        return;
                    } else {
                        DataManager.incrementPlayerHomeSlots(player);
                        MessageManager.sendMessage(player, "set_home_spent_money", VaultIntegration.format(setHomeCost));
                    }
                } else if (currentHomeCount == (currentPlayerHomeSlots - 1)) {
                    MessageManager.sendMessage(player, "set_home_used_free_slots", Integer.toString(PermissionHomeLimits.getFreeHomes(player)), VaultIntegration.format(setHomeCost));
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
