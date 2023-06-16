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
import net.william278.huskhomes.network.Message;
import net.william278.huskhomes.network.Payload;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.teleport.Teleport;
import net.william278.huskhomes.teleport.TeleportationException;
import net.william278.huskhomes.user.OnlineUser;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TpAllCommand extends InGameCommand {

    protected TpAllCommand(@NotNull HuskHomes plugin) {
        super("tpall", List.of(), "", plugin);
        setOperatorCommand(true);
    }

    @Override
    public void execute(@NotNull OnlineUser executor, @NotNull String[] args) {
        if (plugin.getGlobalPlayerList().size() <= 1) {
            plugin.getLocales().getLocale("error_no_players_online")
                    .ifPresent(executor::sendMessage);
            return;
        }

        final Position targetPosition = executor.getPosition();
        try {
            for (OnlineUser user : plugin.getOnlineUsers()) {
                Teleport.builder(plugin)
                        .teleporter(user)
                        .target(targetPosition)
                        .toTeleport().execute();
            }
        } catch (TeleportationException e) {
            e.displayMessage(executor, args);
            return;
        }

        if (plugin.getSettings().doCrossServer()) {
            Message.builder()
                    .target(Message.TARGET_ALL)
                    .type(Message.Type.TELEPORT_TO_POSITION)
                    .payload(Payload.withPosition(targetPosition))
                    .build().send(plugin.getMessenger(), executor);
        }

        plugin.getLocales().getLocale("teleporting_all_players")
                .ifPresent(executor::sendMessage);
    }

}
