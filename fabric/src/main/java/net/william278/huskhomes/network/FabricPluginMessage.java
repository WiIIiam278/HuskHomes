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

import lombok.*;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
public class FabricPluginMessage implements CustomPayload {

    public static final Id<FabricPluginMessage> BUNGEE_CHANNEL_ID = new Id<>(
            Identifier.of("bungeecord", "main")
    );

    private Id<FabricPluginMessage> id;
    private byte[] data;

    @NotNull
    public static FabricPluginMessage of(@NotNull String id, byte[] data) {
        return new FabricPluginMessage(
                id.equals("BungeeCord") ? BUNGEE_CHANNEL_ID : new Id<>(Identifier.tryParse(id)), data
        );
    }

}
