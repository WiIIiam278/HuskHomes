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

package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.teleport.Teleport;
import net.william278.huskhomes.teleport.TeleportBuilder;
import net.william278.huskhomes.teleport.Teleportable;
import net.william278.huskhomes.teleport.TeleportationException;
import net.william278.huskhomes.user.CommandUser;
import net.william278.huskhomes.util.TransactionResolver;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SpawnCommand extends Command {

    protected SpawnCommand(@NotNull HuskHomes plugin) {
        super("spawn", List.of(), "[player]", plugin);
        addAdditionalPermissions(Map.of("other", true));
    }

    @Override
    public void execute(@NotNull CommandUser executor, @NotNull String[] args) {
        final Optional<? extends Position> spawn = plugin.getSpawn();
        if (spawn.isEmpty()) {
            plugin.getLocales().getLocale("error_spawn_not_set")
                    .ifPresent(executor::sendMessage);
            return;
        }

        final Optional<Teleportable> optionalTeleporter = resolveTeleporter(executor, args);
        if (optionalTeleporter.isEmpty()) {
            plugin.getLocales().getLocale("error_invalid_syntax", getUsage())
                    .ifPresent(executor::sendMessage);
            return;
        }

        this.teleportToSpawn(optionalTeleporter.get(), executor, spawn.get(), args);
    }

    public void teleportToSpawn(@NotNull Teleportable teleporter, @NotNull CommandUser executor,
                                @NotNull Position spawn, @NotNull String[] args) {
        if (!executor.equals(teleporter) && !executor.hasPermission(getPermission("other"))) {
            plugin.getLocales().getLocale("error_no_permission")
                    .ifPresent(executor::sendMessage);
            return;
        }

        final TeleportBuilder builder = Teleport.builder(plugin)
                .teleporter(teleporter)
                .actions(TransactionResolver.Action.SPAWN_TELEPORT)
                .target(spawn);
        try {
            if (teleporter.equals(executor)) {
                builder.toTimedTeleport().execute();
            } else {
                builder.toTeleport().execute();
            }
        } catch (TeleportationException e) {
            e.displayMessage(executor, args);
        }
    }

}
