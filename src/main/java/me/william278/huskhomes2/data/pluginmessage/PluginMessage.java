package me.william278.huskhomes2.data.pluginmessage;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.william278.huskhomes2.HuskHomes;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Locale;

public class PluginMessage {

    private static final HuskHomes plugin = HuskHomes.getInstance();

    // Move a player to a different server in the bungee network
    @SuppressWarnings("UnstableApiUsage")
    public static void sendPlayer(Player p, String targetServer) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("Connect");
            out.writeUTF(targetServer);
            p.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
        });
    }

    private static final String MESSAGE_DATA_SEPARATOR = "$";
    private final int clusterID;
    private final PluginMessageType messageType;
    private final String targetPlayerName;
    private final String messageData;

    /**
     * Creates a Plugin Message ready to be sent
     * @param targetPlayerName Name of the player the message will be sent to over Bungee
     * @param pluginMessageType Type of the plugin message
     * @param messageData Associated message data
     */
    public PluginMessage(String targetPlayerName, PluginMessageType pluginMessageType, String... messageData) {
        this.clusterID = HuskHomes.getSettings().getClusterID();
        this.messageType = pluginMessageType;
        StringBuilder newMessageData = new StringBuilder();
        for (String s : messageData) {
            newMessageData.append(s).append(MESSAGE_DATA_SEPARATOR);
        }
        this.messageData = newMessageData.substring(0, newMessageData.toString().length()-1);
        this.targetPlayerName = targetPlayerName;
    }

    public PluginMessage(PluginMessageType pluginMessageType, String... messageData) {
        this.clusterID = HuskHomes.getSettings().getClusterID();
        this.messageType = pluginMessageType;
        StringBuilder newMessageData = new StringBuilder();
        for (String s : messageData) {
            newMessageData.append(s).append(MESSAGE_DATA_SEPARATOR);
        }
        this.messageData = newMessageData.substring(0, newMessageData.toString().length()-1);
        this.targetPlayerName = null;
    }

    public PluginMessage(int clusterID, String targetPlayerName, String pluginMessageType, String... messageData) {
        this.clusterID = clusterID;
        this.messageType = PluginMessageType.valueOf(pluginMessageType.toUpperCase(Locale.ENGLISH));
        StringBuilder newMessageData = new StringBuilder();
        for (String s : messageData) {
            newMessageData.append(s).append(MESSAGE_DATA_SEPARATOR);
        }
        this.messageData = newMessageData.substring(0, newMessageData.toString().length()-1);
        this.targetPlayerName = targetPlayerName;
    }

    // Get the string version of the plugin message type
    private String getPluginMessageString(PluginMessageType type) {
        return type.name().toLowerCase(Locale.ENGLISH);
    }

    /**
     * Send the plugin message
     * @param sender The player to send the message
     */
    @SuppressWarnings("UnstableApiUsage")
    public void send(Player sender) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();

            // Send a plugin message to the specified player name
            out.writeUTF("ForwardToPlayer");
            out.writeUTF(targetPlayerName);

            // Send the HuskHomes message with a specific type
            out.writeUTF("HuskHomes:" + clusterID + ":" + getPluginMessageString(messageType));
            ByteArrayOutputStream messageBytes = new ByteArrayOutputStream();
            DataOutputStream messageOut = new DataOutputStream(messageBytes);

            // Send the message data; output an exception if there's an error
            try {
                messageOut.writeUTF(messageData);
            } catch (IOException e) {
                plugin.getLogger().warning("An error occurred trying to send a plugin message (" + e.getCause() + ")");
                e.printStackTrace();
            }

            // Write the messages to the output packet
            out.writeShort(messageBytes.toByteArray().length);
            out.write(messageBytes.toByteArray());

            // Send the constructed plugin message packet
            sender.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
        });
    }

    public void sendToServer(Player sender) {
        sendToServer(sender, "ALL");
    }

    @SuppressWarnings("UnstableApiUsage")
    public void sendToServer(Player sender, String server) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();

            // Send a plugin message to the specified player name
            out.writeUTF("Forward");
            out.writeUTF(server);

            // Send the HuskHomes message with a specific type
            out.writeUTF("HuskHomes:" + clusterID + ":" + getPluginMessageString(messageType));
            ByteArrayOutputStream messageBytes = new ByteArrayOutputStream();
            DataOutputStream messageOut = new DataOutputStream(messageBytes);

            // Send the message data; output an exception if there's an error
            try {
                messageOut.writeUTF(messageData);
            } catch (IOException e) {
                plugin.getLogger().warning("An error occurred trying to send a plugin message to a server (" + e.getCause() + ")");
                e.printStackTrace();
            }

            // Write the messages to the output packet
            out.writeShort(messageBytes.toByteArray().length);
            out.write(messageBytes.toByteArray());

            // Send the constructed plugin message packet
            sender.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
        });
    }

    public int getClusterID() {
        return clusterID;
    }

    public PluginMessageType getMessageType() {
        return messageType;
    }

    public String getTargetPlayerName() {
        return targetPlayerName;
    }

    public String getMessageData() {
        return messageData;
    }

    public String[] getMessageDataItems() {
        return messageData.split("\\" + MESSAGE_DATA_SEPARATOR);
    }
}
