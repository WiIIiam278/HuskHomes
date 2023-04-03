package net.william278.huskhomes.hook;

import dev.unnm3d.rediseconomy.api.RedisEconomyAPI;
import net.william278.huskhomes.HuskHomes;
import org.jetbrains.annotations.NotNull;

public class RedisEconomyHook extends VaultEconomyHook {

    public RedisEconomyHook(@NotNull HuskHomes plugin) {
        super(plugin);
    }

    @Override
    public void initialize() {
        RedisEconomyAPI api = RedisEconomyAPI.getAPI();
        if (api != null && !plugin.getSettings().getRedisEconomyName().equalsIgnoreCase("vault")) {
            economy = api.getCurrencyByName(plugin.getSettings().getRedisEconomyName());
            if (economy != null) {
                return;
            }
        }
        super.initialize();
    }
}
