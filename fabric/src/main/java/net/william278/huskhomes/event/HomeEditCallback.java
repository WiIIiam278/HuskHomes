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
import net.william278.huskhomes.position.Home;
import net.william278.huskhomes.user.CommandUser;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.NotNull;

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
    TriFunction<Home, Home, CommandUser, IHomeEditEvent> SUPPLIER = (home, original, editor) ->
            new IHomeEditEvent() {
                private boolean cancelled = false;

                @Override
                @NotNull
                public Home getHome() {
                    return home;
                }

                @NotNull
                @Override
                public Home getOriginalHome() {
                    return original;
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
