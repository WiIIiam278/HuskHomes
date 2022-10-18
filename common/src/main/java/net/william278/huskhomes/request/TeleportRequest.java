package net.william278.huskhomes.request;

import com.google.gson.annotations.SerializedName;
import net.william278.huskhomes.player.OnlineUser;
import net.william278.huskhomes.position.Position;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

/**
 * Represents a request between players for one user to teleport to another, or vice versa
 */
public class TeleportRequest {

    /**
     * The user making the request
     */
    @SerializedName("requester_name")
    protected String requesterName;

    /**
     * The position of the requester, relevant in the case of a {@link RequestType#TPA_HERE} request
     */
    @SerializedName("requester_position")
    protected Position requesterPosition;

    /**
     * Epoch timestamp when the request will expire
     */
    @SerializedName("expiry_time")
    protected long expiryTime;

    /**
     * The type of request; a {@link RequestType#TPA} or {@link RequestType#TPA_HERE}
     */
    protected RequestType type;

    /**
     * The status of the request; a {@link RequestStatus#PENDING}, {@link RequestStatus#ACCEPTED}
     * or {@link RequestStatus#DECLINED}
     */
    protected RequestStatus status;

    /**
     * The name of the request recipient
     */
    protected String recipientName;

    /**
     * Create a teleport request
     *
     * @param requester The user making the request
     */
    protected TeleportRequest(@NotNull OnlineUser requester, @NotNull RequestType requestType,
                              final long expiryTime) {
        this.requesterName = requester.username;
        this.requesterPosition = requester.getPosition();
        this.type = requestType;
        this.status = RequestStatus.PENDING;
        this.expiryTime = expiryTime;
    }

    @SuppressWarnings("unused")
    private TeleportRequest() {
    }

    /**
     * Returns if the request has now expired
     *
     * @return {@code true} the request has passed its expiry time
     */
    public boolean hasExpired() {
        return Instant.now().isAfter(Instant.ofEpochSecond(expiryTime));
    }

    @NotNull
    public String getRecipientName() {
        return recipientName;
    }

    /**
     * Types of teleport requests ({@code /tpa} or {@code /tpahere})
     */
    public enum RequestType {
        /**
         * The request is a {@code /tpa} request, where the requester is requesting to teleport <i>to</i> the recipient
         */
        TPA,
        /**
         * The request is a {@code /tpahere} request, where the requester is requesting that the recipient teleport <i>to them</i>
         */
        TPA_HERE
    }

    /**
     * The current status of a teleport request
     */
    protected enum RequestStatus {
        /**
         * The request is currently pending and can be accepted or declined
         */
        PENDING,
        /**
         * The teleport request has been accepted by the recipient and teleportation will commence
         */
        ACCEPTED,
        /**
         * The teleport request has been declined by the recipient
         */
        DECLINED,
        /**
         * The recipient was ignoring teleport requests at the time it was sent;
         * the request has been automatically declined
         */
        IGNORED
    }

}
