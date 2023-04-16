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
        super(plugin, "Dynmap");
    }

    @Override
    public void initialize() {
        DynmapCommonAPIListener.register(new DynmapCommonAPIListener() {
            @Override
            public void apiEnabled(@NotNull DynmapCommonAPI dynmapCommonAPI) {
                dynmapApi = dynmapCommonAPI;

                if (plugin.getSettings().doPublicHomesOnMap()) {
                    getMarkerIcon(PUBLIC_HOME_MARKER_IMAGE_NAME).orElseThrow();
                    dynmapApi.getMarkerAPI().createMarkerSet(getPublicHomesKey(),
                            getPublicHomesMarkerSetName(), dynmapApi.getMarkerAPI().getMarkerIcons(), false);
                }
                if (plugin.getSettings().doWarpsOnMap()) {
                    getMarkerIcon(WARP_MARKER_IMAGE_NAME).orElseThrow();
                    dynmapApi.getMarkerAPI().createMarkerSet(getWarpsKey(),
                            getWarpsMarkerSetName(), dynmapApi.getMarkerAPI().getMarkerIcons(), false);
                }

                populateMap();
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
                                .field("Command", "/phome " + home.getIdentifier())
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
                                .field("Command", "/warp " + warp.getName())
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
                warpsMarkers = api.getMarkerAPI().createMarkerSet(getWarpsKey(), getWarpsMarkerSetName(),
                        api.getMarkerAPI().getMarkerIcons(), false);
            } else {
                warpsMarkers.setMarkerSetLabel(getWarpsMarkerSetName());
            }
            return warpsMarkers;
        });
    }

    private Optional<MarkerSet> getPublicHomesMarkerSet() {
        return getDynmap().map(api -> {
            publicHomesMarkers = api.getMarkerAPI().getMarkerSet(getPublicHomesKey());
            if (publicHomesMarkers == null) {
                publicHomesMarkers = api.getMarkerAPI().createMarkerSet(getPublicHomesKey(), getPublicHomesMarkerSetName(),
                        api.getMarkerAPI().getMarkerIcons(), false);
            } else {
                publicHomesMarkers.setMarkerSetLabel(getPublicHomesMarkerSetName());
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
