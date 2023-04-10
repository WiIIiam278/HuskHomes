package net.william278.huskhomes.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.util.ActionResult;
import net.william278.huskhomes.teleport.TeleportRequest;
import net.william278.huskhomes.user.OnlineUser;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;

public interface ReceiveTeleportRequestCallback extends FabricEventCallback<IReceiveTeleportRequestEvent> {

    @NotNull
    Event<ReceiveTeleportRequestCallback> EVENT = EventFactory.createArrayBacked(ReceiveTeleportRequestCallback.class,
            (listeners) -> (event) -> {
                for (ReceiveTeleportRequestCallback listener : listeners) {
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
    BiFunction<OnlineUser, TeleportRequest, IReceiveTeleportRequestEvent> SUPPLIER = (recipient, request) ->
            new IReceiveTeleportRequestEvent() {
                private boolean cancelled = false;

                @Override
                @NotNull
                public OnlineUser getRecipient() {
                    return recipient;
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
                public Event<ReceiveTeleportRequestCallback> getEvent() {
                    return EVENT;
                }

            };

}
