package net.william278.huskhomes.messenger;

import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * A message sent cross-proxy to a player
 */
public class Message {

    /**
     * A unique ID representing this message
     */
    @NotNull
    public UUID uuid;

    /**
     * The type of the message
     */
    @NotNull
    public MessageType type;

    /**
     * Represents the context under which this message is being relayed; an outbound {@link RelayType#MESSAGE} or a {@link RelayType#REPLY} to one.
     */
    @SerializedName("relay_type")
    @NotNull
    public RelayType relayType;

    /**
     * Username of the target player the message is bound for
     */
    @SerializedName("target_player")
    @NotNull
    public String targetPlayer;

    /**
     * Username of the sender of the message
     */
    @NotNull
    public String sender;

    /**
     * ID of the cluster this message is for
     */
    @SerializedName("cluster_id")
    @NotNull
    public String clusterId;

    /**
     * Data to send in the message
     */
    @NotNull
    public MessagePayload payload;

    public Message(@NotNull MessageType type, @NotNull String sender, @NotNull String targetPlayer,
                   @NotNull MessagePayload payload, @NotNull Message.RelayType relayType, @NotNull String clusterId) {
        this.type = type;
        this.sender = sender;
        this.targetPlayer = targetPlayer;
        this.payload = payload;
        this.relayType = relayType;
        this.clusterId = clusterId;
        this.uuid = UUID.randomUUID();
    }

    @NotNull
    public String toJson() {
        return new GsonBuilder().create().toJson(this);
    }

    @NotNull
    public static Message fromJson(@NotNull String json) {
        return new GsonBuilder().create().fromJson(json, Message.class);
    }

    /**
     * Identifies the type of message or reply
     */
    public enum MessageType {
        TELEPORT_TO_POSITION_REQUEST,
        POSITION_REQUEST,
        TELEPORT_REQUEST,
        TELEPORT_REQUEST_RESPONSE
    }

    /**
     * Identifies the source of the message being relayed - a {@link RelayType#MESSAGE} or a {@link RelayType#REPLY} to a message.
     */
    public enum RelayType {
        /**
         * An outbound message to/from a server
         */
        MESSAGE,
        /**
         * A reply to an outbound message
         */
        REPLY
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Message message) {
            return message.uuid.equals(this.uuid);
        }
        return super.equals(other);
    }

}
