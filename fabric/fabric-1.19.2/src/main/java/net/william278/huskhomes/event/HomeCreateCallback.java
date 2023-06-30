/*
 * This file is part of HuskHomes, licensed under the Apache License 2.0.
 *
 *  Copyright (c) William278 <will27528@gmail.com>
 *  Copyright (c) contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

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
