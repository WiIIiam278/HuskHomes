package me.william278.huskhomes2.data.message.pluginmessage;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import me.william278.huskhomes2.HuskHomes;
import me.william278.huskhomes2.data.message.CrossServerMessageHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class PluginMessageReceiver implements PluginMessageListener {

    @Override @SuppressWarnings("UnstableApiUsage")
    public void onPluginMessageReceived(String channel, @NotNull Player player, byte[] message) {
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
        if (HuskHomes.getSettings().getClusterId() != clusterID) {
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
        CrossServerMessageHandler.handlePluginMessage(new PluginMessage(clusterID, player.getName(), messageType.split(":")[2], messageData), player);
    }

}
