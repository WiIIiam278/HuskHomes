/*
 * This file is part of HuskHomes, licensed under the Apache License 2.0.
 *
 *  Copyright (c) William278 <will27528@gmail.com>
 *  Copyright (c) contributors
 */

package net.william278.huskhomes.event;

import net.kyori.adventure.audience.Audience;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.position.World;
import net.william278.huskhomes.user.CommandUser;
import net.william278.huskhomes.user.User;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FandEventsTest {

    @Test
    void homeCreatePayloadRemainsMutableAndCancellableThroughEnvelope() {
        final User owner = User.of(UUID.randomUUID(), "Owner");
        final CommandUser creator = new TestCommandUser();
        final Position original = Position.at(1, 2, 3, World.from("overworld"), "server");
        final Position updated = Position.at(4, 5, 6, World.from("overworld"), "server");
        final IHomeCreateEvent payload = FandEvents.homeCreate(owner, "home", original, creator);
        final FandHuskHomesEvent event = new FandHuskHomesEvent(payload);

        assertSame(payload, event.event());
        assertFalse(payload.isCancelled());

        payload.setName("updated");
        payload.setPosition(updated);
        payload.setCancelled(true);

        assertEquals("updated", payload.getName());
        assertSame(updated, payload.getPosition());
        assertTrue(payload.isCancelled());
    }

    private static final class TestCommandUser implements CommandUser {
        @Override
        public @NotNull Audience getAudience() {
            return Audience.empty();
        }

        @Override
        public boolean isPermissionSet(@NotNull String permission) {
            return true;
        }

        @Override
        public boolean hasPermission(@NotNull String permission) {
            return true;
        }
    }
}
