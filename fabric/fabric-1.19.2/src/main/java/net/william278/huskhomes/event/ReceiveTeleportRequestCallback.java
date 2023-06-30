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
import net.william278.huskhomes.teleport.TeleportRequest;
import net.william278.huskhomes.user.OnlineUser;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;

public interface ReceiveTeleportRequestCallback extends FabricEventCallback<IReceiveTeleportRequestEvent> {

    @NotNull
    Event<ReceiveTeleportRequestCallback> EVENT = EventFactory.createArrayBacked(ReceiveTeleportRequestCallback.class,
            (listeners) -> (event) -> {
                for (ReceiveTeleportRequestCallback listener : listeners) {
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
    BiFunction<OnlineUser, TeleportRequest, IReceiveTeleportRequestEvent> SUPPLIER = (recipient, request) ->
            new IReceiveTeleportRequestEvent() {
                private boolean cancelled = false;

                @Override
                @NotNull
                public OnlineUser getRecipient() {
                    return recipient;
                }

                @Override
                @NotNull
                public TeleportRequest getRequest() {
                    return request;
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
                public Event<ReceiveTeleportRequestCallback> getEvent() {
                    return EVENT;
                }

            };

}
