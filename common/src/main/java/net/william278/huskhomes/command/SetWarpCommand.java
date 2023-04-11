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
import net.william278.huskhomes.user.OnlineUser;
import net.william278.huskhomes.util.ValidationException;
import org.jetbrains.annotations.NotNull;

public class SetWarpCommand extends SetPositionCommand {

    protected SetWarpCommand(@NotNull HuskHomes plugin) {
        super("setwarp", plugin);
        setOperatorCommand(true);
    }

    @Override
    protected void execute(@NotNull OnlineUser setter, @NotNull String name) {
        plugin.fireEvent(plugin.getWarpCreateEvent(name, setter.getPosition(), setter), (event) -> {
            try {
                plugin.getManager().warps().createWarp(event.getName(), event.getPosition());
            } catch (ValidationException e) {
                e.dispatchWarpError(setter, plugin, event.getName());
                return;
            }
            plugin.getLocales().getLocale("set_warp_success", event.getName())
                    .ifPresent(setter::sendMessage);
        });
    }
}
