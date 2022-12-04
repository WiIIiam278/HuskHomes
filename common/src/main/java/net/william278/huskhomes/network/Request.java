package net.william278.huskhomes.network;

import com.google.gson.*;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.player.OnlineUser;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * A message sent cross-proxy to a player
 */
public class Request {

    /**
     * A unique ID representing this message
     */
    @NotNull
    @Expose
    private UUID uuid;

    /**
     * The type of the message
     */
    @NotNull
    @Expose
    private MessageType type;

    /**
     * Represents the context under which this message is being relayed; an outbound {@link RelayType#MESSAGE} or a {@link RelayType#REPLY} to one.
     */
    @SerializedName("relay_type")
    @NotNull
    @Expose
    private RelayType relayType;

    /**
     * Username of the target player the message is bound for
     */
    @SerializedName("target_player")
    @NotNull
    @Expose
    private String targetPlayer;

    /**
     * ID of the cluster this message is for
     */
    @SerializedName("cluster_id")
    @NotNull
    @Expose
    private String clusterId;

    /**
     * Data to send in the message
     */
    @NotNull
    @Expose
    private Payload payload;

    /**
     * Username of the sender of the message
     */
    @NotNull
    @Expose
    private String sender;

    protected Request(@NotNull MessageType type, @NotNull String targetPlayer, @NotNull Payload payload,
                      @NotNull Request.RelayType relayType) {
        this.type = type;
        this.targetPlayer = targetPlayer;
        this.payload = payload;
        this.relayType = relayType;
        this.uuid = UUID.randomUUID();
    }

    @SuppressWarnings("unused")
    private Request() {
    }

    @NotNull
    public static RequestBuilder builder() {
        return new RequestBuilder();
    }

    @NotNull
    public String toJson() {
        return new GsonBuilder().create().toJson(this);
    }

    @NotNull
    public static Request fromJson(@NotNull String json) {
        return new GsonBuilder().create().fromJson(json, Request.class);
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

    @NotNull
    public UUID getUuid() {
        return uuid;
    }

    @NotNull
    public MessageType getType() {
        return type;
    }

    @NotNull
    public RelayType getRelayType() {
        return relayType;
    }

    @NotNull
    public String getTargetPlayer() {
        return targetPlayer;
    }

    @NotNull
    public String getSender() {
        return sender;
    }

    @NotNull
    public String getClusterId() {
        return clusterId;
    }

    @NotNull
    public Payload getPayload() {
        return payload;
    }

    public CompletableFuture<Optional<Request>> send(@NotNull OnlineUser sender, @NotNull HuskHomes plugin) {
        this.sender = sender.username;
        this.clusterId = plugin.getSettings().clusterId;
        return plugin.getMessenger().sendMessage(sender, this);
    }

    public void reply(@NotNull OnlineUser sender, @NotNull Payload payload, @NotNull HuskHomes plugin) {
        this.targetPlayer = this.sender;
        this.sender = sender.username;
        this.clusterId = plugin.getSettings().clusterId;
        this.payload = payload;
        this.relayType = RelayType.REPLY;
        plugin.getMessenger().sendReply(sender, this);
    }

    public void reply(@NotNull OnlineUser sender, @NotNull HuskHomes plugin) {
        this.reply(sender, this.payload, plugin);
    }

    @Override
    public boolean equals(@NotNull Object other) {
        if (other instanceof Request request) {
            return request.uuid.equals(this.uuid);
        }
        return super.equals(other);
    }

}
