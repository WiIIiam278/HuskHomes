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
     * Map of user UUIDs to a list of received teleport requests
     */
    private final Map<UUID, List<TeleportRequest>> requests;

    /**
     * Set of users who are ignoring tpa requests
     */
    private final Set<UUID> ignoringRequests;

    public RequestManager(@NotNull HuskHomes implementation) {
        this.plugin = implementation;
        this.requests = new HashMap<>();
        this.ignoringRequests = new HashSet<>();
    }

    public void setIgnoringRequests(@NotNull User user, boolean ignoring) {
        if (ignoring) {
            this.ignoringRequests.add(user.uuid);
        } else {
            this.ignoringRequests.remove(user.uuid);
        }
    }

    public boolean isIgnoringRequests(@NotNull User user) {
        return this.ignoringRequests.contains(user.uuid);
    }

    public void addTeleportRequest(@NotNull TeleportRequest request, @NotNull User recipient) {
        this.requests.computeIfAbsent(recipient.uuid, uuid -> new ArrayList<>()).add(request);
    }

    public void removeTeleportRequest(@NotNull TeleportRequest request, @NotNull User recipient) {
        this.requests.computeIfPresent(recipient.uuid, (uuid, set) -> {
            set.remove(request);
            return set.isEmpty() ? null : set;
        });
    }

    public Optional<TeleportRequest> getLastTeleportRequest(@NotNull User recipient) {
        return this.requests.getOrDefault(recipient.uuid, Collections.emptyList()).stream().reduce((a, b) -> b);
    }

    public Optional<TeleportRequest> getTeleportRequest(@NotNull String requesterName, @NotNull User recipient) {
        return this.requests.getOrDefault(recipient.uuid, Collections.emptyList()).stream().filter(request ->
                request.requesterName.equals(requesterName)).findFirst();
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
            return plugin.getNetworkMessenger().findPlayer(requester, targetUser).thenApply(networkedTarget -> {
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

        // If the person already has a request of the same type by this player, don't bother sending another one
        if (getTeleportRequest(request.requesterName, recipient).map(existingRequest -> existingRequest.type)
                    .orElse(null) == request.type) {
            return Optional.of(request);
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

    private void handleRequestResponse(@NotNull TeleportRequest request, @NotNull OnlineUser recipient,
                                       boolean accepted) {
        removeTeleportRequest(request, recipient);

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

    public void handleLocalRequestResponse(@NotNull OnlineUser requester, @NotNull TeleportRequest request) {
        boolean accepted = request.status == TeleportRequest.RequestStatus.ACCEPTED;
        plugin.getLocales().getLocale("teleport_request_" + (accepted ? "accepted" : "declined"),
                request.recipientName).ifPresent(requester::sendMessage);

        // If the request is a tpa request, teleport the requester to the recipient
        if (accepted && (request.type == TeleportRequest.RequestType.TPA)) {
            System.out.println("Teleporting " + requester.username + " to " + request.recipientName);
            plugin.getTeleportManager().teleportToPlayerByName(requester, request.recipientName, true);
        }
    }

}
