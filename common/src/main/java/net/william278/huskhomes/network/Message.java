package net.william278.huskhomes.network;

import com.google.gson.annotations.Expose;
import net.william278.huskhomes.user.OnlineUser;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Represents a message sent by a {@link Broker} cross-server. See {@link #builder()} for
 * a builder to create a message.
 */
public class Message {

    public static final String TARGET_ALL = "ALL";

    @Expose
    private UUID id;
    @Expose
    private Type type;
    @Expose
    private String target;
    @Expose
    private Payload payload;
    @Expose
    private String sender;
    @Expose
    private String sourceServer;
    @Expose
    private Direction direction;

    private Message(@NotNull Type type, @NotNull String target, @NotNull Payload payload) {
        this.type = type;
        this.target = target;
        this.payload = payload;
        this.id = UUID.randomUUID();
    }

    @SuppressWarnings("unused")
    private Message() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public CompletableFuture<Message> send(@NotNull Broker broker, @NotNull OnlineUser sender) {
        this.sender = sender.getUsername();
        this.sourceServer = broker.getServer();
        this.direction = Direction.OUTBOUND;
        final CompletableFuture<Message> future = new CompletableFuture<>();
        broker.getOutboundMessages().put(getUuid(), future);
        broker.send(this, sender);
        return future;
    }

    public void reply(@NotNull Broker broker, @NotNull OnlineUser receiver, @NotNull Payload payload) {
        this.sender = receiver.getUsername();
        this.sourceServer = broker.getServer();
        this.direction = Direction.INBOUND;
        this.payload = payload;
        broker.send(this, receiver);
    }

    @NotNull
    public Type getType() {
        return type;
    }

    @NotNull
    public String getTarget() {
        return target;
    }

    @NotNull
    public Payload getPayload() {
        return payload;
    }

    @NotNull
    public String getSender() {
        return sender;
    }

    @NotNull
    public String getSourceServer() {
        return sourceServer;
    }

    @NotNull
    public Direction getDirection() {
        return direction;
    }

    @NotNull
    public UUID getUuid() {
        return id;
    }

    /**
     * Builder for {@link Message}s
     */
    public static class Builder {
        private Type type;
        private Payload payload = Payload.empty();
        private String target;

        private Builder() {
        }

        @NotNull
        public Builder type(@NotNull Type type) {
            this.type = type;
            return this;
        }

        @NotNull
        public Builder payload(@NotNull Payload payload) {
            this.payload = payload;
            return this;
        }

        @NotNull
        public Builder target(@NotNull String target) {
            this.target = target;
            return this;
        }

        @NotNull
        public Message build() {
            return new Message(type, target, payload);
        }

    }

    /**
     * Different types of cross-server messages
     */
    public enum Type {
        TELEPORT_TO_POSITION_REQUEST,
        POSITION_REQUEST,
        TELEPORT_REQUEST,
        POSITION_RESPONSE, TELEPORT_REQUEST_RESPONSE
    }

    public enum Direction {
        /**
         * The message is being sent from the sender to the target
         */
        OUTBOUND,
        /**
         * The message is being sent from the target to the sender
         */
        INBOUND
    }

}