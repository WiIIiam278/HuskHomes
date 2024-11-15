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

import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.event.EventHandler;
import net.pl3x.map.core.event.EventListener;
import net.pl3x.map.core.event.server.Pl3xMapDisabledEvent;
import net.pl3x.map.core.event.server.Pl3xMapEnabledEvent;
import net.pl3x.map.core.event.world.WorldLoadedEvent;
import net.pl3x.map.core.event.world.WorldUnloadedEvent;
import net.pl3x.map.core.image.IconImage;
import net.pl3x.map.core.markers.Point;
import net.pl3x.map.core.markers.layer.SimpleLayer;
import net.pl3x.map.core.markers.marker.Icon;
import net.pl3x.map.core.markers.marker.Marker;
import net.pl3x.map.core.markers.option.Options;
import net.pl3x.map.core.markers.option.Popup;
import net.pl3x.map.core.markers.option.Tooltip;
import net.pl3x.map.core.world.World;
import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.config.Settings;
import net.william278.huskhomes.position.Home;
import net.william278.huskhomes.position.SavedPosition;
import net.william278.huskhomes.position.Warp;
import net.william278.huskhomes.user.User;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
@PluginHook(
        name = "Pl3xMap",
        register = PluginHook.Register.ON_ENABLE
)
public class Pl3xMapHook extends MapHook implements EventListener {

    private static final String ICON_PATH = "/images/icon/registered/";
    private static final String WARPS_LAYER = "warp_markers";
    private static final String PUBLIC_HOMES_LAYER = "public_home_markers";
    private final ConcurrentLinkedQueue<Home> publicHomes = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<Warp> warps = new ConcurrentLinkedQueue<>();

    public Pl3xMapHook(@NotNull HuskHomes plugin) {
        super(plugin);
    }

    @Override
    public void load() {
        Pl3xMap.api().getEventRegistry().register(this);
        if (Pl3xMap.api().isEnabled()) {
            onPl3xMapEnabled(new Pl3xMapEnabledEvent());
        }
    }

    @Override
    public void addHome(@NotNull Home home) {
        publicHomes.remove(home);
        if (isValidPosition(home)) {
            publicHomes.add(home);
        }
    }

    @Override
    public void removeHome(@NotNull Home home) {
        publicHomes.remove(home);
    }

    @Override
    public void clearHomes(@NotNull User user) {
        publicHomes.removeIf(home -> home.getOwner().equals(user));
    }

    @Override
    public void clearHomes(@NotNull String worldName) {
        publicHomes.removeIf(home -> home.getWorld().getName().equals(worldName));
    }

    @Override
    public void addWarp(@NotNull Warp warp) {
        warps.remove(warp);
        if (isValidPosition(warp)) {
            warps.add(warp);
        }
    }

    @Override
    public void removeWarp(@NotNull Warp warp) {
        warps.remove(warp);
    }

    @Override
    public void clearWarps() {
        warps.clear();
    }

    @Override
    public void clearWarps(@NotNull String worldName) {
        warps.removeIf(warp -> warp.getWorld().getName().equals(worldName));
    }

    private void registerIcon(@NotNull String key, @NotNull String iconFileName) {
        try (InputStream iconStream = plugin.getResource(iconFileName)) {
            if (iconStream == null) {
                plugin.log(Level.WARNING, "Failed to load Pl3xMap icon (" + key + "): icon file not found");
                return;
            }
            Pl3xMap.api().getIconRegistry().register(new IconImage(key, ImageIO.read(iconStream), "png"));
        } catch (IOException e) {
            plugin.log(Level.WARNING, "Failed to load Pl3xMap icon (" + key + "): " + e.getMessage(), e);
        }
    }

    private void registerLayers(@NotNull World world) {
        final Settings.MapHookSettings settings = plugin.getSettings().getMapHook();
        if (settings.isShowWarps()) {
            WarpsLayer layer = new WarpsLayer(this, world);
            world.getLayerRegistry().register(layer);
        }
        if (settings.isShowPublicHomes()) {
            PublicHomesLayer layer = new PublicHomesLayer(this, world);
            world.getLayerRegistry().register(layer);
        }
    }

    @EventHandler
    public void onPl3xMapEnabled(@NotNull Pl3xMapEnabledEvent event) {
        // Register icons
        final Settings.MapHookSettings settings = plugin.getSettings().getMapHook();
        if (settings.isShowWarps()) {
            this.registerIcon(WARPS_LAYER, "markers/16x/warp.png");
        }
        if (settings.isShowPublicHomes()) {
            this.registerIcon(PUBLIC_HOMES_LAYER, "markers/16x/public-home.png");
        }

        // Register layers for each world
        Pl3xMap.api().getWorldRegistry().forEach(this::registerLayers);

        // Update home positions
        plugin.runAsync(() -> {
            plugin.getDatabase().getLocalPublicHomes(plugin).forEach(this::addHome);
            plugin.getDatabase().getLocalWarps(plugin).forEach(this::addWarp);
        });
    }

    @EventHandler
    public void onPl3xMapDisabled(@NotNull Pl3xMapDisabledEvent event) {
        Pl3xMap.api().getWorldRegistry().forEach(world -> {
            world.getLayerRegistry().unregister(WARPS_LAYER);
            world.getLayerRegistry().unregister(PUBLIC_HOMES_LAYER);
        });
    }

    @EventHandler
    public void onWorldLoaded(@NotNull WorldLoadedEvent event) {
        registerLayers(event.getWorld());
    }

    @EventHandler
    public void onWorldUnloaded(@NotNull WorldUnloadedEvent event) {
        event.getWorld().getLayerRegistry().unregister(WARPS_LAYER);
        event.getWorld().getLayerRegistry().unregister(PUBLIC_HOMES_LAYER);
    }

    @NotNull
    public Options getMarkerOptions(@NotNull SavedPosition position) {
        return Options.builder()
                .tooltip(new Tooltip(position.getIdentifier()))
                .popup(new Popup(position instanceof Home home ? MarkerInformationPopup.publicHome(
                        home, ICON_PATH + WARPS_LAYER
                ).toHtml() : position instanceof Warp warp ? MarkerInformationPopup.warp(
                        warp, ICON_PATH + PUBLIC_HOMES_LAYER
                ).toHtml() : ""))
                .build();
    }

    public static class WarpsLayer extends SimpleLayer {

        private final Pl3xMapHook hook;
        private final World mapWorld;

        public WarpsLayer(@NotNull Pl3xMapHook hook, @NotNull World mapWorld) {
            super(WARPS_LAYER, hook::getWarpsMarkerSetName);
            this.hook = hook;
            this.mapWorld = mapWorld;
        }

        @Override
        @NotNull
        public Collection<Marker<?>> getMarkers() {
            return hook.warps.stream()
                    .filter(warp -> warp.getWorld().getName().equals(mapWorld.getName()))
                    .map(warp -> Icon.of(
                            hook.plugin.getKey("warp_" + warp.getUuid()).asString(),
                            Point.of(warp.getX(), warp.getZ()),
                            WARPS_LAYER
                    ).setOptions(hook.getMarkerOptions(warp)))
                    .collect(Collectors.toCollection(LinkedList::new));
        }

    }

    public static class PublicHomesLayer extends SimpleLayer {

        private final Pl3xMapHook hook;
        private final World mapWorld;

        public PublicHomesLayer(@NotNull Pl3xMapHook hook, @NotNull World mapWorld) {
            super(PUBLIC_HOMES_LAYER, hook::getPublicHomesMarkerSetName);
            this.hook = hook;
            this.mapWorld = mapWorld;
        }

        @Override
        @NotNull
        public Collection<Marker<?>> getMarkers() {
            return hook.publicHomes.stream()
                    .filter(home -> home.getWorld().getName().equals(mapWorld.getName()))
                    .map(home -> Marker.icon(
                            hook.plugin.getKey("public_home_" + home.getUuid()).asString(),
                            Point.of(home.getX(), home.getZ()),
                            PUBLIC_HOMES_LAYER
                    ).setOptions(hook.getMarkerOptions(home)))
                    .collect(Collectors.toCollection(LinkedList::new));
        }

    }

}
