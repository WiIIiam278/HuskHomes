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

import java.util.Optional;

/**
 * Utility class for resolving and performing economy transactions through an {@link EconomyHook}
 */
public interface TransactionResolver {

    /**
     * Perform an economy check on the {@link OnlineUser}; returning {@code true} if it passes the check
     *
     * @param player the {@link OnlineUser player} to perform the check on
     * @param action the {@link EconomyHook.Action action} to perform
     * @return {@code true} if the action passes the check, {@code false} if the user has insufficient funds
     */
    default boolean canPerformTransaction(@NotNull OnlineUser player, @NotNull EconomyHook.Action action) {
        return getPlugin().getSettings().getEconomyCost(action)
                .map(Math::abs)
                .flatMap(c -> player.hasPermission(EconomyHook.BYPASS_PERMISSION) ? Optional.empty() : Optional.of(c))
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

    /**
     * Execute an economy transaction if needed, updating the player's balance
     *
     * @param player the {@link OnlineUser player} to deduct the cost from if needed
     * @param action the {@link EconomyHook.Action action} to deduct the cost from if needed
     */
    default void performTransaction(@NotNull OnlineUser player, @NotNull EconomyHook.Action action) {
        getPlugin().getSettings().getEconomyCost(action)
                .map(Math::abs)
                .flatMap(c -> player.hasPermission(EconomyHook.BYPASS_PERMISSION) ? Optional.empty() : Optional.of(c))
                .ifPresent(cost -> getEconomyHook().ifPresent(hook -> {
                    hook.changePlayerBalance(player, -cost);
                    hook.notifyDeducted(player, getPlugin(), action);
                }));
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

}
