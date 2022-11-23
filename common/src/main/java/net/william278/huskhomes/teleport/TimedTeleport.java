package net.william278.huskhomes.teleport;

import de.themoep.minedown.adventure.MineDown;
import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.config.Settings;
import net.william278.huskhomes.player.OnlineUser;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.util.Permission;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TimedTeleport extends Teleport {

    private final HuskHomes plugin;
    private final OnlineUser teleporter;
    private final Position startLocation;
    private final double startHealth;
    private int timeLeft;
    private boolean cancelled = false;

    protected TimedTeleport(@NotNull OnlineUser teleporter, @NotNull OnlineUser executor, @NotNull Position target,
                            @NotNull TeleportType type, int warmupTime, @NotNull Set<Settings.EconomyAction> economyActions,
                            final boolean updateLastPosition, @NotNull HuskHomes plugin) {
        super(teleporter, executor, target, type, economyActions, updateLastPosition, plugin);
        this.plugin = plugin;
        this.startLocation = teleporter.getPosition();
        this.startHealth = teleporter.getHealth();
        this.timeLeft = Math.max(warmupTime, 0);
        this.teleporter = teleporter;
    }

    /**
     * Execute the timed teleport, ticking the countdown and teleporting the player when the countdown is complete
     *
     * @return a {@link CompletableFuture} that completes when the teleport is complete or has been cancelled
     */
    @Override
    public CompletableFuture<CompletedTeleport> execute() {
        // If the target has not been resolved, fail the teleport
        if (target == null) {
            return CompletableFuture.completedFuture(TeleportResult.FAILED_TARGET_NOT_RESOLVED)
                    .thenApply(resultState -> CompletedTeleport.from(resultState, this));
        }

        // Check if the teleporter can bypass warmup
        if (timeLeft == 0 || teleporter.hasPermission(Permission.BYPASS_TELEPORT_WARMUP.node)) {
            return super.execute();
        }

        // Check if the teleporter is already warming up to teleport
        if (plugin.getCache().currentlyOnWarmup.contains(teleporter.uuid)) {
            return CompletableFuture.completedFuture(TeleportResult.FAILED_ALREADY_TELEPORTING)
                    .thenApply(resultState -> CompletedTeleport.from(resultState, this));
        }

        // Check economy actions
        for (Settings.EconomyAction economyAction : economyActions) {
            if (!plugin.validateEconomyCheck(executor, economyAction)) {
                return CompletableFuture.completedFuture(TeleportResult.CANCELLED_ECONOMY)
                        .thenApply(resultState -> CompletedTeleport.from(resultState, this));
            }
        }

        // Check if they are moving at the start of the teleport
        if (teleporter.isMoving()) {
            return CompletableFuture.completedFuture(TeleportResult.FAILED_MOVING)
                    .thenApply(resultState -> CompletedTeleport.from(resultState, this));
        }

        // Process the warmup and execute the teleport
        return process().thenApplyAsync(ignored -> {
            if (cancelled) {
                return CompletedTeleport.from(TeleportResult.CANCELLED, this);
            }
            return super.execute().join();
        });
    }

    /**
     * Start the processing of a {@link TimedTeleport} warmup
     *
     * @return a future, returning when the teleport has finished
     */
    private CompletableFuture<Void> process() {
        final CompletableFuture<Void> timedTeleportFuture = new CompletableFuture<>();

        // Execute the warmup start event
        plugin.getEventDispatcher().dispatchTeleportWarmupEvent(this, timeLeft).thenAccept(event -> {
            // Handle event cancellation
            if (event.isCancelled()) {
                this.cancelled = true;
                return;
            }

            // Mark the player as warming up and display the message
            plugin.getCache().currentlyOnWarmup.add(teleporter.uuid);
            plugin.getLocales().getLocale("teleporting_warmup_start", Integer.toString(timeLeft))
                    .ifPresent(teleporter::sendMessage);

            // Create a scheduled executor to tick the timed teleport
            final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
            executor.scheduleAtFixedRate(() -> {
                // Display countdown action bar message
                if (timeLeft > 0) {
                    plugin.getSettings().getSoundEffect(Settings.SoundEffectAction.TELEPORTATION_WARMUP)
                            .ifPresent(teleporter::playSound);
                    plugin.getLocales().getLocale("teleporting_action_bar_warmup", Integer.toString(timeLeft))
                            .ifPresent(this::sendStatusMessage);
                } else {
                    plugin.getLocales().getLocale("teleporting_action_bar_processing")
                            .ifPresent(this::sendStatusMessage);
                }

                // Tick (decrement) the timed teleport timer and end it if done
                if (tickWarmup()) {
                    plugin.getCache().currentlyOnWarmup.remove(teleporter.uuid);
                    timedTeleportFuture.complete(null);
                    executor.shutdown();
                }
            }, 0, 1, TimeUnit.SECONDS);
        });
        return timedTeleportFuture;
    }

    /**
     * Ticks a timed teleport, decrementing the time left until the teleport is complete
     * <p>
     * A timed teleport will be cancelled if certain criteria are met:
     * <ul>
     *     <li>The player has left the server</li>
     *     <li>The plugin is disabling</li>
     *     <li>The player has moved beyond the movement threshold from when the warmup started</li>
     *     <li>The player has taken damage (though they may heal, have status ailments or lose/gain hunger)</li>
     * </ul>
     *
     * @return {@code true} if the warmup is complete, {@code false} otherwise
     */
    private boolean tickWarmup() {
        if (timeLeft <= 0) {
            return true;
        }

        // Cancel the timed teleport if the player takes damage
        if (hasTeleporterTakenDamage()) {
            plugin.getLocales().getLocale("teleporting_cancelled_damage")
                    .ifPresent(teleporter::sendMessage);
            plugin.getLocales().getLocale("teleporting_action_bar_cancelled")
                    .ifPresent(this::sendStatusMessage);
            plugin.getSettings().getSoundEffect(Settings.SoundEffectAction.TELEPORTATION_CANCELLED)
                    .ifPresent(teleporter::playSound);
            cancelled = true;
            return true;
        }

        // Cancel the timed teleport if the player moves
        if (hasTeleporterMoved()) {
            plugin.getLocales().getLocale("teleporting_cancelled_movement")
                    .ifPresent(teleporter::sendMessage);
            plugin.getLocales().getLocale("teleporting_action_bar_cancelled")
                    .ifPresent(this::sendStatusMessage);
            plugin.getSettings().getSoundEffect(Settings.SoundEffectAction.TELEPORTATION_CANCELLED)
                    .ifPresent(teleporter::playSound);
            cancelled = true;
            return true;
        }

        // Decrement the countdown timer
        timeLeft--;
        return false;
    }

    /**
     * Send a teleport warmup status message to the configured slot
     *
     * @param message the message to send
     */
    private void sendStatusMessage(@NotNull MineDown message) {
        switch (plugin.getSettings().teleportWarmupDisplay) {
            case ACTION_BAR -> teleporter.sendActionBar(message);
            case SUBTITLE -> teleporter.sendTitle(message, true);
            case TITLE -> teleporter.sendTitle(message, false);
            case MESSAGE -> teleporter.sendMessage(message);
        }
    }

    /**
     * Returns if the player has moved since the timed teleport started
     *
     * @return {@code true} if the player has moved; {@code false} otherwise
     */
    private boolean hasTeleporterMoved() {
        final double maxMovementDistance = 0.1d;
        double movementDistance = Math.abs(startLocation.x - teleporter.getPosition().x) +
                                  Math.abs(startLocation.y - teleporter.getPosition().y) +
                                  Math.abs(startLocation.z - teleporter.getPosition().z);
        return movementDistance > maxMovementDistance;
    }

    /**
     * Returns if the player has taken damage since the timed teleport started
     *
     * @return {@code true} if the player has taken damage
     */
    private boolean hasTeleporterTakenDamage() {
        return teleporter.getHealth() < startHealth;
    }

}
