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

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.hook.EconomyHook;
import net.william278.huskhomes.user.OnlineUser;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TeleportBuilder {
    private final HuskHomes plugin;
    private OnlineUser executor;
    private Teleportable teleporter;
    private Target target;
    private boolean updateLastPosition = true;
    private Teleport.Type type = Teleport.Type.TELEPORT;
    private List<EconomyHook.Action> economyActions = List.of();

    protected TeleportBuilder(@NotNull HuskHomes plugin) {
        this.plugin = plugin;
    }

    @NotNull
    public Teleport toTeleport() throws IllegalStateException {
        validateTeleport();
        return new Teleport(executor, teleporter, target, type, updateLastPosition, economyActions, plugin);
    }

    @NotNull
    public TimedTeleport toTimedTeleport() throws TeleportationException, IllegalStateException {
        validateTeleport();
        if (!(teleporter instanceof OnlineUser onlineTeleporter)) {
            throw new IllegalStateException("Teleporter must be an OnlineUser for timed teleportation");
        }
        return new TimedTeleport(executor, onlineTeleporter, target, type,
                plugin.getSettings().getTeleportWarmupTime(), updateLastPosition, economyActions, plugin);
    }

    private void validateTeleport() throws TeleportationException {
        if (teleporter == null) {
            throw new TeleportationException(TeleportationException.Type.TELEPORTER_NOT_FOUND);
        }
        if (executor == null) {
            if (teleporter instanceof OnlineUser onlineUser) {
                executor = onlineUser;
            } else {
                executor = ((Username) teleporter).findLocally(plugin)
                        .orElseThrow(() -> new TeleportationException(TeleportationException.Type.TELEPORTER_NOT_FOUND));
            }
        }
        if (target == null) {
            throw new TeleportationException(TeleportationException.Type.TARGET_NOT_FOUND);
        }
    }

    @NotNull
    public TeleportBuilder executor(@NotNull OnlineUser executor) {
        this.executor = executor;
        return this;
    }

    @NotNull
    public TeleportBuilder teleporter(@NotNull Teleportable teleporter) {
        this.teleporter = teleporter;
        return this;
    }

    @NotNull
    public TeleportBuilder teleporter(@NotNull String teleporter) {
        this.teleporter = Teleportable.username(teleporter);
        return this;
    }

    @NotNull
    public TeleportBuilder target(@NotNull Target target) {
        this.target = target;
        return this;
    }

    @NotNull
    public TeleportBuilder target(@NotNull String target) {
        this.target = Target.username(target);
        return this;
    }

    @NotNull
    public TeleportBuilder updateLastPosition(boolean updateLastPosition) {
        this.updateLastPosition = updateLastPosition;
        return this;
    }

    @NotNull
    public TeleportBuilder economyActions(@NotNull EconomyHook.Action... economyActions) {
        this.economyActions = List.of(economyActions);
        return this;
    }

    @NotNull
    public TeleportBuilder type(@NotNull Teleport.Type type) {
        this.type = type;
        return this;
    }
}
