package net.william278.huskhomes.manager;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.network.Message;
import net.william278.huskhomes.network.Payload;
import net.william278.huskhomes.teleport.Teleport;
import net.william278.huskhomes.teleport.TeleportBuilder;
import net.william278.huskhomes.teleport.TeleportRequest;
import net.william278.huskhomes.user.OnlineUser;
import net.william278.huskhomes.user.User;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.*;

/**
 * Manages {@link TeleportRequest}s between players
 */
public class RequestsManager {

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

    public RequestsManager(@NotNull HuskHomes implementation) {
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
            this.ignoringRequests.add(user.getUuid());
        } else {
            this.ignoringRequests.remove(user.getUuid());
        }
    }

    /**
     * Return if a user is ignoring tpa requests
     *
     * @param user the user to check
     * @return {@code true} if the user is ignoring tpa requests
     */
    public boolean isIgnoringRequests(@NotNull User user) {
        return this.ignoringRequests.contains(user.getUuid());
    }

    /**
     * Add a teleport request to a user's request queue
     *
     * @param request   the {@link TeleportRequest} to add
     * @param recipient the {@link User} recipient of the request
     */
    public void addTeleportRequest(@NotNull TeleportRequest request, @NotNull User recipient) {
        this.requests.computeIfAbsent(recipient.getUuid(), uuid -> new LinkedList<>()).addFirst(request);
    }

    /**
     * Remove {@link TeleportRequest}(s) sent by a requester, by name, from a recipient's queue
     *
     * @param requesterName username of the sender of the request(s) to delete
     * @param recipient     the {@link User} recipient of the request
     */
    public void removeTeleportRequest(@NotNull String requesterName, @NotNull User recipient) {
        this.requests.computeIfPresent(recipient.getUuid(), (uuid, requests) -> {
            requests.removeIf(teleportRequest -> teleportRequest.getRequesterName().equalsIgnoreCase(requesterName));
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
        return this.requests.getOrDefault(recipient.getUuid(), new LinkedList<>()).stream().findFirst();
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
        return this.requests.getOrDefault(recipient.getUuid(), new LinkedList<>()).stream()
                .filter(request -> request.getRequesterName().equalsIgnoreCase(requesterName))
                .filter(request -> !request.hasExpired())
                .findFirst()
                .or(() -> this.requests.getOrDefault(recipient.getUuid(), new LinkedList<>()).stream()
                        .filter(request -> request.getRequesterName().equalsIgnoreCase(requesterName))
                        .findFirst());
    }

    public void sendTeleportAllRequest(@NotNull OnlineUser requester) {
        final long expiry = Instant.now().getEpochSecond() + plugin.getSettings().getTeleportRequestExpiryTime();
        final TeleportRequest request = new TeleportRequest(requester, TeleportRequest.Type.TPA_HERE, expiry);
        for (OnlineUser onlineUser : plugin.getOnlineUsers()) {
            if (onlineUser.equals(requester)) {
                continue;
            }
            request.setRecipientName(onlineUser.getUsername());
            sendLocalTeleportRequest(request, onlineUser);
        }

        if (plugin.getSettings().isCrossServer()) {
            Message.builder()
                    .type(Message.Type.TELEPORT_REQUEST)
                    .payload(Payload.withTeleportRequest(request))
                    .target(Message.TARGET_ALL)
                    .build().send(plugin.getMessenger(), requester);
        }
    }

    /**
     * Sends a teleport request of the given type to the specified user, by name, if they exist.
     *
     * @param requester  The user making the request
     * @param targetUser The user to send the request to
     * @param type       The type of request to send
     */
    public void sendTeleportRequest(@NotNull OnlineUser requester, @NotNull String targetUser,
                                    @NotNull TeleportRequest.Type type) throws IllegalArgumentException {
        final long expiry = Instant.now().getEpochSecond() + plugin.getSettings().getTeleportRequestExpiryTime();
        final TeleportRequest request = new TeleportRequest(requester, type, expiry);
        final Optional<OnlineUser> localTarget = plugin.findOnlinePlayer(targetUser);
        if (localTarget.isPresent()) {
            if (localTarget.get().equals(requester)) {
                throw new IllegalArgumentException("Cannot send a teleport request to yourself");
            }
            sendLocalTeleportRequest(request, localTarget.get());
            return;
        }

        // If the player couldn't be found locally, send the request cross-server
        if (plugin.getSettings().isCrossServer()) {
            request.setRecipientName(targetUser);
            Message.builder()
                    .type(Message.Type.TELEPORT_REQUEST)
                    .payload(Payload.withTeleportRequest(request))
                    .target(targetUser)
                    .build().send(plugin.getMessenger(), requester);
        }
        throw new IllegalArgumentException("Player not found");
    }

    /**
     * Sends a teleport request locally on this server asking the recipient to accept or deny the request.
     *
     * @param request   The {@link TeleportRequest} to send
     * @param recipient The online recipient of the request
     * @return the {@link TeleportRequest} that was sent if it could be sent, otherwise an empty optional if it was not
     * sent because the recipient is ignoring requests or is vanished ({@link OnlineUser#isVanished()})
     */
    public void sendLocalTeleportRequest(@NotNull TeleportRequest request, @NotNull OnlineUser recipient) {
        request.setRecipientName(recipient.getUsername());

        // Silently ignore the request if the recipient is ignoring requests or is vanished
        if (isIgnoringRequests(recipient) || recipient.isVanished()) {
            request.setStatus(TeleportRequest.Status.IGNORED);
            return;
        }

        // If the person already has an unexpired request of the same type by this player, don't bother sending another
        final Optional<TeleportRequest> existingRequest = getTeleportRequest(request.getRequesterName(), recipient);
        if (existingRequest.isPresent()) {
            if (existingRequest.get().getType() == request.getType() && !existingRequest.get().hasExpired()) {
                return;
            }
        }

        // Add the request and display a message to the recipient
        addTeleportRequest(request, recipient);
        plugin.getLocales().getLocale((request.getType() == TeleportRequest.Type.TPA ? "tpa" : "tpahere")
                        + "_request_received", request.getRequesterName())
                .ifPresent(recipient::sendMessage);
        plugin.getLocales().getLocale("teleport_request_buttons", request.getRequesterName())
                .ifPresent(recipient::sendMessage);
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
            plugin.getLocales().getLocale("error_ignoring_teleport_requests")
                    .ifPresent(recipient::sendMessage);
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
            plugin.getLocales().getLocale("error_ignoring_teleport_requests")
                    .ifPresent(recipient::sendMessage);
            return;
        }

        // Get the latest request and handle the response
        final Optional<TeleportRequest> lastRequest = getLastTeleportRequest(recipient);
        if (lastRequest.isEmpty()) {
            plugin.getLocales().getLocale("error_no_teleport_requests")
                    .ifPresent(recipient::sendMessage);
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
        removeTeleportRequest(request.getRequesterName(), recipient);

        // Check if the request has expired
        if (request.hasExpired()) {
            plugin.getLocales().getLocale("error_teleport_request_expired").ifPresent(recipient::sendMessage);
            return;
        }

        // Send request response confirmation to the recipient
        plugin.getLocales().getLocale("teleport_request_" + (accepted ? "accepted" : "declined") + "_confirmation",
                        request.getRequesterName())
                .ifPresent(recipient::sendMessage);
        request.setStatus(accepted ? TeleportRequest.Status.ACCEPTED : TeleportRequest.Status.DECLINED);

        // Send request response to the sender
        final Optional<OnlineUser> localRequester = plugin.findOnlinePlayer(request.getRequesterName());
        if (localRequester.isPresent()) {
            handleLocalRequestResponse(localRequester.get(), request);
        } else if (plugin.getSettings().isCrossServer()) {
            Message.builder()
                    .type(Message.Type.TELEPORT_REQUEST_RESPONSE)
                    .payload(Payload.withTeleportRequest(request))
                    .target(request.getRequesterName())
                    .build()
                    .send(plugin.getMessenger(), recipient);
        } else {
            plugin.getLocales().getLocale("error_teleport_request_sender_not_online")
                    .ifPresent(recipient::sendMessage);
            return;
        }

        // If the request is a tpa here request, teleport the recipient to the sender
        if (accepted && request.getType() == TeleportRequest.Type.TPA_HERE) {
            final TeleportBuilder builder = Teleport.builder(plugin)
                    .teleporter(recipient);

            // Strict /tpahere requests will teleport to where the sender was when typing the command
            if (plugin.getSettings().doStrictTpaHereRequests()) {
                builder.target(request.getRequesterPosition());
            } else {
                builder.target(request.getRequesterName());
            }

            builder.toTimedTeleport().execute();
        }

    }

    /**
     * Handle a teleport request response for a local user
     *
     * @param requester The user who sent the request
     * @param request   The {@link TeleportRequest} to handle
     */
    public void handleLocalRequestResponse(@NotNull OnlineUser requester, @NotNull TeleportRequest request) {
        boolean accepted = request.getStatus() == TeleportRequest.Status.ACCEPTED;
        plugin.getLocales().getLocale("teleport_request_" + (accepted ? "accepted" : "declined"),
                request.getRecipientName()).ifPresent(requester::sendMessage);

        // If the request is a tpa request, teleport the requester to the recipient
        if (accepted && (request.getType() == TeleportRequest.Type.TPA)) {
            Teleport.builder(plugin)
                    .teleporter(requester)
                    .target(request.getRecipientName())
                    .toTimedTeleport()
                    .execute();
        }
    }

}
