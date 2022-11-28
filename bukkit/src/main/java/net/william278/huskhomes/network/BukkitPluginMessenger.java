package net.william278.huskhomes.network;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.william278.huskhomes.BukkitHuskHomes;
import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.player.BukkitPlayer;
import net.william278.huskhomes.player.OnlineUser;
import net.william278.huskhomes.position.Server;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

/**
 * <a href="https://www.spigotmc.org/wiki/bukkit-bungee-plugin-messaging-channel/">Plugin Messaging channel</a> messenger implementation
 */
public class BukkitPluginMessenger extends NetworkMessenger implements PluginMessageListener {

    /**
     * The name of BungeeCord's provided plugin channel.
     *
     * @implNote Technically, the effective identifier of this channel is {@code bungeecord:main},  but Spigot remaps
     * {@code BungeeCord} automatically to the new one (<a href="https://wiki.vg/Plugin_channels#bungeecord:main">source</a>).
     * Spigot's <a href="https://www.spigotmc.org/wiki/bukkit-bungee-plugin-messaging-channel/">official documentation</a>
     * still instructs usage of {@code BungeeCord} as the name to use, however. It's all a bit inconsistent, so just in case
     * it's best to leave it how it is for to maintain backwards compatibility.
     */
    private static final String BUNGEE_PLUGIN_CHANNEL_NAME = "BungeeCord";

    @Override
    public void initialize(@NotNull HuskHomes implementor) {
        super.initialize(implementor);

        // Register HuskHomes messaging channel
        final JavaPlugin bukkitPlugin = (BukkitHuskHomes) implementor;
        Bukkit.getMessenger().registerIncomingPluginChannel(bukkitPlugin, BUNGEE_PLUGIN_CHANNEL_NAME, this);
        Bukkit.getMessenger().registerOutgoingPluginChannel(bukkitPlugin, BUNGEE_PLUGIN_CHANNEL_NAME);
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public CompletableFuture<String[]> getOnlinePlayerNames(@NotNull OnlineUser requester) {
        final CompletableFuture<String[]> future = new CompletableFuture<>();
        final BukkitPlayer dispatcher = ((BukkitPlayer) requester);
        onlinePlayerNamesRequests.add(future);
        final ByteArrayDataOutput messageWriter = ByteStreams.newDataOutput();
        messageWriter.writeUTF("PlayerList");
        messageWriter.writeUTF("ALL");
        dispatcher.sendPluginMessage(BUNGEE_PLUGIN_CHANNEL_NAME, messageWriter.toByteArray());
        return future;
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public CompletableFuture<String> fetchServerName(@NotNull OnlineUser requester) {
        final CompletableFuture<String> future = new CompletableFuture<>();
        final BukkitPlayer dispatcher = ((BukkitPlayer) requester);
        serverNameRequests.add(future);
        final ByteArrayDataOutput messageWriter = ByteStreams.newDataOutput();
        messageWriter.writeUTF("GetServer");
        dispatcher.sendPluginMessage(BUNGEE_PLUGIN_CHANNEL_NAME, messageWriter.toByteArray());
        return future;
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public CompletableFuture<String[]> fetchOnlineServerList(@NotNull OnlineUser requester) {
        final CompletableFuture<String[]> future = new CompletableFuture<>();
        final BukkitPlayer dispatcher = ((BukkitPlayer) requester);
        onlineServersRequests.add(future);
        final ByteArrayDataOutput messageWriter = ByteStreams.newDataOutput();
        messageWriter.writeUTF("GetServers");
        dispatcher.sendPluginMessage(BUNGEE_PLUGIN_CHANNEL_NAME, messageWriter.toByteArray());
        return future;
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public CompletableFuture<Boolean> sendPlayer(@NotNull OnlineUser onlineUser, @NotNull Server server) {
        final BukkitPlayer dispatcher = ((BukkitPlayer) onlineUser);
        return fetchOnlineServerList(onlineUser).thenApply(onlineServers -> {
            // Ensure the server is online
            final Optional<String> targetServer = Arrays.stream(onlineServers)
                    .filter(serverName -> serverName.equals(server.name))
                    .findFirst();
            if (targetServer.isEmpty()) {
                plugin.getLoggingAdapter().log(Level.WARNING,
                        "Failed to send " + dispatcher.username + " to " + server.name + "; server offline?");
                return false;
            }

            // Send player to target server
            final ByteArrayDataOutput messageWriter = ByteStreams.newDataOutput();
            messageWriter.writeUTF("Connect");
            messageWriter.writeUTF(server.name);
            dispatcher.sendPluginMessage(BUNGEE_PLUGIN_CHANNEL_NAME, messageWriter.toByteArray());
            return true;
        });
    }

    @Override
    public CompletableFuture<Message> dispatchMessage(@NotNull OnlineUser sender, @NotNull Message message) {
        final CompletableFuture<Message> repliedMessage = new CompletableFuture<>();
        processingMessages.put(message.uuid, repliedMessage);
        sendPluginMessage((BukkitPlayer) sender, message);
        return repliedMessage;
    }

    @Override
    protected void sendReply(@NotNull OnlineUser replier, @NotNull Message reply) {
        sendPluginMessage((BukkitPlayer) replier, reply);
    }

    @SuppressWarnings("UnstableApiUsage")
    private void sendPluginMessage(@NotNull BukkitPlayer player, @NotNull Message message) {
        final BukkitHuskHomes plugin = (BukkitHuskHomes) this.plugin;
        final ByteArrayDataOutput messageWriter = ByteStreams.newDataOutput();
        messageWriter.writeUTF("ForwardToPlayer");
        messageWriter.writeUTF(message.targetPlayer);
        messageWriter.writeUTF(NETWORK_MESSAGE_CHANNEL);

        // Write the plugin message
        try (final ByteArrayOutputStream messageByteOutputStream = new ByteArrayOutputStream()) {
            try (DataOutputStream messageDataOutputStream = new DataOutputStream(messageByteOutputStream)) {
                messageDataOutputStream.writeUTF(message.toJson());
                messageWriter.writeShort(messageByteOutputStream.toByteArray().length);
                messageWriter.write(messageByteOutputStream.toByteArray());
            }
        } catch (IOException e) {
            plugin.getLoggingAdapter().log(Level.SEVERE, "Exception dispatching plugin message", e);
            return;
        }

        // Send the written message
        player.sendPluginMessage(BUNGEE_PLUGIN_CHANNEL_NAME, messageWriter.toByteArray());
    }

    @Override
    public void terminate() {
        Bukkit.getMessenger().unregisterIncomingPluginChannel((BukkitHuskHomes) plugin);
        Bukkit.getMessenger().unregisterIncomingPluginChannel((BukkitHuskHomes) plugin);
    }

    @Override
    @SuppressWarnings({"UnstableApiUsage", "ForLoopReplaceableByForEach"})
    public void onPluginMessageReceived(@NotNull String channel, @NotNull org.bukkit.entity.Player player,
                                        final byte[] messageBytes) {
        if (!channel.equals(BUNGEE_PLUGIN_CHANNEL_NAME)) {
            return;
        }
        final ByteArrayDataInput pluginMessage = ByteStreams.newDataInput(messageBytes);
        final String subChannel = pluginMessage.readUTF();
        switch (subChannel) {
            case NETWORK_MESSAGE_CHANNEL -> {
                final short messageLength = pluginMessage.readShort();
                final byte[] messageBody = new byte[messageLength];
                pluginMessage.readFully(messageBody);

                // Read the message in byte-by-byte and parse it
                try (DataInputStream messageReader = new DataInputStream(new ByteArrayInputStream(messageBody))) {
                    final Message message = Message.fromJson(messageReader.readUTF());

                    // Ignore plugin messages to other clusters
                    if (!message.clusterId.equals(clusterId)) {
                        return;
                    }

                    handleMessage(BukkitPlayer.adapt(player), message);
                } catch (IOException e) {
                    plugin.getLoggingAdapter().log(Level.SEVERE,
                            "Failed to read an inbound plugin message", e);
                }
            }
            case "GetServer" -> {
                final String serverName = pluginMessage.readUTF();
                for (int i = 0; i < serverNameRequests.size(); i++) {
                    final CompletableFuture<String> future = serverNameRequests.get(i);
                    future.complete(serverName);
                }
                serverNameRequests.clear();
            }
            case "PlayerList" -> {
                pluginMessage.readUTF(); // Read the server name (unused)
                handleArrayPluginMessage(pluginMessage, onlinePlayerNamesRequests);
            }
            case "GetServers" -> handleArrayPluginMessage(pluginMessage, onlineServersRequests);
        }
    }

    @SuppressWarnings("ForLoopReplaceableByForEach")
    private void handleArrayPluginMessage(@NotNull ByteArrayDataInput pluginMessage,
                                          @NotNull List<CompletableFuture<String[]>> requests) {
        final String[] fetchedData = pluginMessage.readUTF().split(", ");
        for (int i = 0; i < requests.size(); i++) {
            final CompletableFuture<String[]> request = requests.get(i);
            request.complete(fetchedData);
        }
        requests.clear();
    }

}
