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

import net.milkbowl.vault.economy.Economy;
import net.william278.huskhomes.BukkitHuskHomes;
import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.user.BukkitUser;
import net.william278.huskhomes.user.OnlineUser;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;

/**
 * A hook that hooks into the Vault API to provide economy features.
 */
public class VaultEconomyHook extends EconomyHook {

    protected Economy economy;

    public VaultEconomyHook(@NotNull HuskHomes plugin) {
        super(plugin, "Vault (Economy)");
    }

    @Override
    public void initialize()  {
        final RegisteredServiceProvider<Economy> economyProvider = ((BukkitHuskHomes) plugin).getServer()
                .getServicesManager().getRegistration(Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }
    }

    @Override
    public double getPlayerBalance(@NotNull OnlineUser player) {
        return economy.getBalance(((BukkitUser) player).getPlayer());
    }

    @Override
    public void changePlayerBalance(@NotNull OnlineUser player, double amount) {
        if (amount != 0d) {
            final Player bukkitPlayer = ((BukkitUser) player).getPlayer();
            final double currentBalance = getPlayerBalance(player);
            final double amountToChange = Math.abs(currentBalance - Math.max(0d, currentBalance + amount));
            if (amount < 0d) {
                economy.withdrawPlayer(bukkitPlayer, amountToChange);
            } else {
                economy.depositPlayer(bukkitPlayer, amountToChange);
            }
        }
    }

    @Override
    public String formatCurrency(double amount) {
        return economy.format(amount);
    }
}
