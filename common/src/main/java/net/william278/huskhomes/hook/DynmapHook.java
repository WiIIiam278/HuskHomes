package net.william278.huskhomes.hook;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.position.Home;
import net.william278.huskhomes.position.Warp;
import net.william278.huskhomes.user.User;
import org.apache.commons.text.StringEscapeUtils;
import org.dynmap.DynmapCommonAPI;
import org.dynmap.DynmapCommonAPIListener;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Hook to display warps and public homes on Dynmap
 */
public class DynmapHook extends MapHook {

    @Nullable
    private DynmapCommonAPI dynmapApi;
    @Nullable
    private MarkerSet publicHomesMarkers;
    @Nullable
    private MarkerSet warpsMarkers;

    public DynmapHook(@NotNull HuskHomes plugin) {
        super(plugin, Plugin.DYNMAP);
    }

    @Override
    public void initialize() {
        DynmapCommonAPIListener.register(new DynmapCommonAPIListener() {
            @Override
            public void apiEnabled(@NotNull DynmapCommonAPI dynmapCommonAPI) {
                dynmapApi = dynmapCommonAPI;
                populateMap();
            }
        });
    }

    @Override
    protected void populateMap() {
        getDynmap().ifPresent(api -> {
            if (plugin.getSettings().isPublicHomesOnMap()) {
                getMarkerIcon(PUBLIC_HOME_MARKER_IMAGE_NAME).orElseThrow();
                api.getMarkerAPI().createMarkerSet(getPublicHomesKey(),
                        "Public Homes", api.getMarkerAPI().getMarkerIcons(), false);
            }
            if (plugin.getSettings().isWarpsOnMap()) {
                getMarkerIcon(WARP_MARKER_IMAGE_NAME).orElseThrow();
                api.getMarkerAPI().createMarkerSet(getPublicHomesKey(),
                        "Public Homes", api.getMarkerAPI().getMarkerIcons(), false);
            }
        });
    }

    @Override
    public void updateHome(@NotNull Home home) {
        if (!isValidPosition(home)) {
            return;
        }

        plugin.runSync(() -> {
            final String markerId = home.getOwner().getUuid() + ":" + home.getUuid();
            getPublicHomesMarkerSet().ifPresent(markerSet -> {
                markerSet.getMarkers().stream()
                        .filter(marker -> marker.getMarkerID().equals(markerId))
                        .forEach(Marker::deleteMarker);
                markerSet.createMarker(markerId, home.getName(), home.getWorld().getName(),
                                home.getX(), home.getY(), home.getZ(),
                                getMarkerIcon(PUBLIC_HOME_MARKER_IMAGE_NAME).orElseThrow(), false)
                        .setDescription(MarkerInformationPopup.create(home.getName())
                                .thumbnail(PUBLIC_HOME_MARKER_IMAGE_NAME)
                                .field("Owner", home.getOwner().getUsername())
                                .field("Description", plugin.getLocales().wrapText(home.getMeta().getDescription(), 60))
                                .field("Command", "/phome " + home.getName())
                                .toHtml());
            });
        });
    }

    @Override
    public void removeHome(@NotNull Home home) {
        plugin.runSync(() -> {
            final String markerId = home.getOwner().getUuid() + ":" + home.getUuid();
            getPublicHomesMarkerSet().ifPresent(markerSet -> markerSet.getMarkers().stream()
                    .filter(marker -> marker.getMarkerID().equals(markerId))
                    .forEach(Marker::deleteMarker));
        });
    }

    @Override
    public void clearHomes(@NotNull User user) {
        plugin.runSync(() -> getPublicHomesMarkerSet().ifPresent(markerSet -> markerSet.getMarkers().stream()
                .filter(marker -> marker.getMarkerID().startsWith(user.getUuid().toString()))
                .forEach(Marker::deleteMarker)));
    }

    @Override
    public void updateWarp(@NotNull Warp warp) {
        if (!isValidPosition(warp)) {
            return;
        }

        plugin.runSync(() -> {
            final String markerId = warp.getUuid().toString();
            getWarpsMarkerSet().ifPresent(markerSet -> {
                markerSet.getMarkers().stream()
                        .filter(marker -> marker.getMarkerID().equals(markerId))
                        .forEach(Marker::deleteMarker);
                markerSet.createMarker(markerId, warp.getName(), warp.getWorld().getName(),
                                warp.getX(), warp.getY(), warp.getZ(),
                                getMarkerIcon(WARP_MARKER_IMAGE_NAME).orElseThrow(), false)
                        .setDescription(MarkerInformationPopup.create(warp.getName())
                                .thumbnail(WARP_MARKER_IMAGE_NAME)
                                .field("Description", plugin.getLocales().wrapText(warp.getMeta().getDescription(), 60))
                                .field("Command", "/phome " + warp.getName())
                                .toHtml());
            });
        });
    }

    @Override
    public void removeWarp(@NotNull Warp warp) {
        plugin.runSync(() -> {
            final String markerId = warp.getUuid().toString();
            getWarpsMarkerSet().ifPresent(markerSet -> markerSet.getMarkers().stream()
                    .filter(marker -> marker.getMarkerID().equals(markerId))
                    .forEach(Marker::deleteMarker));
        });
    }

    @Override
    public void clearWarps() {
        plugin.runSync(() -> getWarpsMarkerSet().ifPresent(markerSet -> markerSet.getMarkers()
                .forEach(Marker::deleteMarker)));
    }

    private Optional<DynmapCommonAPI> getDynmap() {
        return Optional.ofNullable(dynmapApi);
    }

    private Optional<MarkerSet> getWarpsMarkerSet() {
        return getDynmap().map(api -> {
            warpsMarkers = api.getMarkerAPI().getMarkerSet(getWarpsKey());
            if (warpsMarkers == null) {
                warpsMarkers = api.getMarkerAPI().createMarkerSet(getWarpsKey(), "Warps",
                        api.getMarkerAPI().getMarkerIcons(), false);
            } else {
                warpsMarkers.setMarkerSetLabel("Warps");
            }
            return warpsMarkers;
        });
    }

    private Optional<MarkerSet> getPublicHomesMarkerSet() {
        return getDynmap().map(api -> {
            publicHomesMarkers = api.getMarkerAPI().getMarkerSet(getPublicHomesKey());
            if (publicHomesMarkers == null) {
                publicHomesMarkers = api.getMarkerAPI().createMarkerSet(getPublicHomesKey(), "Public Homes",
                        api.getMarkerAPI().getMarkerIcons(), false);
            } else {
                publicHomesMarkers.setMarkerSetLabel("Public Homes");
            }
            return publicHomesMarkers;
        });
    }

    private Optional<MarkerIcon> getMarkerIcon(@NotNull String imageName) {
        return getDynmap().map(api -> {
            MarkerIcon icon = api.getMarkerAPI().getMarkerIcon(imageName);
            if (icon == null) {
                icon = api.getMarkerAPI().createMarkerIcon(imageName, imageName,
                        plugin.getResource("markers/16x/" + imageName + ".png"));
            }
            return icon;
        });
    }

    /**
     * Creates an HTML Dynmap marker information popup widget
     */
    private static class MarkerInformationPopup {
        @NotNull
        private final String title;

        @Nullable
        private String thumbnailMarkerId;

        @NotNull
        private final Map<String, String> fields;

        private MarkerInformationPopup(@NotNull String title) {
            this.title = title;
            this.fields = new HashMap<>();
        }

        @NotNull
        private static DynmapHook.MarkerInformationPopup create(@NotNull String title) {
            return new MarkerInformationPopup(title);
        }

        @NotNull
        private DynmapHook.MarkerInformationPopup thumbnail(@NotNull String thumbnailMarkerId) {
            this.thumbnailMarkerId = thumbnailMarkerId;
            return this;
        }

        @NotNull
        private DynmapHook.MarkerInformationPopup field(@NotNull String key, @NotNull String value) {
            fields.put(key, value);
            return this;
        }

        @NotNull
        private String toHtml() {
            final StringBuilder html = new StringBuilder();
            html.append("<div class=\"infowindow\">");
            if (thumbnailMarkerId != null) {
                html.append("<img src=\"/tiles/_markers_/")
                        .append(thumbnailMarkerId)
                        .append(".png\" class=\"thumbnail\"/>")
                        .append("&nbsp;");
            }
            html.append("<span style=\"font-weight: bold;\">")
                    .append(StringEscapeUtils.escapeHtml4(title))
                    .append("</span><br/>");
            fields.forEach((key, value) -> html.append("<span style=\"font-weight: bold;\">")
                    .append(StringEscapeUtils.escapeHtml4(key))
                    .append(": </span><span>")
                    .append(StringEscapeUtils.escapeHtml4(value))
                    .append("</span><br/>"));
            return html.toString();
        }
    }

}
