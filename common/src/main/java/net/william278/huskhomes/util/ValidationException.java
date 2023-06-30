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

package net.william278.huskhomes.util;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.user.CommandUser;
import net.william278.huskhomes.user.OnlineUser;
import org.jetbrains.annotations.NotNull;

public class ValidationException extends IllegalArgumentException {

    private final Type type;

    public ValidationException(@NotNull ValidationException.Type type) {
        super("Error validating SavedPosition: " + type.name());
        this.type = type;
    }

    public void dispatchHomeError(@NotNull CommandUser viewer, boolean other, @NotNull HuskHomes plugin,
                                  @NotNull String... args) {
        switch (type) {
            case NOT_FOUND -> plugin.getLocales()
                    .getLocale(other ? "error_home_invalid_other" : "error_home_invalid", args)
                    .ifPresent(viewer::sendMessage);
            case NAME_TAKEN -> plugin.getLocales()
                    .getLocale("error_home_name_taken")
                    .ifPresent(viewer::sendMessage);
            case NAME_INVALID -> plugin.getLocales()
                    .getLocale("error_home_name_characters", args)
                    .ifPresent(viewer::sendMessage);
            case DESCRIPTION_INVALID -> plugin.getLocales()
                    .getLocale("error_home_description_characters", args)
                    .ifPresent(viewer::sendMessage);
            case NOT_ENOUGH_HOME_SLOTS, REACHED_MAX_HOMES -> plugin.getLocales()
                    .getLocale("error_set_home_maximum_homes",
                            Integer.toString(plugin.getManager().homes()
                            .getMaxHomes(viewer instanceof OnlineUser user ? user : null)))
                    .ifPresent(viewer::sendMessage);
            case REACHED_MAX_PUBLIC_HOMES -> plugin.getLocales()
                    .getLocale("error_edit_home_maximum_public_homes",
                            Integer.toString(plugin.getManager().homes()
                            .getMaxPublicHomes(viewer instanceof OnlineUser user ? user : null)))
                    .ifPresent(viewer::sendMessage);
            default -> {
                // Do nothing (silently handle validation errors)
            }
        }
    }

    public void dispatchWarpError(@NotNull CommandUser viewer, @NotNull HuskHomes plugin, @NotNull String... args) {
        switch (type) {
            case NOT_FOUND -> plugin.getLocales()
                    .getLocale("error_warp_invalid", args)
                    .ifPresent(viewer::sendMessage);
            case NAME_TAKEN -> plugin.getLocales()
                    .getLocale("error_warp_name_taken", args)
                    .ifPresent(viewer::sendMessage);
            case NAME_INVALID -> plugin.getLocales()
                    .getLocale("error_warp_name_characters", args)
                    .ifPresent(viewer::sendMessage);
            case DESCRIPTION_INVALID -> plugin.getLocales()
                    .getLocale("error_warp_description_characters", args)
                    .ifPresent(viewer::sendMessage);
            default -> {
                // Do nothing (silently handle validation errors)
            }
        }
    }

    @NotNull
    @SuppressWarnings("unused")
    public Type getType() {
        return type;
    }

    /**
     * The type of validation error.
     */
    public enum Type {
        NOT_FOUND,
        NAME_TAKEN,
        NAME_INVALID,
        REACHED_MAX_HOMES,
        NOT_ENOUGH_HOME_SLOTS,
        REACHED_MAX_PUBLIC_HOMES,
        TRANSACTION_FAILED,
        DESCRIPTION_INVALID
    }

}
