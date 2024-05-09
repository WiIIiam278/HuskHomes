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
import org.jetbrains.annotations.NotNull;

/**
 * Representation of an event that fires when a timed teleport warmup is cancelled.
 */
public interface ITeleportWarmupCancelledEvent extends Event {

    /**
     * The duration of the timed teleport warmup in seconds.
     *
     * @return the teleport warmup duration
     */
    int getWarmupDuration();

    /**
     * The time passed before the teleport warmup was cancelled in seconds.
     *
     * @return the time passed before the teleport warmup was cancelled
     */
    int cancelledAfter();

    /**
     * The {@link TimedTeleport} not being executed due to the warmup being cancelled.
     *
     * @return the timed teleport that has started
     */
    @NotNull
    TimedTeleport getTimedTeleport();

    /**
     * The reason the teleport warmup was cancelled.
     *
     * @return the reason the teleport warmup was cancelled
     */
    @NotNull
    CancelReason getCancelReason();

    /**
     * The reason the teleport warmup was cancelled.
     */
    enum CancelReason {
        /**
         * The teleport warmup was cancelled due to the player moving.
         */
        PLAYER_MOVE,
        /**
         * The teleport warmup was cancelled due to the player taking damage.
         */
        PLAYER_DAMAGE,
    }

}