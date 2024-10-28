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

package net.william278.huskhomes.hook;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.william278.huskhomes.BukkitHuskHomes;
import net.william278.huskhomes.user.BukkitUser;
import net.william278.huskhomes.user.OnlineUser;
import net.william278.huskhomes.user.SavedUser;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

@PluginHook(
        name = "PlaceholderAPI",
        register = PluginHook.Register.ON_LOAD
)
public class PlaceholderAPIHook extends Hook {

    public PlaceholderAPIHook(@NotNull BukkitHuskHomes plugin) {
        super(plugin);
    }

    @Override
    public void load() {
        new HuskHomesExpansion(
                (BukkitHuskHomes) plugin,
                plugin.getPluginVersion().toStringWithoutMetadata()
        ).register();
    }

    @Override
    public void unload() {
    }

    @Getter
    @RequiredArgsConstructor
    public static class HuskHomesExpansion extends PlaceholderExpansion {

        @NotNull
        private final BukkitHuskHomes plugin;
        private final String version;
        private final String author = "William278";
        private final String identifier = "huskhomes";

        @Override
        @Nullable
        public String onRequest(OfflinePlayer offlinePlayer, @NotNull String params) {
            if (offlinePlayer == null || !offlinePlayer.isOnline() || offlinePlayer.getPlayer() == null) {
                return "Player not online";
            }

            // Return the requested data
            final OnlineUser player = BukkitUser.adapt(offlinePlayer.getPlayer(), plugin);
            return switch (params) {
                case "homes_count" -> String.valueOf(plugin.getManager().homes()
                        .getUserHomes()
                        .getOrDefault(player.getName(), List.of()).size());
                case "max_homes" -> String.valueOf(plugin.getManager().homes().getMaxHomes(player));
                case "max_public_homes" -> String.valueOf(plugin.getManager().homes().getMaxPublicHomes(player));
                case "free_home_slots" -> String.valueOf(plugin.getManager().homes().getFreeHomes(player));
                case "home_slots" -> String.valueOf(plugin.getSavedUser(player)
                        .map(SavedUser::getHomeSlots)
                        .orElse(0));
                case "homes_list" -> String.join(", ", plugin.getManager().homes()
                        .getUserHomes()
                        .getOrDefault(player.getName(), List.of()));
                case "public_homes_count" -> String.valueOf(plugin.getManager().homes()
                        .getPublicHomes()
                        .getOrDefault(player.getName(), List.of()).size());
                case "public_homes_list" -> String.join(", ", plugin.getManager().homes()
                        .getPublicHomes()
                        .getOrDefault(player.getName(), List.of()));
                case "ignoring_tp_requests" -> getBooleanValue(plugin.getManager().requests()
                        .isIgnoringRequests(player));
                default -> null;
            };
        }

        @NotNull
        private String getBooleanValue(final boolean bool) {
            return bool ? PlaceholderAPIPlugin.booleanTrue() : PlaceholderAPIPlugin.booleanFalse();
        }

    }

}
