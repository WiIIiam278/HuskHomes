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

import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.user.CommandUser;
import net.william278.huskhomes.user.User;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.Event;

public class SpongeHomeCreateEvent implements IHomeCreateEvent, Event, Cancellable {

    private boolean cancelled = false;
    private final User owner;
    private String name;
    private Position position;
    private final CommandUser creator;

    public SpongeHomeCreateEvent(@NotNull User owner, @NotNull String name, @NotNull Position position,
                                 @NotNull CommandUser creator) {
        this.owner = owner;
        this.name = name;
        this.position = position;
        this.creator = creator;
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
    @Override
    public User getOwner() {
        return owner;
    }

    @NotNull
    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(@NotNull String name) {
        this.name = name;
    }

    @NotNull
    @Override
    public Position getPosition() {
        return position;
    }

    @Override
    public void setPosition(@NotNull Position position) {
        this.position = position;
    }

    @NotNull
    @Override
    public CommandUser getCreator() {
        return creator;
    }

    @Override
    public Cause cause() {
        return Cause.builder()
                .append(creator)
                .build();
    }

}
