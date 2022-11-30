package net.william278.huskhomes.hook;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.HuskHomesInitializationException;
import net.william278.huskhomes.player.OnlineUser;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.account.Account;

import java.math.BigDecimal;
import java.util.logging.Level;

/**
 * Economy hook implementation on the sponge economy service
 */
public class SpongeEconomyHook extends EconomyHook {
    private EconomyService economyService;
    private Currency currency;

    protected SpongeEconomyHook(@NotNull HuskHomes implementor) {
        super(implementor, "sponge_economy");
    }

    @Override
    public double getPlayerBalance(@NotNull OnlineUser player) {
        return economyService.findOrCreateAccount(player.uuid)
                .map(account -> account.balance(currency))
                .orElse(BigDecimal.ZERO)
                .doubleValue();
    }

    @Override
    public void changePlayerBalance(@NotNull OnlineUser player, double amount) {
        if (amount != 0d) {
            final Account account = economyService.findOrCreateAccount(player.uuid)
                    .orElseThrow(() -> new IllegalStateException("Account not found for " + player.username));
            final double currentBalance = getPlayerBalance(player);
            final double amountToChange = Math.abs(currentBalance - Math.max(0d, currentBalance + amount));
            if (amount < 0d) {
                account.withdraw(currency, BigDecimal.valueOf(amountToChange), Sponge.server().causeStackManager().currentCause());
            } else {
                account.deposit(currency, BigDecimal.valueOf(amountToChange), Sponge.server().causeStackManager().currentCause());
            }
        }
    }

    @Override
    public String formatCurrency(double amount) {
        return currency.format(BigDecimal.valueOf(amount)).insertion();
    }

    @Override
    public boolean initialize() throws HuskHomesInitializationException {
        try {
            this.economyService = Sponge.server().serviceProvider().economyService()
                    .orElseThrow(() -> new IllegalStateException("No economy service has been registered!"));
            this.currency = economyService.defaultCurrency();
            return true;
        } catch (IllegalArgumentException e) {
            plugin.getLoggingAdapter().log(Level.SEVERE, "Failed to initialize Sponge Economy Hook", e);
            return false;
        }
    }
}
