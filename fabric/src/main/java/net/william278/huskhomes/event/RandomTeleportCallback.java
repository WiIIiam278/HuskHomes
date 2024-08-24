package net.william278.huskhomes.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.util.ActionResult;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.teleport.Teleport;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public interface RandomTeleportCallback extends FabricEventCallback<IRandomTeleportEvent> {
    @NotNull
    net.fabricmc.fabric.api.event.Event<RandomTeleportCallback> EVENT = EventFactory.createArrayBacked(RandomTeleportCallback.class,
            (listeners) -> (event) -> {
                for (RandomTeleportCallback listener : listeners) {
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
    Function<Teleport, IRandomTeleportEvent> SUPPLIER = (teleport) ->
            new IRandomTeleportEvent() {
                private boolean cancelled = false;

                @Override
                @NotNull
                public Position getPosition() {
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

                public @NotNull Event<RandomTeleportCallback> getEvent() {
                    return EVENT;
                }

            };
}
