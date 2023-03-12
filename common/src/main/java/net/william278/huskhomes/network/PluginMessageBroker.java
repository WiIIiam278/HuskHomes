package net.william278.huskhomes.network;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.user.OnlineUser;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * <a href="https://www.spigotmc.org/wiki/bukkit-bungee-plugin-messaging-channel/">Plugin Messaging channel</a> message
 * broker implementation
 */
public class PluginMessageBroker extends Broker {

    /**
     * The name of BungeeCord's provided plugin channel.
     *
     * @implNote Technically, the effective identifier of this channel is {@code bungeecord:main},  but Spigot remaps
     * {@code BungeeCord} automatically to the new one (<a href="https://wiki.vg/Plugin_channels#bungeecord:main">source</a>).
     * Spigot's <a href="https://www.spigotmc.org/wiki/bukkit-bungee-plugin-messaging-channel/">official documentation</a>
     * still instructs usage of {@code BungeeCord} as the name to use, however. It's all a bit inconsistent, so just in case
     * it's best to leave it how it is for to maintain backwards compatibility.
     */
    public static final String BUNGEE_CHANNEL_ID = "BungeeCord";

    public PluginMessageBroker(@NotNull HuskHomes plugin) {
        super(plugin);
    }

    @Override
    public void initialize() throws RuntimeException {
        plugin.initializePluginChannels();
    }

    @SuppressWarnings("UnstableApiUsage")
    public final void onReceive(@NotNull String channel, @NotNull OnlineUser user, byte[] message) {
        if (!channel.equals(BUNGEE_CHANNEL_ID)) {
            return;
        }

        final ByteArrayDataInput inputStream = ByteStreams.newDataInput(message);
        final String subChannelId = inputStream.readUTF();
        if (!subChannelId.equals(getSubChannelId())) {
            return;
        }

        short messageLength = inputStream.readShort();
        byte[] messageBody = new byte[messageLength];
        inputStream.readFully(messageBody);

        try (final DataInputStream messageReader = new DataInputStream(new ByteArrayInputStream(messageBody))) {
            super.handle(user, plugin.getGson().fromJson(messageReader.readUTF(), Message.class));
        } catch (IOException e) {
            plugin.log(Level.SEVERE, "Failed to fully read plugin message", e);
        }
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    protected void send(@NotNull Message message, @NotNull OnlineUser sender) {
        final ByteArrayDataOutput messageWriter = ByteStreams.newDataOutput();
        messageWriter.writeUTF("ForwardToPlayer");
        messageWriter.writeUTF(message.getTarget());
        messageWriter.writeUTF(getSubChannelId());

        // Write the plugin message
        try (final ByteArrayOutputStream messageByteStream = new ByteArrayOutputStream()) {
            try (DataOutputStream messageDataStream = new DataOutputStream(messageByteStream)) {
                messageDataStream.writeUTF(plugin.getGson().toJson(message));
                messageWriter.writeShort(messageByteStream.toByteArray().length);
                messageWriter.write(messageByteStream.toByteArray());
            }
        } catch (IOException e) {
            plugin.log(Level.SEVERE, "Exception dispatching plugin message", e);
            return;
        }

        sender.sendPluginMessage(BUNGEE_CHANNEL_ID, messageWriter.toByteArray());
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public void changeServer(@NotNull OnlineUser user, @NotNull String server) {
        final ByteArrayDataOutput outputStream = ByteStreams.newDataOutput();

        outputStream.writeUTF("Connect");
        outputStream.writeUTF(server);

        user.sendPluginMessage(BUNGEE_CHANNEL_ID, outputStream.toByteArray());
    }

    @Override
    public void close() {
    }
}