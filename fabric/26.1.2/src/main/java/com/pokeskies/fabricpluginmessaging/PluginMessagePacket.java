package com.pokeskies.fabricpluginmessaging;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.Arrays;

public final class PluginMessagePacket implements CustomPacketPayload {

    public static final Type<PluginMessagePacket> CHANNEL_ID = new Type<>(FabricPluginMessaging.BUNGEE_CHANNEL);
    public static final StreamCodec<FriendlyByteBuf, PluginMessagePacket> CODEC = CustomPacketPayload.codec(
            (value, buf) -> buf.writeByteArray(value.data),
            buf -> new PluginMessagePacket(buf.readByteArray())
    );

    private final byte[] data;

    public PluginMessagePacket(byte[] data) {
        this.data = data.clone();
    }

    public byte[] getData() {
        return data.clone();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return CHANNEL_ID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PluginMessagePacket that = (PluginMessagePacket) o;
        return Arrays.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(data);
    }

}
