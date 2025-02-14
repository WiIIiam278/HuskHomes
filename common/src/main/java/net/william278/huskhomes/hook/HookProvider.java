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

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.config.Settings;
import net.william278.huskhomes.importer.Importer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public interface HookProvider extends MapHookProvider {

    @NotNull
    Set<Hook> getHooks();

    default <H extends Hook> Optional<H> getHook(@NotNull Class<H> hookClass) {
        return getHooks().stream()
                .filter(hook -> hookClass.isAssignableFrom(hook.getClass()))
                .map(hookClass::cast)
                .findFirst();
    }

    @NotNull
    @Unmodifiable
    default Set<Importer> getImporters() {
        return getHooks().stream()
                .filter(hook -> hook instanceof Importer)
                .map(hook -> (Importer) hook)
                .collect(Collectors.toSet());
    }

    default Optional<Importer> getImporterByName(@NotNull String name) {
        return getImporters().stream()
                .filter(i -> i.getName().toLowerCase(Locale.ENGLISH).equals(name.toLowerCase(Locale.ENGLISH)))
                .findFirst();
    }

    void setHooks(@NotNull Set<Hook> hooks);

    default void loadHooks(@NotNull PluginHook.Register... register) {
        final Set<PluginHook.Register> registers = Arrays.stream(register).collect(Collectors.toSet());
        final List<Hook> load = getAvailableHooks().stream().filter(h -> registers.contains(h.getRegister())).toList();
        getHooks().addAll(load);
    }

    default void registerHooks(@NotNull PluginHook.Register... register) {
        final Set<PluginHook.Register> registers = Arrays.stream(register).collect(Collectors.toSet());
        getHooks().removeIf(hook -> {
            if (!registers.contains(hook.getRegister())) {
                return false;
            }
            try {
                hook.load();
                return false;
            } catch (Throwable e) {
                getPlugin().log(Level.SEVERE, "Failed to register the %s hook".formatted(hook.getName()), e);
            }
            return true;
        });
        getPlugin().log(Level.INFO, "Registered '%s' hooks".formatted(registers.stream()
                .map(h -> h.name().toLowerCase(Locale.ENGLISH)).collect(Collectors.joining(" & "))));
    }

    default void loadAfterLoadHooks() {
        getPlugin().runAsyncDelayed(() -> {
            loadHooks(PluginHook.Register.AFTER_LOAD);
            registerHooks(PluginHook.Register.AFTER_LOAD);
        }, 5 * 20); // 5 secs later
    }

    default void unloadHooks(@NotNull PluginHook.Register... register) {
        final Set<PluginHook.Register> registers = Arrays.stream(register).collect(Collectors.toSet());
        getHooks().removeIf(hook -> {
            if (!registers.contains(hook.getRegister())) {
                return false;
            }
            try {
                hook.unload();
            } catch (Throwable e) {
                getPlugin().log(Level.SEVERE, "Failed to unload the %s hook".formatted(hook.getName()), e);
            }
            return true;
        });
    }

    @NotNull
    default List<Hook> getAvailableHooks() {
        final List<Hook> hooks = Lists.newArrayList();
        final Settings settings = getPlugin().getSettings();

        // Common hooks
        if (isDependencyAvailable("Plan")) {
            hooks.add(new PlanHook(getPlugin()));
        }

        // Map hooks
        if (settings.getMapHook().isEnabled()) {
            if (isDependencyAvailable("Dynmap")) {
                hooks.add(new DynmapHook(getPlugin()));
            } else if (isDependencyAvailable("BlueMap")) {
                hooks.add(new BlueMapHook(getPlugin()));
            } else if (isDependencyAvailable("Pl3xMap")) {
                hooks.add(new Pl3xMapHook(getPlugin()));
            }
        }

        return hooks;
    }

    boolean isDependencyAvailable(@NotNull String name);

    @NotNull
    HuskHomes getPlugin();

}
