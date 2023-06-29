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

package net.william278.huskhomes.util;

import net.kyori.adventure.key.Key;
import net.william278.annotaml.YamlFile;
import net.william278.annotaml.YamlKey;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@YamlFile(header = """
        ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
        ┃      Unsafe /rtp Blocks      ┃
        ┃    Developed by William278   ┃
        ┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛""")
public class UnsafeBlocks {

    @YamlKey("unsafe_blocks")
    public List<String> unsafeBlocks;

    @YamlKey("safe_occupation_blocks")
    public List<String> safeOccupationBlocks;

    private String formatBlockId(@NotNull String blockId) {
        if (!blockId.startsWith(Key.MINECRAFT_NAMESPACE + ":")) {
            blockId = Key.MINECRAFT_NAMESPACE + ":" + blockId;
        }
        return blockId;
    }

    /**
     * Returns if the block, by provided identifier, is unsafe
     *
     * @param blockId The block identifier (e.g. {@code minecraft:stone})
     * @return {@code true} if the block is on the unsafe blocks list, {@code false} otherwise
     */
    public boolean isUnsafe(@NotNull String blockId) {
        blockId = this.formatBlockId(blockId);
        return unsafeBlocks.contains(blockId);
    }

    /**
     * Returns if the block, by provided identifier, is unsafe to stand in
     *
     * @param blockId The block identifier (e.g. {@code minecraft:stone})
     * @return {@code true} if the block is on the unsafe blocks list, {@code false} otherwise
     */
    public boolean isUnsafeToOccupy(@NotNull String blockId) {
        blockId = this.formatBlockId(blockId);
        return !safeOccupationBlocks.contains(blockId);
    }

    @SuppressWarnings("unused")
    public UnsafeBlocks() {
    }

}
