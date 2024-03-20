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

import net.impactdev.impactor.api.economy.EconomyService;
import net.impactdev.impactor.api.economy.accounts.Account;
import net.impactdev.impactor.api.economy.currency.Currency;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.user.OnlineUser;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.concurrent.ExecutionException;

public class FabricImpactorEconomyHook extends EconomyHook {
    private EconomyService economyService;
    private Currency currency;

    public FabricImpactorEconomyHook(@NotNull HuskHomes plugin) {
        super(plugin, "Fabric Impactor Economy");
    }

    @Override
    public double getPlayerBalance(@NotNull OnlineUser player) {
        try {
            //If no account is found, a new account will be generated instead.
            final Account account = economyService.account(player.getUuid()).get();
            return account.balance().doubleValue();
        } catch (ExecutionException | InterruptedException e) {
            return BigDecimal.ZERO.doubleValue();
        }
    }

    @Override
    public void changePlayerBalance(@NotNull OnlineUser player, double amount) {
        if (amount == 0d) {
            return;
        }
        final Account account;
        try {
            account = economyService.account(player.getUuid()).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        final double currentBalance = account.balance().doubleValue();
        final double amountToChange = Math.abs(currentBalance - Math.max(0d, currentBalance + amount));
        if (amount < 0d) {
            account.withdrawAsync(
                    BigDecimal.valueOf(amountToChange)
            );
        } else {
            account.depositAsync(
                    BigDecimal.valueOf(amountToChange)
            );
        }
    }

    @Override
    public String formatCurrency(double amount) {
        return PlainTextComponentSerializer.plainText().serialize(currency.format(BigDecimal.valueOf(amount)));
    }

    @Override
    public void initialize() {
        this.economyService = EconomyService.instance();
        if (this.economyService == null) {
            throw new IllegalStateException("Impactor API is not available, No economy service has been registered!");
        }
        this.currency = this.economyService.currencies().primary();
    }
}