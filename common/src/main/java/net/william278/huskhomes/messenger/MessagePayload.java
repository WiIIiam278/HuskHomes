package net.william278.huskhomes.messenger;

import com.google.gson.annotations.SerializedName;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.request.TeleportRequest;
import net.william278.huskhomes.teleport.TeleportResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a payload sent in a cross-server {@link Message}
 */
public class MessagePayload {

    /**
     * A position field
     */
    @Nullable
    public Position position;

    /**
     * A teleport result field
     */
    @Nullable
    @SerializedName("teleport_result")
    public TeleportResult.ResultState resultState;

    /**
     * A teleport request field
     */
    @Nullable
    @SerializedName("teleport_request")
    public TeleportRequest teleportRequest;

    /**
     * Returns an empty cross-server message payload
     *
     * @return an empty payload
     */
    @NotNull
    public static MessagePayload empty() {
        return new MessagePayload();
    }

    /**
     * Returns a payload containing a {@link Position}
     *
     * @param position the position to send
     * @return a payload containing the position
     */
    @NotNull
    public static MessagePayload withPosition(@NotNull Position position) {
        final MessagePayload payload = new MessagePayload();
        payload.position = position;
        return payload;
    }

    /**
     * Returns a payload containing a {@link TeleportResult.ResultState}
     *
     * @param resultState the teleport to send
     * @return a payload containing the teleport result
     */
    @NotNull
    public static MessagePayload withTeleportResult(@NotNull TeleportResult.ResultState resultState) {
        final MessagePayload payload = new MessagePayload();
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
    public static MessagePayload withTeleportRequest(@NotNull TeleportRequest teleportRequest) {
        final MessagePayload payload = new MessagePayload();
        payload.teleportRequest = teleportRequest;
        return payload;
    }

    private MessagePayload() {
    }

}
