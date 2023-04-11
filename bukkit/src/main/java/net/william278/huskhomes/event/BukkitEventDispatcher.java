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

import net.william278.huskhomes.position.Home;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.position.Warp;
import net.william278.huskhomes.teleport.Teleport;
import net.william278.huskhomes.teleport.TeleportRequest;
import net.william278.huskhomes.teleport.TimedTeleport;
import net.william278.huskhomes.user.CommandUser;
import net.william278.huskhomes.user.OnlineUser;
import net.william278.huskhomes.user.User;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface BukkitEventDispatcher extends EventDispatcher {


    @Override
    default <T extends Event> boolean fireIsCancelled(@NotNull T event) {
        Bukkit.getPluginManager().callEvent((org.bukkit.event.Event) event);
        return event instanceof Cancellable cancellable && cancellable.isCancelled();
    }

    @Override
    default ITeleportEvent getTeleportEvent(@NotNull Teleport teleport) {
        return teleport.getType() == Teleport.Type.BACK ? new TeleportBackEvent(teleport) : new TeleportEvent(teleport);
    }

    @Override
    default ITeleportWarmupEvent getTeleportWarmupEvent(@NotNull TimedTeleport teleport, int duration) {
        return new TeleportWarmupEvent(teleport, duration);
    }

    @Override
    default ISendTeleportRequestEvent getSendTeleportRequestEvent(@NotNull OnlineUser sender, @NotNull TeleportRequest request) {
        return new SendTeleportRequestEvent(sender, request);
    }

    @Override
    default IReceiveTeleportRequestEvent getReceiveTeleportRequestEvent(@NotNull OnlineUser recipient, @NotNull TeleportRequest request) {
        return new ReceiveTeleportRequestEvent(recipient, request);
    }

    @Override
    default IReplyTeleportRequestEvent getReplyTeleportRequestEvent(@NotNull OnlineUser recipient, @NotNull TeleportRequest request) {
        return new ReplyTeleportRequestEvent(recipient, request);
    }

    @Override
    default IHomeCreateEvent getHomeCreateEvent(@NotNull User owner, @NotNull String name, @NotNull Position position, @NotNull CommandUser creator) {
        return new HomeCreateEvent(owner, name, position, creator);
    }

    @Override
    default IHomeEditEvent getHomeEditEvent(@NotNull Home home, @NotNull CommandUser editor) {
        return new HomeEditEvent(home, editor);
    }

    @Override
    default IHomeDeleteEvent getHomeDeleteEvent(@NotNull Home home, @NotNull CommandUser deleter) {
        return new HomeDeleteEvent(home, deleter);
    }

    @Override
    default IWarpCreateEvent getWarpCreateEvent(@NotNull String name, @NotNull Position position, @NotNull CommandUser creator) {
        return new WarpCreateEvent(name, position, creator);
    }

    @Override
    default IWarpEditEvent getWarpEditEvent(@NotNull Warp warp, @NotNull CommandUser editor) {
        return new WarpEditEvent(warp, editor);
    }

    @Override
    default IWarpDeleteEvent getWarpDeleteEvent(@NotNull Warp warp, @NotNull CommandUser deleter) {
        return new WarpDeleteEvent(warp, deleter);
    }

    @Override
    default IHomeListEvent getViewHomeListEvent(@NotNull List<Home> homes, @NotNull CommandUser listViewer, boolean publicHomeList) {
        return new HomeListEvent(homes, listViewer, publicHomeList);
    }

    @Override
    default IWarpListEvent getViewWarpListEvent(@NotNull List<Warp> warps, @NotNull CommandUser listViewer) {
        return new WarpListEvent(warps, listViewer);
    }

    @Override
    default IDeleteAllHomesEvent getDeleteAllHomesEvent(@NotNull User user, @NotNull CommandUser deleter) {
        return new DeleteAllHomesEvent(user, deleter);
    }

    @Override
    default IDeleteAllWarpsEvent getDeleteAllWarpsEvent(@NotNull CommandUser deleter) {
        return new DeleteAllWarpsEvent(deleter);
    }

}
