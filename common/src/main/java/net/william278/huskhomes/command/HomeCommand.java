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
import net.william278.huskhomes.position.Home;
import net.william278.huskhomes.teleport.Teleportable;
import net.william278.huskhomes.user.CommandUser;
import net.william278.huskhomes.user.OnlineUser;
import net.william278.huskhomes.util.TransactionResolver;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class HomeCommand extends SavedPositionCommand<Home> {

    protected HomeCommand(@NotNull String name, @NotNull List<String> aliases, @NotNull PositionCommandType type,
                          @NotNull HuskHomes plugin) {
        super(name, aliases, type, List.of(), plugin);
        addAdditionalPermissions(Map.of("player", true));
    }

    @Override
    public void execute(@NotNull CommandUser executor, @NotNull Home home, @NotNull String[] args) {
        final Optional<Teleportable> optionalTeleporter = resolveTeleporter(executor, args);
        if (optionalTeleporter.isEmpty()) {
            plugin.getLocales().getLocale("error_invalid_syntax", getUsage())
                    .ifPresent(executor::sendMessage);
            return;
        }

        if (executor instanceof OnlineUser user && !user.hasPermission(getOtherPermission())
                && (!home.getOwner().equals(user) && !home.isPublic())) {
            plugin.getLocales().getLocale("error_public_home_invalid",
                            home.getOwner().getUsername(), home.getName())
                    .ifPresent(executor::sendMessage);
            return;
        }

        this.teleport(
                executor, optionalTeleporter.get(), home,
                (executor instanceof OnlineUser user && home.getOwner().equals(user)
                        ? TransactionResolver.Action.HOME_TELEPORT : TransactionResolver.Action.PUBLIC_HOME_TELEPORT)
        );
    }

}
