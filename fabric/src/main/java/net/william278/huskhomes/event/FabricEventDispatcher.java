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

import net.minecraft.util.ActionResult;
import net.william278.huskhomes.FabricHuskHomes;
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.logging.Level;

public interface FabricEventDispatcher extends EventDispatcher {

    @SuppressWarnings("unchecked")
    @Override
    default <T extends Event> boolean fireIsCancelled(@NotNull T event) {
        try {
            final Method field = event.getClass().getDeclaredMethod("getEvent");
            field.setAccessible(true);

            net.fabricmc.fabric.api.event.Event<?> fabricEvent =
                    (net.fabricmc.fabric.api.event.Event<?>) field.invoke(event);

            final FabricEventCallback<T> invoker = (FabricEventCallback<T>) fabricEvent.invoker();
            return invoker.invoke(event) == ActionResult.FAIL;
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            getPlugin().log(Level.WARNING, "Failed to fire event (" + event.getClass().getName() + ")", e);
            return false;
        }
    }

    @Override
    @NotNull
    default ITeleportEvent getTeleportEvent(@NotNull Teleport teleport) {
        return TeleportCallback.SUPPLIER.apply(teleport);
    }

    @Override
    default @NotNull ITeleportWarmupEvent getTeleportWarmupEvent(@NotNull TimedTeleport teleport, int duration) {
        return TeleportWarmupCallback.SUPPLIER.apply(teleport, duration);
    }

    @Override
    default @NotNull ISendTeleportRequestEvent getSendTeleportRequestEvent(@NotNull OnlineUser sender,
                                                                           @NotNull TeleportRequest request) {
        return SendTeleportRequestCallback.SUPPLIER.apply(sender, request);
    }

    @Override
    default @NotNull IReceiveTeleportRequestEvent getReceiveTeleportRequestEvent(@NotNull OnlineUser recipient,
                                                                                 @NotNull TeleportRequest request) {
        return ReceiveTeleportRequestCallback.SUPPLIER.apply(recipient, request);
    }

    @Override
    default @NotNull IReplyTeleportRequestEvent getReplyTeleportRequestEvent(@NotNull OnlineUser recipient,
                                                                             @NotNull TeleportRequest request) {
        return ReplyTeleportRequestCallback.SUPPLIER.apply(recipient, request);
    }

    @Override
    default @NotNull IHomeCreateEvent getHomeCreateEvent(@NotNull User owner, @NotNull String name,
                                                         @NotNull Position position, @NotNull CommandUser creator) {
        return HomeCreateCallback.SUPPLIER.apply(owner, name, position, creator);
    }

    @Override
    default @NotNull IHomeEditEvent getHomeEditEvent(@NotNull Home home, @NotNull Home original,
                                                     @NotNull CommandUser editor) {
        return HomeEditCallback.SUPPLIER.apply(home, original, editor);
    }

    @Override
    default @NotNull IHomeDeleteEvent getHomeDeleteEvent(@NotNull Home home, @NotNull CommandUser deleter) {
        return HomeDeleteCallback.SUPPLIER.apply(home, deleter);
    }

    @Override
    default @NotNull IWarpCreateEvent getWarpCreateEvent(@NotNull String name, @NotNull Position position,
                                                         @NotNull CommandUser creator) {
        return WarpCreateCallback.SUPPLIER.apply(name, position, creator);
    }

    @Override
    default @NotNull IWarpEditEvent getWarpEditEvent(@NotNull Warp warp, @NotNull Warp original,
                                                     @NotNull CommandUser editor) {
        return WarpEditCallback.SUPPLIER.apply(warp, original, editor);
    }

    @Override
    default @NotNull IWarpDeleteEvent getWarpDeleteEvent(@NotNull Warp warp, @NotNull CommandUser deleter) {
        return WarpDeleteCallback.SUPPLIER.apply(warp, deleter);
    }

    @Override
    default @NotNull IHomeListEvent getViewHomeListEvent(@NotNull List<Home> homes, @NotNull CommandUser listViewer,
                                                         boolean publicHomeList) {
        return HomeListCallback.SUPPLIER.apply(homes, listViewer, publicHomeList);
    }

    @Override
    default @NotNull IWarpListEvent getViewWarpListEvent(@NotNull List<Warp> homes, @NotNull CommandUser listViewer) {
        return WarpListCallback.SUPPLIER.apply(homes, listViewer);
    }

    @Override
    default @NotNull IDeleteAllHomesEvent getDeleteAllHomesEvent(@NotNull User user, @NotNull CommandUser deleter) {
        return DeleteAllHomesCallback.SUPPLIER.apply(user, deleter);
    }

    @Override
    default @NotNull IDeleteAllWarpsEvent getDeleteAllWarpsEvent(@NotNull CommandUser deleter) {
        return DeleteAllWarpsCallback.SUPPLIER.apply(deleter);
    }

    @Override
    @NotNull
    FabricHuskHomes getPlugin();

}
