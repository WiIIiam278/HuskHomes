package net.william278.huskhomes.teleport;

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
import java.util.function.Consumer;

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
            if (!plugin.getSettings().isCrossServer()) {
                throw new TeleportationException(TeleportationException.Type.TELEPORTER_NOT_FOUND);
            }

            fireEvent((event) -> {
                executeEconomyActions();
                if (target instanceof Username username) {
                    Message.builder()
                            .type(Message.Type.TELEPORT_TO_NETWORKED_USER)
                            .target(username.name())
                            .payload(Payload.withString(teleporter.name()))
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

                    teleporter.teleportLocally(localTarget.get().getPosition(), async);
                    this.displayTeleportingComplete(teleporter);
                });
                return;
            }

            if (plugin.getSettings().isCrossServer()) {
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
            if (!plugin.getSettings().isCrossServer() || target.getServer().equals(plugin.getServerName())) {
                teleporter.teleportLocally(target, async);
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
        plugin.getSettings().getSoundEffect(Settings.SoundEffectAction.TELEPORTATION_CANCELLED)
                .ifPresent(teleporter::playSound);
    }

    // Fire the teleport event
    private void fireEvent(@NotNull Consumer<ITeleportEvent> afterFired) {
        plugin.fireEvent(plugin.getTeleportEvent(this), afterFired);
    }

    // Check economy actions
    protected void validateEconomyActions() throws TeleportationException {
        if (economyActions.stream()
                .map(action -> plugin.validateEconomyCheck(executor, action))
                .anyMatch(result -> !result)) {
            throw new TeleportationException(TeleportationException.Type.ECONOMY_ACTION_FAILED);
        }
    }

    // Perform transactions on economy actions
    private void executeEconomyActions() {
        economyActions.forEach(action -> plugin.performEconomyTransaction(executor, action));
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
        RESPAWN(1);

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