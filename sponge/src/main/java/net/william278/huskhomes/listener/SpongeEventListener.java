package net.william278.huskhomes.listener;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.SpongeHuskHomes;
import net.william278.huskhomes.player.SpongePlayer;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.entity.living.player.RespawnPlayerEvent;
import org.spongepowered.api.event.network.ServerSideConnectionEvent;
import org.spongepowered.plugin.PluginContainer;

public class SpongeEventListener extends EventListener {

    public SpongeEventListener(@NotNull HuskHomes implementor, @NotNull PluginContainer container) {
        super(implementor);
        Sponge.eventManager().registerListeners(container, this);
    }

    @Listener
    public void onPlayerJoin(final ServerSideConnectionEvent.Join event) {
        super.handlePlayerJoin(SpongePlayer.adapt(event.player()));
    }

    @Listener
    public void onPlayerLeave(final ServerSideConnectionEvent.Disconnect event) {
        super.handlePlayerJoin(SpongePlayer.adapt(event.player()));
    }

    @Listener
    public void onPlayerDeath(final DestructEntityEvent.Death event) {
        if (event.entity() instanceof ServerPlayer player) {
            super.handlePlayerJoin(SpongePlayer.adapt(player));
        }
    }

    @Listener
    public void onPlayerRespawn(final RespawnPlayerEvent event) {
        super.handlePlayerJoin(SpongePlayer.adapt(event.entity()));
    }

}
