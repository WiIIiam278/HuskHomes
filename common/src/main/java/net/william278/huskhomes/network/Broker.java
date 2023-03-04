package net.william278.huskhomes.network;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.user.OnlineUser;
import net.william278.huskhomes.teleport.Teleport;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class Broker {

    protected final HuskHomes plugin;

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
        switch (message.getType()) {
            case TELEPORT_TO_POSITION -> message.getPayload()
                    .getPosition().ifPresent(position -> Teleport.builder(plugin)
                            .teleporter(receiver)
                            .target(position)
                            .toTeleport()
                            .execute());
            case TELEPORT_TO_NETWORKED_POSITION -> Message.builder()
                    .type(Message.Type.TELEPORT_TO_POSITION)
                    .target(message.getSender())
                    .payload(Payload.withPosition(receiver.getPosition()))
                    .build().send(this, receiver);
            case TELEPORT_TO_NETWORKED_USER -> message.getPayload()
                    .getString().ifPresent(target -> Message.builder()
                            .type(Message.Type.TELEPORT_TO_NETWORKED_POSITION)
                            .target(target)
                            .build().send(this, receiver));
            case TELEPORT_REQUEST -> message.getPayload()
                    .getTeleportRequest()
                    .ifPresent(teleportRequest -> plugin.getManager().requests()
                            .sendLocalTeleportRequest(teleportRequest, receiver));
            case TELEPORT_REQUEST_RESPONSE -> message.getPayload()
                    .getTeleportRequest()
                    .ifPresent(teleportRequest -> plugin.getManager().requests()
                            .handleLocalRequestResponse(receiver, teleportRequest));
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