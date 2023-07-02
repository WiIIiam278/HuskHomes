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
import net.william278.huskhomes.teleport.Teleport;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public interface TeleportCallback extends FabricEventCallback<ITeleportEvent> {

    @NotNull
    Event<TeleportCallback> EVENT = EventFactory.createArrayBacked(TeleportCallback.class,
            (listeners) -> (event) -> {
                for (TeleportCallback listener : listeners) {
                    final ActionResult result = listener.invoke(event);
                    if (event.isCancelled()) {
                        return ActionResult.FAIL;
                    } else if (result == ActionResult.FAIL) {
                        event.setCancelled(true);
                        return result;
                    }
                }

                return ActionResult.PASS;
            });

    @NotNull
    Function<Teleport, ITeleportEvent> SUPPLIER = (teleport) ->
            new ITeleportEvent() {
                private boolean cancelled = false;

                @Override
                @NotNull
                public Teleport getTeleport() {
                    return teleport;
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
                public Event<TeleportCallback> getEvent() {
                    return EVENT;
                }

            };

}
