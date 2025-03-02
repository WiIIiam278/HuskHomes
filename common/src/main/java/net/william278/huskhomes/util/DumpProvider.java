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
import net.william278.huskhomes.hook.Hook;
import net.william278.huskhomes.position.Home;
import net.william278.huskhomes.position.Warp;
import net.william278.huskhomes.user.CommandUser;
import net.william278.huskhomes.user.OnlineUser;
import net.william278.toilet.DumpOptions;
import net.william278.toilet.Toilet;
import net.william278.toilet.dump.DumpUser;
import net.william278.toilet.dump.PluginInfo;
import net.william278.toilet.dump.PluginStatus;
import net.william278.toilet.dump.ProjectMeta;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NotNull;

import static net.william278.toilet.DumpOptions.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public interface DumpProvider {

    @NotNull String BYTEBIN_URL = "https://bytebin.lucko.me";
    @NotNull String VIEWER_URL = "https://william278.net/dump";

    @NotNull
    Toilet getToilet();

    @NotNull
    @Blocking
    default String createDump(@NotNull CommandUser u) {
        return getToilet().dump(getPluginStatus(), u instanceof OnlineUser o
                ? new DumpUser(o.getName(), o.getUuid()) : null).toString();
    }

    @NotNull
    default DumpOptions getDumpOptions() {
        return builder()
                .bytebinUrl(BYTEBIN_URL)
                .viewerUrl(VIEWER_URL)
                .projectMeta(ProjectMeta.builder()
                        .id("huskhomes")
                        .name("HuskHomes")
                        .version(getPlugin().getPluginVersion().toString())
                        .md5("unknown")
                        .author("William278")
                        .sourceCode("https://github.com/WiIIiam278/HuskHomes")
                        .website("https://william278.net/project/huskhomes")
                        .support("https://discord.gg/tVYhJfyDWG")
                        .build())
                .fileInclusionRules(List.of(
                        FileInclusionRule.configFile("config.yml", "Config File"),
                        FileInclusionRule.configFile("spawn.yml", "Spawn File"),
                        FileInclusionRule.configFile(getMessagesFile(), "Locales File")
                ))
                .compatibilityRules(List.of(
                        getCompatibilityWarning("CMI", "CMI may cause compatibility issues with " +
                                "HuskHomes. If you're using Vault, ensure the CMI-compatible version is in use."),
                        getCompatibilityWarning("Essentials", "Command conflicts likely. " +
                                "Check to resolve: https://william278.net/docs/huskhomes/command-conflicts"),
                        getCompatibilityWarning("floodgate", "Ensure the home_delimiter is set to" +
                                "a different value from the Bedrock user prefix in Floodgate/Geyser!")
                ))
                .build();
    }

    @NotNull
    @Blocking
    private PluginStatus getPluginStatus() {
        return PluginStatus.builder()
                .blocks(List.of(getSystemStatus(), getHookStatus(), getPublicHomes(), getWarps()))
                .build();
    }

    @NotNull
    @Blocking
    private PluginStatus.MapStatusBlock getSystemStatus() {
        return new PluginStatus.MapStatusBlock(
                Map.ofEntries(
                        Map.entry("Language", StatusLine.LANGUAGE.getValue(getPlugin())),
                        Map.entry("Database Type", StatusLine.DATABASE_TYPE.getValue(getPlugin())),
                        Map.entry("Database Local", StatusLine.IS_DATABASE_LOCAL.getValue(getPlugin())),
                        Map.entry("Economy Mode", StatusLine.ECONOMY_MODE.getValue(getPlugin())),
                        Map.entry("Home Delimiter", StatusLine.HOME_DELIMITER.getValue(getPlugin())),
                        Map.entry("Cross Server", StatusLine.IS_CROSS_SERVER.getValue(getPlugin())),
                        Map.entry("Server Name", StatusLine.SERVER_NAME.getValue(getPlugin())),
                        Map.entry("Message Broker", StatusLine.MESSAGE_BROKER_TYPE.getValue(getPlugin())),
                        Map.entry("Redis Sentinel", StatusLine.USING_REDIS_SENTINEL.getValue(getPlugin())),
                        Map.entry("Redis Password", StatusLine.USING_REDIS_PASSWORD.getValue(getPlugin())),
                        Map.entry("Redis SSL", StatusLine.REDIS_USING_SSL.getValue(getPlugin())),
                        Map.entry("Redis Local", StatusLine.IS_REDIS_LOCAL.getValue(getPlugin()))
                ), "Plugin Status", "fa6-solid:wrench"
        );
    }

    @NotNull
    @Blocking
    private PluginStatus.ChartStatusBlock getPublicHomes() {
        final List<Home> homes = getPlugin().getDatabase().getLocalPublicHomes(getPlugin());
        final Set<String> worlds = homes.stream().map(l -> l.getWorld().getName()).collect(Collectors.toSet());
        return new PluginStatus.ChartStatusBlock(
                worlds.stream().collect(Collectors.toMap(
                        PluginStatus.ChartKey::new,
                        (w) -> (int) homes.stream().filter(h -> h.getWorld().getName().equals(w)).count(),
                        Integer::sum
                )),
                PluginStatus.ChartType.BAR, "Public Homes by World", "ic:round-home"
        );
    }

    @NotNull
    @Blocking
    private PluginStatus.ChartStatusBlock getWarps() {
        final List<Warp> warps = getPlugin().getDatabase().getLocalWarps(getPlugin());
        final Set<String> worlds = warps.stream().map(l -> l.getWorld().getName()).collect(Collectors.toSet());
        return new PluginStatus.ChartStatusBlock(
                worlds.stream().collect(Collectors.toMap(
                        PluginStatus.ChartKey::new,
                        (w) -> (int) warps.stream().filter(h -> h.getWorld().getName().equals(w)).count(),
                        Integer::sum
                )),
                PluginStatus.ChartType.BAR, "Warps by World", "mdi:location"
        );
    }

    @NotNull
    @Blocking
    private PluginStatus.ListStatusBlock getHookStatus() {
        return new PluginStatus.ListStatusBlock(
                getPlugin().getHooks().stream().map(Hook::getName).toList(),
                "Loaded Hooks", "fa6-solid:plug"
        );
    }

    @NotNull
    @SuppressWarnings("SameParameterValue")
    private CompatibilityRule getCompatibilityWarning(@NotNull String plugin, @NotNull String description) {
        return CompatibilityRule.builder()
                .labelToApply(new PluginInfo.Label("Warning", "#fcba03", description))
                .resourceName(plugin).build();
    }

    @NotNull
    private String getMessagesFile() {
        return "messages-%s.yml".formatted(getPlugin().getSettings().getLanguage());
    }

    @NotNull
    HuskHomes getPlugin();

}