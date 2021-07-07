package me.william278.huskhomes2.teleport;

import me.william278.huskhomes2.HuskHomes;
import me.william278.huskhomes2.MessageManager;
import me.william278.huskhomes2.data.pluginmessage.PluginMessage;
import me.william278.huskhomes2.data.pluginmessage.PluginMessageType;
import me.william278.huskhomes2.integrations.VanishChecker;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class TeleportRequestHandler {

    private static final HuskHomes plugin = HuskHomes.getInstance();

    // Target player and teleport request to them hashmap
    public static Map<Player, TeleportRequest> teleportRequests = new HashMap<>();

    private static void sendTeleportRequestCrossServer(Player requester, String targetPlayerName, TeleportRequest.RequestType requestType) {
        if (requestType == TeleportRequest.RequestType.TPA) {
            new PluginMessage(targetPlayerName, PluginMessageType.TPA_REQUEST, requester.getName()).send(requester);
        } else if (requestType == TeleportRequest.RequestType.TPAHERE) {
            new PluginMessage(targetPlayerName, PluginMessageType.TPAHERE_REQUEST, requester.getName()).send(requester);
        }
    }

    private static void replyTeleportRequestCrossServer(Player replier, String requesterName, TeleportRequest.RequestType requestType, boolean accepted) {
        if (requestType == TeleportRequest.RequestType.TPA) {
            new PluginMessage(requesterName, PluginMessageType.TPA_REQUEST_REPLY, replier.getName(), Boolean.toString(accepted)).send(replier);
        } else if (requestType == TeleportRequest.RequestType.TPAHERE) {
            new PluginMessage(requesterName, PluginMessageType.TPAHERE_REQUEST_REPLY, replier.getName(), Boolean.toString(accepted)).send(replier);
        }
    }

    public static void sendTeleportToRequest(Player requester, String targetPlayerName) {
        Player targetPlayer = Bukkit.getPlayer(targetPlayerName);
        if (targetPlayer != null) {
            if (targetPlayer.getUniqueId() != requester.getUniqueId()) {
                if (!VanishChecker.isVanished(targetPlayer)) {
                    MessageManager.sendMessage(requester, "tpa_request_sent", targetPlayerName);
                    if (!HuskHomes.isIgnoringTeleportRequests(targetPlayer.getUniqueId())) {
                        teleportRequests.put(targetPlayer, new TeleportRequest(requester.getName(), TeleportRequest.RequestType.TPA));
                        MessageManager.sendMessage(targetPlayer, "tpa_request_ask", requester.getName());
                        MessageManager.sendMessage(targetPlayer, "teleport_request_options");
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

    public static void sendTeleportHereRequest(Player requester, String targetPlayerName) {
        Player targetPlayer = Bukkit.getPlayer(targetPlayerName);
        if (targetPlayer != null) {
            if (targetPlayer.getUniqueId() != requester.getUniqueId()) {
                MessageManager.sendMessage(requester, "tpahere_request_sent", targetPlayerName);
                if (!HuskHomes.isIgnoringTeleportRequests(targetPlayer.getUniqueId())) {
                    teleportRequests.put(targetPlayer, new TeleportRequest(requester.getName(), TeleportRequest.RequestType.TPAHERE));
                    MessageManager.sendMessage(targetPlayer, "tpahere_request_ask", requester.getName());
                    MessageManager.sendMessage(targetPlayer, "teleport_request_options");
                }
            } else {
                MessageManager.sendMessage(requester, "error_tp_self");
            }
        } else {
            if (HuskHomes.getSettings().doBungee()) {
                sendTeleportRequestCrossServer(requester, targetPlayerName, TeleportRequest.RequestType.TPAHERE);
                MessageManager.sendMessage(requester, "tpahere_request_sent", targetPlayerName);
            } else {
                MessageManager.sendMessage(requester, "error_player_not_found", targetPlayerName);
            }
        }
    }

    public static void replyTpRequest(Player p, boolean accepted) {
        if (!teleportRequests.containsKey(p)) {
            MessageManager.sendMessage(p, "error_tpa_no_pending_request");
            return;
        }
        if (HuskHomes.isIgnoringTeleportRequests(p.getUniqueId())) {
            MessageManager.sendMessage(p, "error_ignoring_teleport_requests");
            return;
        }
        TeleportRequest teleportRequest = teleportRequests.get(p);
        if (teleportRequest.isExpired()) {
            teleportRequests.remove(p);
            MessageManager.sendMessage(p, "error_tp_request_expired");
            return;
        }
        String requesterName = teleportRequest.getSenderName();
        TeleportRequest.RequestType requestType = teleportRequest.getRequestType();
        Player requester = Bukkit.getPlayer(requesterName);

        Connection connection = HuskHomes.getConnection();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                if (requester != null) {
                    if (accepted) {
                        MessageManager.sendMessage(p, "tpa_you_accepted", requesterName);
                        MessageManager.sendMessage(requester, "tpa_has_accepted", p.getName());

                        if (requestType == TeleportRequest.RequestType.TPA) {
                            TeleportManager.queueTimedTeleport(requester, p.getName(), connection);
                        } else if (requestType == TeleportRequest.RequestType.TPAHERE) {
                            TeleportManager.queueTimedTeleport(p, requesterName, connection);
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
                            if (requestType == TeleportRequest.RequestType.TPAHERE) {
                                TeleportManager.queueTimedTeleport(p, requesterName, connection);
                            }
                        } else {
                            replyTeleportRequestCrossServer(p, requesterName, requestType, false);
                            MessageManager.sendMessage(p, "tpa_you_declined", requesterName);
                        }
                    } else {
                        MessageManager.sendMessage(p, "error_player_not_found", requesterName);
                    }
                }
                teleportRequests.remove(p);
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred!", e);
            }
        });
    }
}
