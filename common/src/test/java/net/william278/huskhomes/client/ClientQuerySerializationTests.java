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

package net.william278.huskhomes.client;

import com.google.gson.Gson;
import net.william278.huskhomes.network.Payload;
import net.william278.huskhomes.position.*;
import net.william278.huskhomes.user.User;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@DisplayName("Client Query Serialization Tests")
public class ClientQuerySerializationTests {

    private final static List<ClientQuery> TEST_QUERIES = List.of(
            ClientQuery.builder()
                    .type(ClientQuery.Type.GET_PRIVATE_HOMES)
                    .payload(Payload.withHomeList(
                            List.of(
                                    Home.from(
                                            Position.at(10, 20, 30, World.from("TestWorld", UUID.randomUUID()), "TestServer"),
                                            PositionMeta.create("TestHome", "This is a test description!"),
                                            User.of(UUID.randomUUID(), "TestUser")
                                    ),
                                    Home.from(
                                            Position.at(10, 20, 30, World.from("TestWorld", UUID.randomUUID()), "TestServer"),
                                            PositionMeta.create("TestHome2", "This is a test description!"),
                                            User.of(UUID.randomUUID(), "TestUser")
                                    ),
                                    Home.from(
                                            Position.at(10, 20, 30, World.from("TestWorld", UUID.randomUUID()), "TestServer"),
                                            PositionMeta.create("TestHome3", "This is a test description!"),
                                            User.of(UUID.randomUUID(), "TestUser")
                                    )
                            )
                    ))
                    .build(),
            ClientQuery.builder()
                    .type(ClientQuery.Type.GET_WARPS)
                    .payload(Payload.withWarpList(
                            List.of(
                                    Warp.from(
                                            Position.at(10, 20, 30, World.from("TestWorld", UUID.randomUUID()), "TestServer"),
                                            PositionMeta.create("TestWarp", "This is a test description!")
                                    ),
                                    Warp.from(
                                            Position.at(10, 20, 30, World.from("TestWorld", UUID.randomUUID()), "TestServer"),
                                            PositionMeta.create("TestWarp2", "This is a test description!")
                                    ),
                                    Warp.from(
                                            Position.at(10, 20, 30, World.from("TestWorld", UUID.randomUUID()), "TestServer"),
                                            PositionMeta.create("TestWarp3", "This is a test description!")
                                    )
                            )
                    ))
                    .build()
    );

    // Parameterized test for Message serialization
    @DisplayName("Test Query Serialization/Deserialization")
    @ParameterizedTest(name = "{1} Client Query")
    @MethodSource("provideQueries")
    public void testQuerySerialization(@NotNull ClientQuery message, @SuppressWarnings("unused") String ignored) {
        final Gson gson = new Gson();
        final String serializedMessage = gson.toJson(message);
        Assertions.assertNotNull(serializedMessage);
        final ClientQuery deserializedMessage = gson.fromJson(serializedMessage, ClientQuery.class);
        Assertions.assertNotNull(deserializedMessage);
        Assertions.assertEquals(message.getType(), deserializedMessage.getType());

        Assertions.assertEquals(message.getPayload().getHomeList().isPresent(), deserializedMessage.getPayload().getHomeList().isPresent());
        Assertions.assertTrue(message.getPayload().getHomeList()
                .map(homes -> homes.size() == deserializedMessage.getPayload().getHomeList().get().size())
                .orElse(true));

        Assertions.assertEquals(message.getPayload().getWarpList().isPresent(), deserializedMessage.getPayload().getWarpList().isPresent());
        Assertions.assertTrue(message.getPayload().getWarpList()
                .map(warps -> warps.size() == deserializedMessage.getPayload().getWarpList().get().size())
                .orElse(true));

    }

    private static Stream<Arguments> provideQueries() {
        return TEST_QUERIES.stream().map(message -> Arguments.of(message, message.getType().name()));
    }

}