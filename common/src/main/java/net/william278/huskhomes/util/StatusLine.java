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

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.importer.Importer;
import org.apache.commons.text.WordUtils;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public enum StatusLine {
    PLUGIN_VERSION(plugin -> Component.text("v" + plugin.getPluginVersion().toStringWithoutMetadata())
            .appendSpace().append(plugin.getPluginVersion().getMetadata().isBlank() ? Component.empty()
                    : Component.text("(build " + plugin.getPluginVersion().getMetadata() + ")"))),
    SERVER_VERSION(plugin -> Component.text(plugin.getServerType())),
    LANGUAGE(plugin -> Component.text(plugin.getSettings().getLanguage())),
    MINECRAFT_VERSION(plugin -> Component.text(plugin.getMinecraftVersion().toString())),
    JAVA_VERSION(plugin -> Component.text(System.getProperty("java.version"))),
    JAVA_VENDOR(plugin -> Component.text(System.getProperty("java.vendor"))),
    IS_CROSS_SERVER(plugin -> getBoolean(plugin.getSettings().getCrossServer().isEnabled())),
    MESSAGE_BROKER_TYPE(plugin -> Component.text(plugin.getSettings().getCrossServer().getBrokerType().getDisplayName())),
    SERVER_NAME(plugin -> Component.text(plugin.getServerName())),
    DATABASE_TYPE(plugin -> Component.text(plugin.getSettings().getDatabase().getType().getDisplayName())),
    IS_DATABASE_LOCAL(plugin -> getLocalhostBoolean(plugin.getSettings().getDatabase().getCredentials().getHost())),
    USING_REDIS_SENTINEL(plugin -> getBoolean(!plugin.getSettings().getCrossServer().getRedis().getSentinel()
            .getMasterName().isBlank())),
    USING_REDIS_PASSWORD(plugin -> getBoolean(!plugin.getSettings().getCrossServer().getRedis().getPassword()
            .isBlank())),
    REDIS_USING_SSL(plugin -> getBoolean(!plugin.getSettings().getCrossServer().getRedis().isUseSsl())),
    IS_REDIS_LOCAL(plugin -> getLocalhostBoolean(plugin.getSettings().getCrossServer().getRedis().getHost())),
    ECONOMY_MODE(plugin -> getBoolean(plugin.isUsingEconomy())),
    LOADED_HOOKS(plugin -> Component.join(
            JoinConfiguration.commas(true),
            plugin.getHooks().stream().filter(hook -> !(hook instanceof Importer))
                    .map(hook -> Component.text(hook.getName())).toList()
    )),
    LOADED_IMPORTERS(plugin -> Component.join(
            JoinConfiguration.commas(true),
            plugin.getImporters().stream().map(hook -> Component.text(hook.getName())).toList()
    ));

    private final Function<HuskHomes, Component> supplier;

    StatusLine(@NotNull Function<HuskHomes, Component> supplier) {
        this.supplier = supplier;
    }

    @NotNull
    public Component get(@NotNull HuskHomes plugin) {
        return Component
                .text("•").appendSpace()
                .append(Component.text(
                        WordUtils.capitalizeFully(name().replaceAll("_", " ")),
                        TextColor.color(0x848484)
                ))
                .append(Component.text(':')).append(Component.space().color(NamedTextColor.WHITE))
                .append(supplier.apply(plugin));
    }

    @NotNull
    public String getValue(@NotNull HuskHomes plugin) {
        return PlainTextComponentSerializer.plainText().serialize(supplier.apply(plugin));
    }

    @NotNull
    private static Component getBoolean(boolean value) {
        return Component.text(value ? "Yes" : "No", value ? NamedTextColor.GREEN : NamedTextColor.RED);
    }

    @NotNull
    private static Component getLocalhostBoolean(@NotNull String value) {
        return getBoolean(value.equals("127.0.0.1") || value.equals("0.0.0.0")
                || value.equals("localhost") || value.equals("::1"));
    }
}
