/*
 * This file is part of HuskHomes, licensed under the Apache License 2.0.
 *
 *  Copyright (c) William278 <will27528@gmail.com>
 *  Copyright (c) contributors
 */

package net.william278.huskhomes;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class FandHuskHomesTest {

    @Test
    void pluginEntrypointLinksAndHasNoArgConstructor() {
        final Object plugin = assertDoesNotThrow(() ->
                Class.forName("net.william278.huskhomes.FandHuskHomes").getConstructor().newInstance()
        );
        assertInstanceOf(FandHuskHomes.class, plugin);
    }
}
