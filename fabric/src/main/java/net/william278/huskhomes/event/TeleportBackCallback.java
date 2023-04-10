package net.william278.huskhomes.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.util.ActionResult;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.teleport.Teleport;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public interface TeleportBackCallback extends FabricEventCallback<ITeleportBackEvent> {

    @NotNull
    Event<TeleportBackCallback> EVENT = EventFactory.createArrayBacked(TeleportBackCallback.class,
            (listeners) -> (event) -> {
                for (TeleportBackCallback listener : listeners) {
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
    Function<Teleport, ITeleportBackEvent> SUPPLIER = (teleport) ->
            new ITeleportBackEvent() {
                private boolean cancelled = false;

                @Override
                @NotNull
                public Position getLastPosition() {
                    return (Position) getTeleport().getTarget();
                }

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
                public Event<TeleportBackCallback> getEvent() {
                    return EVENT;
                }

            };

}
