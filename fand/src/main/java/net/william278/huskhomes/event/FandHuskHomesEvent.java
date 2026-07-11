/*
 * This file is part of HuskHomes, licensed under the Apache License 2.0.
 *
 *  Copyright (c) William278 <will27528@gmail.com>
 *  Copyright (c) contributors
 */

package net.william278.huskhomes.event;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Fand event-bus envelope for a platform-neutral HuskHomes API event.
 *
 * @param event mutable HuskHomes event payload
 */
public record FandHuskHomesEvent(@NotNull Event event) implements io.fand.api.event.Event {

    public FandHuskHomesEvent {
        Objects.requireNonNull(event, "event");
    }
}
