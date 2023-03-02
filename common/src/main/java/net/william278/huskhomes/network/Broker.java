package net.william278.huskhomes.network;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.request.TeleportRequest;
import net.william278.huskhomes.user.OnlineUser;
import net.william278.huskhomes.teleport.Teleport;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public abstract class Broker {

    protected final HuskHomes plugin;
    private final Map<UUID, CompletableFuture<Message>> outboundMessages = new HashMap<>();

    /**
     * Create a new broker
     *
     * @param plugin the HuskTowns plugin instance
     */
    protected Broker(@NotNull HuskHomes plugin) {
        this.plugin = plugin;
    }

    /**
     * Handle an inbound {@link Message}
     *
     * @param receiver The user who received the message, if a receiver exists
     * @param message  The message
     */
    protected void handle(@Nullable OnlineUser receiver, @NotNull Message message) {
        if (message.getSourceServer().equals(getServer()) || receiver == null) {
            return;
        }
        if (message.getDirection() == Message.Direction.INBOUND) {
            this.outboundMessages.getOrDefault(message.getUuid(), new CompletableFuture<>()).complete(message);
            this.outboundMessages.remove(message.getUuid());
            return;
        }
        switch (message.getType()) {
            case TELEPORT_TO_POSITION_REQUEST -> {
                if (message.getPayload().getPosition() != null) {
                    Teleport.builder(plugin, receiver)
                            .setTarget(message.getPayload().getPosition()).toTeleport()
                            .thenAccept(teleport -> teleport.execute()
                                    .thenAccept(result -> message.reply(
                                            this,
                                            receiver,
                                            Payload.withTeleportResult(result.getState())
                                    )));
                    return;
                }
            }
            case POSITION_REQUEST -> message.reply(this, receiver, Payload.withPosition(receiver.getPosition()));
            case TELEPORT_REQUEST -> {
                if (message.getPayload().getTeleportRequest() != null) {
                    message.reply(this, receiver, plugin.getRequestManager()
                            .sendLocalTeleportRequest(message.getPayload().getTeleportRequest(), receiver)
                            .map(Payload::withTeleportRequest)
                            .orElse(Payload.empty()));
                    return;
                }
                message.reply(this, receiver, Payload.empty());
            }
            case TELEPORT_REQUEST_RESPONSE -> {
                if (message.getPayload().getTeleportRequest() != null) {
                    final TeleportRequest request = message.getPayload().getTeleportRequest();
                    plugin.getRequestManager().handleLocalRequestResponse(receiver, request);
                    message.reply(this, receiver, Payload.withTeleportRequest(request));
                    return;
                }
                message.reply(this, receiver, Payload.empty());
            }
        }
    }

    /**
     * Initialize the message broker
     *
     * @throws RuntimeException if the broker fails to initialize
     */
    public abstract void initialize() throws RuntimeException;

    /**
     * Send a message to the broker
     *
     * @param message the message to send
     * @param sender  the sender of the message
     */
    protected abstract void send(@NotNull Message message, @NotNull OnlineUser sender);

    /**
     * Move an {@link OnlineUser} to a new server on the proxy network
     *
     * @param user   the user to move
     * @param server the server to move the user to
     */
    public abstract void changeServer(@NotNull OnlineUser user, @NotNull String server);

    @NotNull
    protected Map<UUID, CompletableFuture<Message>> getOutboundMessages() {
        return outboundMessages;
    }

    /**
     * Terminate the broker
     */
    public abstract void close();

    @NotNull
    protected String getSubChannelId() {
        final String version = plugin.getVersion().getMajor() + "." + plugin.getVersion().getMinor();
        return plugin.getKey(plugin.getSettings().getClusterId(), version).asString();
    }

    @NotNull
    protected String getServer() {
        return plugin.getServerName();
    }

    /**
     * Identifies types of message brokers
     */
    public enum Type {
        PLUGIN_MESSAGE("Plugin Messages"),
        REDIS("Redis");
        @NotNull
        private final String displayName;

        Type(@NotNull String displayName) {
            this.displayName = displayName;
        }

        @NotNull
        public String getDisplayName() {
            return displayName;
        }
    }

}