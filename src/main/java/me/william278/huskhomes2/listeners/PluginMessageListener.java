package me.william278.huskhomes2.listeners;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import me.william278.huskhomes2.HuskHomes;
import me.william278.huskhomes2.MessageManager;
import me.william278.huskhomes2.data.DataManager;
import me.william278.huskhomes2.data.pluginmessage.PluginMessage;
import me.william278.huskhomes2.data.pluginmessage.PluginMessageType;
import me.william278.huskhomes2.integrations.VanishChecker;
import me.william278.huskhomes2.teleport.TeleportManager;
import me.william278.huskhomes2.teleport.TeleportRequest;
import me.william278.huskhomes2.teleport.TeleportRequestHandler;
import me.william278.huskhomes2.teleport.points.TeleportationPoint;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;

public class PluginMessageListener implements org.bukkit.plugin.messaging.PluginMessageListener {

    private static final HuskHomes plugin = HuskHomes.getInstance();

    private void handlePluginMessage(PluginMessage pluginMessage, Player recipient) {
        Connection connection = HuskHomes.getConnection();
        switch (pluginMessage.getMessageType()) {
            case SET_TP_DESTINATION:
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    try {
                        DataManager.setPlayerDestinationLocation(pluginMessage.getMessageData(),
                                new TeleportationPoint(recipient.getLocation(), HuskHomes.getSettings().getServerID()), connection);
                    } catch (SQLException e) {
                        plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred responding to a plugin message teleport destination update");
                        new PluginMessage(pluginMessage.getMessageData(), PluginMessageType.CONFIRM_DESTINATION_SET, "confirmed").send(recipient);
                    }
                });
                return;
            case CONFIRM_DESTINATION_SET:
                if (pluginMessage.getMessageData().equals("confirmed")) {
                    TeleportManager.teleportPlayer(recipient);
                }
                return;
            case TPA_REQUEST:
                if (!VanishChecker.isVanished(recipient)) {
                    TeleportRequestHandler.teleportRequests.put(recipient, new TeleportRequest(pluginMessage.getMessageData(), TeleportRequest.RequestType.TPA));
                    MessageManager.sendMessage(recipient, "tpa_request_ask", pluginMessage.getMessageData());
                    TeleportRequestHandler.sendTpAcceptDenyButtons(recipient);
                }
                return;
            case TPAHERE_REQUEST:
                if (!VanishChecker.isVanished(recipient)) {
                    TeleportRequestHandler.teleportRequests.put(recipient, new TeleportRequest(pluginMessage.getMessageData(), TeleportRequest.RequestType.TPAHERE));
                    MessageManager.sendMessage(recipient, "tpahere_request_ask", pluginMessage.getMessageData());
                    TeleportRequestHandler.sendTpAcceptDenyButtons(recipient);
                }
                return;
            case TPA_REQUEST_REPLY:
                final String tpaReplierName = pluginMessage.getMessageDataItems()[0];
                final boolean tpaAccepted = Boolean.parseBoolean(pluginMessage.getMessageDataItems()[1]);
                if (tpaAccepted) {
                    MessageManager.sendMessage(recipient, "tpa_has_accepted", tpaReplierName);
                    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                        try {
                            TeleportManager.queueTimedTeleport(recipient, tpaReplierName, connection);
                        } catch (SQLException e) {
                            plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred responding to a plugin message teleport request reply");
                        }
                    });
                } else {
                    MessageManager.sendMessage(recipient, "tpa_has_declined", tpaReplierName);
                }
                return;
            case TPAHERE_REQUEST_REPLY:
                final String tpaHereReplierName = pluginMessage.getMessageDataItems()[0];
                final boolean tpaHereAccepted = Boolean.parseBoolean(pluginMessage.getMessageDataItems()[1]);
                if (tpaHereAccepted) {
                    MessageManager.sendMessage(recipient, "tpa_has_accepted", tpaHereReplierName);
                } else {
                    MessageManager.sendMessage(recipient, "tpa_has_declined", tpaHereReplierName);
                }
                return;
            case TELEPORT_TO_ME:
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    try {
                        TeleportManager.teleportPlayer(recipient, pluginMessage.getMessageData(), connection);
                    } catch (SQLException e) {
                        plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred responding to a plugin message teleport-to-me request");
                    }
                });
                return;
            case GET_ONLINE_PLAYERS:
                /*if (Bukkit.getOnlinePlayers().size() > 0) {
                    returnPlayerList(messageData, recipient);
                }*/
                return;
            default:
                HuskHomes.getInstance().getLogger().log(Level.WARNING, "Received a HuskHomes plugin message with an unrecognised type. Is your version of HuskTowns up to date?");
        }
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        // Return if the message is not a Bungee message
        if (!channel.equals("BungeeCord")) {
            return;
        }
        ByteArrayDataInput input = ByteStreams.newDataInput(message);

        // Plugin messages are formatted as such:
        // HuskHomes:<cluster_id>:<message_type>, followed by the message arguments and data.
        String messageType = input.readUTF();
        int clusterID;

        // Return if the message was not sent by HuskHomes
        if (!messageType.contains("HuskHomes:")) {
            return;
        }

        // Ensure the cluster ID matches
        try {
            clusterID = Integer.parseInt(messageType.split(":")[1]);
        } catch (Exception e) {
            // In case the message is malformed or the cluster ID is invalid
            HuskHomes.getInstance().getLogger().warning("Received a HuskHomes plugin message with an invalid server Cluster ID! \n" +
                    "Please ensure that the cluster ID is set to a valid integer on all servers.");
            return;
        }
        if (HuskHomes.getSettings().getClusterID() != clusterID) {
            return;
        }

        // Get the message data packets
        String messageData = "";
        short messageLength = input.readShort();
        byte[] messageBytes = new byte[messageLength];
        input.readFully(messageBytes);
        DataInputStream messageIn = new DataInputStream(new ByteArrayInputStream(messageBytes));

        // Get the message data string from the packets received
        try {
            messageData = messageIn.readUTF();
        } catch (IOException e) {
            Bukkit.getLogger().warning("An error occurred trying to read a plugin message (" + e.getCause() + ")");
            e.printStackTrace();
        }

        // Handle the plugin message appropriately
        handlePluginMessage(new PluginMessage(clusterID, player.getName(), messageType.split(":")[2], messageData), player);
    }

}
