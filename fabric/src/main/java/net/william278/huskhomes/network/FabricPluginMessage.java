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

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

@Getter
@AllArgsConstructor
public class FabricPluginMessage implements CustomPayload {

    public static final PacketCodec<RegistryByteBuf, FabricPluginMessage> CODEC = PacketCodec.of(
            (value, buf) -> writeBytes(buf, value.getData()),
            FabricPluginMessage::new
    );
    public static final Id<FabricPluginMessage> CHANNEL_ID = new Id<>(
            Identifier.of("bungeecord", "main")
    );

    private byte[] data;

    private FabricPluginMessage(@NotNull PacketByteBuf buf) {
        this(getWrittenBytes(buf));
    }

    private static byte[] getWrittenBytes(@NotNull PacketByteBuf buf) {
        byte[] bs = new byte[buf.readableBytes()];
        buf.readBytes(bs);
        return bs;
    }

    private static void writeBytes(@NotNull PacketByteBuf buf, byte[] v) {
        buf.writeBytes(v);
    }

    @Override
    public Id<FabricPluginMessage> getId() {
        return CHANNEL_ID;
    }

}
