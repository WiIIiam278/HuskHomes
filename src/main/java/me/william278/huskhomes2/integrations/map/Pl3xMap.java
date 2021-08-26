package me.william278.huskhomes2.integrations.map;

import me.william278.huskhomes2.HuskHomes;
import me.william278.huskhomes2.teleport.points.Home;
import me.william278.huskhomes2.teleport.points.Warp;
import net.pl3x.map.api.Key;
import net.pl3x.map.api.Pl3xMapProvider;
import net.pl3x.map.api.Point;
import net.pl3x.map.api.SimpleLayerProvider;
import net.pl3x.map.api.marker.Icon;
import net.pl3x.map.api.marker.Marker;
import net.pl3x.map.api.marker.MarkerOptions;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.HashMap;

public class Pl3xMap extends Map {

    private final HashMap<String,SimpleLayerProvider> publicHomeProviders = new HashMap<>();
    private final HashMap<String,SimpleLayerProvider> warpProviders = new HashMap<>();

    @Override
    public void addWarpMarker(Warp warp) {
        World world = Bukkit.getWorld(warp.getWorldName());
        if (world != null) {
            if (warpProviders.containsKey(world.getName())) {
                Icon marker = Marker.icon(
                        Point.of(warp.getX(), warp.getZ()),
                        Key.of(HuskHomes.getSettings().getMapWarpMarkerIconID()),
                        10);
                marker.markerOptions(MarkerOptions.builder()
                        .hoverTooltip(warp.getName())
                        .clickTooltip(getWarpInfoMenu(warp))
                        .build());
                warpProviders.get(world.getName()).addMarker(Key.of(getWarpMarkerName(warp.getName())), marker);
            }
        }
    }

    @Override
    public void removeWarpMarker(String warpName) {
        String warpMarkerWorld = null;
        final Key warpMarkerKey = Key.of(getWarpMarkerName(warpName));
        for (String worldName : warpProviders.keySet()) {
            SimpleLayerProvider warpProvider = warpProviders.get(worldName);
            if (warpProvider.hasMarker(warpMarkerKey)) {
                warpMarkerWorld = worldName;
                break;
            }
        }
        if (warpMarkerWorld != null) {
            warpProviders.get(warpMarkerWorld).removeMarker(warpMarkerKey);
        }
    }

    @Override
    public void addPublicHomeMarker(Home home) {
        World world = Bukkit.getWorld(home.getWorldName());
        if (world != null) {
            if (publicHomeProviders.containsKey(world.getName())) {
                Icon marker = Marker.icon(
                        Point.of(home.getX(), home.getZ()),
                        Key.of(HuskHomes.getSettings().getMapPublicHomeMarkerIconID()),
                        10);
                marker.markerOptions(MarkerOptions.builder()
                        .hoverTooltip(home.getName())
                        .clickTooltip(getPublicHomeInfoMenu(home))
                        .build());
                publicHomeProviders.get(world.getName()).addMarker(
                        Key.of(getPublicHomeMarkerName(home.getOwnerUsername(), home.getName())), marker);
            }
        }
    }

    @Override
    public void removePublicHomeMarker(String homeName, String ownerName) {
        String publicHomeMarkerWorld = null;
        final Key publicHomeMarkerKey = Key.of(getPublicHomeMarkerName(ownerName, homeName));
        for (String worldName : publicHomeProviders.keySet()) {
            SimpleLayerProvider warpProvider = publicHomeProviders.get(worldName);
            if (warpProvider.hasMarker(publicHomeMarkerKey)) {
                publicHomeMarkerWorld = worldName;
                break;
            }
        }
        if (publicHomeMarkerWorld != null) {
            publicHomeProviders.get(publicHomeMarkerWorld).removeMarker(publicHomeMarkerKey);
        }
    }

    @Override
    public void initialize() {
        net.pl3x.map.api.Pl3xMap mapAPI = Pl3xMapProvider.get();
        for (net.pl3x.map.api.MapWorld world : mapAPI.mapWorlds()) {
            // Register public home map layer
            SimpleLayerProvider publicHomeProvider =
                    SimpleLayerProvider.builder(HuskHomes.getSettings().getMapPublicHomeMarkerSet())
                            .showControls(true)
                            .defaultHidden(false)
                            .layerPriority(6)
                            .zIndex(250)
                            .build();
            world.layerRegistry().register(Key.of(PUBLIC_HOMES_MARKER_SET_ID), publicHomeProvider);
            publicHomeProviders.put(world.name(), publicHomeProvider);

            // Register warps map layer
            SimpleLayerProvider warpProvider =
                    SimpleLayerProvider.builder(HuskHomes.getSettings().getMapPublicHomeMarkerSet())
                            .showControls(true)
                            .defaultHidden(false)
                            .layerPriority(7)
                            .zIndex(250)
                            .build();
            world.layerRegistry().register(Key.of(WARPS_MARKER_SET_ID), warpProvider);
            warpProviders.put(world.name(), warpProvider);
        }
    }

}
