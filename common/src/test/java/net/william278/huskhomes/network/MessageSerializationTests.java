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

import com.fatboyindustrial.gsonjavatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.position.World;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

@DisplayName("Message Serialization Tests")
public class MessageSerializationTests {

    private static final List<Message> TEST_MESSAGES = List.of(
            Message.builder()
                    .type(Message.MessageType.REQUEST_USER_LIST)
                    .target("TestTarget", Message.TargetType.PLAYER)
                    .payload(Payload.empty())
                    .build(),
            Message.builder()
                    .type(Message.MessageType.TELEPORT_REQUEST)
                    .target("TestTarget", Message.TargetType.PLAYER)
                    .payload(Payload.position(
                            Position.at(63.25, 127.43, -32, 180f, -94.3f,
                                    World.from("TestWorld"), "TestServer")))
                    .build(),
            Message.builder()
                    .type(Message.MessageType.TELEPORT_TO_NETWORKED_USER)
                    .target("TestTarget", Message.TargetType.PLAYER)
                    .payload(Payload.string("TestString"))
                    .build(),
            Message.builder()
                    .type(Message.MessageType.TELEPORT_TO_POSITION)
                    .target("TestTarget", Message.TargetType.PLAYER)
                    .payload(Payload.position(
                            Position.at(63.25, 127.43, -32, 180f, -94.3f,
                                    World.from("TestWorld"), "TestServer")))
                    .build()
    );

    // Parameterized 0-mariadb-add_metadata_table.sql for Message serialization
    @DisplayName("Test Message Serialization/Deserialization")
    @ParameterizedTest(name = "{1} Message")
    @MethodSource("provideMessages")
    public void testMessageSerialization(@NotNull Message message, @SuppressWarnings("unused") String ignored) {
        final Gson gson = createGson();
        final String serializedMessage = gson.toJson(message);
        Assertions.assertNotNull(serializedMessage);
        final Message deserialized = gson.fromJson(serializedMessage, Message.class);
        Assertions.assertNotNull(deserialized);
        Assertions.assertEquals(message.getType(), deserialized.getType());
        Assertions.assertEquals(message.getTargetType(), deserialized.getTargetType());
        Assertions.assertEquals(message.getTarget(), deserialized.getTarget());
        Assertions.assertEquals(
                message.getPayload().getPosition().isPresent(),
                deserialized.getPayload().getPosition().isPresent()
        );
        Assertions.assertEquals(
                message.getPayload().getString().isPresent(),
                deserialized.getPayload().getString().isPresent()
        );
    }

    @NotNull
    private static Gson createGson() {
        return Converters.registerOffsetDateTime(new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
        ).create();
    }

    private static Stream<Arguments> provideMessages() {
        return TEST_MESSAGES.stream().map(message -> Arguments.of(message, message.getType().name()));
    }

}
