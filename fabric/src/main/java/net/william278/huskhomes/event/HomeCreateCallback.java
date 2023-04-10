package net.william278.huskhomes.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.data.client.BlockStateVariantMap.QuadFunction;
import net.minecraft.util.ActionResult;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.user.CommandUser;
import net.william278.huskhomes.user.User;
import org.jetbrains.annotations.NotNull;

public interface HomeCreateCallback extends FabricEventCallback<IHomeCreateEvent> {

    @NotNull
    Event<HomeCreateCallback> EVENT = EventFactory.createArrayBacked(HomeCreateCallback.class,
            (listeners) -> (event) -> {
                for (HomeCreateCallback listener : listeners) {
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
    QuadFunction<User, String, Position, CommandUser, IHomeCreateEvent> SUPPLIER = (user, name, position, creator) ->
            new IHomeCreateEvent() {
                private boolean cancelled = false;
                private String homeName = name;

                @Override
                @NotNull
                public User getOwner() {
                    return user;
                }

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
                public @NotNull CommandUser getCreator() {
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
                public Event<HomeCreateCallback> getEvent() {
                    return EVENT;
                }
            };

}
