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
import net.william278.huskhomes.SpongeHuskHomes;
import net.william278.huskhomes.user.OnlineUser;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Server;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.account.Account;

import java.math.BigDecimal;

public class SpongeEconomyHook extends EconomyHook {
    private EconomyService economyService;
    private Currency currency;

    public SpongeEconomyHook(@NotNull HuskHomes plugin) {
        super(plugin, "Sponge Economy");
    }

    @Override
    public double getPlayerBalance(@NotNull OnlineUser player) {
        return economyService.findOrCreateAccount(player.getUuid())
                .map(account -> account.balance(currency))
                .orElse(BigDecimal.ZERO)
                .doubleValue();
    }

    @Override
    public void changePlayerBalance(@NotNull OnlineUser player, double amount) {
        if (amount != 0d) {
            final Account account = economyService.findOrCreateAccount(player.getUuid())
                    .orElseThrow(() -> new IllegalStateException("Account not found for " + player.getUsername()));
            final double currentBalance = getPlayerBalance(player);
            final double amountToChange = Math.abs(currentBalance - Math.max(0d, currentBalance + amount));
            if (amount < 0d) {
                account.withdraw(
                        currency,
                        BigDecimal.valueOf(amountToChange),
                        getServer().causeStackManager().currentCause()
                );
            } else {
                account.deposit(
                        currency,
                        BigDecimal.valueOf(amountToChange),
                        getServer().causeStackManager().currentCause()
                );
            }
        }
    }

    @Override
    public String formatCurrency(double amount) {
        return currency.format(BigDecimal.valueOf(amount)).insertion();
    }

    @Override
    public void initialize() {
        this.economyService = getServer().serviceProvider().economyService()
                .orElseThrow(() -> new IllegalStateException("No economy service has been registered!"));
        this.currency = economyService.defaultCurrency();
    }

    @NotNull
    private Server getServer() {
        return ((SpongeHuskHomes) plugin).getGame().server();
    }

}
