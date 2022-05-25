package net.william278.huskhomes.messenger;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.config.Settings;
import net.william278.huskhomes.player.Player;
import net.william278.huskhomes.position.Server;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public abstract class NetworkMessenger {

    /**
     * Name of the network message channel for HuskHomes messages
     */
    public static final String NETWORK_MESSAGE_CHANNEL = "huskhomes:main";

    /**
     * Map of message UUIDs to messages being processed
     */
    protected HashMap<UUID, CompletableFuture<Message>> processingMessages;

    /**
     * Future for processing {@link #getServerName(Player)} requests
     */
    protected CompletableFuture<String> serverNameRequest;

    /**
     * Future for processing {@link #getOnlinePlayerNames(Player)} requests
     */
    protected CompletableFuture<String[]> onlinePlayerNamesRequest;

    /**
     * Future for processing {@link #getOnlineServers(Player)} requests
     */
    protected CompletableFuture<String[]> onlineServersRequest;

    /**
     * ID of the HuskHomes network cluster this server is on
     */
    protected int clusterId;

    /**
     * Initialize the network messenger
     *
     * @param implementor Instance of the implementing plugin
     */
    public void initialize(@NotNull HuskHomes implementor) {
        this.processingMessages = new HashMap<>();
        this.clusterId = implementor.getSettings().getIntegerValue(Settings.ConfigOption.CLUSTER_ID);
    }

    /**
     * Fetch a list of online player usernames from the proxy
     *
     * @param requester {@link Player} to send the request
     * @return Future returning a {@link List} of usernames of players on the proxy network
     */
    public abstract CompletableFuture<String[]> getOnlinePlayerNames(@NotNull Player requester);

    /**
     * Fetch the name of this server on the proxy network
     *
     * @param requester {@link Player} to send the request
     * @return Future returning the name of this server on the network
     */
    public abstract CompletableFuture<String> getServerName(@NotNull Player requester);

    /**
     * Fetch a list of online server names proxy
     *
     * @param requester {@link Player} to send the request
     * @return Future returning a {@link List} of online servers connected to the proxy network
     */
    public abstract CompletableFuture<String[]> getOnlineServers(@NotNull Player requester);

    /**
     * Send a {@link Player} to a target {@link Server} on the proxy network
     *
     * @param server The {@link Server} to send the {@link Player} to
     * @return A future, returning {@code true} if the player was sent.<p>
     * If the player could not be sent (i.e. the server was offline), this will return {@code false}
     */
    public abstract CompletableFuture<Boolean> sendPlayer(@NotNull Player player, @NotNull Server server);

    /**
     * Send a network {@link Message} to a target player on the proxy
     *
     * @param message The {@link Message} to send
     * @return A future containing a reply to the {@link Message} that was sent
     */
    public abstract CompletableFuture<Message> sendMessage(@NotNull Player sender, @NotNull Message message);

    /**
     * Send a reply to a received {@link Message}
     *
     * @param reply The reply {@link Message} to send
     * @return A future completing when the message has been sent
     */
    protected abstract CompletableFuture<Void> sendReply(@NotNull Player replier, @NotNull Message reply);

    /**
     * Handle and action received network {@link Message}s
     *
     * @param receiver The online {@link Player} receiving the message
     * @param message  The received {@link Message}
     */
    protected final void handleMessage(@NotNull Player receiver, @NotNull Message message) {
        switch (message.kind) {
            // Handle a message and send reply
            case MESSAGE -> {
                // Handle different message types and apply changes for reply message
                switch (message.type) {
                    case TP_REQUEST -> {
                        message = message;
                    }
                }

                // Reply with the modified message
                message.targetPlayer = message.sender;
                message.sender = receiver.getName();
                sendReply(receiver, message);
            }
            // Handle a reply message
            case REPLY -> {
                if (processingMessages.containsKey(message.uuid)) {
                    final Message finalMessage = message;
                    processingMessages.get(message.uuid).completeAsync(() -> finalMessage);
                }
            }
        }
    }

    /**
     * Close the network messenger
     */
    public abstract void terminate();

}