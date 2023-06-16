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
import net.william278.huskhomes.teleport.TeleportationException;
import net.william278.huskhomes.user.OnlineUser;
import net.william278.huskhomes.util.TransactionResolver;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BackCommand extends InGameCommand {

    protected BackCommand(@NotNull HuskHomes plugin) {
        super("back", List.of(), "", plugin);
        addAdditionalPermissions(Map.of(
            "death", false,
            "previous", false
        ));
    }

    @Override
    public void execute(@NotNull OnlineUser executor, @NotNull String[] args) {
        final Optional<Position> lastPosition = plugin.getDatabase().getLastPosition(executor);
        if (lastPosition.isEmpty()) {
            plugin.getLocales().getLocale("error_no_last_position")
                    .ifPresent(executor::sendMessage);
            return;
        }

        try {
            Teleport.builder(plugin)
                    .teleporter(executor)
                    .target(lastPosition.get())
                    .actions(TransactionResolver.Action.BACK_COMMAND)
                    .type(Teleport.Type.BACK)
                    .toTimedTeleport()
                    .execute();
        } catch (TeleportationException e) {
            e.displayMessage(executor, args);
        }
    }

}
