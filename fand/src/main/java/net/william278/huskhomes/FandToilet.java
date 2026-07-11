/*
 * This file is part of HuskHomes, licensed under the Apache License 2.0.
 *
 *  Copyright (c) William278 <will27528@gmail.com>
 *  Copyright (c) contributors
 */

package net.william278.huskhomes;

import net.william278.toilet.DumpOptions;
import net.william278.toilet.Toilet;
import net.william278.toilet.dump.PluginInfo;
import net.william278.toilet.dump.ServerMeta;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.List;

final class FandToilet extends Toilet {

    private final FandHuskHomes plugin;

    private FandToilet(@NotNull DumpOptions options, @NotNull FandHuskHomes plugin) {
        super(options);
        this.plugin = plugin;
    }

    static FandToilet create(@NotNull DumpOptions options, @NotNull FandHuskHomes plugin) {
        return new FandToilet(options, plugin);
    }

    @Override
    public List<PluginInfo> getPlugins() {
        return List.of(PluginInfo.builder()
                .name(plugin.getContext().descriptor().id())
                .version(plugin.getContext().descriptor().version())
                .description(plugin.getContext().descriptor().description())
                .authors(plugin.getContext().descriptor().authors())
                .enabled(true)
                .build());
    }

    @Override
    public ServerMeta getServerMeta() {
        return ServerMeta.builder()
                .minecraftVersion(plugin.server().minecraftVersion())
                .serverJarType("fand")
                .serverJarVersion(plugin.server().version())
                .proxyState(ServerMeta.ProxyState.NO_PROXY)
                .onlineMode(true)
                .build();
    }

    @Override
    public Path getProjectConfigDirectory() {
        return plugin.getConfigDirectory();
    }
}
