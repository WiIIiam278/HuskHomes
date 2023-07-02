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

package net.william278.huskhomes.position;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents metadata about a {@link Position}, used in {@link SavedPosition} implementations.
 */
public class PositionMeta {

    private String name;
    private String description;
    private Map<String, String> tags;
    private Instant creationTime;

    private PositionMeta(@NotNull String name, @NotNull String description,
                         @NotNull Instant creationTime, @Nullable String serializedTags) {
        this.setName(name);
        this.setDescription(description);
        this.setCreationTime(creationTime);
        this.setTags(deserializeTags(serializedTags));
    }

    @NotNull
    public static PositionMeta from(@NotNull String name, @NotNull String description,
                                    @NotNull Instant creationTime, @Nullable String serializedTags) {
        return new PositionMeta(name, description, creationTime, serializedTags);
    }

    @NotNull
    public static PositionMeta create(@NotNull String name, @NotNull String description) {
        return PositionMeta.from(name, description, Instant.now(), "");
    }

    /**
     * Deserialize a JSON string into a {@link Map} of meta tags.
     *
     * @param serializedTags The JSON string to deserialize
     * @return The deserialized {@link Map}
     */
    @NotNull
    private static Map<String, String> deserializeTags(@Nullable String serializedTags) {
        try {
            if (serializedTags == null || serializedTags.isBlank()) {
                return new HashMap<>();
            }
            return new Gson().fromJson(serializedTags, new TypeToken<Map<String, String>>() {
            }.getType());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new HashMap<>();
    }

    /**
     * Serialize a {@link Map} of meta tags into a JSON string.
     *
     * @return The serialized JSON string
     */
    @Nullable
    public String getSerializedTags() {
        try {
            if (getTags().isEmpty()) {
                return null;
            }
            return new Gson().toJson(getTags());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * The name of a position.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * A description of a position.
     */
    @NotNull
    public String getDescription() {
        return description;
    }

    public void setDescription(@Nullable String description) {
        this.description = description;
    }

    /**
     * Map of metadata tags for a position.
     */
    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    /**
     * The time the position was created.
     */
    @NotNull
    public Instant getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Instant creationTime) {
        this.creationTime = creationTime;
    }
}
