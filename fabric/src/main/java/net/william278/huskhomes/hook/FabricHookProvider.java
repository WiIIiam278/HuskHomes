package net.william278.huskhomes.hook;

import net.fabricmc.loader.api.FabricLoader;
import net.william278.huskhomes.FabricHuskHomes;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;

public interface FabricHookProvider extends HookProvider {

    @Override
    @NotNull
    default List<Hook> getAvailableHooks() {
        final List<Hook> hooks = HookProvider.super.getAvailableHooks();

        // Register the impactor economy service if it is available
        if (getPlugin().getSettings().getEconomy().isEnabled() && isDependencyAvailable("impactor")) {
            getHooks().add(new FabricImpactorEconomyHook(getPlugin()));
        }

        if (isDependencyAvailable("placeholder-api")) {
            getHooks().add(new FabricPlaceholderAPIHook(getPlugin()));
        }

        return hooks;
    }


    @Override
    default boolean isDependencyAvailable(@NotNull String name) {
        return FabricLoader.getInstance().isModLoaded(name)
               || FabricLoader.getInstance().isModLoaded(name.toLowerCase(Locale.ENGLISH));

    }

    @Override
    @NotNull
    FabricHuskHomes getPlugin();

}
