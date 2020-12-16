package me.william278.huskhomes2;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.william278.huskhomes2.Integrations.vanishChecker;
import me.william278.huskhomes2.Objects.TeleportRequest;
import me.william278.huskhomes2.Objects.TeleportationPoint;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.*;

public class pluginMessageHandler implements PluginMessageListener {

    private static final HuskHomes plugin = HuskHomes.getInstance();

    // Move a player to a different server in the bungee network
    public static void sendPlayer(Player p, String targetServer) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(targetServer);
        p.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
    }

    // Send a plugin message
    public static void sendPluginMessage(Player sender, String targetPlayerName, String messageType, String messageData) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        // Send a plugin message to the specified player name
        out.writeUTF("ForwardToPlayer");
        out.writeUTF(targetPlayerName);

        // Send the HuskHomes message with a specific type
        out.writeUTF("HuskHomes:" + messageType);
        ByteArrayOutputStream messageBytes = new ByteArrayOutputStream();
        DataOutputStream messageOut = new DataOutputStream(messageBytes);

        // Send the message data; output an exception if there's an error
        try {
            messageOut.writeUTF(messageData);
        } catch (IOException e) {
            Bukkit.getLogger().warning("An error occurred trying to send a plugin message (" + e.getCause() + ")");
            e.printStackTrace();
        }

        // Write the messages to the output packet
        out.writeShort(messageBytes.toByteArray().length);
        out.write(messageBytes.toByteArray());

        // Send the constructed plugin message packet
        sender.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
    }

    // When a plugin message is received
    @Override
    public void onPluginMessageReceived(String channel, Player recipient, byte[] message) {
        // Return if the message is not a Bungee message
        if (!channel.equals("BungeeCord")) {
            return;
        }
        ByteArrayDataInput input = ByteStreams.newDataInput(message);
        String messageType = input.readUTF();

        // Return if the message was not sent by HuskHomes
        if (!messageType.contains("HuskHomes:")) {
            return;
        }

        // Get the HuskHomes message type
        messageType = messageType.split(":")[1];

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

        // Used in replying to tp requests
        String replierName;
        boolean accepted;

        // Respond to different cross-server requests
        switch (messageType) {
            case "set_tp_destination":
                setTeleportDestination(messageData, recipient);
                pluginMessageHandler.sendPluginMessage(recipient, messageData, "confirm_destination_set", "confirmed");
                break;
            case "confirm_destination_set":
                if (messageData.equals("confirmed")) {
                    teleportManager.teleportPlayer(recipient);
                }
                break;
            case "tpa_request":
                if (!vanishChecker.isVanished(recipient)) {
                    teleportRequestHandler.teleportRequests.put(recipient, new TeleportRequest(messageData, "tpa"));
                    messageManager.sendMessage(recipient, "tpa_request_ask", messageData);
                }
                break;
            case "tpahere_request":
                if (!vanishChecker.isVanished(recipient)) {
                    teleportRequestHandler.teleportRequests.put(recipient, new TeleportRequest(messageData, "tpahere"));
                    messageManager.sendMessage(recipient, "tpahere_request_ask", messageData);
                }
                break;
            case "tpa_request_reply":
                replierName = messageData.split(":")[0];
                accepted = Boolean.parseBoolean(messageData.split(":")[1]);
                if (accepted) {
                    messageManager.sendMessage(recipient, "tpa_has_accepted", replierName);
                    teleportManager.queueTimedTeleport(recipient, replierName);
                } else {
                    messageManager.sendMessage(recipient, "tpa_has_declined", replierName);
                }
                break;
            case "tpahere_request_reply":
                replierName = messageData.split(":")[0];
                accepted = Boolean.parseBoolean(messageData.split(":")[1]);
                if (accepted) {
                    messageManager.sendMessage(recipient, "tpa_has_accepted", replierName);
                } else {
                    messageManager.sendMessage(recipient, "tpa_has_declined", replierName);
                }
                break;
            case "teleport_to_me":
                teleportManager.teleportPlayer(recipient, messageData);
                break;
        }
    }

    // Set the requesters' teleport destination to a teleportation point of the recipient's current location
    public void setTeleportDestination(String requesterName, Player recipient) {
        dataManager.setPlayerDestinationLocation(requesterName,
                new TeleportationPoint(recipient.getLocation(), HuskHomes.settings.getServerID()));
    }
}
