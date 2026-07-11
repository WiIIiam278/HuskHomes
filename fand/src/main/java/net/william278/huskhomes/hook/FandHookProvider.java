/*
 * This file is part of HuskHomes, licensed under the Apache License 2.0.
 *
 *  Copyright (c) William278 <will27528@gmail.com>
 *  Copyright (c) contributors
 */

package net.william278.huskhomes.hook;

import net.william278.huskhomes.FandHuskHomes;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface FandHookProvider extends HookProvider {

    @Override
    @NotNull
    default List<Hook> getAvailableHooks() {
        return List.of();
    }

    @Override
    default boolean isDependencyAvailable(@NotNull String name) {
        return getPlugin().server().plugins().isEnabled(name.toLowerCase());
    }

    @Override
    @NotNull
    FandHuskHomes getPlugin();
}
