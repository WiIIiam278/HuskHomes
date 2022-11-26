package net.william278.huskhomes.listener;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.player.SpongePlayer;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.util.SpongeAdapter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.cause.entity.MovementType;
import org.spongepowered.api.event.cause.entity.MovementTypes;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.entity.living.player.RespawnPlayerEvent;
import org.spongepowered.api.event.network.ServerSideConnectionEvent;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.plugin.PluginContainer;

import java.util.Optional;

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

    @Listener
    public void onPlayerTeleport(final MoveEntityEvent event) {
        if (event.entity() instanceof ServerPlayer player) {
            final Optional<MovementType> type = event.context().get(EventContextKeys.MOVEMENT_TYPE);
            if (type.isPresent() && type.get().equals(MovementTypes.ENTITY_TELEPORT.get())) {
                SpongeAdapter.adaptLocation(ServerLocation.of(player.world(), event.originalPosition()))
                        .ifPresent(location -> super.handlePlayerTeleport(SpongePlayer.adapt(player),
                                new Position(location, plugin.getPluginServer())));
            }
        }
    }

    @Listener
    @SuppressWarnings("unchecked")
    public void onPlayerUpdateRespawnLocation(InteractBlockEvent.Secondary event) {
        if (event.context().get(EventContextKeys.PLAYER).isPresent()) {
            final ServerPlayer player = (ServerPlayer) event.context().get(EventContextKeys.PLAYER).get();
            final BlockType type = event.block().state().type();

            if (type.isAnyOf(BlockTypes.BLACK_BED, BlockTypes.BLUE_BED, BlockTypes.BROWN_BED, BlockTypes.CYAN_BED,
                    BlockTypes.GRAY_BED, BlockTypes.GREEN_BED, BlockTypes.LIGHT_BLUE_BED, BlockTypes.LIGHT_GRAY_BED,
                    BlockTypes.LIME_BED, BlockTypes.MAGENTA_BED, BlockTypes.ORANGE_BED, BlockTypes.PINK_BED,
                    BlockTypes.PURPLE_BED, BlockTypes.RED_BED, BlockTypes.WHITE_BED, BlockTypes.YELLOW_BED,
                    BlockTypes.RESPAWN_ANCHOR)) {

                super.handlePlayerUpdateSpawnPoint(SpongePlayer.adapt(player));
            }
        }
    }

}
