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

package net.william278.huskhomes.teleport;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.william278.desertwell.util.ThrowingConsumer;
import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.command.BackCommand;
import net.william278.huskhomes.config.Settings;
import net.william278.huskhomes.event.ITeleportEvent;
import net.william278.huskhomes.network.Message;
import net.william278.huskhomes.network.Payload;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.user.OnlineUser;
import net.william278.huskhomes.util.TransactionResolver;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Represents the process of a {@link Teleportable} being teleported to a {@link Target}.
 *
 * @see Teleport#builder(HuskHomes)
 */
@Getter
public class Teleport implements Completable {

    protected final HuskHomes plugin;
    protected final OnlineUser executor;
    protected final Teleportable teleporter;
    protected final Target target;
    protected final Type type;
    protected final List<TransactionResolver.Action> actions;
    private final boolean async;
    protected final boolean updateLastPosition;

    protected Teleport(@NotNull OnlineUser executor, @NotNull Teleportable teleporter, @NotNull Target target,
                       @NotNull Type type, boolean updateLastPosition,
                       @NotNull List<TransactionResolver.Action> actions, @NotNull HuskHomes plugin) {
        this.plugin = plugin;
        this.executor = executor;
        this.teleporter = teleporter;
        this.target = target;
        this.type = type;
        this.actions = actions;
        this.async = plugin.getSettings().getGeneral().isTeleportAsync();
        this.updateLastPosition = updateLastPosition && plugin.getCommand(BackCommand.class)
                .map(command -> executor.hasPermission(command.getPermission())
                        && executor.hasPermission(command.getPermission("previous")))
                .orElse(false);
    }

    @NotNull
    public static TeleportBuilder builder(@NotNull HuskHomes plugin) {
        return new TeleportBuilder(plugin);
    }

    /**
     * Execute a teleport, throwing a {@link TeleportationException} if it fails.
     *
     * @throws TeleportationException if the teleport fails for some reason.
     */
    public void execute() throws TeleportationException {
        validateTransactions();
        resolveLocalTeleporter().ifPresentOrElse(this::executeLocal, this::executeRemote);
    }

    private void executeLocal(@NotNull OnlineUser teleporter) throws TeleportationException {
        if (target instanceof Username username) {
            final Optional<OnlineUser> localTarget = username.name().equals("@s")
                    ? Optional.of(executor) : username.findLocally(plugin);
            if (localTarget.isPresent()) {
                fireEvent((event) -> {
                    performTransactions();
                    if (updateLastPosition) {
                        plugin.getDatabase().setLastPosition(teleporter, teleporter.getPosition());
                    }
                    teleporter.teleportLocally(localTarget.get().getPosition(), async);
                    this.displayTeleportingComplete(teleporter);
                    teleporter.handleInvulnerability();
                });
                return;
            }

            if (plugin.getSettings().getCrossServer().isEnabled()) {
                fireEvent((event) -> {
                    performTransactions();
                    Message.builder()
                            .type(Message.Type.TELEPORT_TO_NETWORKED_POSITION)
                            .target(username.name())
                            .build().send(plugin.getMessenger(), executor);
                });
                return;
            }

            throw new TeleportationException(TeleportationException.Type.TARGET_NOT_FOUND, plugin);
        }

        fireEvent((event) -> {
            performTransactions();
            if (updateLastPosition) {
                plugin.getDatabase().setLastPosition(teleporter, teleporter.getPosition());
            }

            final Position target = (Position) this.target;
            if (!plugin.getSettings().getCrossServer().isEnabled()
                    || target.getServer().equals(plugin.getServerName())) {
                teleporter.teleportLocally(target, async);
                this.displayTeleportingComplete(teleporter);
                teleporter.handleInvulnerability();
                return;
            }

            plugin.getDatabase().setCurrentTeleport(teleporter, this);
            plugin.getMessenger().changeServer(teleporter, target.getServer());
        });
    }

    private void executeRemote() throws TeleportationException {
        final Username teleporter = (Username) this.teleporter;
        if (!plugin.getSettings().getCrossServer().isEnabled()) {
            throw new TeleportationException(TeleportationException.Type.TELEPORTER_NOT_FOUND, plugin);
        }

        fireEvent((event) -> {
            performTransactions();
            if (target instanceof Username username) {
                Message.builder()
                        .type(Message.Type.TELEPORT_TO_NETWORKED_USER)
                        .target(teleporter.name())
                        .payload(Payload.withString(username.name()))
                        .build().send(plugin.getMessenger(), executor);
                return;
            }

            Message.builder()
                    .type(Message.Type.TELEPORT_TO_POSITION)
                    .target(teleporter.name())
                    .payload(Payload.withPosition((Position) target))
                    .build().send(plugin.getMessenger(), executor);
        });
    }

    @NotNull
    private Optional<OnlineUser> resolveLocalTeleporter() throws TeleportationException {
        if (this.teleporter instanceof Username username) {
            return username.findLocally(plugin);
        }
        return Optional.of((OnlineUser) this.teleporter);
    }

    public void displayTeleportingComplete(@NotNull OnlineUser teleporter) {
        plugin.getLocales().getLocale("teleporting_complete")
                .ifPresent(teleporter::sendMessage);
        plugin.getSettings().getGeneral().getSoundEffects().get(Settings.SoundEffectAction.TELEPORTATION_COMPLETE)
                .ifPresent(teleporter::playSound);
    }

    // Fire the teleport event
    private void fireEvent(@NotNull ThrowingConsumer<ITeleportEvent> afterFired) {
        plugin.fireEvent(plugin.getTeleportEvent(this), afterFired);
    }

    // Check economy actions
    protected void validateTransactions() throws TeleportationException {
        if (actions.stream()
                .map(action -> plugin.validateTransaction(executor, action))
                .anyMatch(result -> !result)) {
            throw new TeleportationException(TeleportationException.Type.TRANSACTION_FAILED, plugin);
        }
    }

    // Perform economy and cooldown transactions
    private void performTransactions() {
        actions.forEach(action -> plugin.performTransaction(executor, action));
    }

    /**
     * Represents the type of teleport being used.
     */
    @Getter
    @AllArgsConstructor
    public enum Type {
        TELEPORT(0),
        RESPAWN(1),
        BACK(2),
        RANDOM_TELEPORT(3);

        private final int typeId;

        /**
         * Returns a {@link Type} by its type id.
         *
         * @param typeId The type id of the {@link Type} to return.
         * @return The {@link Type} of the given type id.
         */
        public static Optional<Type> getTeleportType(int typeId) {
            return Arrays.stream(values())
                    .filter(type -> type.getTypeId() == typeId)
                    .findFirst();
        }

    }
}