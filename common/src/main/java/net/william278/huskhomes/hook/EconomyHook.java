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
import net.william278.huskhomes.config.Locales;
import net.william278.huskhomes.user.OnlineUser;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

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
     * Send the player a message notifying them that they have been charged
     * for performing an action
     *
     * @param user   the user to notify
     * @param plugin the plugin instance
     * @param action the action that was performed
     */
    public final void notifyDeducted(@NotNull OnlineUser user, @NotNull HuskHomes plugin, @NotNull Action action) {
        plugin.getSettings().getEconomyCost(action)
                .flatMap(cost -> plugin.getLocales().getLocale(
                        "economy_transaction_complete",
                        formatCurrency(cost),
                        Locales.escapeText(plugin.getLocales()
                                .getRawLocale("economy_action_" + action.name().toLowerCase(Locale.ENGLISH))
                                .orElse(action.name()))
                ))
                .ifPresent(user::sendMessage);
    }

    /**
     * Identifies actions that have a price associated with performing them
     */
    public enum Action {

        /*
         * Home and public home slots
         */
        ADDITIONAL_HOME_SLOT(100.00),
        MAKE_HOME_PUBLIC(50.00),
        BACK_COMMAND(0.00),

        /*
         * Teleportation actions
         */
        RANDOM_TELEPORT(25.00),
        HOME_TELEPORT(0.00),
        PUBLIC_HOME_TELEPORT(0.00),
        WARP_TELEPORT(0.00),
        SPAWN_TELEPORT(0.00),

        /*
         * Teleport request actions
         */
        SEND_TELEPORT_REQUEST(0.00),
        ACCEPT_TELEPORT_REQUEST(0.00);

        private final double defaultCost;

        Action(double defaultCost) {
            this.defaultCost = defaultCost;
        }

        public double getDefaultCost() {
            return defaultCost;
        }

    }
}
