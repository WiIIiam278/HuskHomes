package com.pokeskies.fabricpluginmessaging;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.Identifier;

public final class FabricPluginMessaging implements ServerPlayNetworking.PlayPayloadHandler<PluginMessagePacket> {

    public static final FabricPluginMessaging INSTANCE = new FabricPluginMessaging();
    public static final Identifier BUNGEE_CHANNEL = Identifier.fromNamespaceAndPath("bungeecord", "main");

    private FabricPluginMessaging() {
    }

    public static void initialize() {
        PayloadTypeRegistry.clientboundPlay().register(PluginMessagePacket.CHANNEL_ID, PluginMessagePacket.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(PluginMessagePacket.CHANNEL_ID, PluginMessagePacket.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(PluginMessagePacket.CHANNEL_ID, INSTANCE);
    }

    @Override
    public void receive(PluginMessagePacket payload, ServerPlayNetworking.Context context) {
        PluginMessageEvent.EVENT.invoker().onReceive(payload, context);
    }

}
