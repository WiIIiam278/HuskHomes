package com.pokeskies.fabricpluginmessaging;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public interface PluginMessageEvent {

    Event<PluginMessageEvent> EVENT = EventFactory.createArrayBacked(PluginMessageEvent.class,
            listeners -> (payload, context) -> {
                for (PluginMessageEvent listener : listeners) {
                    listener.onReceive(payload, context);
                }
            });

    void onReceive(PluginMessagePacket payload, ServerPlayNetworking.Context context);

}
