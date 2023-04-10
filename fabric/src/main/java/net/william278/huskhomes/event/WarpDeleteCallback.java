package net.william278.huskhomes.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.util.ActionResult;
import net.william278.huskhomes.position.Warp;
import net.william278.huskhomes.user.CommandUser;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;

public interface WarpDeleteCallback extends FabricEventCallback<IWarpDeleteEvent> {

    @NotNull
    Event<WarpDeleteCallback> EVENT = EventFactory.createArrayBacked(WarpDeleteCallback.class,
            (listeners) -> (event) -> {
                for (WarpDeleteCallback listener : listeners) {
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
    BiFunction<Warp, CommandUser, IWarpDeleteEvent> SUPPLIER = (warp, deleter) ->
            new IWarpDeleteEvent() {
                private boolean cancelled = false;

                @Override
                @NotNull
                public Warp getWarp() {
                    return warp;
                }

                @Override
                @NotNull
                public CommandUser getDeleter() {
                    return deleter;
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
                public Event<WarpDeleteCallback> getEvent() {
                    return EVENT;
                }

            };

}
