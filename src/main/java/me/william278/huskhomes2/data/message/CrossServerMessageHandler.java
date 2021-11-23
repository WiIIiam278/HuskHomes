package me.william278.huskhomes2.data.message;

import me.william278.huskhomes2.HuskHomes;
import me.william278.huskhomes2.MessageManager;
import me.william278.huskhomes2.data.DataManager;
import me.william278.huskhomes2.data.message.pluginmessage.PluginMessage;
import me.william278.huskhomes2.data.message.redis.RedisMessage;
import me.william278.huskhomes2.integrations.VanishChecker;
import me.william278.huskhomes2.teleport.TeleportManager;
import me.william278.huskhomes2.teleport.TeleportRequest;
import me.william278.huskhomes2.teleport.TeleportRequestHandler;
import me.william278.huskhomes2.teleport.points.TeleportationPoint;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.StringJoiner;
import java.util.logging.Level;

public class CrossServerMessageHandler {

    private static final HuskHomes plugin = HuskHomes.getInstance();

    public static void handlePluginMessage(Message receivedMessage, Player recipient) {
        switch (receivedMessage.getMessageType()) {
            case SET_TP_DESTINATION -> Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try (Connection connection = HuskHomes.getConnection()) {
                    DataManager.setPlayerDestinationLocation(receivedMessage.getMessageData(),
                            new TeleportationPoint(recipient.getLocation(), HuskHomes.getSettings().getServerID()), connection);
                    CrossServerMessageHandler.getMessage(receivedMessage.getMessageData(), Message.MessageType.CONFIRM_DESTINATION_SET, "confirmed").send(recipient);
                } catch (SQLException e) {
                    plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred responding to a plugin message teleport destination update");
                }
            });
            case CONFIRM_DESTINATION_SET -> {
                if (receivedMessage.getMessageData().equals("confirmed")) {
                    TeleportManager.teleportPlayer(recipient);
                }
            }
            case TPA_REQUEST -> {
                if (!VanishChecker.isVanished(recipient)) {
                    if (!HuskHomes.isIgnoringTeleportRequests(recipient.getUniqueId())) {
                        if (TeleportRequestHandler.teleportRequests.containsKey(recipient)) {
                            if (TeleportRequestHandler.teleportRequests.get(recipient).containsKey(receivedMessage.getMessageData())) {
                                return; // Prevent request spam
                            }
                            TeleportRequestHandler.teleportRequests.put(recipient, new HashMap<>());
                        }
                        TeleportRequestHandler.teleportRequests.get(recipient).put(receivedMessage.getMessageData(), new TeleportRequest(receivedMessage.getMessageData(), TeleportRequest.RequestType.TPA));
                        MessageManager.sendMessage(recipient, "tpa_request_ask", receivedMessage.getMessageData());
                        MessageManager.sendMessage(recipient, "teleport_request_options");
                    }
                }
            }
            case TPA_HERE_REQUEST -> {
                if (!VanishChecker.isVanished(recipient)) {
                    if (!HuskHomes.isIgnoringTeleportRequests(recipient.getUniqueId())) {
                        if (TeleportRequestHandler.teleportRequests.containsKey(recipient)) {
                            if (TeleportRequestHandler.teleportRequests.get(recipient).containsKey(receivedMessage.getMessageData())) {
                                return; // Prevent request spam
                            }
                            TeleportRequestHandler.teleportRequests.put(recipient, new HashMap<>());
                        }
                        TeleportRequestHandler.teleportRequests.get(recipient).put(receivedMessage.getMessageData(), new TeleportRequest(receivedMessage.getMessageData(), TeleportRequest.RequestType.TPA_HERE));
                        MessageManager.sendMessage(recipient, "tpahere_request_ask", receivedMessage.getMessageData());
                        MessageManager.sendMessage(recipient, "teleport_request_options");
                    }
                }
            }
            case TPA_REQUEST_REPLY -> {
                final String tpaReplierName = receivedMessage.getMessageDataItems()[0];
                final boolean tpaAccepted = Boolean.parseBoolean(receivedMessage.getMessageDataItems()[1]);
                if (tpaAccepted) {
                    MessageManager.sendMessage(recipient, "tpa_has_accepted", tpaReplierName);
                    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                        try (Connection connection = HuskHomes.getConnection()) {
                            TeleportManager.queueTimedTeleport(recipient, tpaReplierName, connection);
                        } catch (SQLException e) {
                            plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred responding to a plugin message teleport request reply");
                        }
                    });
                } else {
                    MessageManager.sendMessage(recipient, "tpa_has_declined", tpaReplierName);
                }
            }
            case TPA_HERE_REQUEST_REPLY -> {
                final String tpaHereReplierName = receivedMessage.getMessageDataItems()[0];
                final boolean tpaHereAccepted = Boolean.parseBoolean(receivedMessage.getMessageDataItems()[1]);
                if (tpaHereAccepted) {
                    MessageManager.sendMessage(recipient, "tpa_has_accepted", tpaHereReplierName);
                } else {
                    MessageManager.sendMessage(recipient, "tpa_has_declined", tpaHereReplierName);
                }
            }
            case TELEPORT_TO_ME -> Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try (Connection connection = HuskHomes.getConnection()) {
                    TeleportManager.teleportPlayer(recipient, receivedMessage.getMessageData(), connection);
                } catch (SQLException e) {
                    plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred responding to a plugin message teleport-to-me request");
                }
            });
            case GET_PLAYER_LIST -> {
                final String requestingServer = receivedMessage.getMessageData();
                StringJoiner playerList = new StringJoiner("£");
                for (Player p : Bukkit.getOnlinePlayers()) {
                    playerList.add(p.getName());
                }
                if (playerList.toString().equals("")) {
                    return;
                }
                CrossServerMessageHandler.getMessage(Message.MessageType.RETURN_PLAYER_LIST, playerList.toString()).sendToServer(recipient, requestingServer);
            }
            case RETURN_PLAYER_LIST -> {
                final String[] returningPlayers = receivedMessage.getMessageData().split("£");
                HuskHomes.getPlayerList().addPlayers(returningPlayers);
            }
            default -> HuskHomes.getInstance().getLogger().log(Level.WARNING, "Received a HuskHomes plugin message with an unrecognised type. Is your version of HuskHomes up to date?");
        }
    }

    public static Message getMessage(String targetPlayerName, Message.MessageType pluginMessageType, String... messageData) {
        return switch (HuskHomes.getSettings().getMessengerType().toLowerCase()) {
            case "pluginmessage" -> new PluginMessage(targetPlayerName, pluginMessageType, messageData);
            case "redis" -> new RedisMessage(targetPlayerName, pluginMessageType, messageData);
            default -> null;
        };
    }

    public static Message getMessage(Message.MessageType pluginMessageType, String... messageData) {
        return switch (HuskHomes.getSettings().getMessengerType().toLowerCase()) {
            case "pluginmessage" -> new PluginMessage(pluginMessageType, messageData);
            case "redis" -> new RedisMessage(pluginMessageType, messageData);
            default -> null;
        };
    }
}
