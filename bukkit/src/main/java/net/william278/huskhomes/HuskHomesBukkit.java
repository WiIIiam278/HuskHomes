package net.william278.huskhomes;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import net.william278.huskhomes.config.Messages;
import net.william278.huskhomes.config.Settings;
import net.william278.huskhomes.player.Player;
import net.william278.huskhomes.position.Server;
import net.william278.huskhomes.util.BukkitLogger;
import net.william278.huskhomes.util.Logger;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;

public class HuskHomesBukkit extends JavaPlugin implements HuskHomes {

    private Settings settings;
    private Messages messages;
    private BukkitLogger logger;

    private Server server;

    @Override
    public void onLoad() {

    }

    @Override
    public void onEnable() {
        // Set the logging adapter
        logger = new BukkitLogger(getLogger());

        // Load plugin settings and messages
        loadConfigData();

        //todo set the Server object through sending a plugin message
    }

    @Override
    public void onDisable() {

    }

    @Override
    public Logger getLoggingAdapter() {
        return logger;
    }

    @Override
    public Set<Player> getOnlinePlayers() {
        return null;
    }

    @Override
    public Settings getSettings() {
        return settings;
    }

    @Override
    public Messages getMessages() {
        return messages;
    }

    @Override
    public Server getServerData() {
        return null;
    }

    // Load from the plugin config
    private void loadConfigData() {
        try {
            settings = Settings.load(YamlDocument.create(new File(getDataFolder(), "config.yml"),
                    Objects.requireNonNull(getResource("config.yml")),
                    GeneralSettings.builder().setUseDefaults(false).build(),
                    LoaderSettings.builder().setAutoUpdate(true).build(),
                    DumperSettings.builder().setEncoding(DumperSettings.Encoding.UNICODE).build(),
                    UpdaterSettings.builder().setVersioning(new BasicVersioning("config-version")).build()));
            messages = Messages.load(YamlDocument.create(new File(getDataFolder(),
                            "messages-" + settings.getStringValue(Settings.ConfigOption.LANGUAGE) + ".yml"),
                    Objects.requireNonNull(getResource(
                            "languages/" + settings.getStringValue(Settings.ConfigOption.LANGUAGE) + ".yml"))));
        } catch (IOException | NullPointerException e) {
            getLoggingAdapter().log(Level.SEVERE, "Failed to load data from the config", e);
        }
    }
}
