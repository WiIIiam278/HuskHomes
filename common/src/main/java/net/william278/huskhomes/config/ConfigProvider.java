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

package net.william278.huskhomes.config;

import de.exlll.configlib.*;
import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.position.Home;
import net.william278.huskhomes.position.Location;
import net.william278.huskhomes.util.UnsafeBlocks;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public interface ConfigProvider {

    @NotNull
    YamlConfigurationProperties.Builder<?> YAML_CONFIGURATION_PROPERTIES = YamlConfigurationProperties.newBuilder()
            .charset(StandardCharsets.UTF_8)
            .setNameFormatter(NameFormatters.LOWER_UNDERSCORE);


    default void loadConfigs() throws IllegalStateException {
        loadSettings();
        loadLocales();
        loadServer();
        loadServerSpawn();
        loadUnsafeBlocks();
    }

    /**
     * Get the plugin settings, read from the config file.
     *
     * @return the plugin settings
     */
    @NotNull
    Settings getSettings();

    /**
     * Set the plugin settings.
     *
     * @param settings The settings to set
     */
    void setSettings(@NotNull Settings settings);

    /**
     * Load the plugin settings from the config file.
     */
    default void loadSettings() {
        setSettings(YamlConfigurations.update(
                getConfigDirectory().resolve("config.yml"),
                Settings.class,
                YAML_CONFIGURATION_PROPERTIES.header(Settings.CONFIG_HEADER).build()
        ));
        Home.setDelimiter(getSettings().getGeneral().getHomeDelimiter());
    }

    /**
     * Get the locales for the plugin.
     *
     * @return the locales for the plugin
     */
    @NotNull
    Locales getLocales();

    /**
     * Set the locales for the plugin.
     *
     * @param locales The locales to set
     */
    void setLocales(@NotNull Locales locales);

    /**
     * Load the locales from the config file.
     */
    default void loadLocales() {
        final YamlConfigurationStore<Locales> store = new YamlConfigurationStore<>(
                Locales.class, YAML_CONFIGURATION_PROPERTIES.header(Locales.CONFIG_HEADER).build()
        );
        // Read existing locales if present
        final Path path = getConfigDirectory().resolve(String.format("messages-%s.yml", getSettings().getLanguage()));
        if (Files.exists(path)) {
            setLocales(store.load(path));
            return;
        }

        // Otherwise, save and read the default locales
        try (InputStream input = getResource(String.format("locales/%s.yml", getSettings().getLanguage()))) {
            final Locales locales = store.read(input);
            store.save(locales, path);
            setLocales(locales);
        } catch (Throwable e) {
            throw new IllegalStateException("An error occurred loading the locales (invalid lang code?)", e);
        }
    }

    @NotNull
    String getServerName();

    void setServerName(@NotNull Server server);

    default void loadServer() {
        if (getSettings().getCrossServer().isEnabled()) {
            setServerName(YamlConfigurations.update(
                    getConfigDirectory().resolve("server.yml"),
                    Server.class,
                    YAML_CONFIGURATION_PROPERTIES.header(Server.CONFIG_HEADER).build()
            ));
        }
    }

    Optional<Spawn> getServerSpawn();

    void setServerSpawn(@NotNull Spawn spawn);

    /**
     * Update the {@link Spawn} position to a location on the server.
     *
     * @param location the new {@link Spawn} location
     */
    default void setServerSpawn(@NotNull Location location) {
        final Spawn spawn = new Spawn(location);
        setServerSpawn(spawn);
        saveServerSpawn(spawn);
    }

    default void saveServerSpawn(@NotNull Spawn spawn) {
        YamlConfigurations.save(
                getConfigDirectory().resolve("spawn.yml"),
                Spawn.class,
                spawn,
                YAML_CONFIGURATION_PROPERTIES.header(Spawn.CONFIG_HEADER).build()
        );
    }

    /**
     * Load the spawn location from the config file.
     */
    default void loadServerSpawn() {
        final Path spawn = getConfigDirectory().resolve("spawn.yml");
        if (Files.exists(spawn)) {
            setServerSpawn(YamlConfigurations.update(
                    getConfigDirectory().resolve("spawn.yml"),
                    Spawn.class,
                    YAML_CONFIGURATION_PROPERTIES.header(Spawn.CONFIG_HEADER).build()
            ));
        }
    }

    /**
     * Get the unsafe blocks for the plugin.
     *
     * @return the unsafe blocks for the plugin
     */
    @NotNull
    UnsafeBlocks getUnsafeBlocks();

    /**
     * Set the unsafe blocks for the plugin.
     *
     * @param unsafeBlocks The unsafe blocks to set
     */
    void setUnsafeBlocks(@NotNull UnsafeBlocks unsafeBlocks);

    /**
     * Load the unsafe blocks from the internal resource file.
     */
    default void loadUnsafeBlocks() {
        try (InputStream input = getResource("safety/unsafe_blocks.yml")) {
            setUnsafeBlocks(YamlConfigurations.read(
                    input,
                    UnsafeBlocks.class,
                    YAML_CONFIGURATION_PROPERTIES.build()
            ));
        } catch (Throwable e) {
            throw new IllegalStateException("An error occurred loading the unsafe blocks", e);
        }
    }

    /**
     * Get a plugin resource.
     *
     * @param name The name of the resource
     * @return the resource, if found
     */
    InputStream getResource(@NotNull String name);

    /**
     * Get the plugin config directory.
     *
     * @return the plugin config directory
     */
    @NotNull
    Path getConfigDirectory();

    @NotNull
    HuskHomes getPlugin();

}
