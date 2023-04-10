package net.william278.huskhomes.util;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.network.PacketByteBuf;
import org.jetbrains.annotations.NotNull;

/*
 * Mixin work taken from Awakened Redstone's work on PAPIProxyBridge (Licensed Under Apache-2.0)
 */
@FunctionalInterface
public interface CustomPayloadCallback {

    Event<CustomPayloadCallback> EVENT = EventFactory.createArrayBacked(CustomPayloadCallback.class,
            (listeners) -> (channel, byteBuf) -> {
                for (CustomPayloadCallback listener : listeners) {
                    listener.invoke(channel, byteBuf);
                }
            });

    void invoke(@NotNull String channel, @NotNull PacketByteBuf byteBuf);

}