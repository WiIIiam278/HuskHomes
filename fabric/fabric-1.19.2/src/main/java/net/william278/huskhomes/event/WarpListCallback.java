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
import net.william278.huskhomes.position.Warp;
import net.william278.huskhomes.user.CommandUser;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.BiFunction;

public interface WarpListCallback extends FabricEventCallback<IWarpListEvent> {

    @NotNull
    Event<WarpListCallback> EVENT = EventFactory.createArrayBacked(WarpListCallback.class,
            (listeners) -> (event) -> {
                for (WarpListCallback listener : listeners) {
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
    BiFunction<List<Warp>, CommandUser, IWarpListEvent> SUPPLIER = (warps, viewer) ->
            new IWarpListEvent() {
                private boolean cancelled = false;
                private List<Warp> listWarps = warps;

                @Override
                @NotNull
                public List<Warp> getWarps() {
                    return listWarps;
                }

                @Override
                public void setWarps(@NotNull List<Warp> homes) {
                    this.listWarps = homes;
                }

                @Override
                @NotNull
                public CommandUser getListViewer() {
                    return viewer;
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
                public Event<WarpListCallback> getEvent() {
                    return EVENT;
                }

            };

}
