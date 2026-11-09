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

import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.BlueMapWorld;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import de.bluecolored.bluemap.api.markers.POIMarker;
import net.william278.desertwell.util.ThrowingConsumer;
import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.position.Home;
import net.william278.huskhomes.position.Warp;
import net.william278.huskhomes.position.World;
import net.william278.huskhomes.user.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.logging.Level;

@PluginHook(
        name = "BlueMap",
        register = PluginHook.Register.ON_ENABLE
)
public class BlueMapHook extends MapHook {

    private final Consumer<BlueMapAPI> onEnableListener = this::onBlueMapEnable;
    private final Consumer<BlueMapAPI> onDisableListener = this::onBlueMapDisable;
    private volatile Map<String, MarkerSet> publicHomesMarkerSets;
    private volatile Map<String, MarkerSet> warpsMarkerSets;

    public BlueMapHook(@NotNull HuskHomes plugin) {
        super(plugin);
    }

    @Override
    public void load() {
        // Listeners are invoked immediately if BlueMap is already enabled, and again on every BlueMap reload
        BlueMapAPI.onEnable(onEnableListener);
        BlueMapAPI.onDisable(onDisableListener);
    }

    @Override
    public void unload() {
        BlueMapAPI.unregisterListener(onEnableListener);
        BlueMapAPI.unregisterListener(onDisableListener);

        // Only clear markers if BlueMap is still running; if it has already shut down, its markers are gone
        if (BlueMapAPI.getInstance().isPresent()) {
            super.unload();
        }
        this.publicHomesMarkerSets = null;
        this.warpsMarkerSets = null;
    }

    private void onBlueMapEnable(@NotNull BlueMapAPI api) {
        final Map<String, MarkerSet> homes = new ConcurrentHashMap<>();
        final Map<String, MarkerSet> warps = new ConcurrentHashMap<>();

        for (World world : plugin.getWorlds()) {
            api.getWorld(world.getName()).ifPresent(mapWorld -> {
                final MarkerSet homeMarkers = MarkerSet.builder().label(getPublicHomesMarkerSetName()).build();
                final MarkerSet warpsMarkers = MarkerSet.builder().label(getWarpsMarkerSetName()).build();

                for (BlueMapMap map : mapWorld.getMaps()) {
                    map.getMarkerSets().put(getPublicHomesKey(), homeMarkers);
                    map.getMarkerSets().put(getWarpsKey(), warpsMarkers);
                }

                homes.put(world.getName(), homeMarkers);
                warps.put(world.getName(), warpsMarkers);
            });
        }

        this.publicHomesMarkerSets = homes;
        this.warpsMarkerSets = warps;
        this.populateMap();
    }

    private void onBlueMapDisable(@NotNull BlueMapAPI api) {
        // BlueMap discards its maps (and our marker sets) when it disables or reloads
        this.publicHomesMarkerSets = null;
        this.warpsMarkerSets = null;
    }

    @Override
    public void addHome(@NotNull Home home) {
        if (!isValidPosition(home)) {
            return;
        }

        this.editPublicHomesMarkerSet(home.getWorld(), (markerSet -> {
            final String markerId = home.getOwner().getUuid() + ":" + home.getUuid();
            markerSet.remove(markerId);
            markerSet.put(markerId, POIMarker.builder()
                    .label("/phome " + home.getIdentifier())
                    .position(home.getX(), home.getY(), home.getZ())
                    .maxDistance(5000)
                    .icon(getIcon(PUBLIC_HOME_MARKER_IMAGE_NAME), 25, 25)
                    .build());
        }));
    }

    @Override
    public void removeHome(@NotNull Home home) {
        this.editPublicHomesMarkerSet(home.getWorld(), markerSet -> markerSet
                .remove(home.getOwner().getUuid() + ":" + home.getUuid()));

    }

    @Override
    public void clearHomes(@NotNull User user) {
        final Map<String, MarkerSet> markerSets = publicHomesMarkerSets;
        if (markerSets != null) {
            markerSets.values().forEach(markerSet -> markerSet.getMarkers().keySet()
                    .removeIf(markerId -> markerId.startsWith(user.getUuid().toString())));
        }
    }

    @Override
    public void clearHomes(@NotNull String worldName) {
        final Map<String, MarkerSet> markerSets = publicHomesMarkerSets;
        if (markerSets != null) {
            final MarkerSet markerSet = markerSets.get(worldName);
            if (markerSet != null) {
                markerSet.getMarkers().clear();
            }
        }
    }

    @Override
    public void addWarp(@NotNull Warp warp) {
        if (!isValidPosition(warp)) {
            return;
        }

        this.editWarpsMarkerSet(warp.getWorld(), (markerSet -> {
            final String markerId = warp.getUuid().toString();
            markerSet.remove(markerId);
            markerSet.put(markerId, POIMarker.builder()
                    .label("/warp " + warp.getName())
                    .position(warp.getX(), warp.getY(), warp.getZ())
                    .maxDistance(5000)
                    .icon(getIcon(WARP_MARKER_IMAGE_NAME), 25, 25)
                    .build());
        }));
    }

    @Override
    public void removeWarp(@NotNull Warp warp) {
        editWarpsMarkerSet(warp.getWorld(), markerSet -> markerSet.remove(warp.getUuid().toString()));
    }

    @Override
    public void clearWarps() {
        final Map<String, MarkerSet> markerSets = warpsMarkerSets;
        if (markerSets != null) {
            markerSets.values().forEach(markerSet -> markerSet.getMarkers().clear());
        }
    }

    @Override
    public void clearWarps(@NotNull String worldName) {
        final Map<String, MarkerSet> markerSets = warpsMarkerSets;
        if (markerSets != null) {
            final MarkerSet markerSet = markerSets.get(worldName);
            if (markerSet != null) {
                markerSet.getMarkers().clear();
            }
        }
    }

    @Nullable
    private String getIcon(@NotNull String iconName) {
        return BlueMapAPI.getInstance().map(api -> {
            final Path icons = api.getWebApp().getWebRoot().resolve("icons").resolve("huskhomes");
            if (!icons.toFile().exists() && !icons.toFile().mkdirs()) {
                plugin.log(Level.WARNING, "Failed to create BlueMap icons directory");
            }

            final String iconFileName = iconName + ".png";
            final File iconFile = icons.resolve(iconFileName).toFile();
            if (!iconFile.exists()) {
                try (InputStream readIcon = plugin.getResource("markers/50x/" + iconFileName)) {
                    if (readIcon == null) {
                        throw new FileNotFoundException("Could not find icon resource: " + iconFileName);
                    }
                    Files.copy(readIcon, iconFile.toPath());
                } catch (IOException e) {
                    plugin.log(Level.WARNING, "Failed to load icon for BlueMap hook", e);
                }
            }

            return "icons/huskhomes/" + iconFileName;
        }).orElse(null);
    }

    private void editPublicHomesMarkerSet(@NotNull World world, @NotNull ThrowingConsumer<MarkerSet> editor) {
        editMapWorld(world, (mapWorld -> {
            final Map<String, MarkerSet> markerSets = publicHomesMarkerSets;
            if (markerSets == null) {
                return;
            }
            final MarkerSet markerSet = markerSets.get(world.getName());
            if (markerSet != null) {
                editor.accept(markerSet);
            }
        }));
    }

    private void editWarpsMarkerSet(@NotNull World world, @NotNull ThrowingConsumer<MarkerSet> editor) {
        editMapWorld(world, (mapWorld -> {
            final Map<String, MarkerSet> markerSets = warpsMarkerSets;
            if (markerSets == null) {
                return;
            }
            final MarkerSet markerSet = markerSets.get(world.getName());
            if (markerSet != null) {
                editor.accept(markerSet);
            }
        }));
    }

    private void editMapWorld(@NotNull World world, @NotNull ThrowingConsumer<BlueMapWorld> editor) {
        BlueMapAPI.getInstance().flatMap(api -> api.getWorld(world.getName())).ifPresent(editor);
    }

}
