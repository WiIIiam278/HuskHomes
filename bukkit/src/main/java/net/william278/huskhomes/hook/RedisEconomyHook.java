package net.william278.huskhomes.hook;

import dev.unnm3d.rediseconomy.api.RedisEconomyAPI;
import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.HuskHomesInitializationException;
import org.jetbrains.annotations.NotNull;

public class RedisEconomyHook extends VaultEconomyHook {


    public RedisEconomyHook(@NotNull HuskHomes implementor) {
        super(implementor);
    }


    @Override
    public boolean initialize() throws HuskHomesInitializationException {
        RedisEconomyAPI api=RedisEconomyAPI.getAPI();
        if(api!=null){
            economy=api.getCurrencyByName(plugin.getSettings().currencyName);
            return true;
        }else
            return super.initialize();
    }
}
