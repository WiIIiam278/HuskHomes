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

import net.william278.huskhomes.position.Warp;
import net.william278.huskhomes.user.CommandUser;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class WarpEditEvent extends Event implements IWarpEditEvent, Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final Warp warp;
    private final Warp original;
    private final CommandUser editor;
    private boolean cancelled;

    public WarpEditEvent(@NotNull Warp warp, @NotNull Warp original, @NotNull CommandUser editor) {
        this.warp = warp;
        this.original = warp;
        this.editor = editor;
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
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    @NotNull
    public Warp getWarp() {
        return warp;
    }

    @NotNull
    @Override
    public Warp getOriginalWarp() {
        return original;
    }

    @Override
    @NotNull
    public CommandUser getEditor() {
        return editor;
    }
}
