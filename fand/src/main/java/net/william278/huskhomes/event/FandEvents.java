/*
 * This file is part of HuskHomes, licensed under the Apache License 2.0.
 *
 *  Copyright (c) William278 <will27528@gmail.com>
 *  Copyright (c) contributors
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
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

final class FandEvents {

    private FandEvents() {
    }

    static ITeleportEvent teleport(Teleport teleport) {
        return switch (teleport.getType()) {
            case BACK -> new BackTeleportEvent(teleport);
            case RANDOM_TELEPORT -> new RandomTeleportEvent(teleport);
            default -> new TeleportEvent(teleport);
        };
    }

    static ITeleportWarmupEvent teleportWarmup(TimedTeleport teleport, int duration) {
        return new WarmupEvent(teleport, duration);
    }

    static ITeleportWarmupCancelledEvent teleportWarmupCancelled(TimedTeleport teleport, int duration,
                                                                  int cancelledAfter,
                                                                  ITeleportWarmupCancelledEvent.CancelReason reason) {
        return new WarmupCancelledEvent(teleport, duration, cancelledAfter, reason);
    }

    static ISendTeleportRequestEvent sendRequest(OnlineUser sender, TeleportRequest request) {
        return new SendRequestEvent(sender, request);
    }

    static IReceiveTeleportRequestEvent receiveRequest(OnlineUser recipient, TeleportRequest request) {
        return new ReceiveRequestEvent(recipient, request);
    }

    static IReplyTeleportRequestEvent replyRequest(OnlineUser recipient, TeleportRequest request) {
        return new ReplyRequestEvent(recipient, request);
    }

    static IHomeCreateEvent homeCreate(User owner, String name, Position position, CommandUser creator) {
        return new HomeCreateEvent(owner, name, position, creator);
    }

    static IHomeEditEvent homeEdit(Home home, Home original, CommandUser editor) {
        return new HomeEditEvent(home, original, editor);
    }

    static IHomeDeleteEvent homeDelete(Home home, CommandUser deleter) {
        return new HomeDeleteEvent(home, deleter);
    }

    static IWarpCreateEvent warpCreate(String name, Position position, CommandUser creator) {
        return new WarpCreateEvent(name, position, creator);
    }

    static IWarpEditEvent warpEdit(Warp warp, Warp original, CommandUser editor) {
        return new WarpEditEvent(warp, original, editor);
    }

    static IWarpDeleteEvent warpDelete(Warp warp, CommandUser deleter) {
        return new WarpDeleteEvent(warp, deleter);
    }

    static IHomeListEvent homeList(List<Home> homes, CommandUser viewer, boolean publicHomes) {
        return new HomeListEvent(homes, viewer, publicHomes);
    }

    static IWarpListEvent warpList(List<Warp> warps, CommandUser viewer) {
        return new WarpListEvent(warps, viewer);
    }

    static IDeleteAllHomesEvent deleteAllHomes(User owner, CommandUser deleter) {
        return new DeleteAllHomesEvent(owner, deleter);
    }

    static IDeleteAllWarpsEvent deleteAllWarps(CommandUser deleter) {
        return new DeleteAllWarpsEvent(deleter);
    }

    private abstract static class CancellableEvent implements Cancellable {
        private boolean cancelled;

        @Override
        public final void setCancelled(boolean cancelled) {
            this.cancelled = cancelled;
        }

        @Override
        public final boolean isCancelled() {
            return cancelled;
        }
    }

    private static class TeleportEvent extends CancellableEvent implements ITeleportEvent {
        private final Teleport teleport;

        private TeleportEvent(Teleport teleport) {
            this.teleport = Objects.requireNonNull(teleport, "teleport");
        }

        @Override
        public Teleport getTeleport() {
            return teleport;
        }
    }

    private static final class BackTeleportEvent extends TeleportEvent implements ITeleportBackEvent {
        private BackTeleportEvent(Teleport teleport) {
            super(teleport);
        }

        @Override
        public Position getLastPosition() {
            return (Position) getTeleport().getTarget();
        }
    }

    private static final class RandomTeleportEvent extends TeleportEvent implements IRandomTeleportEvent {
        private RandomTeleportEvent(Teleport teleport) {
            super(teleport);
        }

        @Override
        public Position getPosition() {
            return (Position) getTeleport().getTarget();
        }
    }

    private static final class WarmupEvent extends CancellableEvent implements ITeleportWarmupEvent {
        private final TimedTeleport teleport;
        private final int duration;

        private WarmupEvent(TimedTeleport teleport, int duration) {
            this.teleport = teleport;
            this.duration = duration;
        }

        @Override
        public int getWarmupDuration() {
            return duration;
        }

        @Override
        public TimedTeleport getTimedTeleport() {
            return teleport;
        }
    }

    private record WarmupCancelledEvent(TimedTeleport teleport, int duration, int cancelledAfter,
                                        CancelReason reason) implements ITeleportWarmupCancelledEvent {
        @Override
        public int getWarmupDuration() {
            return duration;
        }

        @Override
        public TimedTeleport getTimedTeleport() {
            return teleport;
        }

        @Override
        public CancelReason getCancelReason() {
            return reason;
        }
    }

    private abstract static class RequestEvent extends CancellableEvent implements ITeleportRequestEvent {
        private final TeleportRequest request;

        private RequestEvent(TeleportRequest request) {
            this.request = request;
        }

        @Override
        public TeleportRequest getRequest() {
            return request;
        }
    }

    private static final class SendRequestEvent extends RequestEvent implements ISendTeleportRequestEvent {
        private final OnlineUser sender;

        private SendRequestEvent(OnlineUser sender, TeleportRequest request) {
            super(request);
            this.sender = sender;
        }

        @Override
        public OnlineUser getSender() {
            return sender;
        }
    }

    private static final class ReceiveRequestEvent extends RequestEvent implements IReceiveTeleportRequestEvent {
        private final OnlineUser recipient;

        private ReceiveRequestEvent(OnlineUser recipient, TeleportRequest request) {
            super(request);
            this.recipient = recipient;
        }

        @Override
        public OnlineUser getRecipient() {
            return recipient;
        }
    }

    private static final class ReplyRequestEvent extends RequestEvent implements IReplyTeleportRequestEvent {
        private final OnlineUser recipient;

        private ReplyRequestEvent(OnlineUser recipient, TeleportRequest request) {
            super(request);
            this.recipient = recipient;
        }

        @Override
        public OnlineUser getRecipient() {
            return recipient;
        }
    }

    private static final class HomeCreateEvent extends CancellableEvent implements IHomeCreateEvent {
        private final User owner;
        private final CommandUser creator;
        private String name;
        private Position position;

        private HomeCreateEvent(User owner, String name, Position position, CommandUser creator) {
            this.owner = owner;
            this.name = name;
            this.position = position;
            this.creator = creator;
        }

        @Override public User getOwner() { return owner; }
        @Override public String getName() { return name; }
        @Override public void setName(@NotNull String name) { this.name = name; }
        @Override public Position getPosition() { return position; }
        @Override public void setPosition(@NotNull Position position) { this.position = position; }
        @Override public CommandUser getCreator() { return creator; }
    }

    private static final class HomeEditEvent extends CancellableEvent implements IHomeEditEvent {
        private final Home home;
        private final Home original;
        private final CommandUser editor;

        private HomeEditEvent(Home home, Home original, CommandUser editor) {
            this.home = home;
            this.original = original;
            this.editor = editor;
        }

        @Override public Home getHome() { return home; }
        @Override public Home getOriginalHome() { return original; }
        @Override public CommandUser getEditor() { return editor; }
    }

    private static final class HomeDeleteEvent extends CancellableEvent implements IHomeDeleteEvent {
        private final Home home;
        private final CommandUser deleter;
        private HomeDeleteEvent(Home home, CommandUser deleter) { this.home = home; this.deleter = deleter; }
        @Override public Home getHome() { return home; }
        @Override public CommandUser getDeleter() { return deleter; }
    }

    private static final class WarpCreateEvent extends CancellableEvent implements IWarpCreateEvent {
        private final CommandUser creator;
        private String name;
        private Position position;
        private WarpCreateEvent(String name, Position position, CommandUser creator) {
            this.name = name; this.position = position; this.creator = creator;
        }
        @Override public String getName() { return name; }
        @Override public void setName(@NotNull String name) { this.name = name; }
        @Override public Position getPosition() { return position; }
        @Override public void setPosition(@NotNull Position position) { this.position = position; }
        @Override public CommandUser getCreator() { return creator; }
    }

    private static final class WarpEditEvent extends CancellableEvent implements IWarpEditEvent {
        private final Warp warp;
        private final Warp original;
        private final CommandUser editor;
        private WarpEditEvent(Warp warp, Warp original, CommandUser editor) {
            this.warp = warp; this.original = original; this.editor = editor;
        }
        @Override public Warp getWarp() { return warp; }
        @Override public Warp getOriginalWarp() { return original; }
        @Override public CommandUser getEditor() { return editor; }
    }

    private static final class WarpDeleteEvent extends CancellableEvent implements IWarpDeleteEvent {
        private final Warp warp;
        private final CommandUser deleter;
        private WarpDeleteEvent(Warp warp, CommandUser deleter) { this.warp = warp; this.deleter = deleter; }
        @Override public Warp getWarp() { return warp; }
        @Override public CommandUser getDeleter() { return deleter; }
    }

    private static final class HomeListEvent extends CancellableEvent implements IHomeListEvent {
        private final CommandUser viewer;
        private final boolean publicHomes;
        private List<Home> homes;
        private HomeListEvent(List<Home> homes, CommandUser viewer, boolean publicHomes) {
            this.homes = homes; this.viewer = viewer; this.publicHomes = publicHomes;
        }
        @Override public List<Home> getHomes() { return homes; }
        @Override public void setHomes(@NotNull List<Home> homes) { this.homes = homes; }
        @Override public CommandUser getListViewer() { return viewer; }
        @Override public boolean getIsPublicHomeList() { return publicHomes; }
    }

    private static final class WarpListEvent extends CancellableEvent implements IWarpListEvent {
        private final CommandUser viewer;
        private List<Warp> warps;
        private WarpListEvent(List<Warp> warps, CommandUser viewer) { this.warps = warps; this.viewer = viewer; }
        @Override public List<Warp> getWarps() { return warps; }
        @Override public void setWarps(@NotNull List<Warp> warps) { this.warps = warps; }
        @Override public CommandUser getListViewer() { return viewer; }
    }

    private static final class DeleteAllHomesEvent extends CancellableEvent implements IDeleteAllHomesEvent {
        private final User owner;
        private final CommandUser deleter;
        private DeleteAllHomesEvent(User owner, CommandUser deleter) { this.owner = owner; this.deleter = deleter; }
        @Override public User getHomeOwner() { return owner; }
        @Override public CommandUser getDeleter() { return deleter; }
    }

    private static final class DeleteAllWarpsEvent extends CancellableEvent implements IDeleteAllWarpsEvent {
        private final CommandUser deleter;
        private DeleteAllWarpsEvent(CommandUser deleter) { this.deleter = deleter; }
        @Override public CommandUser getDeleter() { return deleter; }
    }
}
