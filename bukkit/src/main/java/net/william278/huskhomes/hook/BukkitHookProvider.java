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
