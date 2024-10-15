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
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.teleport.Teleport;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public interface RandomTeleportCallback extends FabricEventCallback<IRandomTeleportEvent> {
    @NotNull
    Event<RandomTeleportCallback> EVENT = EventFactory.createArrayBacked(RandomTeleportCallback.class,
            (listeners) -> (event) -> {
                for (RandomTeleportCallback listener : listeners) {
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
    Function<Teleport, IRandomTeleportEvent> SUPPLIER = (teleport) ->
            new IRandomTeleportEvent() {
                private boolean cancelled = false;

                @Override
                @NotNull
                public Position getPosition() {
                    return (Position) getTeleport().getTarget();
                }

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

                public @NotNull Event<RandomTeleportCallback> getEvent() {
                    return EVENT;
                }

            };
}
