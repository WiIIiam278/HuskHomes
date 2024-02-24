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
import net.william278.huskhomes.user.CommandUser;
import net.william278.huskhomes.user.OnlineUser;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PrivateHomeCommand extends HomeCommand {

    protected PrivateHomeCommand(@NotNull HuskHomes plugin) {
        super("home", List.of(), plugin);
    }

    @Override
    public void execute(@NotNull CommandUser executor, @NotNull String[] args) {
        if (args.length == 0) {
            if (!(executor instanceof OnlineUser user)) {
                plugin.getLocales().getLocale("error_invalid_syntax", getUsage())
                        .ifPresent(executor::sendMessage);
                return;
            }

            // If the user has a home, teleport them there, otherwise show them their home list
            final List<Home> homes = plugin.getDatabase().getHomes(user);
            if (homes.size() == 1) {
                super.execute(executor, homes.get(0), args);
                return;
            }
            plugin.getCommand(HomeListCommand.class)
                    .ifPresent(command -> command.showHomeList(executor, user.getUsername(), 1));
            return;
        }
        super.execute(executor, args);
    }

}
