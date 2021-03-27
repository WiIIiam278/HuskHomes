package me.william278.huskhomes2;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.william278.huskhomes2.data.DataManager;
import me.william278.huskhomes2.integrations.VanishChecker;
import me.william278.huskhomes2.teleport.TeleportManager;
import me.william278.huskhomes2.teleport.TeleportRequest;
import me.william278.huskhomes2.teleport.TeleportRequestHandler;
import me.william278.huskhomes2.teleport.points.TeleportationPoint;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

public class PluginMessageHandler implements PluginMessageListener {

    private static final HuskHomes plugin = HuskHomes.getInstance();

    // Move a player to a different server in the bungee network
    public static void sendPlayer(Player p, String targetServer) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(targetServer);
        p.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
    }

    // Send a plugin message (to a specific player)
    public static void sendPluginMessage(Player sender, String targetPlayerName, String messageType, String messageData) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        int clusterID = HuskHomes.getSettings().getServerClusterID();

        // Send a plugin message to the specified player name
        out.writeUTF("ForwardToPlayer");
        out.writeUTF(targetPlayerName);

        // Send the HuskHomes message with a specific type
        out.writeUTF("HuskHomes:" + clusterID + ":" + messageType);
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

    // Sends a plugin message request asking for all the player lists on servers
    public static void requestPlayerLists(Player sender) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        int clusterID = HuskHomes.getSettings().getServerClusterID();

        // Send a plugin message to the specified player name
        out.writeUTF("Forward");
        out.writeUTF("ONLINE");

        // Send the HuskHomes message with a specific type
        out.writeUTF("HuskHomes:" + clusterID + ":get_online_players");
        ByteArrayOutputStream messageBytes = new ByteArrayOutputStream();
        DataOutputStream messageOut = new DataOutputStream(messageBytes);

        // Send the message data; output an exception if there's an error
        try {
            messageOut.writeUTF(HuskHomes.getSettings().getServerID());
        } catch (IOException e) {
            Bukkit.getLogger().warning("An error occurred trying to send a plugin message (" + e.getCause() + ")");
            e.printStackTrace();
        }

        // Write the messages to the output packet
        out.writeShort(messageBytes.toByteArray().length);
        out.write(messageBytes.toByteArray());

        // Send the constructed plugin message packet
        if (sender == null) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                sender = p;
                break;
            }
            if (sender == null) {
                return;
            }
        }
        sender.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
    }

    // Sends a plugin message informing other servers the player list needs updating
    public static void broadcastPlayerChange(Player sender) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        int clusterID = HuskHomes.getSettings().getServerClusterID();

        // Send a plugin message to the specified player name
        out.writeUTF("Forward");
        out.writeUTF("ONLINE");

        // Send the HuskHomes message with a specific type
        out.writeUTF("HuskHomes:" + clusterID + ":player_change");
        ByteArrayOutputStream messageBytes = new ByteArrayOutputStream();
        DataOutputStream messageOut = new DataOutputStream(messageBytes);

        // Send the message data; output an exception if there's an error
        try {
            messageOut.writeUTF(HuskHomes.getSettings().getServerID());
        } catch (IOException e) {
            Bukkit.getLogger().warning("An error occurred trying to send a plugin message (" + e.getCause() + ")");
            e.printStackTrace();
        }

        // Write the messages to the output packet
        out.writeShort(messageBytes.toByteArray().length);
        out.write(messageBytes.toByteArray());

        // Send the constructed plugin message packet
        if (sender == null) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                sender = p;
                break;
            }
            if (sender == null) {
                return;
            }
        }
        sender.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
    }

    // Sends a plugin message with player list
    public static void returnPlayerList(String returnTo, Player sender) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        int clusterID = HuskHomes.getSettings().getServerClusterID();

        // Send a plugin message to the specified player name
        out.writeUTF("Forward");
        if (returnTo == null) {
            returnTo = "ONLINE";
        }
        out.writeUTF(returnTo);

        // Send the HuskHomes message with a specific type
        out.writeUTF("HuskHomes:" + clusterID + ":return_online_players");
        ByteArrayOutputStream messageBytes = new ByteArrayOutputStream();
        DataOutputStream messageOut = new DataOutputStream(messageBytes);

        // Send the message data; output an exception if there's an error
        try {
            StringBuilder playerList = new StringBuilder();
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!VanishChecker.isVanished(p)) {
                    playerList.append(p.getName()).append(",");
                }
            }
            if (!playerList.toString().isEmpty()) {
                messageOut.writeUTF(HuskHomes.getSettings().getServerID() + "/" + StringUtils.removeEnd(playerList.toString(), ","));
            }
        } catch (IOException e) {
            Bukkit.getLogger().warning("An error occurred trying to send a plugin message (" + e.getCause() + ")");
            e.printStackTrace();
        }

        // Write the messages to the output packet
        out.writeShort(messageBytes.toByteArray().length);
        out.write(messageBytes.toByteArray());

        // Send the constructed plugin message packet
        if (sender == null) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                sender = p;
                break;
            }
        }
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
            Bukkit.getLogger().warning("Received a HuskHomes plugin message with an invalid server cluster ID! \n" +
                    "Please ensure you are running the latest version of HuskHomes on all your servers and that the cluster ID is set to a valid integer on all of them.");
            return;
        }
        if (HuskHomes.getSettings().getServerClusterID() != clusterID) {
            return;
        }

        // Get the HuskHomes message type
        messageType = messageType.split(":")[2];

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
                PluginMessageHandler.sendPluginMessage(recipient, messageData, "confirm_destination_set", "confirmed");
                break;
            case "confirm_destination_set":
                if (messageData.equals("confirmed")) {
                    TeleportManager.teleportPlayer(recipient);
                }
                break;
            case "tpa_request":
                if (!VanishChecker.isVanished(recipient)) {
                    TeleportRequestHandler.teleportRequests.put(recipient, new TeleportRequest(messageData, "tpa"));
                    MessageManager.sendMessage(recipient, "tpa_request_ask", messageData);
                    TeleportRequestHandler.sendTpAcceptDenyButtons(recipient);
                }
                break;
            case "tpahere_request":
                if (!VanishChecker.isVanished(recipient)) {
                    TeleportRequestHandler.teleportRequests.put(recipient, new TeleportRequest(messageData, "tpahere"));
                    MessageManager.sendMessage(recipient, "tpahere_request_ask", messageData);
                    TeleportRequestHandler.sendTpAcceptDenyButtons(recipient);
                }
                break;
            case "tpa_request_reply":
                replierName = messageData.split(":")[0];
                accepted = Boolean.parseBoolean(messageData.split(":")[1]);
                if (accepted) {
                    MessageManager.sendMessage(recipient, "tpa_has_accepted", replierName);
                    TeleportManager.queueTimedTeleport(recipient, replierName);
                } else {
                    MessageManager.sendMessage(recipient, "tpa_has_declined", replierName);
                }
                break;
            case "tpahere_request_reply":
                replierName = messageData.split(":")[0];
                accepted = Boolean.parseBoolean(messageData.split(":")[1]);
                if (accepted) {
                    MessageManager.sendMessage(recipient, "tpa_has_accepted", replierName);
                } else {
                    MessageManager.sendMessage(recipient, "tpa_has_declined", replierName);
                }
                break;
            case "teleport_to_me":
                TeleportManager.teleportPlayer(recipient, messageData);
                break;
            case "get_online_players":
                if (Bukkit.getOnlinePlayers().size() > 0) {
                    returnPlayerList(messageData, recipient);
                }
                break;
            case "return_online_players":
                String server = messageData.split("/")[0];
                String players = messageData.split("/")[1];
                HashSet<String> playerList = new HashSet<>(Arrays.asList(players.split(",")));
                CrossServerListHandler.updateHashset(server, playerList);
                break;
            case "player_change":
                CrossServerListHandler.updatePlayerList(null);
                break;
        }
    }

    // Set the requesters' teleport destination to a teleportation point of the recipient's current location
    public void setTeleportDestination(String requesterName, Player recipient) {
        DataManager.setPlayerDestinationLocation(requesterName,
                new TeleportationPoint(recipient.getLocation(), HuskHomes.getSettings().getServerID()));
    }
}
