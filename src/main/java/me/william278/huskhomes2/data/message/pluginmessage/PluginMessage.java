package me.william278.huskhomes2.data.message.pluginmessage;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.william278.huskhomes2.HuskHomes;
import me.william278.huskhomes2.data.message.Message;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PluginMessage extends Message {

    private static final HuskHomes plugin = HuskHomes.getInstance();

    public PluginMessage(String targetPlayerName, MessageType pluginMessageType, String... messageData) {
        super(targetPlayerName, pluginMessageType, messageData);
    }

    public PluginMessage(MessageType pluginMessageType, String... messageData) {
        super(pluginMessageType, messageData);
    }

    public PluginMessage(int clusterID, String targetPlayerName, String pluginMessageType, String... messageData) {
        super(clusterID, targetPlayerName, pluginMessageType, messageData);
    }

    @SuppressWarnings("UnstableApiUsage")
    public void send(Player sender) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();

            // Send a plugin message to the specified player name
            out.writeUTF("ForwardToPlayer");
            out.writeUTF(getTargetPlayerName());

            // Send the HuskHomes message with a specific type
            out.writeUTF("HuskHomes:" + getClusterId() + ":" + getPluginMessageString(getMessageType()));
            ByteArrayOutputStream messageBytes = new ByteArrayOutputStream();
            DataOutputStream messageOut = new DataOutputStream(messageBytes);

            // Send the message data; output an exception if there's an error
            try {
                messageOut.writeUTF(getMessageData());
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

    public void sendToAllServers(Player sender) {
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
            out.writeUTF("HuskHomes:" + getClusterId() + ":" + getPluginMessageString(getMessageType()));
            ByteArrayOutputStream messageBytes = new ByteArrayOutputStream();
            DataOutputStream messageOut = new DataOutputStream(messageBytes);

            // Send the message data; output an exception if there's an error
            try {
                messageOut.writeUTF(getMessageData());
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
}
