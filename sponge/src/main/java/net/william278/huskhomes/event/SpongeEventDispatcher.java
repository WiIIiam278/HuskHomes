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

import net.william278.huskhomes.SpongeHuskHomes;
import net.william278.huskhomes.position.Home;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.position.Warp;
import net.william278.huskhomes.teleport.Teleport;
import net.william278.huskhomes.teleport.TeleportRequest;
import net.william278.huskhomes.teleport.TimedTeleport;
import net.william278.huskhomes.user.CommandUser;
import net.william278.huskhomes.user.OnlineUser;
import net.william278.huskhomes.user.User;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.event.Event;

import java.util.List;

import static net.william278.huskhomes.event.ITeleportWarmupCancelledEvent.CancelReason;

public interface SpongeEventDispatcher extends EventDispatcher {

    @Override
    default <T extends net.william278.huskhomes.event.Event> boolean fireIsCancelled(@NotNull T event) {
        return getPlugin().getGame().eventManager().post((Event) event);
    }

    @Override
    @NotNull
    default ITeleportEvent getTeleportEvent(@NotNull Teleport teleport) {
        return new SpongeTeleportEvent(teleport);
    }

    @Override
    default @NotNull ITeleportWarmupEvent getTeleportWarmupEvent(@NotNull TimedTeleport teleport, int duration) {
        return new SpongeTeleportWarmupEvent(teleport, duration);
    }

    @Override
    @NotNull
    default ITeleportWarmupCancelledEvent getTeleportWarmupCancelledEvent(@NotNull TimedTeleport teleport,
                                                                          int duration,
                                                                          int cancelledAfter,
                                                                          @NotNull CancelReason cancelReason) {
        return new SpongeTeleportWarmupCancelledEvent(teleport, duration, cancelledAfter, cancelReason);
    }

    @Override
    default @NotNull ISendTeleportRequestEvent getSendTeleportRequestEvent(@NotNull OnlineUser sender,
                                                                           @NotNull TeleportRequest request) {
        return new SpongeSendTeleportRequestEvent(sender, request);
    }

    @Override
    default @NotNull IReceiveTeleportRequestEvent getReceiveTeleportRequestEvent(@NotNull OnlineUser recipient,
                                                                                 @NotNull TeleportRequest request) {
        return new SpongeReceiveTeleportRequestEvent(recipient, request);
    }

    @Override
    default @NotNull IReplyTeleportRequestEvent getReplyTeleportRequestEvent(@NotNull OnlineUser recipient,
                                                                             @NotNull TeleportRequest request) {
        return new SpongeReplyTeleportRequestEvent(recipient, request);
    }

    @Override
    default @NotNull IHomeCreateEvent getHomeCreateEvent(@NotNull User owner, @NotNull String name,
                                                         @NotNull Position position, @NotNull CommandUser creator) {
        return new SpongeHomeCreateEvent(owner, name, position, creator);
    }

    @Override
    default @NotNull IHomeEditEvent getHomeEditEvent(@NotNull Home home, @NotNull Home original,
                                                     @NotNull CommandUser editor) {
        return new SpongeHomeEditEvent(home, original, editor);
    }

    @Override
    default @NotNull IHomeDeleteEvent getHomeDeleteEvent(@NotNull Home home, @NotNull CommandUser deleter) {
        return new SpongeHomeDeleteEvent(home, deleter);
    }

    @Override
    default @NotNull IWarpCreateEvent getWarpCreateEvent(@NotNull String name, @NotNull Position position,
                                                         @NotNull CommandUser creator) {
        return new SpongeWarpCreateEvent(name, position, creator);
    }

    @Override
    default @NotNull IWarpEditEvent getWarpEditEvent(@NotNull Warp warp, @NotNull Warp original,
                                                     @NotNull CommandUser editor) {
        return new SpongeWarpEditEvent(warp, original, editor);
    }

    @Override
    default @NotNull IWarpDeleteEvent getWarpDeleteEvent(@NotNull Warp warp, @NotNull CommandUser deleter) {
        return new SpongeWarpDeleteEvent(warp, deleter);
    }

    @Override
    default @NotNull IHomeListEvent getViewHomeListEvent(@NotNull List<Home> homes, @NotNull CommandUser listViewer,
                                                         boolean publicHomeList) {
        return new SpongeHomeListEvent(homes, listViewer, publicHomeList);
    }

    @Override
    default @NotNull IWarpListEvent getViewWarpListEvent(@NotNull List<Warp> homes, @NotNull CommandUser listViewer) {
        return new SpongeWarpListEvent(homes, listViewer);
    }

    @Override
    default @NotNull IDeleteAllHomesEvent getDeleteAllHomesEvent(@NotNull User user, @NotNull CommandUser deleter) {
        return new SpongeDeleteAllHomesEvent(user, deleter);
    }

    @NotNull
    @Override
    default IDeleteAllWarpsEvent getDeleteAllWarpsEvent(@NotNull CommandUser deleter) {
        return new SpongeDeleteAllWarpsEvent(deleter);
    }

    @NotNull
    @Override
    SpongeHuskHomes getPlugin();

}
