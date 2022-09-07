package net.william278.huskhomes.request;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.messenger.Message;
import net.william278.huskhomes.messenger.MessagePayload;
import net.william278.huskhomes.player.OnlineUser;
import net.william278.huskhomes.player.User;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Manages {@link TeleportRequest}s between players
 */
public class RequestManager {

    @NotNull
    private final HuskHomes plugin;

    /**
     * Map of user UUIDs to a queue of received teleport requests
     */
    private final Map<UUID, Deque<TeleportRequest>> requests;

    /**
     * Set of users who are ignoring tpa requests
     */
    private final Set<UUID> ignoringRequests;

    public RequestManager(@NotNull HuskHomes implementation) {
        this.plugin = implementation;
        this.requests = new HashMap<>();
        this.ignoringRequests = new HashSet<>();
    }

    /**
     * Mark a user as ignoring or listening to tpa requests
     *
     * @param user     the user to update
     * @param ignoring whether the user should be ignoring requests
     */
    public void setIgnoringRequests(@NotNull User user, boolean ignoring) {
        if (ignoring) {
            this.ignoringRequests.add(user.uuid);
        } else {
            this.ignoringRequests.remove(user.uuid);
        }
    }

    /**
     * Return if a user is ignoring tpa requests
     *
     * @param user the user to check
     * @return {@code true} if the user is ignoring tpa requests
     */
    public boolean isIgnoringRequests(@NotNull User user) {
        return this.ignoringRequests.contains(user.uuid);
    }

    /**
     * Add a teleport request to a user's request queue
     *
     * @param request   the {@link TeleportRequest} to add
     * @param recipient the {@link User} recipient of the request
     */
    public void addTeleportRequest(@NotNull TeleportRequest request, @NotNull User recipient) {
        this.requests.computeIfAbsent(recipient.uuid, uuid -> new LinkedList<>()).addFirst(request);
    }

    /**
     * Remove {@link TeleportRequest}(s) sent by a requester, by name, from a recipient's queue
     *
     * @param requesterName username of the sender of the request(s) to delete
     * @param recipient     the {@link User} recipient of the request
     */
    public void removeTeleportRequest(@NotNull String requesterName, @NotNull User recipient) {
        this.requests.computeIfPresent(recipient.uuid, (uuid, requests) -> {
            requests.removeIf(teleportRequest -> teleportRequest.requesterName.equalsIgnoreCase(requesterName));
            return requests.isEmpty() ? null : requests;
        });
    }

    /**
     * Get the last received teleport request for a user
     *
     * @param recipient the user to get the request for
     * @return the last received request, if present
     */
    public Optional<TeleportRequest> getLastTeleportRequest(@NotNull User recipient) {
        return Optional.of(this.requests.getOrDefault(recipient.uuid, new LinkedList<>()).getFirst());
    }

    /**
     * Returns the last non-expired teleport request received from a requester.
     * <ol>
     * <li>If there are no unexpired requests sent by the requester, then the last expired request is returned.</li>
     * <li>If there are no requests at all from the requester, then an empty optional is returned.</li>
     * </ol>
     *
     * @param requesterName the name of the requester
     * @param recipient     the recipient {@link User}
     * @return the last unexpired teleport request received from the requester, if present
     */
    public Optional<TeleportRequest> getTeleportRequest(@NotNull String requesterName, @NotNull User recipient) {
        return this.requests.getOrDefault(recipient.uuid, new LinkedList<>()).stream()
                .filter(request -> request.requesterName.equalsIgnoreCase(requesterName))
                .filter(request -> !request.hasExpired())
                .findFirst()
                .or(() -> this.requests.getOrDefault(recipient.uuid, new LinkedList<>()).stream()
                        .filter(request -> request.requesterName.equalsIgnoreCase(requesterName))
                        .findFirst());
    }

    /**
     * Sends a teleport request of the given type to the specified user, by name, if they exist.
     *
     * @param requester   The user making the request
     * @param targetUser  The user to send the request to
     * @param requestType The type of request to send
     * @return A {@link CompletableFuture} that will return the request that was sent if it was sent successfully,
     * or an empty {@link Optional} if the request was not sent
     */
    public CompletableFuture<Optional<TeleportRequest>> sendTeleportRequest(@NotNull OnlineUser requester, @NotNull String targetUser,
                                                                            @NotNull TeleportRequest.RequestType requestType) {
        final TeleportRequest request = new TeleportRequest(requester, requestType,
                Instant.now().getEpochSecond() + plugin.getSettings().teleportRequestExpiryTime);
        final Optional<OnlineUser> localTarget = plugin.findPlayer(targetUser);
        if (localTarget.isPresent()) {
            if (localTarget.get().uuid.equals(requester.uuid)) {
                return CompletableFuture.completedFuture(Optional.empty());
            }
            request.recipientName = localTarget.get().username;
            return CompletableFuture.completedFuture(sendLocalTeleportRequest(request, localTarget.get()));
        }

        // If the player couldn't be found locally, send the request cross-server
        if (plugin.getSettings().crossServer) {
            // Find the matching networked target
            return plugin.getNetworkMessenger().findPlayer(requester, targetUser).thenApplyAsync(networkedTarget -> {
                if (networkedTarget.isEmpty()) {
                    return Optional.empty();
                }

                // Use the network messenger to send the request
                request.recipientName = networkedTarget.get();
                return Optional.ofNullable(plugin.getNetworkMessenger().sendMessage(requester,
                                new Message(Message.MessageType.TELEPORT_REQUEST,
                                        requester.username,
                                        networkedTarget.get(),
                                        MessagePayload.withTeleportRequest(request),
                                        Message.RelayType.MESSAGE,
                                        plugin.getSettings().clusterId))
                        .orTimeout(3, TimeUnit.SECONDS)
                        .exceptionally(throwable -> null)
                        .thenApply(reply -> {
                            if (reply == null || reply.payload.teleportRequest == null) {
                                return null;
                            }

                            // If the message was ignored by the recipient, return false
                            if (reply.payload.teleportRequest.status == TeleportRequest.RequestStatus.PENDING) {
                                return request;
                            }
                            return null;
                        }).join());
            });
        }
        return CompletableFuture.completedFuture(Optional.empty());
    }

    /**
     * Sends a teleport request locally on this server asking the recipient to accept or deny the request.
     *
     * @param request   The {@link TeleportRequest} to send
     * @param recipient The online recipient of the request
     * @return the {@link TeleportRequest} that was sent if it could be sent, otherwise an empty optional if it was not
     * sent because the recipient is ignoring requests or is vanished ({@link OnlineUser#isVanished()})
     */
    public Optional<TeleportRequest> sendLocalTeleportRequest(@NotNull TeleportRequest request, @NotNull OnlineUser recipient) {
        if (isIgnoringRequests(recipient) || recipient.isVanished()) {
            request.status = TeleportRequest.RequestStatus.IGNORED;
            return Optional.empty();
        }

        // If the person already has an unexpired request of the same type by this player, don't bother sending another
        final Optional<TeleportRequest> existingRequest = getTeleportRequest(request.requesterName, recipient);
        if (existingRequest.isPresent()) {
            if (existingRequest.get().type == request.type && !existingRequest.get().hasExpired()) {
                return Optional.of(request);
            }
        }

        // Add the request and display a message to the recipient
        addTeleportRequest(request, recipient);
        plugin.getLocales().getLocale((request.type == TeleportRequest.RequestType.TPA ? "tpa" : "tpahere")
                        + "_request_received", request.requesterName)
                .ifPresent(recipient::sendMessage);
        plugin.getLocales().getLocale("teleport_request_buttons", request.requesterName)
                .ifPresent(recipient::sendMessage);
        return Optional.of(request);
    }

    /**
     * Respond to a teleport request with the given status by name of the sender
     *
     * @param recipient  The user receiving the request
     * @param senderName The name of the user sending the request
     * @param accepted   Whether the request was accepted or not
     */
    public void respondToTeleportRequestBySenderName(@NotNull OnlineUser recipient, @NotNull String senderName,
                                                     boolean accepted) {
        // Check the recipient is not ignoring teleport requests
        if (isIgnoringRequests(recipient)) {
            plugin.getLocales().getLocale("error_ignoring_teleport_requests").ifPresent(recipient::sendMessage);
            return;
        }

        // Get the sender-named request and handle the response
        final Optional<TeleportRequest> namedRequest = getTeleportRequest(senderName, recipient);
        if (namedRequest.isEmpty()) {
            plugin.getLocales().getLocale("error_invalid_teleport_request", senderName)
                    .ifPresent(recipient::sendMessage);
            return;
        }
        handleRequestResponse(namedRequest.get(), recipient, accepted);
    }

    /**
     * Respond to the last received teleport request for a user, if there is one
     *
     * @param recipient The user receiving the request
     * @param accepted  Whether the request should be accepted or not
     */
    public void respondToTeleportRequest(@NotNull OnlineUser recipient, boolean accepted) {
        // Check the recipient is not ignoring teleport requests
        if (isIgnoringRequests(recipient)) {
            plugin.getLocales().getLocale("error_ignoring_teleport_requests").ifPresent(recipient::sendMessage);
            return;
        }

        // Get the latest request and handle the response
        final Optional<TeleportRequest> lastRequest = getLastTeleportRequest(recipient);
        if (lastRequest.isEmpty()) {
            plugin.getLocales().getLocale("error_no_teleport_requests").ifPresent(recipient::sendMessage);
            return;
        }
        handleRequestResponse(lastRequest.get(), recipient, accepted);
    }

    /**
     * Handle; respond to; a teleport request
     *
     * @param request   The request to handle
     * @param recipient The recipient of the request
     * @param accepted  Whether the request should be accepted or not
     */
    private void handleRequestResponse(@NotNull TeleportRequest request, @NotNull OnlineUser recipient,
                                       boolean accepted) {
        // Remove the request(s) from the sender from the recipient's queue
        removeTeleportRequest(request.requesterName, recipient);

        // Check if the request has expired
        if (request.hasExpired()) {
            plugin.getLocales().getLocale("error_teleport_request_expired").ifPresent(recipient::sendMessage);
            return;
        }

        // Send request response confirmation to the recipient
        plugin.getLocales().getLocale("teleport_request_" + (accepted ? "accepted" : "declined") + "_confirmation",
                        request.requesterName)
                .ifPresent(recipient::sendMessage);
        request.status = accepted ? TeleportRequest.RequestStatus.ACCEPTED : TeleportRequest.RequestStatus.DECLINED;

        // Send request response to the sender
        CompletableFuture.runAsync(() -> {
            final Optional<OnlineUser> localRequester = plugin.findPlayer(request.requesterName);
            if (localRequester.isPresent()) {
                handleLocalRequestResponse(localRequester.get(), request);
            } else if (plugin.getSettings().crossServer) {

                // Ensure the sender is still online
                if (!plugin.getNetworkMessenger().findPlayer(recipient, request.requesterName).thenApply(networkedTarget -> {
                    if (networkedTarget.isEmpty()) {
                        plugin.getLocales().getLocale("error_teleport_request_sender_not_online")
                                .ifPresent(recipient::sendMessage);
                        return false;
                    }

                    return plugin.getNetworkMessenger().sendMessage(recipient,
                                    new Message(Message.MessageType.TELEPORT_REQUEST_RESPONSE,
                                            recipient.username,
                                            networkedTarget.get(),
                                            MessagePayload.withTeleportRequest(request),
                                            Message.RelayType.MESSAGE,
                                            plugin.getSettings().clusterId))
                            .thenApply(reply -> true)
                            .orTimeout(3, TimeUnit.SECONDS)
                            .exceptionally(throwable -> false).join();
                }).join()) {
                    plugin.getLocales().getLocale("error_teleport_request_sender_not_online")
                            .ifPresent(recipient::sendMessage);
                    return;
                }
            } else {
                plugin.getLocales().getLocale("error_teleport_request_sender_not_online")
                        .ifPresent(recipient::sendMessage);
                return;
            }

            // If the request is a tpa here request, teleport the recipient to the sender
            if (accepted && request.type == TeleportRequest.RequestType.TPA_HERE) {
                // Strict /tpahere requests will teleport to where the sender was when typing the command
                if (plugin.getSettings().strictTpaHereRequests) {
                    plugin.getTeleportManager().timedTeleport(recipient, request.requesterPosition).thenAccept(
                            teleportResult -> plugin.getTeleportManager().finishTeleport(recipient, teleportResult));
                } else {
                    plugin.getTeleportManager().teleportToPlayerByName(recipient, request.requesterName, true);
                }
            }
        });
    }

    /**
     * Handle a teleport request response for a local user
     *
     * @param requester The user who sent the request
     * @param request   The {@link TeleportRequest} to handle
     */
    public void handleLocalRequestResponse(@NotNull OnlineUser requester, @NotNull TeleportRequest request) {
        boolean accepted = request.status == TeleportRequest.RequestStatus.ACCEPTED;
        plugin.getLocales().getLocale("teleport_request_" + (accepted ? "accepted" : "declined"),
                request.recipientName).ifPresent(requester::sendMessage);

        // If the request is a tpa request, teleport the requester to the recipient
        if (accepted && (request.type == TeleportRequest.RequestType.TPA)) {
            plugin.getTeleportManager().teleportToPlayerByName(requester, request.recipientName, true);
        }
    }

}
