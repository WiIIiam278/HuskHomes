package net.william278.huskhomes.util;

import net.william278.huskhomes.HuskHomesBukkit;

import java.io.InputStream;

public class BukkitResourceReader implements ResourceReader {

    private final HuskHomesBukkit plugin;

    public BukkitResourceReader(HuskHomesBukkit plugin) {
        this.plugin = plugin;
    }

    @Override
    public InputStream getResource(String fileName) {
        return plugin.getResource(fileName);
    }

}
