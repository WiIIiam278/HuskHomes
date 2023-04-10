package net.william278.huskhomes.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.util.ActionResult;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.user.CommandUser;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.NotNull;

public interface WarpCreateCallback extends FabricEventCallback<IWarpCreateEvent> {

    @NotNull
    Event<WarpCreateCallback> EVENT = EventFactory.createArrayBacked(WarpCreateCallback.class,
            (listeners) -> (event) -> {
                for (WarpCreateCallback listener : listeners) {
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
    TriFunction<String, Position, CommandUser, IWarpCreateEvent> SUPPLIER = (name, position, creator) ->
            new IWarpCreateEvent() {
                private boolean cancelled = false;
                private String homeName = name;

                @Override
                @NotNull
                public String getName() {
                    return homeName;
                }

                @Override
                public void setName(@NotNull String name) {
                    this.homeName = name;
                }

                @Override
                @NotNull
                public Position getPosition() {
                    return position;
                }

                @Override
                public void setPosition(@NotNull Position position) {
                }

                @Override
                @NotNull
                public CommandUser getCreator() {
                    return creator;
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
                public Event<WarpCreateCallback> getEvent() {
                    return EVENT;
                }

            };

}
