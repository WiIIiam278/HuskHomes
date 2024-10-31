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

package net.william278.huskhomes.network;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.NoArgsConstructor;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.position.World;
import net.william278.huskhomes.teleport.TeleportRequest;
import net.william278.huskhomes.user.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * Represents a payload sent in a cross-server {@link Message}.
 */
@NoArgsConstructor
public class Payload {

    @Nullable
    @Expose
    private String string;
    @Nullable
    @Expose
    private Position position;
    @Nullable
    @Expose
    private World world;
    @Nullable
    @Expose
    private TeleportRequest teleportRequest;
    @Nullable
    @Expose
    @SerializedName("user_list")
    private List<User> userList;

    @NotNull
    public static Payload empty() {
        return new Payload();
    }

    @NotNull
    public static Payload string(@Nullable String target) {
        final Payload payload = new Payload();
        payload.string = target;
        return payload;
    }

    @NotNull
    public static Payload position(@Nullable Position position) {
        final Payload payload = new Payload();
        payload.position = position;
        return payload;
    }

    @NotNull
    public static Payload world(@Nullable World world) {
        final Payload payload = new Payload();
        payload.world = world;
        return payload;
    }

    @NotNull
    public static Payload teleportRequest(@Nullable TeleportRequest teleportRequest) {
        final Payload payload = new Payload();
        payload.teleportRequest = teleportRequest;
        return payload;
    }

    @NotNull
    public static Payload userList(@Nullable List<User> target) {
        final Payload payload = new Payload();
        payload.userList = target;
        return payload;
    }

    public Optional<String> getString() {
        return Optional.ofNullable(string);
    }

    public Optional<Position> getPosition() {
        return Optional.ofNullable(position);
    }

    public Optional<World> getWorld() {
        return Optional.ofNullable(world);
    }

    public Optional<TeleportRequest> getTeleportRequest() {
        return Optional.ofNullable(teleportRequest);
    }

    public Optional<List<User>> getUserList() {
        return Optional.ofNullable(userList);
    }

}
