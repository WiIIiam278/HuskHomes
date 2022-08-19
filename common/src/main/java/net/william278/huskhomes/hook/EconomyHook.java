package net.william278.huskhomes.hook;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.player.OnlineUser;
import org.jetbrains.annotations.NotNull;

/**
 * A hook that provides economy features
 */
public abstract class EconomyHook extends PluginHook {

    protected EconomyHook(@NotNull HuskHomes implementor, @NotNull String hookName) {
        super(implementor, hookName);
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

}
