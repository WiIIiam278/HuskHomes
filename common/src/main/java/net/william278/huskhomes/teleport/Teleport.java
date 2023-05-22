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

import net.william278.desertwell.util.ThrowingConsumer;
import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.command.BackCommand;
import net.william278.huskhomes.config.Settings;
import net.william278.huskhomes.event.ITeleportEvent;
import net.william278.huskhomes.hook.EconomyHook;
import net.william278.huskhomes.network.Message;
import net.william278.huskhomes.network.Payload;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.user.OnlineUser;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Represents the process of a {@link Teleportable} being teleported to a {@link Target}.
 *
 * @see Teleport#builder(HuskHomes)
 */
public class Teleport {

    protected final HuskHomes plugin;
    protected final OnlineUser executor;
    protected final Teleportable teleporter;
    protected final Target target;
    protected final Type type;
    protected final List<EconomyHook.Action> economyActions;
    private final boolean async;
    protected final boolean updateLastPosition;

    protected Teleport(@NotNull OnlineUser executor, @NotNull Teleportable teleporter, @NotNull Target target,
                       @NotNull Type type, boolean updateLastPosition, @NotNull List<EconomyHook.Action> actions,
                       @NotNull HuskHomes plugin) {
        this.plugin = plugin;
        this.executor = executor;
        this.teleporter = teleporter;
        this.target = target;
        this.type = type;
        this.economyActions = actions;
        this.async = plugin.getSettings().doAsynchronousTeleports();
        this.updateLastPosition = updateLastPosition && plugin.getCommand(BackCommand.class)
                .map(command -> executor.hasPermission(command.getPermission()))
                .orElse(false);
    }

    @NotNull
    public static TeleportBuilder builder(@NotNull HuskHomes plugin) {
        return new TeleportBuilder(plugin);
    }

    public void execute() throws TeleportationException {
        final Optional<OnlineUser> localTeleporter = resolveLocalTeleporter();

        // Validate economy actions
        validateEconomyActions();

        // Teleport a user on another server
        if (localTeleporter.isEmpty()) {
            final Username teleporter = (Username) this.teleporter;
            if (!plugin.getSettings().doCrossServer()) {
                throw new TeleportationException(TeleportationException.Type.TELEPORTER_NOT_FOUND);
            }

            fireEvent((event) -> {
                executeEconomyActions();
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
            return;
        }

        // Teleport a local user
        final OnlineUser teleporter = localTeleporter.get();
        if (target instanceof Username username) {
            final Optional<OnlineUser> localTarget = username.name().equals("@s")
                    ? Optional.of(executor) : username.findLocally(plugin);
            if (localTarget.isPresent()) {
                fireEvent((event) -> {
                    executeEconomyActions();
                    if (updateLastPosition) {
                        plugin.getDatabase().setLastPosition(teleporter, teleporter.getPosition());
                    }

                    try {
                        teleporter.teleportLocally(localTarget.get().getPosition(), async);
                    } catch (TeleportationException e) {
                        e.displayMessage(teleporter, plugin);
                        return;
                    }
                    this.displayTeleportingComplete(teleporter);
                });
                return;
            }

            if (plugin.getSettings().doCrossServer()) {
                fireEvent((event) -> {
                    executeEconomyActions();
                    Message.builder()
                            .type(Message.Type.TELEPORT_TO_NETWORKED_POSITION)
                            .target(username.name())
                            .build().send(plugin.getMessenger(), executor);
                });
                return;
            }

            throw new TeleportationException(TeleportationException.Type.TARGET_NOT_FOUND);
        }

        fireEvent((event) -> {
            executeEconomyActions();
            if (updateLastPosition) {
                plugin.getDatabase().setLastPosition(teleporter, teleporter.getPosition());
            }

            final Position target = (Position) this.target;
            if (!plugin.getSettings().doCrossServer() || target.getServer().equals(plugin.getServerName())) {
                try {
                    teleporter.teleportLocally(target, async);
                } catch (TeleportationException e) {
                    e.displayMessage(teleporter, plugin);
                    return;
                }
                this.displayTeleportingComplete(teleporter);
                return;
            }

            plugin.getDatabase().setCurrentTeleport(teleporter, this);
            plugin.getMessenger().changeServer(teleporter, target.getServer());
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
        plugin.getSettings().getSoundEffect(Settings.SoundEffectAction.TELEPORTATION_COMPLETE)
                .ifPresent(teleporter::playSound);
    }

    // Fire the teleport event
    private void fireEvent(@NotNull ThrowingConsumer<ITeleportEvent> afterFired) {
        plugin.fireEvent(plugin.getTeleportEvent(this), afterFired);
    }

    // Check economy actions
    protected void validateEconomyActions() throws TeleportationException {
        if (economyActions.stream()
                .map(action -> plugin.canPerformTransaction(executor, action))
                .anyMatch(result -> !result)) {
            throw new TeleportationException(TeleportationException.Type.ECONOMY_ACTION_FAILED);
        }
    }

    // Perform transactions on economy actions
    private void executeEconomyActions() {
        economyActions.forEach(action -> plugin.performTransaction(executor, action));
    }

    @NotNull
    public Type getType() {
        return type;
    }

    @NotNull
    public Teleportable getTeleporter() {
        return teleporter;
    }

    @NotNull
    public Target getTarget() {
        return target;
    }

    /**
     * Represents the type of teleport being used
     */
    public enum Type {
        TELEPORT(0),
        RESPAWN(1),
        BACK(2);

        private final int typeId;

        Type(final int typeId) {
            this.typeId = typeId;
        }

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

        public int getTypeId() {
            return typeId;
        }
    }
}