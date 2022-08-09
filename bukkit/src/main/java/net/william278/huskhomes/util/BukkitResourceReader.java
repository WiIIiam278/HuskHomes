package net.william278.huskhomes.util;

import net.william278.huskhomes.BukkitHuskHomes;

import java.io.File;
import java.io.InputStream;

public class BukkitResourceReader implements ResourceReader {

    private final BukkitHuskHomes plugin;

    public BukkitResourceReader(BukkitHuskHomes plugin) {
        this.plugin = plugin;
    }

    @Override
    public InputStream getResource(String fileName) {
        return plugin.getResource(fileName);
    }

    @Override
    public File getDataFolder() {
        return plugin.getDataFolder();
    }

}
