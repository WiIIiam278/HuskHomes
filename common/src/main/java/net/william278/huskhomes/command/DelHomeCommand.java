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
import net.william278.huskhomes.util.ValidationException;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DelHomeCommand extends SavedPositionCommand<Home> {

    public DelHomeCommand(@NotNull HuskHomes plugin) {
        super(
                List.of("delhome"),
                PositionCommandType.HOME,
                List.of(),
                plugin
        );
    }

    @Override
    public void execute(@NotNull CommandUser executor, @NotNull String[] args) {
        if (executor instanceof OnlineUser user && handleDeleteAll(user, args)) {
            return;
        }
        super.execute(executor, args);
    }

    @Override
    public void execute(@NotNull CommandUser executor, @NotNull Home home, @NotNull String[] args) {
        if (executor instanceof OnlineUser user && !home.getOwner().equals(user)
            && !user.hasPermission(getOtherPermission())) {
            plugin.getLocales().getLocale("error_no_permission")
                    .ifPresent(user::sendMessage);
            return;
        }

        plugin.fireEvent(plugin.getHomeDeleteEvent(home, executor), (event) -> {
            try {
                plugin.getManager().homes().deleteHome(home);
            } catch (ValidationException e) {
                e.dispatchHomeError(executor, !home.getOwner().equals(executor), plugin, home.getName());
                return;
            }
            plugin.getLocales().getLocale("home_deleted", home.getName())
                    .ifPresent(executor::sendMessage);
        });
    }

    private boolean handleDeleteAll(@NotNull OnlineUser user, @NotNull String[] args) {
        if (args.length >= 1 && args[0].equalsIgnoreCase("all")) {
            if (!parseStringArg(args, 1)
                    .map(confirm -> confirm.equalsIgnoreCase("confirm"))
                    .orElse(false)) {
                plugin.getLocales().getLocale("delete_all_homes_confirm")
                        .ifPresent(user::sendMessage);
                return true;
            }

            plugin.fireEvent(plugin.getDeleteAllHomesEvent(user, user), (event) -> {
                final int deleted = plugin.getManager().homes().deleteAllHomes(user);
                if (deleted == 0) {
                    plugin.getLocales().getLocale("error_no_homes_set")
                            .ifPresent(user::sendMessage);
                    return;
                }

                plugin.getLocales().getLocale("delete_all_homes_success", Integer.toString(deleted))
                        .ifPresent(user::sendMessage);
            });
            return true;
        }
        return false;
    }

}
