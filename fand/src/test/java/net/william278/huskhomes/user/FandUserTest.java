/*
 * This file is part of HuskHomes, licensed under the Apache License 2.0.
 *
 *  Copyright (c) William278 <will27528@gmail.com>
 *  Copyright (c) contributors
 */

package net.william278.huskhomes.user;

import io.fand.api.world.Vector3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FandUserTest {

    @Test
    void ignoresStationaryGroundVelocity() {
        assertFalse(FandUser.isMoving(Vector3.ZERO));
        assertFalse(FandUser.isMoving(new Vector3(0.0D, -0.0784D, 0.0D)));
        assertFalse(FandUser.isMoving(new Vector3(0.099D, 0.0D, 0.0D)));
    }

    @Test
    void detectsVelocityAtMovementThreshold() {
        assertTrue(FandUser.isMoving(new Vector3(0.1D, 0.0D, 0.0D)));
        assertTrue(FandUser.isMoving(new Vector3(0.08D, 0.06D, 0.0D)));
    }
}
