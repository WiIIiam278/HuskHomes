package net.william278.huskhomes.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.util.ActionResult;
import net.william278.huskhomes.user.CommandUser;
import net.william278.huskhomes.user.User;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;

public interface DeleteAllHomesCallback extends FabricEventCallback<IDeleteAllHomesEvent> {

    @NotNull
    Event<DeleteAllHomesCallback> EVENT = EventFactory.createArrayBacked(DeleteAllHomesCallback.class,
            (listeners) -> (event) -> {
                for (DeleteAllHomesCallback listener : listeners) {
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
    BiFunction<User, CommandUser, IDeleteAllHomesEvent> SUPPLIER = (owner, deleter) ->
            new IDeleteAllHomesEvent() {
                private boolean cancelled = false;

                @Override
                @NotNull
                public User getHomeOwner() {
                    return owner;
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
                public Event<DeleteAllHomesCallback> getEvent() {
                    return EVENT;
                }
            };

}
