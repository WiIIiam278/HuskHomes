package net.william278.huskhomes.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.util.ActionResult;
import net.william278.huskhomes.position.Warp;
import net.william278.huskhomes.user.CommandUser;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;

public interface WarpEditCallback extends FabricEventCallback<IWarpEditEvent> {

    @NotNull
    Event<WarpEditCallback> EVENT = EventFactory.createArrayBacked(WarpEditCallback.class,
            (listeners) -> (event) -> {
                for (WarpEditCallback listener : listeners) {
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
    BiFunction<Warp, CommandUser, IWarpEditEvent> SUPPLIER = (warp, editor) ->
            new IWarpEditEvent() {
                private boolean cancelled = false;

                @Override
                @NotNull
                public Warp getWarp() {
                    return warp;
                }

                @Override
                @NotNull
                public CommandUser getEditor() {
                    return editor;
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
                public Event<WarpEditCallback> getEvent() {
                    return EVENT;
                }

            };

}
