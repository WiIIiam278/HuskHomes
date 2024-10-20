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

import net.william278.huskhomes.teleport.TimedTeleport;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class TeleportWarmupCancelledEvent extends Event implements ITeleportWarmupCancelledEvent {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final TimedTeleport warp;
    private final int duration;
    private final int cancelledAfter;
    private final CancelReason cancelReason;

    public TeleportWarmupCancelledEvent(@NotNull TimedTeleport warp, int duration,
                                        int cancelledAfter, @NotNull CancelReason cancelReason) {
        this.warp = warp;
        this.duration = duration;
        this.cancelledAfter = cancelledAfter;
        this.cancelReason = cancelReason;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    @SuppressWarnings("unused")
    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @Override
    public int getWarmupDuration() {
        return duration;
    }

    @Override
    public int cancelledAfter() {
        return cancelledAfter;
    }

    @NotNull
    @Override
    public TimedTeleport getTimedTeleport() {
        return warp;
    }

    @Override
    public @NotNull CancelReason getCancelReason() {
        return cancelReason;
    }
}