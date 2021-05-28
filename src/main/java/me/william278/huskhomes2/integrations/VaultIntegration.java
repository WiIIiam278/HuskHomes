package me.william278.huskhomes2.integrations;

import me.william278.huskhomes2.HuskHomes;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultIntegration {
    private static Economy economy = null;
    private static final HuskHomes plugin = HuskHomes.getInstance();

    public static void initializeEconomy() {
        RegisteredServiceProvider<Economy> economyProvider =
                plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }
    }

    public static String format(double monetaryValue) {
        return economy.format(monetaryValue);
    }

    public static boolean hasMoney(Player p, Double amount) {
        Economy e = economy;
        return e.getBalance(p) >= amount;
    }

    public static boolean takeMoney(Player p, Double amount) {
        Economy e = economy;
        if (e.getBalance(p) >= amount) {
            e.withdrawPlayer(p, amount);
            return true;
        } else {
            return false;
        }
    }
}
