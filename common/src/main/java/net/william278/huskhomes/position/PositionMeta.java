package net.william278.huskhomes.position;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Represents metadata about a {@link Position}, used in {@link SavedPosition} implementations
 */
public class PositionMeta {

    private String name;
    @Nullable
    private String description;
    private Map<String, String> tags;
    private Instant creationTime;

    public PositionMeta(@NotNull String name, @NotNull String description, @NotNull Instant creationTime,
                        @Nullable String serializedTags) {
        this.setName(name);
        this.setDescription(description);
        this.setCreationTime(creationTime);
        this.setTags(deserializeTags(serializedTags));
    }

    public PositionMeta(@NotNull String name, @NotNull String description) {
        this.setName(name);
        this.setDescription(description);
        this.setCreationTime(Instant.now());
        this.setTags(new HashMap<>());
    }

    /**
     * Deserialize a JSON string into a {@link Map} of meta tags
     *
     * @param serializedTags The JSON string to deserialize
     * @return The deserialized {@link Map}
     */
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
     * Serialize a {@link Map} of meta tags into a JSON string
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
     * The name of a position
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * A description of a position
     */
    @NotNull
    public String getDescription() {
        return Optional.ofNullable(description).orElse("");
    }

    public void setDescription(@Nullable String description) {
        this.description = description;
    }

    /**
     * Map of metadata tags for a position
     */
    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    /**
     * The time the position was created
     */
    @NotNull
    public Instant getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Instant creationTime) {
        this.creationTime = creationTime;
    }
}
