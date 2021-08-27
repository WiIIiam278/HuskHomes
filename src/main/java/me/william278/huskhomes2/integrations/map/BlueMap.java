package me.william278.huskhomes2.integrations.map;

import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.marker.*;
import me.william278.huskhomes2.HuskHomes;
import me.william278.huskhomes2.data.DataManager;
import me.william278.huskhomes2.teleport.points.Home;
import me.william278.huskhomes2.teleport.points.Warp;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.logging.Level;

public class BlueMap extends Map {

    private static final HuskHomes plugin = HuskHomes.getInstance();
    private static final HashMap<String, Boolean> queuedWarps = new HashMap<>();
    private static final HashMap<String, Boolean> queuedPublicHomes = new HashMap<>();
    private static String warpMarkerImageAddress;
    private static String publicHomeMarkerImageAddress;

    private void executeQueuedOperations() {
        try {
            for (String warpName : queuedWarps.keySet()) {
                boolean add = queuedWarps.get(warpName);
                if (add) {
                    Warp warpToAdd = DataManager.getWarp(warpName, HuskHomes.getConnection());
                    if (warpToAdd != null) {
                        addWarpMarker(warpToAdd);
                    }
                } else {
                    removeWarpMarker(warpName);
                }
            }
            for (String fullHomeName : queuedPublicHomes.keySet()) {
                boolean add = queuedPublicHomes.get(fullHomeName);
                String ownerName = fullHomeName.split("\\.")[0];
                String homeName = fullHomeName.split("\\.")[1];
                if (add) {
                    Home homeToAdd = DataManager.getHome(ownerName, homeName, HuskHomes.getConnection());
                    if (homeToAdd != null) {
                        addPublicHomeMarker(homeToAdd);
                    }
                } else {
                    removePublicHomeMarker(homeName, ownerName);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "An SQL exception occurred retrieving warp and home data when updating the BlueMap.", e);
        }
    }

    @Override
    public void addWarpMarker(Warp warp) {
        World world = warp.getLocation().getWorld();
        if (world == null) {
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> BlueMapAPI.getInstance().ifPresentOrElse(api -> {
            try {
                MarkerAPI markerAPI = api.getMarkerAPI();
                markerAPI.getMarkerSet(WARPS_MARKER_SET_ID).ifPresent(markerSet -> api.getWorld(world.getUID()).ifPresent(blueMapWorld -> {
                    String markerId = getWarpMarkerName(warp.getName());
                    for (BlueMapMap map : blueMapWorld.getMaps()) {
                        POIMarker marker = markerSet.createPOIMarker(markerId, map, warp.getX(), warp.getY(), warp.getZ());
                        marker.setLabel(getWarpInfoMenu(warp));
                        if (warpMarkerImageAddress != null) {
                            marker.setIcon(warpMarkerImageAddress, marker.getAnchor());
                        }
                    }
                    try {
                        markerAPI.save();
                    } catch (IOException ignored) {
                    }
                }));
            } catch (IOException ignored) {
            }
        }, () -> queuedWarps.put(warp.getName(), true)));
    }

    @Override
    public void removeWarpMarker(String warpName) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            // Remove the marker
            BlueMapAPI.getInstance().ifPresentOrElse(api -> {
                try {
                    MarkerAPI markerAPI = api.getMarkerAPI();
                    markerAPI.getMarkerSet(WARPS_MARKER_SET_ID).ifPresent(markerSet -> {
                        String markerId = getWarpMarkerName(warpName);
                        markerSet.removeMarker(markerId);
                        try {
                            markerAPI.save();
                        } catch (IOException ignored) {
                        }
                    });
                } catch (IOException ignored) {
                }
            }, () -> queuedWarps.put(warpName, false));
        });
    }

    @Override
    public void addPublicHomeMarker(Home home) {
        World world = home.getLocation().getWorld();
        if (world == null) {
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> BlueMapAPI.getInstance().ifPresentOrElse(api -> {
            try {
                MarkerAPI markerAPI = api.getMarkerAPI();
                markerAPI.getMarkerSet(PUBLIC_HOMES_MARKER_SET_ID).ifPresent(markerSet -> api.getWorld(world.getUID()).ifPresent(blueMapWorld -> {
                    String markerId = getPublicHomeMarkerName(home.getOwnerUsername(), home.getName());
                    for (BlueMapMap map : blueMapWorld.getMaps()) {
                        POIMarker marker = markerSet.createPOIMarker(markerId, map, home.getX(), home.getY(), home.getZ());
                        marker.setLabel(getPublicHomeInfoMenu(home));
                        if (publicHomeMarkerImageAddress != null) {
                            marker.setIcon(publicHomeMarkerImageAddress, marker.getAnchor());
                        }
                    }
                    try {
                        markerAPI.save();
                    } catch (IOException ignored) {
                    }
                }));
            } catch (IOException ignored) {
            }
        }, () -> queuedPublicHomes.put(home.getOwnerUsername() + "." + home.getName(), true)));
    }

    @Override
    public void removePublicHomeMarker(String homeName, String ownerName) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            // Remove the marker
            BlueMapAPI.getInstance().ifPresentOrElse(api -> {
                try {
                    MarkerAPI markerAPI = api.getMarkerAPI();
                    markerAPI.getMarkerSet(PUBLIC_HOMES_MARKER_SET_ID).ifPresent(markerSet -> {
                        String markerId = getPublicHomeMarkerName(ownerName, homeName);
                        markerSet.removeMarker(markerId);
                        try {
                            markerAPI.save();
                        } catch (IOException ignored) {
                        }
                    });
                } catch (IOException ignored) {
                }
            }, () -> queuedPublicHomes.put(ownerName + "." + homeName, false));
        });
    }

    @Override
    public void initialize() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Connection connection = HuskHomes.getConnection();
            try {
                for (Home home : DataManager.getPublicHomes(connection)) {
                    if (!HuskHomes.getSettings().doBungee() || home.getServer().equals(HuskHomes.getSettings().getServerID())) {
                        addPublicHomeMarker(home);
                    }
                }
                for (Warp warp : DataManager.getWarps(connection)) {
                    if (!HuskHomes.getSettings().doBungee() || warp.getServer().equals(HuskHomes.getSettings().getServerID())) {
                        addWarpMarker(warp);
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.WARNING, "An SQL exception occurred initialising homes and warps onto the BlueMap");
            }

            // Create Marker Set
            BlueMapAPI.onEnable(api -> {
                try {
                    publicHomeMarkerImageAddress = api.createImage(getPublicHomeIcon(), "huskhomes/" + PUBLIC_HOME_MARKER_IMAGE_NAME);
                    warpMarkerImageAddress = api.createImage(getWarpIcon(), "huskhomes/" + WARP_MARKER_IMAGE_NAME);

                    MarkerAPI markerAPI = api.getMarkerAPI();

                    MarkerSet publicHomeMarkerSet = markerAPI.getMarkerSet(PUBLIC_HOMES_MARKER_SET_ID).orElse(markerAPI.createMarkerSet(PUBLIC_HOMES_MARKER_SET_ID));
                    publicHomeMarkerSet.setLabel(HuskHomes.getSettings().getMapPublicHomeMarkerSet());

                    MarkerSet warpsMarkerSet = markerAPI.getMarkerSet(WARPS_MARKER_SET_ID).orElse(markerAPI.createMarkerSet(WARPS_MARKER_SET_ID));
                    warpsMarkerSet.setLabel(HuskHomes.getSettings().getMapWarpMarkerSet());

                    markerAPI.save();
                    plugin.getLogger().info("Enabled BlueMap integration!");

                    executeQueuedOperations();
                } catch (IOException e) {
                    plugin.getLogger().warning("An exception occurred initialising BlueMap.");
                }
            });
        });
    }
}
