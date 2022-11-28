package net.william278.huskhomes.event;

import net.william278.huskhomes.SpongeHuskHomes;
import net.william278.huskhomes.player.OnlineUser;
import net.william278.huskhomes.player.User;
import net.william278.huskhomes.position.Home;
import net.william278.huskhomes.position.Warp;
import net.william278.huskhomes.teleport.Teleport;
import net.william278.huskhomes.teleport.TimedTeleport;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;

//todo base implementation
public class SpongeEventDispatcher implements EventDispatcher {
    public SpongeEventDispatcher(SpongeHuskHomes spongeHuskHomes) {
    }

    @Override
    public void dispatchTeleportEvent(@NotNull Teleport teleport) {

    }

    @Override
    public CompletableFuture<ITeleportWarmupEvent> dispatchTeleportWarmupEvent(@NotNull TimedTeleport teleport, int duration) {
        return CompletableFuture.completedFuture(new ITeleportWarmupEvent() {
            @Override
            public int getWarmupDuration() {
                return duration;
            }

            @Override
            @NotNull
            public TimedTeleport getTimedTeleport() {
                return teleport;
            }

            @Override
            public void setCancelled(boolean cancelled) {

            }

            @Override
            public boolean isCancelled() {
                return false;
            }
        });
    }

    @Override
    public CompletableFuture<IHomeSaveEvent> dispatchHomeSaveEvent(@NotNull Home home) {
        return CompletableFuture.completedFuture(new IHomeSaveEvent() {
            @Override
            @NotNull
            public Home getHome() {
                return home;
            }

            @Override
            public void setCancelled(boolean cancelled) {

            }

            @Override
            public boolean isCancelled() {
                return false;
            }
        });
    }

    @Override
    public CompletableFuture<IHomeDeleteEvent> dispatchHomeDeleteEvent(@NotNull Home home) {
        return CompletableFuture.completedFuture(new IHomeDeleteEvent() {
            @Override
            @NotNull
            public Home getHome() {
                return home;
            }

            @Override
            public void setCancelled(boolean cancelled) {

            }

            @Override
            public boolean isCancelled() {
                return false;
            }
        });
    }

    @Override
    public CompletableFuture<IWarpSaveEvent> dispatchWarpSaveEvent(@NotNull Warp warp) {
        return CompletableFuture.completedFuture(new IWarpSaveEvent() {
            @Override
            @NotNull
            public Warp getWarp() {
                return warp;
            }

            @Override
            public void setCancelled(boolean cancelled) {

            }

            @Override
            public boolean isCancelled() {
                return false;
            }
        });
    }

    @Override
    public CompletableFuture<IWarpDeleteEvent> dispatchWarpDeleteEvent(@NotNull Warp warp) {
        return CompletableFuture.completedFuture(new IWarpDeleteEvent() {
            @Override
            @NotNull
            public Warp getWarp() {
                return warp;
            }

            @Override
            public void setCancelled(boolean cancelled) {

            }

            @Override
            public boolean isCancelled() {
                return false;
            }
        });
    }

    @Override
    public CompletableFuture<IHomeListEvent> dispatchViewHomeListEvent(@NotNull List<Home> homes, @NotNull OnlineUser user, boolean publicHomeList) {
        return CompletableFuture.completedFuture(new IHomeListEvent() {
            @Override
            @NotNull
            public List<Home> getHomes() {
                return homes;
            }

            @Override
            @NotNull
            public OnlineUser getOnlineUser() {
                return user;
            }

            @Override
            public boolean getIsPublicHomeList() {
                return publicHomeList;
            }

            @Override
            public void setCancelled(boolean cancelled) {

            }

            @Override
            public boolean isCancelled() {
                return false;
            }
        });
    }

    @Override
    public CompletableFuture<IWarpListEvent> dispatchViewWarpListEvent(@NotNull List<Warp> homes, @NotNull OnlineUser user) {
        return CompletableFuture.completedFuture(new IWarpListEvent() {
            @Override
            @NotNull
            public List<Warp> getWarps() {
                return homes;
            }

            @Override
            @NotNull
            public OnlineUser getOnlineUser() {
                return user;
            }

            @Override
            public void setCancelled(boolean cancelled) {

            }

            @Override
            public boolean isCancelled() {
                return false;
            }
        });
    }

    @Override
    public CompletableFuture<IDeleteAllHomesEvent> dispatchDeleteAllHomesEvent(@NotNull User user) {
        return null;
    }

    @Override
    public CompletableFuture<IDeleteAllWarpsEvent> dispatchDeleteAllWarpsEvent() {
        return null;
    }
}
