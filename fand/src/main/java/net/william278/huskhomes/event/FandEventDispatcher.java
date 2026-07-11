/*
 * This file is part of HuskHomes, licensed under the Apache License 2.0.
 *
 *  Copyright (c) William278 <will27528@gmail.com>
 *  Copyright (c) contributors
 */

package net.william278.huskhomes.event;

import net.william278.huskhomes.FandHuskHomes;
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

import java.util.List;

public interface FandEventDispatcher extends EventDispatcher {

    @Override
    default <T extends Event> boolean fireIsCancelled(@NotNull T event) {
        getPlugin().getContext().events().fire(new FandHuskHomesEvent(event));
        return event instanceof Cancellable cancellable && cancellable.isCancelled();
    }

    @Override
    default ITeleportEvent getTeleportEvent(@NotNull Teleport teleport) {
        return FandEvents.teleport(teleport);
    }

    @Override
    default ITeleportWarmupEvent getTeleportWarmupEvent(@NotNull TimedTeleport teleport, int duration) {
        return FandEvents.teleportWarmup(teleport, duration);
    }

    @Override
    default ITeleportWarmupCancelledEvent getTeleportWarmupCancelledEvent(@NotNull TimedTeleport teleport,
                                                                          int duration, int cancelledAfter,
                                                                          @NotNull ITeleportWarmupCancelledEvent.CancelReason reason) {
        return FandEvents.teleportWarmupCancelled(teleport, duration, cancelledAfter, reason);
    }

    @Override
    default ISendTeleportRequestEvent getSendTeleportRequestEvent(@NotNull OnlineUser sender,
                                                                  @NotNull TeleportRequest request) {
        return FandEvents.sendRequest(sender, request);
    }

    @Override
    default IReceiveTeleportRequestEvent getReceiveTeleportRequestEvent(@NotNull OnlineUser recipient,
                                                                        @NotNull TeleportRequest request) {
        return FandEvents.receiveRequest(recipient, request);
    }

    @Override
    default IReplyTeleportRequestEvent getReplyTeleportRequestEvent(@NotNull OnlineUser recipient,
                                                                    @NotNull TeleportRequest request) {
        return FandEvents.replyRequest(recipient, request);
    }

    @Override
    default IHomeCreateEvent getHomeCreateEvent(@NotNull User owner, @NotNull String name,
                                                @NotNull Position position, @NotNull CommandUser creator) {
        return FandEvents.homeCreate(owner, name, position, creator);
    }

    @Override
    default IHomeEditEvent getHomeEditEvent(@NotNull Home home, @NotNull Home original,
                                            @NotNull CommandUser editor) {
        return FandEvents.homeEdit(home, original, editor);
    }

    @Override
    default IHomeDeleteEvent getHomeDeleteEvent(@NotNull Home home, @NotNull CommandUser deleter) {
        return FandEvents.homeDelete(home, deleter);
    }

    @Override
    default IWarpCreateEvent getWarpCreateEvent(@NotNull String name, @NotNull Position position,
                                                @NotNull CommandUser creator) {
        return FandEvents.warpCreate(name, position, creator);
    }

    @Override
    default IWarpEditEvent getWarpEditEvent(@NotNull Warp warp, @NotNull Warp original,
                                            @NotNull CommandUser editor) {
        return FandEvents.warpEdit(warp, original, editor);
    }

    @Override
    default IWarpDeleteEvent getWarpDeleteEvent(@NotNull Warp warp, @NotNull CommandUser deleter) {
        return FandEvents.warpDelete(warp, deleter);
    }

    @Override
    default IHomeListEvent getViewHomeListEvent(@NotNull List<Home> homes, @NotNull CommandUser viewer,
                                                boolean publicHomeList) {
        return FandEvents.homeList(homes, viewer, publicHomeList);
    }

    @Override
    default IWarpListEvent getViewWarpListEvent(@NotNull List<Warp> warps, @NotNull CommandUser viewer) {
        return FandEvents.warpList(warps, viewer);
    }

    @Override
    default IDeleteAllHomesEvent getDeleteAllHomesEvent(@NotNull User user, @NotNull CommandUser deleter) {
        return FandEvents.deleteAllHomes(user, deleter);
    }

    @Override
    default IDeleteAllWarpsEvent getDeleteAllWarpsEvent(@NotNull CommandUser deleter) {
        return FandEvents.deleteAllWarps(deleter);
    }

    @Override
    @NotNull
    FandHuskHomes getPlugin();
}
