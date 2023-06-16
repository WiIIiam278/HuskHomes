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

package net.william278.huskhomes.teleport;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.user.CommandUser;
import org.jetbrains.annotations.NotNull;

public class TeleportationException extends IllegalStateException {

    private final HuskHomes plugin;
    private final Type type;

    public TeleportationException(@NotNull Type cause, @NotNull HuskHomes plugin) {
        super("Error executing teleport: " + cause.name());
        this.type = cause;
        this.plugin = plugin;
    }

    public void displayMessage(@NotNull CommandUser user, @NotNull String... args) {
        switch (type) {
            case TELEPORTER_NOT_FOUND, TARGET_NOT_FOUND -> plugin.getLocales()
                    .getLocale("error_player_not_found", args)
                    .ifPresent(user::sendMessage);
            case ALREADY_WARMING_UP -> plugin.getLocales()
                    .getLocale("error_already_teleporting")
                    .ifPresent(user::sendMessage);
            case WARMUP_ALREADY_MOVING -> plugin.getLocales()
                    .getLocale("error_teleport_warmup_stand_still")
                    .ifPresent(user::sendMessage);
            case CANNOT_TELEPORT_TO_SELF -> plugin.getLocales()
                    .getLocale("error_teleport_request_self")
                    .ifPresent(user::sendMessage);
            case ILLEGAL_TARGET_COORDINATES -> plugin.getLocales()
                    .getLocale("error_illegal_target_coordinates")
                    .ifPresent(user::sendMessage);
            case WORLD_NOT_FOUND -> plugin.getLocales()
                    .getLocale("error_invalid_world")
                    .ifPresent(user::sendMessage);
        }
    }

    @NotNull
    @SuppressWarnings("unused")
    public Type getType() {
        return type;
    }

    /**
     * Represents different causes of {@link TeleportationException}s
     */
    public enum Type {
        TELEPORTER_NOT_FOUND,
        TARGET_NOT_FOUND,
        ALREADY_WARMING_UP,
        TRANSACTION_FAILED,
        WARMUP_ALREADY_MOVING,
        WORLD_NOT_FOUND,
        ILLEGAL_TARGET_COORDINATES,
        CANNOT_TELEPORT_TO_SELF
    }

}
