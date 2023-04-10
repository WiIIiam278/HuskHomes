package net.william278.huskhomes.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.util.ActionResult;
import net.william278.huskhomes.position.Warp;
import net.william278.huskhomes.user.CommandUser;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.BiFunction;

public interface WarpListCallback extends FabricEventCallback<IWarpListEvent> {

    @NotNull
    Event<WarpListCallback> EVENT = EventFactory.createArrayBacked(WarpListCallback.class,
            (listeners) -> (event) -> {
                for (WarpListCallback listener : listeners) {
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
    BiFunction<List<Warp>, CommandUser, IWarpListEvent> SUPPLIER = (warps, viewer) ->
            new IWarpListEvent() {
                private boolean cancelled = false;
                private List<Warp> listWarps = warps;

                @Override
                @NotNull
                public List<Warp> getWarps() {
                    return listWarps;
                }

                @Override
                public void setWarps(@NotNull List<Warp> homes) {
                    this.listWarps = homes;
                }

                @Override
                @NotNull
                public CommandUser getListViewer() {
                    return viewer;
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
                public Event<WarpListCallback> getEvent() {
                    return EVENT;
                }

            };

}
