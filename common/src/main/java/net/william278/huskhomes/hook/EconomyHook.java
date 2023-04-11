/*
 * This file is part of HuskHomes, licensed under the Apache License 2.0.
 *
 *  Copyright (c) William278 <will27528@gmail.com>
 *  Copyright (c) contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.william278.huskhomes.hook;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.user.OnlineUser;
import org.jetbrains.annotations.NotNull;

/**
 * A hook that provides economy features
 */
public abstract class EconomyHook extends Hook {

    public static final String BYPASS_PERMISSION = "huskhomes.bypass_economy_checks";

    protected EconomyHook(@NotNull HuskHomes plugin, @NotNull String hookName) {
        super(plugin, hookName);
    }

    /**
     * Get the balance of a player
     *
     * @param player the player to get the balance of
     * @return the balance of the player
     */
    public abstract double getPlayerBalance(@NotNull OnlineUser player);

    /**
     * Set the balance of a player
     *
     * @param player the player to set the balance of
     * @param amount the amount to set the balance to
     */
    public abstract void changePlayerBalance(@NotNull OnlineUser player, final double amount);

    /**
     * Format a balance to a string
     *
     * @param amount the amount to format
     * @return the formatted string
     */
    public abstract String formatCurrency(final double amount);

    /**
     * Identifies actions that incur an economic cost if economy is enabled
     */
    public enum Action {
        ADDITIONAL_HOME_SLOT(100.00, "economy_action_additional_home_slot"),
        MAKE_HOME_PUBLIC(50.00, "economy_action_make_home_public"),
        RANDOM_TELEPORT(25.00, "economy_action_random_teleport"),
        BACK_COMMAND(0.00, "economy_action_back_command");

        private final double defaultCost;
        @NotNull
        public final String confirmationLocaleId;

        Action(final double defaultCost, @NotNull String confirmationLocaleId) {
            this.defaultCost = defaultCost;
            this.confirmationLocaleId = confirmationLocaleId;
        }

        public double getDefaultCost() {
            return defaultCost;
        }
    }
}
