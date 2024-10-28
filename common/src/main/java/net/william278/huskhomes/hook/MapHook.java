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
import net.william278.huskhomes.config.Settings;
import net.william278.huskhomes.position.Home;
import net.william278.huskhomes.position.SavedPosition;
import net.william278.huskhomes.position.Warp;
import net.william278.huskhomes.user.User;
import org.apache.commons.text.StringEscapeUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * A hook for a mapping plugin, such as Dynmap.
 */
public abstract class MapHook extends Hook {

    protected static final String WARP_MARKER_IMAGE_NAME = "warp";
    protected static final String PUBLIC_HOME_MARKER_IMAGE_NAME = "public-home";

    protected MapHook(@NotNull HuskHomes plugin) {
        super(plugin);
    }

    @Override
    public void unload() {
        getPlugin().getWorlds().forEach(world -> clearHomes(world.getName()));
        clearWarps();
    }

    /**
     * Populate the map with public homes and warps.
     */
    protected void populateMap() {
        final Settings.MapHookSettings settings = plugin.getSettings().getMapHook();
        if (settings.isShowPublicHomes()) {
            plugin.getDatabase()
                    .getLocalPublicHomes(plugin)
                    .forEach(this::updateHome);
        }
        if (settings.isShowWarps()) {
            plugin.getDatabase()
                    .getLocalWarps(plugin)
                    .forEach(this::updateWarp);
        }
    }

    /**
     * Update a home, adding it to the map if it exists, or updating it on the map if it doesn't.
     *
     * @param home the home to update
     */
    public abstract void updateHome(@NotNull Home home);

    /**
     * Removes a home from the map.
     *
     * @param home the home to remove
     */
    public abstract void removeHome(@NotNull Home home);

    /**
     * Clears homes owned by a player from the map.
     *
     * @param user the player whose homes to clear
     */
    public abstract void clearHomes(@NotNull User user);

    /**
     * Clears homes on a world from the map.
     *
     * @param worldName the world to clear homes from
     */
    public abstract void clearHomes(@NotNull String worldName);

    /**
     * Update a warp, adding it to the map if it exists, or updating it on the map if it doesn't.
     *
     * @param warp the warp to update
     */
    public abstract void updateWarp(@NotNull Warp warp);

    /**
     * Removes a warp from the map.
     *
     * @param warp the warp to remove
     */
    public abstract void removeWarp(@NotNull Warp warp);

    /**
     * Clears all warps from the map.
     */
    public abstract void clearWarps();

    /**
     * Clears all warps from a world from the map.
     *
     * @param worldName the world to clear warps from
     */
    public abstract void clearWarps(@NotNull String worldName);

    /**
     * Returns if the position is valid to be set on this server.
     *
     * @param position the position to check
     * @return if the position is valid
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    protected final boolean isValidPosition(@NotNull SavedPosition position) {
        final Settings.MapHookSettings settings = plugin.getSettings().getMapHook();
        if (position instanceof Warp && !settings.isShowWarps()) {
            return false;
        }
        if (position instanceof Home && !settings.isShowPublicHomes()) {
            return false;
        }

        return !plugin.getSettings().getCrossServer().isEnabled()
                || position.getServer().equals(plugin.getServerName());
    }

    @NotNull
    protected final String getPublicHomesKey() {
        return plugin.getKey(getName().toLowerCase(), "public_home_markers").toString();
    }

    @NotNull
    protected final String getWarpsKey() {
        return plugin.getKey(getName().toLowerCase(), "warp_markers").toString();
    }

    @NotNull
    protected final String getPublicHomesMarkerSetName() {
        return plugin.getLocales().getRawLocale("map_hook_public_homes_marker_set_name")
                .orElse("Public Homes");
    }

    @NotNull
    protected final String getWarpsMarkerSetName() {
        return plugin.getLocales().getRawLocale("map_hook_warps_marker_set_name")
                .orElse("Warps");
    }

    /**
     * Creates an HTML Dynmap marker information popup widget.
     */
    protected static class MarkerInformationPopup {
        @NotNull
        private final String title;

        @Nullable
        private String thumbnail;

        @NotNull
        private final Map<String, String> fields;

        private MarkerInformationPopup(@NotNull String title) {
            this.title = title;
            this.fields = new HashMap<>();
        }

        @NotNull
        protected static MarkerInformationPopup warp(@NotNull Warp warp, @NotNull String thumbnail) {
            final MarkerInformationPopup popup = MarkerInformationPopup.create(warp.getName())
                    .thumbnail(thumbnail)
                    .field("Location", warp.toString())
                    .field("Command", "/warp " + warp.getSafeIdentifier());
            if (!warp.getMeta().getDescription().isBlank()) {
                popup.field("Description", warp.getMeta().getDescription());
            }
            return popup;
        }

        @NotNull
        protected static MarkerInformationPopup publicHome(@NotNull Home home, @NotNull String thumbnail) {
            final MarkerInformationPopup popup = MarkerInformationPopup.create(home.getName())
                    .thumbnail(thumbnail)
                    .field("Owner", home.getOwner().getName())
                    .field("Location", home.toString())
                    .field("Command", "/phome " + home.getSafeIdentifier());
            if (!home.getMeta().getDescription().isBlank()) {
                popup.field("Description", home.getMeta().getDescription());
            }
            return popup;
        }

        @NotNull
        protected static MarkerInformationPopup create(@NotNull String title) {
            return new MarkerInformationPopup(title);
        }

        @NotNull
        protected MarkerInformationPopup thumbnail(@NotNull String thumbnail) {
            this.thumbnail = thumbnail;
            return this;
        }

        @NotNull
        protected MarkerInformationPopup field(@NotNull String key, @NotNull String value) {
            fields.put(key, value);
            return this;
        }

        @NotNull
        protected String toHtml() {
            final StringBuilder html = new StringBuilder();
            html.append("<div class=\"infowindow\">");
            if (thumbnail != null) {
                html.append("<img src=\"")
                        .append(thumbnail)
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
