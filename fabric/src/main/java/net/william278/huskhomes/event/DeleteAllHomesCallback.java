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
