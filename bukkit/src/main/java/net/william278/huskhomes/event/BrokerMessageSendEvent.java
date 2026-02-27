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

import net.william278.huskhomes.network.Message;
import net.william278.huskhomes.user.OnlineUser;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class BrokerMessageSendEvent extends Event implements IBrokerMessageSendEvent, Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final OnlineUser user;
    private String subChannelId;
    private Message message;
    private boolean cancelled;

    public BrokerMessageSendEvent(@NotNull OnlineUser user, @NotNull String subChannelId, @NotNull Message message) {
        this.user = user;
        this.message = message;
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
    @NotNull
    public OnlineUser getUser() {
        return user;
    }

    @Override
    @NotNull
    public String getSubChannelId() {
        return subChannelId;
    }

    @Override
    public @NotNull Message getMessage() {
        return message;
    }

    @Override
    public void setMessage(@NotNull Message message) {
        this.message = message;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

}
