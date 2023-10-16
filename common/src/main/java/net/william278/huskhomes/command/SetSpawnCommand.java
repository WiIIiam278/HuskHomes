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
import net.william278.huskhomes.user.OnlineUser;
import net.william278.huskhomes.util.ValidationException;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SetSpawnCommand extends InGameCommand {

    protected SetSpawnCommand(@NotNull HuskHomes plugin) {
        super("setspawn", List.of(), "", plugin);
        setOperatorCommand(true);
    }

    @Override
    public void execute(@NotNull OnlineUser executor, @NotNull String[] args) {
        final Position position = executor.getPosition();
        try {
            if (plugin.getSettings().doCrossServer() && plugin.getSettings().isGlobalSpawn()) {
                final String warpName = plugin.getSettings().getGlobalSpawnName();
                plugin.getManager().warps().createWarp(warpName, position, true);
                plugin.getLocales().getRawLocale("spawn_warp_default_description").ifPresent(
                        description -> plugin.getManager().warps().setWarpDescription(warpName, description)
                );
            } else {
                plugin.setServerSpawn(position);
            }
        } catch (ValidationException e) {
            e.dispatchWarpError(executor, plugin, plugin.getSettings().getGlobalSpawnName());
            return;
        }

        plugin.getLocales().getLocale("set_spawn_success")
                .ifPresent(executor::sendMessage);
    }

}
