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
import net.william278.huskhomes.teleport.Teleport;
import net.william278.huskhomes.teleport.TeleportationException;
import net.william278.huskhomes.user.OnlineUser;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class TpHereCommand extends InGameCommand implements UserListTabProvider {

    protected TpHereCommand(@NotNull HuskHomes plugin) {
        super("tphere", List.of("tpohere"), "<player>", plugin);
        setOperatorCommand(true);
    }

    @Override
    public void execute(@NotNull OnlineUser executor, @NotNull String[] args) {
        final Optional<String> optionalTarget = parseStringArg(args, 0);
        if (optionalTarget.isEmpty()) {
            plugin.getLocales().getLocale("error_invalid_syntax", getUsage())
                    .ifPresent(executor::sendMessage);
            return;
        }

        try {
            Teleport.builder(plugin)
                    .executor(executor)
                    .teleporter(optionalTarget.get())
                    .target(executor.getPosition())
                    .toTeleport().execute();

            plugin.getLocales().getLocale("teleporting_other_complete",
                    optionalTarget.get(), executor.getUsername());
        } catch (TeleportationException e) {
            e.displayMessage(executor, plugin, args);
        }
    }

}
