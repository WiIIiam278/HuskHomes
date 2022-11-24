package net.william278.huskhomes.util;

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

    /**
     * Returns if the block, by provided identifier, is unsafe
     *
     * @param blockId The block identifier (e.g. {@code minecraft:stone})
     * @return {@code true} if the block is on the unsafe blocks list, {@code false} otherwise
     */
    public boolean isUnsafe(@NotNull String blockId) {
        if (!blockId.startsWith("minecraft:")) {
            blockId = "minecraft:" + blockId;
        }
        return unsafeBlocks.contains(blockId);
    }

    @SuppressWarnings("unused")
    public UnsafeBlocks() {
    }

}
