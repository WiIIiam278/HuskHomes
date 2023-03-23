package net.william278.huskhomes.network;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.teleport.TeleportRequest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * Represents a payload sent in a cross-server {@link Message}
 */
public class Payload {

    @Nullable
    @Expose
    private Position position;

    @Nullable
    @Expose
    @SerializedName("teleport_request")
    private TeleportRequest teleportRequest;

    @Nullable
    @Expose
    private String string;

    @Nullable
    @Expose
    @SerializedName("string_list")
    private List<String> stringList;

    /**
     * Returns an empty cross-server message payload
     *
     * @return an empty payload
     */
    @NotNull
    public static Payload empty() {
        return new Payload();
    }

    /**
     * Returns a payload containing a {@link Position}
     *
     * @param position the position to send
     * @return a payload containing the position
     */
    @NotNull
    public static Payload withPosition(@NotNull Position position) {
        final Payload payload = new Payload();
        payload.position = position;
        return payload;
    }

    /**
     * Returns a payload containing a {@link TeleportRequest}
     *
     * @param teleportRequest the teleport to send
     * @return a payload containing the teleport request
     */
    @NotNull
    public static Payload withTeleportRequest(@NotNull TeleportRequest teleportRequest) {
        final Payload payload = new Payload();
        payload.teleportRequest = teleportRequest;
        return payload;
    }

    /**
     * A string field
     */
    @NotNull
    public static Payload withString(@NotNull String target) {
        final Payload payload = new Payload();
        payload.string = target;
        return payload;
    }

    /**
     * A string list field
     */
    @NotNull
    public static Payload withStringList(@NotNull List<String> target) {
        final Payload payload = new Payload();
        payload.stringList = target;
        return payload;
    }

    private Payload() {
    }

    /**
     * A position field
     */
    public Optional<Position> getPosition() {
        return Optional.ofNullable(position);
    }


    /**
     * A teleport request field
     */
    public Optional<TeleportRequest> getTeleportRequest() {
        return Optional.ofNullable(teleportRequest);
    }

    /**
     * A string field
     */
    public Optional<String> getString() {
        return Optional.ofNullable(string);
    }

    /**
     * A string list field
     */
    public Optional<List<String>> getStringList() {
        return Optional.ofNullable(stringList);
    }

}
