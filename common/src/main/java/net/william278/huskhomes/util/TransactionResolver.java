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

package net.william278.huskhomes.util;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.hook.EconomyHook;
import net.william278.huskhomes.user.OnlineUser;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.logging.Level;

/**
 * Class for validating and performing economy and cooldown transactions involving {@link Action}s
 */
public interface TransactionResolver {

    /**
     * Validates whether an {@link OnlineUser} can perform an {@link Action}, in terms of whether they have sufficient
     * funds and are not on cooldown. This method will also send the user a message if they cannot perform the action.
     * <p>
     * This method will return {@code true} if the user has sufficient funds and is not on cooldown, otherwise
     * {@code false}.
     *
     * @param player the {@link OnlineUser player} to perform the check on
     * @param action the {@link Action action} to perform
     * @return {@code true} if the action can be performed, {@code false} otherwise
     */
    default boolean validateTransaction(@NotNull OnlineUser player, @NotNull Action action) {
        return hasFunds(player, action) && isNotOnCooldown(player, action);
    }

    // Validates if the user has funds to perform an action
    private boolean hasFunds(@NotNull OnlineUser player, @NotNull Action action) {
        return getPlugin().getSettings().getEconomyCost(action).map(Math::abs)
                .flatMap(c -> player.hasPermission(Action.BYPASS_ECONOMY_PERMISSION) ? Optional.empty() : Optional.of(c))
                .map(c -> getEconomyHook()
                        .map(hook -> {
                            if (hook.getPlayerBalance(player) < c) {
                                getPlugin().getLocales().getLocale("error_insufficient_funds", hook.formatCurrency(c))
                                        .ifPresent(player::sendMessage);
                                return false;
                            }
                            return true;
                        })
                        .orElse(true))
                .orElse(true);
    }

    // Validates if the user is on cooldown for an action
    private boolean isNotOnCooldown(@NotNull OnlineUser player, @NotNull Action action) {
        final long configCooldown = getPlugin().getSettings().getCooldown(action);
        if (configCooldown <= 0 || player.hasPermission(Action.BYPASS_COOLDOWNS_PERMISSION)) {
            return true;
        }
        return getPlugin().getDatabase().getCooldown(action, player)
                .map(cooldownEnds -> {
                    if (cooldownEnds.isAfter(Instant.now())) {
                        getPlugin().getLocales().getLocale("error_on_cooldown",
                                        formatDuration(Duration.between(Instant.now(), cooldownEnds).abs()))
                                .ifPresent(player::sendMessage);
                        return false;
                    }
                    getPlugin().getDatabase().removeCooldown(action, player);
                    return true;
                })
                .orElse(true);
    }

    // Formats a Duration object into a human-readable days/hours/minutes/seconds string (e.g. "1d 2h 3m 4s")
    @NotNull
    private String formatDuration(@NotNull Duration duration) {
        final long days = duration.toDays();
        final long hours = duration.minusDays(days).toHours();
        final long minutes = duration.minusDays(days).minusHours(hours).toMinutes();
        final long seconds = duration.minusDays(days).minusHours(hours).minusMinutes(minutes).getSeconds();
        final StringJoiner formattedDuration = new StringJoiner(" ");
        if (days > 0) {
            formattedDuration.add(days + "d");
        }
        if (hours > 0) {
            formattedDuration.add(hours + "h");
        }
        if (minutes > 0) {
            formattedDuration.add(minutes + "m");
        }
        if (seconds > 0) {
            formattedDuration.add(seconds + "s");
        }
        return formattedDuration.toString();
    }

    /**
     * Execute an economy transaction if needed, updating the player's balance
     *
     * @param player the {@link OnlineUser player} to deduct the cost from if needed
     * @param action the {@link Action action} to deduct the cost from if needed
     */
    default void performTransaction(@NotNull OnlineUser player, @NotNull Action action) {
        getEconomyHook().ifPresent(hook -> getPlugin().getSettings()
                .getEconomyCost(action).map(Math::abs)
                .flatMap(c -> player.hasPermission(Action.BYPASS_ECONOMY_PERMISSION) ? Optional.empty() : Optional.of(c))
                .ifPresent(cost -> {
                    hook.changePlayerBalance(player, -cost);
                    hook.notifyDeducted(player, getPlugin(), action);
                }));

        final long configCooldown = getPlugin().getSettings().getCooldown(action);
        if (configCooldown > 0 && !player.hasPermission(Action.BYPASS_COOLDOWNS_PERMISSION)) {
            getPlugin().getDatabase().setCooldown(action, player, Instant.now().plusSeconds(configCooldown));
        }
    }

    /**
     * @deprecated use {@link #validateTransaction(OnlineUser, Action)} instead
     */
    @SuppressWarnings("removal")
    @Deprecated(since = "4.4", forRemoval = true)
    default boolean canPerformTransaction(@NotNull OnlineUser player, @NotNull EconomyHook.Action action) {
        return this.validateTransaction(player, action.getTransactionAction());
    }

    /**
     * @deprecated use {@link #performTransaction(OnlineUser, Action)} instead
     */
    @SuppressWarnings("removal")
    @Deprecated(since = "4.4", forRemoval = true)
    default void performTransaction(@NotNull OnlineUser player, @NotNull EconomyHook.Action action) {
        this.performTransaction(player, action.getTransactionAction());
    }

    /**
     * Get the {@link EconomyHook} instance, if present
     *
     * @return the {@link EconomyHook} instance
     */
    @NotNull
    default Optional<EconomyHook> getEconomyHook() {
        return getPlugin().getHook(EconomyHook.class);
    }

    @NotNull
    HuskHomes getPlugin();

    /**
     * Represents actions that can be the subject of a transaction
     */
    enum Action {

        /*
         * Home and public home slots
         */
        ADDITIONAL_HOME_SLOT(100d, 0),
        MAKE_HOME_PUBLIC(50d, 0),
        BACK_COMMAND,

        /*
         * Teleportation actions
         */
        RANDOM_TELEPORT(25d, 600),
        HOME_TELEPORT,
        PUBLIC_HOME_TELEPORT,
        WARP_TELEPORT,
        SPAWN_TELEPORT,

        /*
         * Teleport request actions
         */
        SEND_TELEPORT_REQUEST,
        ACCEPT_TELEPORT_REQUEST;

        public static final String BYPASS_ECONOMY_PERMISSION = "huskhomes.bypass_economy_checks";
        public static final String BYPASS_COOLDOWNS_PERMISSION = "huskhomes.bypass_cooldowns";

        private final double defaultCost;
        private final int defaultCooldown;

        Action(double defaultCost, int defaultCooldown) {
            this.defaultCost = defaultCost;
            this.defaultCooldown = defaultCooldown;
        }

        Action() {
            this(0d, 0);
        }

        /**
         * Create an action with a default cost
         *
         * @return the default cost
         */
        public double getDefaultCost() {
            return defaultCost;
        }

        /**
         * Get the default cooldown for this action (in seconds)
         *
         * @return the default cooldown in seconds
         */
        public int getDefaultCooldown() {
            return defaultCooldown;
        }

    }
}
