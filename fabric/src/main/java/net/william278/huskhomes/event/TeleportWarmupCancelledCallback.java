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
import net.william278.huskhomes.teleport.TimedTeleport;
import net.william278.huskhomes.util.QuadFunction;
import org.jetbrains.annotations.NotNull;

import static net.william278.huskhomes.event.ITeleportWarmupCancelledEvent.CancelReason;


public interface TeleportWarmupCancelledCallback extends FabricEventCallback<ITeleportWarmupCancelledEvent> {
    @NotNull
    Event<TeleportWarmupCancelledCallback> EVENT = EventFactory.createArrayBacked(TeleportWarmupCancelledCallback.class,
            (listeners) -> (event) -> {
                for (TeleportWarmupCancelledCallback listener : listeners) {
                    listener.invoke(event);
                }

                return ActionResult.PASS;
            });

    @NotNull
    QuadFunction<TimedTeleport, Integer, Integer, CancelReason, ITeleportWarmupCancelledEvent> SUPPLIER =
            (timedTeleport, duration, cancelledAfter, cancelReason) ->
            new ITeleportWarmupCancelledEvent() {

                @Override
                @NotNull
                public TimedTeleport getTimedTeleport() {
                    return timedTeleport;
                }

                @Override
                public int getWarmupDuration() {
                    return duration;
                }

                @Override
                public int cancelledAfter() {
                    return cancelledAfter;
                }

                @Override
                public @NotNull ITeleportWarmupCancelledEvent.CancelReason getCancelReason() {
                    return cancelReason;
                }

                @NotNull
                public Event<TeleportWarmupCancelledCallback> getEvent() {
                    return EVENT;
                }

            };
}