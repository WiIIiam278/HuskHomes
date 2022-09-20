package net.william278.huskhomes.util;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.plugin.PluginContainer;

import java.io.File;
import java.io.InputStream;
import java.net.URI;

public class SpongeResourceReader implements ResourceReader {

    private final PluginContainer pluginContainer;
    private final File dataFolder;

    public SpongeResourceReader(@NotNull PluginContainer pluginContainer, @NotNull File dataFolder) {
        this.pluginContainer = pluginContainer;
        this.dataFolder = dataFolder;
    }

    @Override
    public InputStream getResource(String fileName) {
        return pluginContainer.openResource(URI.create(fileName)).orElse(null);
    }

    @Override
    public File getDataFolder() {
        return dataFolder;
    }
}
