package net.william278.huskhomes.listener;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.william278.huskhomes.FabricHuskHomes;
import net.william278.huskhomes.user.FabricUser;
import org.jetbrains.annotations.NotNull;

public class FabricEventListener extends EventListener {

    public FabricEventListener(@NotNull FabricHuskHomes plugin) {
        super(plugin);
        this.registerEvents(plugin);
    }

    // Register fabric event callback listeners to internal handlers
    private void registerEvents(@NotNull FabricHuskHomes plugin) {
        // Join event
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> handlePlayerJoin(
                FabricUser.adapt(plugin, handler.player)
        ));

        // Quit event
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> handlePlayerLeave(
                FabricUser.adapt(plugin, handler.player)
        ));

        // Death event
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (entity instanceof ServerPlayerEntity player) {
                handlePlayerDeath(FabricUser.adapt(plugin, player));
            }
        });

        // Respawn event
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> handlePlayerRespawn(
                FabricUser.adapt(plugin, newPlayer)
        ));

        // todo: Teleport event
        // todo: Update Spawn Position Event
    }

}
