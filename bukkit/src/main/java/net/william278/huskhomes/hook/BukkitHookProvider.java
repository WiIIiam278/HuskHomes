package net.william278.huskhomes.hook;

import net.william278.huskhomes.BukkitHuskHomes;
import net.william278.huskhomes.importer.EssentialsXImporter;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface BukkitHookProvider extends HookProvider {

    @Override
    @NotNull
    default List<Hook> getAvailableHooks() {
        final List<Hook> hooks = HookProvider.super.getAvailableHooks();

        // Hooks
        if (getPlugin().getSettings().getEconomy().isEnabled() && isDependencyAvailable("Vault")) {
            getHooks().add(new VaultEconomyHook(getPlugin()));
        }
        if (isDependencyAvailable("PlaceholderAPI")) {
            getHooks().add(new PlaceholderAPIHook(getPlugin()));
        }

        // Importers
        if (isDependencyAvailable("Essentials")) {
            getHooks().add(new EssentialsXImporter(getPlugin()));
        }

        return hooks;
    }


    @Override
    default boolean isDependencyAvailable(@NotNull String name) {
        return getPlugin().getServer().getPluginManager().getPlugin(name) != null;
    }

    @Override
    @NotNull
    BukkitHuskHomes getPlugin();

}
