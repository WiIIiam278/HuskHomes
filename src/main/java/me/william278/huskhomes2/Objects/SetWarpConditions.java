package me.william278.huskhomes2.Objects;

import me.william278.huskhomes2.dataManager;

public class SetWarpConditions {

    boolean conditionsMet;
    String conditionsNotMetReason;

    public SetWarpConditions(String warpName) {
        conditionsMet = false;
        if (dataManager.warpExists(warpName)) {
            conditionsNotMetReason = "error_set_warp_name_taken";
            return;
        }
        if (warpName.length() > 16) {
            conditionsNotMetReason = "error_set_warp_invalid_length";
            return;
        }
        if (!warpName.matches("[A-Za-z0-9_\\-]+")) {
            conditionsNotMetReason = "error_set_warp_invalid_characters";
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
