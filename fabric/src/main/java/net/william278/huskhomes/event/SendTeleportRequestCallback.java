package net.william278.huskhomes.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.util.ActionResult;
import net.william278.huskhomes.teleport.TeleportRequest;
import net.william278.huskhomes.user.OnlineUser;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;

public interface SendTeleportRequestCallback extends FabricEventCallback<ISendTeleportRequestEvent> {

    @NotNull
    Event<SendTeleportRequestCallback> EVENT = EventFactory.createArrayBacked(SendTeleportRequestCallback.class,
            (listeners) -> (event) -> {
                for (SendTeleportRequestCallback listener : listeners) {
                    final ActionResult result = listener.invoke(event);
                    if (event.isCancelled()) {
                        return ActionResult.CONSUME;
                    } else if (result != ActionResult.PASS) {
                        event.setCancelled(true);
                        return result;
                    }
                }

                return ActionResult.PASS;
            });

    @NotNull
    BiFunction<OnlineUser, TeleportRequest, ISendTeleportRequestEvent> SUPPLIER = (sender, request) ->
            new ISendTeleportRequestEvent() {
                private boolean cancelled = false;

                @Override
                @NotNull
                public OnlineUser getSender() {
                    return sender;
                }

                @Override
                @NotNull
                public TeleportRequest getRequest() {
                    return request;
                }

                @Override
                public void setCancelled(boolean cancelled) {
                    this.cancelled = cancelled;
                }

                @Override
                public boolean isCancelled() {
                    return cancelled;
                }

                @NotNull
                public Event<SendTeleportRequestCallback> getEvent() {
                    return EVENT;
                }

            };

}
