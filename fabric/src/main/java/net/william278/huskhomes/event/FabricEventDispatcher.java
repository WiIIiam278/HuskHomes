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

            net.fabricmc.fabric.api.event.Event<?> fabricEvent = (net.fabricmc.fabric.api.event.Event<?>) field.invoke(event);

            final FabricEventCallback<T> invoker = (FabricEventCallback<T>) fabricEvent.invoker();
            return invoker.invoke(event) == ActionResult.FAIL;
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            getPlugin().log(Level.WARNING, "Failed to fire event (" + event.getClass().getName() + ")", e);
            return false;
        }
    }

    @Override
    default ITeleportEvent getTeleportEvent(@NotNull Teleport teleport) {
        return TeleportCallback.SUPPLIER.apply(teleport);
    }

    @Override
    default ITeleportWarmupEvent getTeleportWarmupEvent(@NotNull TimedTeleport teleport, int duration) {
        return TeleportWarmupCallback.SUPPLIER.apply(teleport, duration);
    }

    @Override
    default ISendTeleportRequestEvent getSendTeleportRequestEvent(@NotNull OnlineUser sender, @NotNull TeleportRequest request) {
        return SendTeleportRequestCallback.SUPPLIER.apply(sender, request);
    }

    @Override
    default IReceiveTeleportRequestEvent getReceiveTeleportRequestEvent(@NotNull OnlineUser recipient, @NotNull TeleportRequest request) {
        return ReceiveTeleportRequestCallback.SUPPLIER.apply(recipient, request);
    }

    @Override
    default IReplyTeleportRequestEvent getReplyTeleportRequestEvent(@NotNull OnlineUser recipient, @NotNull TeleportRequest request) {
        return ReplyTeleportRequestCallback.SUPPLIER.apply(recipient, request);
    }

    @Override
    default IHomeCreateEvent getHomeCreateEvent(@NotNull User owner, @NotNull String name, @NotNull Position position, @NotNull CommandUser creator) {
        return HomeCreateCallback.SUPPLIER.apply(owner, name, position, creator);
    }

    @Override
    default IHomeEditEvent getHomeEditEvent(@NotNull Home home, @NotNull CommandUser editor) {
        return HomeEditCallback.SUPPLIER.apply(home, editor);
    }

    @Override
    default IHomeDeleteEvent getHomeDeleteEvent(@NotNull Home home, @NotNull CommandUser deleter) {
        return HomeDeleteCallback.SUPPLIER.apply(home, deleter);
    }

    @Override
    default IWarpCreateEvent getWarpCreateEvent(@NotNull String name, @NotNull Position position, @NotNull CommandUser creator) {
        return WarpCreateCallback.SUPPLIER.apply(name, position, creator);
    }

    @Override
    default IWarpEditEvent getWarpEditEvent(@NotNull Warp warp, @NotNull CommandUser editor) {
        return WarpEditCallback.SUPPLIER.apply(warp, editor);
    }

    @Override
    default IWarpDeleteEvent getWarpDeleteEvent(@NotNull Warp warp, @NotNull CommandUser deleter) {
        return WarpDeleteCallback.SUPPLIER.apply(warp, deleter);
    }

    @Override
    default IHomeListEvent getViewHomeListEvent(@NotNull List<Home> homes, @NotNull CommandUser listViewer, boolean publicHomeList) {
        return HomeListCallback.SUPPLIER.apply(homes, listViewer, publicHomeList);
    }

    @Override
    default IWarpListEvent getViewWarpListEvent(@NotNull List<Warp> homes, @NotNull CommandUser listViewer) {
        return WarpListCallback.SUPPLIER.apply(homes, listViewer);
    }

    @Override
    default IDeleteAllHomesEvent getDeleteAllHomesEvent(@NotNull User user, @NotNull CommandUser deleter) {
        return DeleteAllHomesCallback.SUPPLIER.apply(user, deleter);
    }

    @Override
    default IDeleteAllWarpsEvent getDeleteAllWarpsEvent(@NotNull CommandUser deleter) {
        return DeleteAllWarpsCallback.SUPPLIER.apply(deleter);
    }

    @Override
    @NotNull
    FabricHuskHomes getPlugin();

}
