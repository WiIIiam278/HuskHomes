package net.william278.huskhomes.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.util.ActionResult;
import net.william278.huskhomes.position.Home;
import net.william278.huskhomes.user.CommandUser;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;

public interface HomeEditCallback extends FabricEventCallback<IHomeEditEvent> {

    @NotNull
    Event<HomeEditCallback> EVENT = EventFactory.createArrayBacked(HomeEditCallback.class,
            (listeners) -> (event) -> {
                for (HomeEditCallback listener : listeners) {
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
    BiFunction<Home, CommandUser, IHomeEditEvent> SUPPLIER = (home, editor) ->
            new IHomeEditEvent() {
                private boolean cancelled = false;

                @Override
                @NotNull
                public Home getHome() {
                    return home;
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
                public Event<HomeEditCallback> getEvent() {
                    return EVENT;
                }

            };

}
