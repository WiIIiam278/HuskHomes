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
import net.william278.huskhomes.util.ThrowingConsumer;
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
import java.util.logging.Level;

/**
 * Hook to display warps and public homes on <a href="https://github.com/BlueMap-Minecraft/BlueMap">BlueMap</a> maps
 */
public class BlueMapHook extends MapHook {

    private Map<String, MarkerSet> publicHomesMarkerSets;
    private Map<String, MarkerSet> warpsMarkerSets;

    public BlueMapHook(@NotNull HuskHomes plugin) {
        super(plugin, "BlueMap");
    }

    @Override
    public void initialize() {
        BlueMapAPI.onEnable(api -> {
            this.publicHomesMarkerSets = new HashMap<>();
            this.warpsMarkerSets = new HashMap<>();

            for (World world : plugin.getWorlds()) {
                this.editMapWorld(world, (mapWorld -> {
                    final MarkerSet publicHomeMarkers = MarkerSet.builder().label(getPublicHomesMarkerSetName()).build();
                    final MarkerSet warpsMarkers = MarkerSet.builder().label(getWarpsMarkerSetName()).build();

                    for (BlueMapMap map : mapWorld.getMaps()) {
                        map.getMarkerSets().put(getPublicHomesKey(), publicHomeMarkers);
                        map.getMarkerSets().put(getWarpsKey(), warpsMarkers);
                    }

                    publicHomesMarkerSets.put(world.getName(), publicHomeMarkers);
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
                    .label("/phome " + home.getOwner().getUsername() + "." + home.getName())
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
