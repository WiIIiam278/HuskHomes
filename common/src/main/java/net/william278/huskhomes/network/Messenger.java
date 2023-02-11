package net.william278.huskhomes.network;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.player.OnlineUser;
import net.william278.huskhomes.position.Server;
import net.william278.huskhomes.teleport.Teleport;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public abstract class Messenger implements AutoCloseable {

    /**
     * Name of the network message channel for HuskHomes messages
     */
    public static final String NETWORK_MESSAGE_CHANNEL = "huskhomes:main";

    /**
     * Map of message UUIDs to messages being processed
     */
    protected HashMap<UUID, CompletableFuture<Request>> processingMessages;

    /**
     * List of pending futures for processing {@link #getOnlinePlayerNames(OnlineUser)} requests
     */
    protected List<CompletableFuture<String[]>> onlinePlayerNamesRequests;

    /**
     * List of pending futures for processing {@link #fetchOnlineServerList(OnlineUser)} requests
     */
    protected List<CompletableFuture<String[]>> onlineServersRequests;

    /**
     * ID of the HuskHomes network cluster this server is on
     */
    protected String clusterId;

    /**
     * The implementing HuskHomes plugin instance
     */
    protected HuskHomes plugin;

    /**
     * The time-to-live of a message before it expires and the {@link CompletableFuture} is cancelled
     */
    private final long MESSAGE_TIME_OUT = 5L;

    /**
     * Initialize the network messenger
     *
     * @param implementor Instance of the implementing plugin
     */
    public void initialize(@NotNull HuskHomes implementor) {
        this.processingMessages = new HashMap<>();
        this.onlinePlayerNamesRequests = new ArrayList<>();
        this.onlineServersRequests = new ArrayList<>();
        this.clusterId = implementor.getSettings().clusterId;
        this.plugin = implementor;
    }

    /**
     * Fetch a list of online player usernames from the proxy
     *
     * @param requester {@link OnlineUser} to send the request
     * @return Future returning a {@link List} of usernames of players on the proxy network
     */
    public abstract CompletableFuture<String[]> getOnlinePlayerNames(@NotNull OnlineUser requester);

    /**
     * Find a player on the proxy network by username to validate if they exist.
     *
     * @param requester  {@link OnlineUser} to send the request
     * @param playerName Approximate name of the player to find. If an exact match is not found,
     *                   this will attempt to return the closest match
     * @return Future returning an {@link Optional} of the player's canonical username if found;
     * otherwise an empty {@link Optional}
     */
    public final CompletableFuture<Optional<String>> findPlayer(@NotNull OnlineUser requester, @NotNull String playerName) {
        return getOnlinePlayerNames(requester).thenApply(networkedPlayers ->
                Arrays.stream(networkedPlayers)
                        .filter(user -> user.equalsIgnoreCase(playerName))
                        .findFirst()
                        .or(() -> Arrays.stream(networkedPlayers)
                                .filter(user -> user.toLowerCase().startsWith(playerName))
                                .findFirst()));
    }

    /**
     * Fetch a list of online server names proxy
     *
     * @param requester {@link OnlineUser} to send the request
     * @return Future returning a {@link List} of online servers connected to the proxy network
     */
    public abstract CompletableFuture<String[]> fetchOnlineServerList(@NotNull OnlineUser requester);

    /**
     * Send a {@link OnlineUser} to a target {@link Server} on the proxy network
     *
     * @param server The {@link Server} to send the {@link OnlineUser} to
     * @return A future, returning {@code true} if the player was sent.<p>
     * If the player could not be sent (i.e. the server was offline), this will return {@code false}
     */
    public abstract CompletableFuture<Boolean> sendPlayer(@NotNull OnlineUser onlineUser, @NotNull Server server);

    /**
     * Send a network message
     *
     * @param sender  {@link OnlineUser} sending the message
     * @param request {@link Request} to send
     * @return Future returning the {@link Request} sent, that will time out after
     * @apiNote This method invokes {@link #dispatchMessage(OnlineUser, Request)}, applying a time-to-live timeOut
     */
    protected final CompletableFuture<Optional<Request>> sendMessage(@NotNull OnlineUser sender, @NotNull Request request) {
        return dispatchMessage(sender, request)
                .orTimeout(MESSAGE_TIME_OUT, TimeUnit.SECONDS)
                .exceptionally(e -> {
                    plugin.getLoggingAdapter().log(Level.WARNING, "Message dispatch after " + MESSAGE_TIME_OUT + " seconds", e);
                    return null;
                })
                .thenApply(result -> {
                    processingMessages.remove(request.getUuid());
                    return Optional.ofNullable(result);
                });
    }

    /**
     * Dispatch a {@link Request} via the appropriate handler
     *
     * @param sender  The {@link OnlineUser} that sent the {@link Request}
     * @param request The {@link Request} to dispatch
     * @return A future containing a reply to the {@link Request} that was sent
     */
    protected abstract CompletableFuture<Request> dispatchMessage(@NotNull OnlineUser sender, @NotNull Request request);

    /**
     * Send a reply to a received {@link Request}
     *
     * @param reply The reply {@link Request} to send
     */
    protected abstract void sendReply(@NotNull OnlineUser replier, @NotNull Request reply);

    /**
     * Handle and action received network {@link Request}s
     *
     * @param receiver The online {@link OnlineUser} receiving the message
     * @param request  The received {@link Request}
     */
    protected final void handleMessage(@NotNull OnlineUser receiver, @NotNull Request request) {
        switch (request.getRelayType()) {
            // Handle a message and send reply
            case MESSAGE -> handleRequest(receiver, request);
            // Handle a reply message
            case REPLY -> {
                if (processingMessages.containsKey(request.getUuid())) {
                    final Request finalRequest = request;
                    processingMessages.get(request.getUuid()).completeAsync(() -> finalRequest);
                    return;
                }
                plugin.getLoggingAdapter().log(Level.WARNING, "Received a reply to a message that was not sent by this server");
            }
        }
    }

    /**
     * Handle different message types, executed needed operations and dispatches a reply message
     * to the sender
     *
     * @param receiver The {@link OnlineUser} receiving the message
     * @param request  The received {@link Request}
     */
    private void handleRequest(@NotNull OnlineUser receiver, @NotNull Request request) {
        switch (request.getType()) {
            case TELEPORT_TO_POSITION_REQUEST -> {
                if (request.getPayload().position != null) {
                    Teleport.builder(plugin, receiver)
                            .setTarget(request.getPayload().position).toTeleport()
                            .thenAccept(teleport -> teleport.execute()
                                    .thenAccept(result -> request.reply(receiver,
                                            Payload.withTeleportResult(result.getState()), plugin)));
                    return;
                }
                request.reply(receiver, Payload.empty(), plugin);
            }
            case POSITION_REQUEST -> request.reply(receiver, Payload.withPosition(receiver.getPosition()), plugin);
            case TELEPORT_REQUEST -> {
                if (request.getPayload().teleportRequest != null) {
                    request.reply(receiver, plugin.getRequestManager()
                            .sendLocalTeleportRequest(request.getPayload().teleportRequest, receiver)
                            .map(Payload::withTeleportRequest)
                            .orElse(Payload.empty()), plugin);
                    return;
                }
                request.reply(receiver, Payload.empty(), plugin);
            }
            case TELEPORT_REQUEST_RESPONSE -> {
                if (request.getPayload().teleportRequest != null) {
                    plugin.getRequestManager().handleLocalRequestResponse(receiver, request.getPayload().teleportRequest);
                    request.reply(receiver, plugin);
                    return;
                }
                request.reply(receiver, Payload.empty(), plugin);
            }
        }
    }

    /**
     * Close the network messenger
     */
    @Override
    public abstract void close();

}