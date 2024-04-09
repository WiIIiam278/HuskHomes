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
import net.william278.huskhomes.position.Warp;
import net.william278.huskhomes.user.CommandUser;
import net.william278.huskhomes.util.ValidationException;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DelWarpCommand extends SavedPositionCommand<Warp> {

    public DelWarpCommand(@NotNull HuskHomes plugin) {
        super("delwarp", List.of(), PositionCommandType.WARP, List.of(), plugin);
        setOperatorCommand(true);
    }

    @Override
    public void execute(@NotNull CommandUser executor, @NotNull String[] args) {
        if (handleDeleteAll(executor, args)) {
            return;
        }
        super.execute(executor, args);
    }

    @Override
    public void execute(@NotNull CommandUser executor, @NotNull Warp warp, @NotNull String[] args) {
        if (plugin.getSettings().getGeneral().isPermissionRestrictWarps()
                && !executor.hasPermission(warp.getPermission())
                && !executor.hasPermission(Warp.getWildcardPermission())) {
            plugin.getLocales().getLocale("error_no_permission")
                    .ifPresent(executor::sendMessage);
            return;
        }

        plugin.fireEvent(plugin.getWarpDeleteEvent(warp, executor), (event) -> {
            try {
                plugin.getManager().warps().deleteWarp(warp);
            } catch (ValidationException e) {
                e.dispatchWarpError(executor, plugin, warp.getName());
                return;
            }
            plugin.getLocales().getLocale("warp_deleted", warp.getName())
                    .ifPresent(executor::sendMessage);
        });

    }

    private boolean handleDeleteAll(@NotNull CommandUser executor, @NotNull String[] args) {
        if (args.length >= 1 && args[0].equalsIgnoreCase("all")) {
            if (!parseStringArg(args, 1)
                    .map(confirm -> confirm.equalsIgnoreCase("confirm"))
                    .orElse(false)) {
                plugin.getLocales().getLocale("delete_all_warps_confirm")
                        .ifPresent(executor::sendMessage);
                return true;
            }

            plugin.fireEvent(plugin.getDeleteAllWarpsEvent(executor), (event) -> {
                final int deleted = plugin.getManager().warps().deleteAllWarps();
                if (deleted == 0) {
                    plugin.getLocales().getLocale("error_no_warps_set")
                            .ifPresent(executor::sendMessage);
                    return;
                }

                plugin.getLocales().getLocale("delete_all_warps_success", Integer.toString(deleted))
                        .ifPresent(executor::sendMessage);
            });
            return true;
        }
        return false;
    }

}