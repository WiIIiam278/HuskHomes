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
import java.util.logging.Level;

@PluginHook(
        name = "BlueMap",
        register = PluginHook.Register.ON_ENABLE
)
public class BlueMapHook extends MapHook {

    private Map<String, MarkerSet> publicHomesMarkerSets;
    private Map<String, MarkerSet> warpsMarkerSets;

    public BlueMapHook(@NotNull HuskHomes plugin) {
        super(plugin);
    }

    @Override
    public void load() {
        BlueMapAPI.onEnable(api -> {
            this.publicHomesMarkerSets = new ConcurrentHashMap<>();
            this.warpsMarkerSets = new ConcurrentHashMap<>();

            for (World world : plugin.getWorlds()) {
                this.editMapWorld(world, (mapWorld -> {
                    final MarkerSet homeMarkers = MarkerSet.builder().label(getPublicHomesMarkerSetName()).build();
                    final MarkerSet warpsMarkers = MarkerSet.builder().label(getWarpsMarkerSetName()).build();

                    for (BlueMapMap map : mapWorld.getMaps()) {
                        map.getMarkerSets().put(getPublicHomesKey(), homeMarkers);
                        map.getMarkerSets().put(getWarpsKey(), warpsMarkers);
                    }

                    publicHomesMarkerSets.put(world.getName(), homeMarkers);
                    warpsMarkerSets.put(world.getName(), warpsMarkers);
                }));
            }

            this.populateMap();
        });
    }

    @Override
    public void updateHome(@NotNull Home home) {
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
        if (publicHomesMarkerSets != null) {
            publicHomesMarkerSets.values().forEach(markerSet -> markerSet.getMarkers().keySet()
                    .removeIf(markerId -> markerId.startsWith(user.getUuid().toString())));
        }
    }

    @Override
    public void clearHomes(@NotNull String worldName) {
        if (publicHomesMarkerSets != null) {
            publicHomesMarkerSets.get(worldName).getMarkers().clear();
        }
    }

    @Override
    public void updateWarp(@NotNull Warp warp) {
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
        if (warpsMarkerSets != null) {
            warpsMarkerSets.values().forEach(markerSet -> markerSet.getMarkers().clear());
        }
    }

    @Override
    public void clearWarps(@NotNull String worldName) {
        if (warpsMarkerSets != null) {
            warpsMarkerSets.get(worldName).getMarkers().clear();
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
            if (publicHomesMarkerSets != null) {
                editor.accept(publicHomesMarkerSets.get(world.getName()));
            }
        }));
    }

    private void editWarpsMarkerSet(@NotNull World world, @NotNull ThrowingConsumer<MarkerSet> editor) {
        editMapWorld(world, (mapWorld -> {
            if (warpsMarkerSets != null) {
                editor.accept(warpsMarkerSets.get(world.getName()));
            }
        }));
    }

    private void editMapWorld(@NotNull World world, @NotNull ThrowingConsumer<BlueMapWorld> editor) {
        BlueMapAPI.getInstance().flatMap(api -> api.getWorld(world.getName())).ifPresent(editor);
    }

}
