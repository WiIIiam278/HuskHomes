package net.william278.huskhomes.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.util.ActionResult;
import net.william278.huskhomes.teleport.TimedTeleport;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;

public interface TeleportWarmupCallback extends FabricEventCallback<ITeleportWarmupEvent> {
    @NotNull
    Event<TeleportWarmupCallback> EVENT = EventFactory.createArrayBacked(TeleportWarmupCallback.class,
            (listeners) -> (event) -> {
                for (TeleportWarmupCallback listener : listeners) {
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
    BiFunction<TimedTeleport, Integer, ITeleportWarmupEvent> SUPPLIER = (timedTeleport, duration) ->
            new ITeleportWarmupEvent() {
                private boolean cancelled = false;

                @Override
                @NotNull
                public TimedTeleport getTimedTeleport() {
                    return timedTeleport;
                }

                @Override
                public int getWarmupDuration() {
                    return duration;
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
                public Event<TeleportWarmupCallback> getEvent() {
                    return EVENT;
                }

            };
}
