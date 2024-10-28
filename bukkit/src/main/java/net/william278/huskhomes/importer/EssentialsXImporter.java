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

package net.william278.huskhomes.importer;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.Warps;
import com.earth2me.essentials.commands.WarpNotFoundException;
import net.william278.huskhomes.BukkitHuskHomes;
import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.hook.PluginHook;
import net.william278.huskhomes.user.User;
import net.william278.huskhomes.util.ValidationException;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

@PluginHook(
        name = "EssentialsX",
        register = PluginHook.Register.ON_LOAD
)
public class EssentialsXImporter extends Importer {

    private final Essentials essentials;

    public EssentialsXImporter(@NotNull HuskHomes plugin) {
        super(List.of(ImportData.HOMES, ImportData.WARPS), plugin);
        this.essentials = (Essentials) ((BukkitHuskHomes) plugin).getServer()
                .getPluginManager().getPlugin("Essentials");
    }

    private int importHomes() {
        final AtomicInteger homesImported = new AtomicInteger();
        for (UUID uuid : essentials.getUsers().getAllUserUUIDs()) {
            // Ensure the user is present and valid
            final com.earth2me.essentials.User essentialsUser = essentials.getUser(uuid);
            if (essentialsUser == null || essentialsUser.getName() == null) {
                continue;
            }
            final User user = User.of(uuid, essentialsUser.getName());
            plugin.getDatabase().ensureUser(user);

            // Create the home
            for (String homeName : essentialsUser.getHomes()) {
                if (essentialsUser.getHome(homeName) == null || essentialsUser.getHome(homeName).getWorld() == null) {
                    continue;
                }
                plugin.getManager().homes().createHome(
                        user,
                        this.normalizeName(homeName),
                        BukkitHuskHomes.Adapter.adapt(essentialsUser.getHome(homeName), plugin.getServerName()),
                        true,
                        true,
                        true
                );
                homesImported.getAndIncrement();
            }
        }
        return homesImported.get();
    }

    private int importWarps() throws Throwable {
        final AtomicInteger warpsImported = new AtomicInteger();
        final Warps warps = essentials.getWarps();
        for (String warpName : warps.getList()) {
            try {
                if (warps.getWarp(warpName) == null || warps.getWarp(warpName).getWorld() == null) {
                    continue;
                }
                plugin.getManager().warps().createWarp(
                        this.normalizeName(warpName),
                        BukkitHuskHomes.Adapter.adapt(warps.getWarp(warpName), plugin.getServerName()),
                        true
                );
            } catch (WarpNotFoundException e) {
                plugin.log(Level.WARNING, String.format("Skipped importing warp %s (could not be found)", warpName));
            }
        }
        return warpsImported.get();
    }

    @NotNull
    private String normalizeName(@NotNull String name) {
        try {
            plugin.validateName(name);
            return name;
        } catch (ValidationException e) {
            // Remove spaces
            name = name.replaceAll(" ", "_");

            // Remove unicode characters
            if (plugin.getSettings().getGeneral().getNames().isRestrict()) {
                name = name.replaceAll(plugin.getSettings().getGeneral().getNames().getRegex(), "");
            }

            // Ensure the name is not blank
            if (name.isBlank()) {
                name = "imported-" + UUID.randomUUID().toString().substring(0, 5);
            }

            // Ensure name is not too long
            if (name.length() > 16) {
                name = name.substring(0, 16);
            }
            return name;
        }
    }

    @Override
    protected int importData(@NotNull Importer.ImportData importData) throws Throwable {
        return switch (importData) {
            case USERS -> 0;
            case HOMES -> importHomes();
            case WARPS -> importWarps();
        };
    }

}
