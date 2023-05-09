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
import net.william278.huskhomes.hook.EconomyHook;
import net.william278.huskhomes.manager.RequestsManager;
import net.william278.huskhomes.teleport.TeleportRequest;
import net.william278.huskhomes.user.OnlineUser;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class TeleportRequestCommand extends InGameCommand implements UserListTabProvider {

    private final TeleportRequest.Type requestType;

    protected TeleportRequestCommand(@NotNull HuskHomes plugin, @NotNull TeleportRequest.Type requestType) {
        super(requestType == TeleportRequest.Type.TPA ? "tpa" : "tpahere", List.of(), "<player>", plugin);
        this.requestType = requestType;
    }

    @Override
    public void execute(@NotNull OnlineUser onlineUser, @NotNull String[] args) {
        final RequestsManager manager = plugin.getManager().requests();
        if (manager.isIgnoringRequests(onlineUser)) {
            plugin.getLocales().getLocale("error_ignoring_teleport_requests")
                    .ifPresent(onlineUser::sendMessage);
            return;
        }

        final Optional<String> optionalTarget = parseStringArg(args, 0);
        if (optionalTarget.isEmpty()) {
            plugin.getLocales().getLocale("error_invalid_syntax", getUsage())
                    .ifPresent(onlineUser::sendMessage);
            return;
        }

        // Ensure the user does not send a request to themselves
        final String target = optionalTarget.get();
        if (target.equalsIgnoreCase(onlineUser.getUsername())) {
            plugin.getLocales().getLocale("error_teleport_request_self")
                    .ifPresent(onlineUser::sendMessage);
            return;
        }

        // Validate economy check
        if (!plugin.validateEconomyCheck(onlineUser, EconomyHook.Action.SEND_TELEPORT_REQUEST)) {
            return;
        }

        try {
            manager.sendTeleportRequest(onlineUser, target, requestType);
        } catch (IllegalArgumentException e) {
            plugin.getLocales().getLocale("error_player_not_found", target)
                    .ifPresent(onlineUser::sendMessage);
            return;
        }

        plugin.performEconomyTransaction(onlineUser, EconomyHook.Action.SEND_TELEPORT_REQUEST);
        plugin.getLocales()
                .getLocale((requestType == TeleportRequest.Type.TPA ? "tpa" : "tpahere")
                           + "_request_sent", target)
                .ifPresent(onlineUser::sendMessage);
    }

}
