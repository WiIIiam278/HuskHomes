package net.william278.huskhomes.hook;

import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.BlueMapWorld;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import de.bluecolored.bluemap.api.markers.POIMarker;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;

/**
 * Hook to display warps and public homes on <a href="https://github.com/BlueMap-Minecraft/BlueMap">BlueMap</a> maps
 */
public class BlueMapHook extends MapHook {

    private Map<String, MarkerSet> publicHomesMarkerSets;
    private Map<String, MarkerSet> warpsMarkerSets;

    public BlueMapHook(@NotNull HuskHomes plugin) {
        super(plugin, Plugin.BLUEMAP);
    }

    @Override
    public void initialize() {
        BlueMapAPI.onEnable(api -> {
            this.publicHomesMarkerSets = new HashMap<>();
            this.warpsMarkerSets = new HashMap<>();

            for (World world : plugin.getWorlds()) {
                getMapWorld(world).ifPresent(mapWorld -> {
                    final MarkerSet publicHomeMarkers = MarkerSet.builder().label(getPublicHomesMarkerSetName()).build();
                    final MarkerSet warpsMarkers = MarkerSet.builder().label(getWarpsMarkerSetName()).build();

                    for (BlueMapMap map : mapWorld.getMaps()) {
                        map.getMarkerSets().put(plugin.getKey(map.getId()).toString(), publicHomeMarkers);
                        map.getMarkerSets().put(plugin.getKey(map.getId()).toString(), warpsMarkers);
                    }

                    publicHomesMarkerSets.put(world.getName(), publicHomeMarkers);
                    warpsMarkerSets.put(world.getName(), warpsMarkers);
                });
            }

            this.populateMap();
        });
    }

    @Override
    public void updateHome(@NotNull Home home) {
        if (!isValidPosition(home)) {
            return;
        }

        getPublicHomesMarkerSet(home.getWorld())
                .ifPresent(markerSet -> {
                    final String markerId = home.getOwner().getUuid() + ":" + home.getUuid();
                    markerSet.remove(markerId);
                    markerSet.put(markerId, POIMarker.builder()
                            .label("/phome " + home.getOwner().getUsername() + "." + home.getName())
                            .position(home.getX(), home.getY(), home.getZ())
                            .maxDistance(5000)
                            .icon(getIcon(PUBLIC_HOME_MARKER_IMAGE_NAME), 25, 25)
                            .build());
                });
    }

    @Override
    public void removeHome(@NotNull Home home) {
        getPublicHomesMarkerSet(home.getWorld())
                .ifPresent(markerSet -> markerSet.remove(home.getOwner().getUuid() + ":" + home.getUuid()));

    }

    @Override
    public void clearHomes(@NotNull User user) {
        if (publicHomesMarkerSets != null) {
            for (MarkerSet markerSet : publicHomesMarkerSets.values()) {
                markerSet.getMarkers().keySet()
                        .removeIf(markerId -> markerId.startsWith(user.getUuid().toString()));
            }
        }
    }

    @Override
    public void updateWarp(@NotNull Warp warp) {
        if (!isValidPosition(warp)) {
            return;
        }

        getWarpsMarkerSet(warp.getWorld())
                .ifPresent(markerSet -> {
                    final String markerId = warp.getUuid().toString();
                    markerSet.remove(markerId);
                    markerSet.put(markerId, POIMarker.builder()
                            .label("/warp " + warp.getName())
                            .position(warp.getX(), warp.getY(), warp.getZ())
                            .maxDistance(5000)
                            .icon(getIcon(WARP_MARKER_IMAGE_NAME), 25, 25)
                            .build());
                });
    }

    @Override
    public void removeWarp(@NotNull Warp warp) {
        getWarpsMarkerSet(warp.getWorld())
                .ifPresent(markerSet -> markerSet.remove(warp.getUuid().toString()));
    }

    @Override
    public void clearWarps() {
        if (warpsMarkerSets != null) {
            for (MarkerSet markerSet : warpsMarkerSets.values()) {
                markerSet.getMarkers().keySet()
                        .forEach(markerSet::remove);
            }
        }
    }

    @Nullable
    private String getIcon(@NotNull String iconName) {
        return BlueMapAPI.getInstance().map(api -> {
            final Path icons = api.getWebApp().getWebRoot().resolve("icons");
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

            return icons.resolve(iconFile.toPath()).toAbsolutePath().toString();
        }).orElse(null);
    }

    @NotNull
    private Optional<MarkerSet> getPublicHomesMarkerSet(@NotNull World world) {
        return publicHomesMarkerSets == null ? Optional.empty() : Optional.ofNullable(publicHomesMarkerSets.get(world.getName()));
    }

    @NotNull
    private Optional<MarkerSet> getWarpsMarkerSet(@NotNull World world) {
        return warpsMarkerSets == null ? Optional.empty() : Optional.ofNullable(warpsMarkerSets.get(world.getName()));
    }

    @NotNull
    private Optional<BlueMapWorld> getMapWorld(@NotNull World world) {
        return BlueMapAPI.getInstance().flatMap(api -> api.getWorld(world.getName()));
    }

}
