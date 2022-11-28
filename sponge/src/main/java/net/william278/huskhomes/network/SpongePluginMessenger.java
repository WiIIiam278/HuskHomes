package net.william278.huskhomes.network;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.player.OnlineUser;
import net.william278.huskhomes.player.SpongePlayer;
import net.william278.huskhomes.position.Server;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.network.EngineConnection;
import org.spongepowered.api.network.channel.ChannelBuf;
import org.spongepowered.api.network.channel.raw.RawDataChannel;
import org.spongepowered.api.network.channel.raw.play.RawPlayDataChannel;
import org.spongepowered.api.network.channel.raw.play.RawPlayDataHandler;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class SpongePluginMessenger extends NetworkMessenger implements RawPlayDataHandler<EngineConnection> {

    private static final ResourceKey PLUGIN_MESSAGE_CHANNEL_KEY = ResourceKey.of("bungeecord", "main");
    private RawPlayDataChannel channel;

    @Override
    public void initialize(@NotNull HuskHomes implementor) {
        super.initialize(implementor);

        // Register inbound channel
        this.channel = Sponge.channelManager().ofType(PLUGIN_MESSAGE_CHANNEL_KEY, RawDataChannel.class).play();
        this.channel.addHandler(this);
    }

    @Override
    public CompletableFuture<String[]> getOnlinePlayerNames(@NotNull OnlineUser requester) {
        final CompletableFuture<String[]> future = new CompletableFuture<>();
        final SpongePlayer dispatcher = ((SpongePlayer) requester);
        onlinePlayerNamesRequests.add(future);
        this.channel.sendTo(dispatcher.getPlayer(), channelBuf -> {
            channelBuf.writeUTF("PlayerList");
            channelBuf.writeUTF("ALL");
        });
        return future;
    }

    @Override
    public CompletableFuture<String> fetchServerName(@NotNull OnlineUser requester) {
        final CompletableFuture<String> future = new CompletableFuture<>();
        final SpongePlayer dispatcher = ((SpongePlayer) requester);
        serverNameRequests.add(future);
        this.channel.sendTo(dispatcher.getPlayer(), channelBuf -> channelBuf.writeUTF("GetServer"));
        return future;
    }

    @Override
    public CompletableFuture<String[]> fetchOnlineServerList(@NotNull OnlineUser requester) {
        final CompletableFuture<String[]> future = new CompletableFuture<>();
        final SpongePlayer dispatcher = ((SpongePlayer) requester);
        onlineServersRequests.add(future);
        this.channel.sendTo(dispatcher.getPlayer(), channelBuf -> channelBuf.writeUTF("GetServers"));
        return future;
    }

    @Override
    public CompletableFuture<Boolean> sendPlayer(@NotNull OnlineUser onlineUser, @NotNull Server server) {
        final SpongePlayer dispatcher = ((SpongePlayer) onlineUser);
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
            this.channel.sendTo(dispatcher.getPlayer(), channelBuf -> {
                channelBuf.writeUTF("Connect");
                channelBuf.writeUTF(server.name);
            });
            return true;
        });
    }

    /**
     * Dispatch a plugin message, forwarding it via the player
     *
     * @param sender  The player to send the message with
     * @param message The message to send
     */
    private void sendPluginMessage(@NotNull SpongePlayer sender, @NotNull Message message) {
        this.channel.sendTo(sender.getPlayer(), channelBuf -> {
            channelBuf.writeUTF("ForwardToPlayer");
            channelBuf.writeUTF(message.targetPlayer);
            channelBuf.writeUTF(NETWORK_MESSAGE_CHANNEL);

            // Write the plugin message
            try (final ByteArrayOutputStream messageByteOutputStream = new ByteArrayOutputStream()) {
                try (DataOutputStream messageDataOutputStream = new DataOutputStream(messageByteOutputStream)) {
                    messageDataOutputStream.writeUTF(message.toJson());
                    channelBuf.writeShort((short) messageByteOutputStream.toByteArray().length);
                    channelBuf.writeByteArray(messageByteOutputStream.toByteArray());
                }
            } catch (IOException e) {
                plugin.getLoggingAdapter().log(Level.SEVERE, "Exception dispatching plugin message", e);
            }
        });
    }

    @Override
    public CompletableFuture<Message> dispatchMessage(@NotNull OnlineUser sender, @NotNull Message message) {
        final CompletableFuture<Message> repliedMessage = new CompletableFuture<>();
        processingMessages.put(message.uuid, repliedMessage);
        sendPluginMessage((SpongePlayer) sender, message);
        return repliedMessage;
    }

    @Override
    protected void sendReply(@NotNull OnlineUser replier, @NotNull Message reply) {
        sendPluginMessage((SpongePlayer) replier, reply);
    }

    @Override
    public void terminate() {
        channel.removeHandler(this);
    }

    @Override
    @SuppressWarnings("ForLoopReplaceableByForEach")
    public void handlePayload(@NotNull ChannelBuf pluginMessage, @NotNull EngineConnection connection) {
        final Optional<OnlineUser> playerConnection = plugin.getOnlinePlayers().stream()
                .filter(onlineUser -> ((SpongePlayer) onlineUser).getPlayer().connection().equals(connection))
                .findFirst();

        // Player is not online
        if (playerConnection.isEmpty()) {
            throw new IllegalStateException("Recipient connection not found");
        }
        final SpongePlayer player = (SpongePlayer) playerConnection.get();

        final String subChannel = pluginMessage.readUTF();
        switch (subChannel) {
            case NETWORK_MESSAGE_CHANNEL -> {
                final short messageLength = pluginMessage.readShort();
                final byte[] messageBody = pluginMessage.readByteArray(messageLength);

                // Read the message in byte-by-byte and parse it
                try (DataInputStream messageReader = new DataInputStream(new ByteArrayInputStream(messageBody))) {
                    final Message message = Message.fromJson(messageReader.readUTF());

                    // Ignore plugin messages to other clusters
                    if (!message.clusterId.equals(clusterId)) {
                        return;
                    }

                    handleMessage(player, message);
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
    private void handleArrayPluginMessage(@NotNull ChannelBuf pluginMessage,
                                          @NotNull List<CompletableFuture<String[]>> requests) {
        final String[] fetchedData = pluginMessage.readUTF().split(", ");
        for (int i = 0; i < requests.size(); i++) {
            final CompletableFuture<String[]> request = requests.get(i);
            request.complete(fetchedData);
        }
        requests.clear();
    }
}
