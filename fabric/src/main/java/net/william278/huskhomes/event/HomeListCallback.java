package net.william278.huskhomes.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.util.ActionResult;
import net.william278.huskhomes.position.Home;
import net.william278.huskhomes.user.CommandUser;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface HomeListCallback extends FabricEventCallback<IHomeListEvent> {

    @NotNull
    Event<HomeListCallback> EVENT = EventFactory.createArrayBacked(HomeListCallback.class,
            (listeners) -> (event) -> {
                for (HomeListCallback listener : listeners) {
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
    TriFunction<List<Home>, CommandUser, Boolean, IHomeListEvent> SUPPLIER = (homes, viewer, publicHomes) ->
            new IHomeListEvent() {
                private boolean cancelled = false;
                private List<Home> listHomes = homes;

                @Override
                @NotNull
                public List<Home> getHomes() {
                    return listHomes;
                }

                @Override
                public void setHomes(@NotNull List<Home> homes) {
                    this.listHomes = homes;
                }

                @Override
                @NotNull
                public CommandUser getListViewer() {
                    return viewer;
                }

                @Override
                public boolean getIsPublicHomeList() {
                    return publicHomes;
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
                public Event<HomeListCallback> getEvent() {
                    return EVENT;
                }

            };

}
