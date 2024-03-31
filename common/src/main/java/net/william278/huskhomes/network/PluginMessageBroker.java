/*
 * This file is part of HuskHomes, licensed under the Apache License 2.0.
 *
 *  Copyright (c) William278 <will27528@gmail.com>
 *  Copyright (c) contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.william278.huskhomes.network;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.user.OnlineUser;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.logging.Level;

/**
 * <a href="https://www.spigotmc.org/wiki/bukkit-bungee-plugin-messaging-channel/">Plugin Messaging channel</a> message
 * broker implementation.
 */
public class PluginMessageBroker extends Broker {

    /**
     * The name of BungeeCord's provided plugin channel.
     *
     * <p>Internally, this is <a href="https://wiki.vg/Plugin_channels#bungeecord:main">{@code bungeecord:main}</a>,
     * but Spigot remaps {@code BungeeCord} automatically to the new one (hence BungeeCord is kept for back-compat).
     */
    public static final String BUNGEE_CHANNEL_ID = "BungeeCord";

    public PluginMessageBroker(@NotNull HuskHomes plugin) {
        super(plugin);
    }

    @Override
    public void initialize() throws IllegalStateException {
        plugin.initializePluginChannels();
    }

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
    protected void send(@NotNull Message message, @NotNull OnlineUser sender) {
        final ByteArrayDataOutput messageWriter = ByteStreams.newDataOutput();
        messageWriter.writeUTF(message.getScope().getPluginMessageChannel());
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
    public void changeServer(@NotNull OnlineUser user, @NotNull String server) {
        user.dismount().thenRun(() -> {
            final ByteArrayDataOutput outputStream = ByteStreams.newDataOutput();

            outputStream.writeUTF("Connect");
            outputStream.writeUTF(server);

            user.sendPluginMessage(BUNGEE_CHANNEL_ID, outputStream.toByteArray());
        });
    }

    @Override
    public void close() {
    }

}
