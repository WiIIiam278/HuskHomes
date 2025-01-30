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

package net.william278.huskhomes.position;

import net.william278.huskhomes.user.User;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

@DisplayName("Saved Position Tests")
public class SavedPositionTests {

    // Map of positions - true if the position contains unsafe characters, false if not
    private static final Map<String, Boolean> POSITION_UNSAFE_NAMES = Map.of(
            "TestPosition", false,
            "ExamplePos", false,
            "SafeName", false,
            "[Unsafe]", true,
            "Unsafe]", true,
            "[Name](bad)", true,
            "Unsafe[Name]", true,
            "不安全(不安全)", true
    );

    @DisplayName("Test Home Identifiers")
    @ParameterizedTest(name = "Home: \"{1}\" (Unsafe: {2})")
    @MethodSource("provideHomeData")
    @SuppressWarnings("unused")
    public void testHomeIdentifiers(@NotNull Home home, @NotNull String name, boolean isUnsafeName) {
        Assertions.assertEquals(
                home.getIdentifier(),
                home.getOwner().getName() + Home.IDENTIFIER_DELIMITER + home.getName()
        );
        Assertions.assertEquals(
                home.getSafeIdentifier(),
                isUnsafeName
                        ? home.getOwner().getName() + Home.IDENTIFIER_DELIMITER + home.getUuid()
                        : home.getIdentifier()
        );
    }

    @DisplayName("Test Warp Identifiers")
    @ParameterizedTest(name = "Warp: \"{1}\" (Unsafe: {2})")
    @MethodSource("provideWarpData")
    @SuppressWarnings("unused")
    public void testWarpIdentifiers(@NotNull Warp warp, @NotNull String name, boolean isUnsafeName) {
        Assertions.assertEquals(
                warp.getIdentifier(),
                warp.getName()
        );
        Assertions.assertEquals(
                warp.getSafeIdentifier(),
                isUnsafeName ? warp.getUuid().toString() : warp.getIdentifier()
        );
    }

    @NotNull
    private static Stream<Arguments> provideWarpData() {
        final Position position = Position.at(63.25, 127.43, -32, 180f, -94.3f,
                World.from("TestWorld"), "TestServer");
        return POSITION_UNSAFE_NAMES.entrySet().stream()
                .map(entry -> Arguments.of(
                        Warp.from(position, PositionMeta.create(entry.getKey(), "")),
                        entry.getKey(),
                        entry.getValue()
                ));
    }

    @NotNull
    private static Stream<Arguments> provideHomeData() {
        final Position position = Position.at(63.25, 127.43, -32, 180f, -94.3f,
                World.from("TestWorld"), "TestServer");
        return POSITION_UNSAFE_NAMES.entrySet().stream()
                .map(entry -> Arguments.of(
                        Home.from(
                                position,
                                PositionMeta.create(entry.getKey(), ""),
                                User.of(UUID.randomUUID(), "TestUser")
                        ),
                        entry.getKey(),
                        entry.getValue()));
    }


}
