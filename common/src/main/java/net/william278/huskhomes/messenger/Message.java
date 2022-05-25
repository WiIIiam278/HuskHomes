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
     * The kind of message this is - {@link MessageKind#MESSAGE} or a {@link MessageKind#REPLY} to a message.
     */
    @NotNull
    public MessageKind kind;

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
    public int clusterId;

    /**
     * Data to send in the message
     */
    @NotNull
    public String payload;

    public Message(@NotNull MessageType type, @NotNull String sender, @NotNull String targetPlayer,
                   @NotNull MessagePayload payload, @NotNull MessageKind messageKind, int clusterId) {
        this.type = type;
        this.sender = sender;
        this.targetPlayer = targetPlayer;
        this.payload = payload.toJson();
        this.kind = messageKind;
        this.clusterId = clusterId;
        this.uuid = UUID.randomUUID();
    }

    public String toJson() {
        return new GsonBuilder().setPrettyPrinting().create().toJson(this);
    }

    public static Message fromJson(String json) {
        return new GsonBuilder().setPrettyPrinting().create().fromJson(json, Message.class);
    }

    public enum MessageType {
        TP_REQUEST
    }

    public enum MessageKind {
        MESSAGE,
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
