package net.william278.huskhomes.teleport;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.user.OnlineUser;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

/**
 * Represents a request between players for one user to teleport to another, or vice versa
 */
public class TeleportRequest {

    @Expose
    @SerializedName("requester_name")
    private String requesterName;

    @Expose
    @SerializedName("requester_position")
    private Position requesterPosition;

    @Expose
    @SerializedName("expiry_time")
    private long expiryTime;

    @Expose
    private Type type;

    @Expose
    private Status status;

    @Expose
    private String recipientName;

    /**
     * Create a teleport request
     *
     * @param requester The user making the request
     */
    public TeleportRequest(@NotNull OnlineUser requester, @NotNull TeleportRequest.Type type, final long expiryTime) {
        this.setRequesterName(requester.getUsername());
        this.setRequesterPosition(requester.getPosition());
        this.setType(type);
        this.setStatus(Status.PENDING);
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

    /**
     * The name of the request recipient
     */
    @NotNull
    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(@NotNull String recipientName) {
        this.recipientName = recipientName;
    }

    /**
     * The user making the request
     */
    public String getRequesterName() {
        return requesterName;
    }

    public void setRequesterName(String requesterName) {
        this.requesterName = requesterName;
    }

    /**
     * The position of the requester, relevant in the case of a {@link Type#TPA_HERE} request
     */
    @NotNull
    public Position getRequesterPosition() {
        return requesterPosition;
    }

    public void setRequesterPosition(Position requesterPosition) {
        this.requesterPosition = requesterPosition;
    }

    /**
     * The type of request; a {@link Type#TPA} or {@link Type#TPA_HERE}
     */
    @NotNull
    public TeleportRequest.Type getType() {
        return type;
    }

    public void setType(@NotNull TeleportRequest.Type type) {
        this.type = type;
    }

    /**
     * The status of the request; a {@link Status#PENDING}, {@link Status#ACCEPTED}
     * or {@link Status#DECLINED}
     */
    @NotNull
    public TeleportRequest.Status getStatus() {
        return status;
    }

    public void setStatus(@NotNull TeleportRequest.Status status) {
        this.status = status;
    }

    /**
     * Types of teleport requests ({@code /tpa} or {@code /tpahere})
     */
    public enum Type {
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
    public enum Status {
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
