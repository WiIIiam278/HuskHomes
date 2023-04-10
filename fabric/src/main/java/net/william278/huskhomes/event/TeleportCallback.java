package net.william278.huskhomes.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.util.ActionResult;
import net.william278.huskhomes.teleport.Teleport;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public interface TeleportCallback extends FabricEventCallback<ITeleportEvent> {

    @NotNull
    Event<TeleportCallback> EVENT = EventFactory.createArrayBacked(TeleportCallback.class,
            (listeners) -> (event) -> {
                for (TeleportCallback listener : listeners) {
                    final ActionResult result = listener.invoke(event);
                    if (event.isCancelled()) {
                        return ActionResult.FAIL;
                    } else if (result == ActionResult.FAIL) {
                        event.setCancelled(true);
                        return result;
                    }
                }

                return ActionResult.PASS;
            });

    @NotNull
    Function<Teleport, ITeleportEvent> SUPPLIER = (teleport) ->
            new ITeleportEvent() {
                private boolean cancelled = false;

                @Override
                @NotNull
                public Teleport getTeleport() {
                    return teleport;
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
                public Event<TeleportCallback> getEvent() {
                    return EVENT;
                }

            };

}
