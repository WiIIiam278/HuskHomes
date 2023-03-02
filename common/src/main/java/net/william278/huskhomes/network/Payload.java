package net.william278.huskhomes.network;

import com.google.gson.annotations.SerializedName;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.request.TeleportRequest;
import net.william278.huskhomes.teleport.TeleportResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a payload sent in a cross-server {@link Request}
 */
public class Payload {

    @Nullable
    private Position position;

    @Nullable
    @SerializedName("teleport_result")
    private TeleportResult resultState;

    @Nullable
    @SerializedName("teleport_request")
    private TeleportRequest teleportRequest;

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
     * Returns a payload containing a {@link TeleportResult}
     *
     * @param resultState the teleport to send
     * @return a payload containing the teleport result
     */
    @NotNull
    public static Payload withTeleportResult(@NotNull TeleportResult resultState) {
        final Payload payload = new Payload();
        payload.resultState = resultState;
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

    private Payload() {
    }

    /**
     * A position field
     */
    @Nullable
    public Position getPosition() {
        return position;
    }

    /**
     * A teleport result field
     */
    @Nullable
    public TeleportResult getResultState() {
        return resultState;
    }

    /**
     * A teleport request field
     */
    @Nullable
    public TeleportRequest getTeleportRequest() {
        return teleportRequest;
    }
}
