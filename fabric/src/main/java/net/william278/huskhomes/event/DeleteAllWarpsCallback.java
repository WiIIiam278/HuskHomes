package net.william278.huskhomes.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.util.ActionResult;
import net.william278.huskhomes.user.CommandUser;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public interface DeleteAllWarpsCallback extends FabricEventCallback<IDeleteAllWarpsEvent> {

    @NotNull
    Event<DeleteAllWarpsCallback> EVENT = EventFactory.createArrayBacked(DeleteAllWarpsCallback.class,
            (listeners) -> (event) -> {
                for (DeleteAllWarpsCallback listener : listeners) {
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
    Function<CommandUser, IDeleteAllWarpsEvent> SUPPLIER = (deleter) ->
            new IDeleteAllWarpsEvent() {
                private boolean cancelled = false;

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
                public Event<DeleteAllWarpsCallback> getEvent() {
                    return EVENT;
                }
            };

}
