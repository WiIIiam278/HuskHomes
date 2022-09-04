package net.william278.huskhomes.position;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents metadata about a {@link Position}, used in {@link SavedPosition} implementations
 */
public class PositionMeta {

    /**
     * The name of a position
     */
    @NotNull
    public String name;

    /**
     * A description of a position
     */
    @NotNull
    public String description;

    /**
     * Map of metadata tags for a position
     */
    @NotNull
    public Map<String, String> tags;

    /**
     * The time the position was created
     */
    @NotNull
    public Instant creationTime;

    public PositionMeta(@NotNull String name, @NotNull String description, @NotNull Instant creationTime,
                        @Nullable String serializedTags) {
        this.name = name;
        this.description = description;
        this.creationTime = creationTime;
        this.tags = deserializeTags(serializedTags);
    }

    public PositionMeta(@NotNull String name, @NotNull String description) {
        this.name = name;
        this.description = description;
        this.creationTime = Instant.now();
        this.tags = new HashMap<>();
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
            if (tags.isEmpty()) {
                return null;
            }
            return new Gson().toJson(tags);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
