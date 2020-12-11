package me.william278.huskhomes2;

import me.william278.huskhomes2.Objects.TeleportRequest;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class teleportRequestHandler {

    // Target player and teleport request to them hashmap
    public static HashMap<Player, TeleportRequest> teleportRequests = new HashMap<>();

    private static void sendTeleportRequestCrossServer(Player requester, String targetPlayerName, String teleportRequestType) {
        String pluginMessage = teleportRequestType + "_request";
        pluginMessageHandler.sendPluginMessage(requester, targetPlayerName, pluginMessage, requester.getName());
    }

    private static void replyTeleportRequestCrossServer(Player replier, String requesterName, String teleportRequestType, boolean accepted) {
        String pluginMessage = teleportRequestType + "_request_reply";
        pluginMessageHandler.sendPluginMessage(replier, requesterName, pluginMessage, replier.getName() + ":" + accepted);
    }

    public static void sendTeleportToRequest(Player requester, String targetPlayerName) {
        Player targetPlayer = Bukkit.getPlayer(targetPlayerName);
        if (targetPlayer != null) {
            teleportRequests.put(targetPlayer, new TeleportRequest(requester.getName(), "tpa"));
            messageManager.sendMessage(requester, "tpa_request_sent", targetPlayerName);
            messageManager.sendMessage(targetPlayer, "tpa_request_ask", targetPlayerName);
        } else {
            if (Main.settings.doBungee()) {
                sendTeleportRequestCrossServer(requester, targetPlayerName, "tpa");
                messageManager.sendMessage(requester, "tpa_request_sent", targetPlayerName);
            } else {
                messageManager.sendMessage(requester, "error_player_not_found", targetPlayerName);
            }
        }
    }

    public static void sendTeleportHereRequest(Player requester, String targetPlayerName) {
        Player targetPlayer = Bukkit.getPlayer(targetPlayerName);
        if (targetPlayer != null) {
            teleportRequests.put(targetPlayer, new TeleportRequest(requester.getName(), "tpahere"));
            messageManager.sendMessage(requester, "tpahere_request_sent", targetPlayerName);
            messageManager.sendMessage(targetPlayer, "tpahere_request_ask", targetPlayerName);
        } else {
            if (Main.settings.doBungee()) {
                sendTeleportRequestCrossServer(requester, targetPlayerName, "tpahere");
                messageManager.sendMessage(requester, "tpahere_request_sent", targetPlayerName);
            } else {
                messageManager.sendMessage(requester, "error_player_not_found", targetPlayerName);
            }
        }
    }

    public static void replyTpRequest(Player p, boolean accepted) {
        if (!teleportRequests.containsKey(p)) {
            messageManager.sendMessage(p, "error_tpa_no_pending_request");
            return;
        }
        TeleportRequest teleportRequest = teleportRequests.get(p);
        String requesterName = teleportRequest.getSenderName();
        String requestType = teleportRequest.getRequestType();
        Player requester = Bukkit.getPlayer(requesterName);

        if (requester != null) {
            if (accepted) {
                messageManager.sendMessage(p, "tpa_you_accepted", requesterName);
                messageManager.sendMessage(requester, "tpa_has_accepted", p.getName());

                if (requestType.equals("tpa")) {
                    teleportManager.queueTimedTeleport(requester, p.getName());
                } else if (requestType.equals("tpahere")) {
                    teleportManager.queueTimedTeleport(p, requesterName);
                }
            } else {
                messageManager.sendMessage(p, "tpa_you_declined", requesterName);
                messageManager.sendMessage(requester, "tpa_has_declined", p.getName());
            }
        } else {
            if (Main.settings.doBungee()) {
                if (accepted) {
                    messageManager.sendMessage(p, "tpa_you_accepted", requesterName);

                    if (requestType.equals("tpa")) {
                        replyTeleportRequestCrossServer(p, requesterName, "tpa", true);
                    } else if (requestType.equals("tpahere")) {
                        replyTeleportRequestCrossServer(p, requesterName, "tpahere", true);
                        teleportManager.queueTimedTeleport(p, requesterName);
                    }
                } else {
                    replyTeleportRequestCrossServer(p, requesterName, requestType, false);
                    messageManager.sendMessage(p, "tpa_you_declined", requesterName);
                }
            } else {
                messageManager.sendMessage(p, "error_player_not_found", requesterName);
            }
        }
        teleportRequests.remove(p);
    }

}
