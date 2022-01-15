package me.william278.huskhomes2.teleport;

import me.william278.huskhomes2.HuskHomes;
import me.william278.huskhomes2.MessageManager;
import me.william278.huskhomes2.data.message.CrossServerMessageHandler;
import me.william278.huskhomes2.data.message.Message;
import me.william278.huskhomes2.integrations.VanishChecker;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class TeleportRequestHandler {

    private static final HuskHomes plugin = HuskHomes.getInstance();

    // Target player and teleport request(s) to them hashmap
    public static HashMap<Player, HashMap<String, TeleportRequest>> teleportRequests = new HashMap<>();

    private static void sendTeleportRequestCrossServer(Player requester, String targetPlayerName, TeleportRequest.RequestType requestType) {
        if (requestType == TeleportRequest.RequestType.TPA) {
            CrossServerMessageHandler.getMessage(targetPlayerName, Message.MessageType.TPA_REQUEST, requester.getName()).send(requester);
        } else if (requestType == TeleportRequest.RequestType.TPA_HERE) {
            CrossServerMessageHandler.getMessage(targetPlayerName, Message.MessageType.TPA_HERE_REQUEST, requester.getName()).send(requester);
        }
    }

    private static void replyTeleportRequestCrossServer(Player replier, String requesterName, TeleportRequest.RequestType requestType, boolean accepted) {
        if (requestType == TeleportRequest.RequestType.TPA) {
            CrossServerMessageHandler.getMessage(requesterName, Message.MessageType.TPA_REQUEST_REPLY, replier.getName(), Boolean.toString(accepted)).send(replier);
        } else if (requestType == TeleportRequest.RequestType.TPA_HERE) {
            CrossServerMessageHandler.getMessage(requesterName, Message.MessageType.TPA_HERE_REQUEST_REPLY, replier.getName(), Boolean.toString(accepted)).send(replier);
        }
    }

    public static void sendTeleportToRequest(Player requester, String targetPlayerName) {
        Player targetPlayer = Bukkit.getPlayerExact(targetPlayerName);
        if (targetPlayer != null) {
            if (targetPlayer.getUniqueId() != requester.getUniqueId()) {
                if (!VanishChecker.isVanished(targetPlayer)) {
                    MessageManager.sendMessage(requester, "tpa_request_sent", targetPlayerName);
                    if (!HuskHomes.isIgnoringTeleportRequests(targetPlayer.getUniqueId())) {
                        if (isDuplicateRequest(targetPlayerName, targetPlayer)) {
                            return;
                        }
                        teleportRequests.get(targetPlayer).put(requester.getName(), new TeleportRequest(requester.getName(),
                                TeleportRequest.RequestType.TPA));
                        MessageManager.sendMessage(targetPlayer, "tpa_request_ask", requester.getName());
                        MessageManager.sendMessage(targetPlayer, "teleport_request_options", requester.getName());
                    }
                } else {
                    MessageManager.sendMessage(requester, "error_player_not_found", targetPlayerName);
                }
            } else {
                MessageManager.sendMessage(requester, "error_tp_self");
            }
        } else {
            if (HuskHomes.getSettings().doBungee()) {
                sendTeleportRequestCrossServer(requester, targetPlayerName, TeleportRequest.RequestType.TPA);
                MessageManager.sendMessage(requester, "tpa_request_sent", targetPlayerName);
            } else {
                MessageManager.sendMessage(requester, "error_player_not_found", targetPlayerName);
            }
        }
    }

    public static void sendTeleportAllHereRequest(Player requester) {
        if (HuskHomes.getPlayerList().getPlayers().size() <= 1) {
            MessageManager.sendMessage(requester, "error_no_other_online_players");
            return;
        }
        for (String playerName : HuskHomes.getPlayerList().getPlayers()) {
            if (playerName.equals(requester.getName())) {
                continue;
            }
            final Player targetPlayer = Bukkit.getPlayerExact(playerName);
            if (targetPlayer != null) {
                sendLocalTpaHereRequest(requester, playerName, targetPlayer);
            } else {
                if (HuskHomes.getSettings().doBungee()) {
                    sendTeleportRequestCrossServer(requester, playerName, TeleportRequest.RequestType.TPA_HERE);
                }
            }
        }
        MessageManager.sendMessage(requester, "tpaall_request_sent");
    }

    // Send the local tpa here request, provided the user is not ignoring requests
    private static void sendLocalTpaHereRequest(Player requester, String playerName, Player targetPlayer) {
        if (!HuskHomes.isIgnoringTeleportRequests(targetPlayer.getUniqueId())) {
            if (isDuplicateRequest(playerName, targetPlayer)) {
                return;
            }
            teleportRequests.get(targetPlayer).put(requester.getName(), new TeleportRequest(requester.getName(),
                    TeleportRequest.RequestType.TPA_HERE));
            MessageManager.sendMessage(targetPlayer, "tpahere_request_ask", requester.getName());
            MessageManager.sendMessage(targetPlayer, "teleport_request_options", requester.getName());
        }
    }

    public static void sendTeleportHereRequest(Player requester, String targetPlayerName) {
        Player targetPlayer = Bukkit.getPlayerExact(targetPlayerName);
        if (targetPlayer != null) {
            if (targetPlayer.getUniqueId() != requester.getUniqueId()) {
                sendLocalTpaHereRequest(requester, targetPlayerName, targetPlayer);
                MessageManager.sendMessage(requester, "tpahere_request_sent", targetPlayerName);
            } else {
                MessageManager.sendMessage(requester, "error_tp_self");
            }
        } else {
            if (HuskHomes.getSettings().doBungee()) {
                sendTeleportRequestCrossServer(requester, targetPlayerName, TeleportRequest.RequestType.TPA_HERE);
                MessageManager.sendMessage(requester, "tpahere_request_sent", targetPlayerName);
            } else {
                MessageManager.sendMessage(requester, "error_player_not_found", targetPlayerName);
            }
        }
    }

    private static void handleRequestReply(Player p, TeleportRequest teleportRequest, boolean accepted) {
        if (teleportRequest.isExpired()) {
            teleportRequests.get(p).remove(teleportRequest.getSenderName());
            if (teleportRequests.get(p).isEmpty()) {
                teleportRequests.remove(p);
            }
            MessageManager.sendMessage(p, "error_tp_request_expired");
            return;
        }
        String requesterName = teleportRequest.getSenderName();
        TeleportRequest.RequestType requestType = teleportRequest.getRequestType();
        Player requester = Bukkit.getPlayer(requesterName);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            if (requester != null) {
                if (accepted) {
                    MessageManager.sendMessage(p, "tpa_you_accepted", requesterName);
                    MessageManager.sendMessage(requester, "tpa_has_accepted", p.getName());

                    if (requestType == TeleportRequest.RequestType.TPA) {
                        TeleportManager.queueTimedTeleport(requester, p.getName());
                    } else if (requestType == TeleportRequest.RequestType.TPA_HERE) {
                        TeleportManager.queueTimedTeleport(p, requesterName);
                    }
                } else {
                    MessageManager.sendMessage(p, "tpa_you_declined", requesterName);
                    MessageManager.sendMessage(requester, "tpa_has_declined", p.getName());
                }
            } else {
                if (HuskHomes.getSettings().doBungee()) {
                    if (accepted) {
                        MessageManager.sendMessage(p, "tpa_you_accepted", requesterName);
                        replyTeleportRequestCrossServer(p, requesterName, requestType, true);
                        if (requestType == TeleportRequest.RequestType.TPA_HERE) {
                            TeleportManager.queueTimedTeleport(p, requesterName);
                        }
                    } else {
                        replyTeleportRequestCrossServer(p, requesterName, requestType, false);
                        MessageManager.sendMessage(p, "tpa_you_declined", requesterName);
                    }
                } else {
                    MessageManager.sendMessage(p, "error_player_not_found", requesterName);
                }
            }
            teleportRequests.get(p).remove(teleportRequest.getSenderName());
            if (teleportRequests.get(p).isEmpty()) {
                teleportRequests.remove(p);
            }
        });
    }

    public static void replyTpRequest(Player p, boolean accepted) {
        if (!teleportRequests.containsKey(p)) {
            MessageManager.sendMessage(p, "error_tpa_no_pending_request");
            return;
        }
        if (teleportRequests.get(p).isEmpty()) {
            MessageManager.sendMessage(p, "error_tpa_no_pending_request");
            return;
        }
        if (HuskHomes.isIgnoringTeleportRequests(p.getUniqueId())) {
            MessageManager.sendMessage(p, "error_ignoring_teleport_requests");
            return;
        }
        HashMap<String, TeleportRequest> playerRequests = teleportRequests.get(p);
        long firstRequestTime = Long.MAX_VALUE;
        TeleportRequest targetRequest = null;
        for (String playerName : playerRequests.keySet()) {
            TeleportRequest request = playerRequests.get(playerName);
            if (request.getRequestTime() < firstRequestTime) {
                targetRequest = request;
                firstRequestTime = targetRequest.getRequestTime();
            }
        }
        if (targetRequest == null) {
            MessageManager.sendMessage(p, "error_tpa_no_pending_request");
            return;
        }
        handleRequestReply(p, targetRequest, accepted);
    }

    public static void replyTpRequest(Player p, String requesterName, boolean accepted) {
        if (!teleportRequests.containsKey(p)) {
            MessageManager.sendMessage(p, "error_tpa_no_pending_request");
            return;
        }
        if (teleportRequests.get(p).isEmpty()) {
            MessageManager.sendMessage(p, "error_tpa_no_pending_request");
            return;
        }
        if (HuskHomes.isIgnoringTeleportRequests(p.getUniqueId())) {
            MessageManager.sendMessage(p, "error_ignoring_teleport_requests");
            return;
        }
        HashMap<String, TeleportRequest> playerRequests = teleportRequests.get(p);
        TeleportRequest targetRequest = null;
        for (String playerName : playerRequests.keySet()) {
            TeleportRequest request = playerRequests.get(playerName);
            if (request.getSenderName().equalsIgnoreCase(requesterName)) {
                targetRequest = request;
                break;
            }
        }
        if (targetRequest == null) {
            MessageManager.sendMessage(p, "error_invalid_player");
            return;
        }
        handleRequestReply(p, targetRequest, accepted);
    }

    // Handle an incoming request; if it's a duplicate, return true, otherwise allow the new request (return false)
    public static boolean isDuplicateRequest(String senderName, Player recipient) {
        if (teleportRequests.containsKey(recipient)) {
            if (teleportRequests.get(recipient).containsKey(senderName)) {
                TeleportRequest request = teleportRequests.get(recipient).get(senderName);
                if (!request.isExpired()) {
                    return true;
                } else {
                    teleportRequests.get(recipient).remove(senderName);
                    if (teleportRequests.get(recipient).isEmpty()) {
                        teleportRequests.remove(recipient);
                    }
                }
            }
        } else {
            teleportRequests.put(recipient, new HashMap<>());
        }
        return false;
    }
}
