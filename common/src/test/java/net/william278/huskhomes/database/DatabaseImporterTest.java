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

package net.william278.huskhomes.database;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.position.Home;
import net.william278.huskhomes.position.Warp;
import net.william278.huskhomes.user.SavedUser;
import net.william278.huskhomes.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the DatabaseImporter class.
 */
class DatabaseImporterTest {

    @Mock
    private HuskHomes plugin;

    @Mock
    private Database sourceDatabase;

    @Mock
    private Database targetDatabase;

    private DatabaseImporter importer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        importer = new DatabaseImporter(plugin, sourceDatabase, targetDatabase);
    }

    @Test
    void testImportAllData_Success() {
        // Arrange
        List<Home> mockHomes = createMockHomes();
        List<Warp> mockWarps = createMockWarps();

        when(sourceDatabase.getPublicHomes()).thenReturn(mockHomes);
        when(sourceDatabase.getWarps()).thenReturn(mockWarps);
        when(sourceDatabase.getUser(any(UUID.class))).thenReturn(Optional.of(createMockSavedUser()));
        when(sourceDatabase.getHomes(any(User.class))).thenReturn(mockHomes);
        when(sourceDatabase.getLastPosition(any(User.class))).thenReturn(Optional.empty());
        when(sourceDatabase.getOfflinePosition(any(User.class))).thenReturn(Optional.empty());
        when(sourceDatabase.getRespawnPosition(any(User.class))).thenReturn(Optional.empty());
        when(sourceDatabase.getCooldown(any(), any(User.class))).thenReturn(Optional.empty());

        // Act
        CompletableFuture<DatabaseImporter.ImportResult> future = importer.importAllData();
        DatabaseImporter.ImportResult result = future.join();

        // Assert
        assertTrue(result.success);
        assertEquals(1, result.usersImported);
        assertEquals(2, result.homesImported);
        assertEquals(2, result.warpsImported);
        assertEquals(0, result.positionsImported);
        assertEquals(0, result.cooldownsImported);

        // Verify interactions
        verify(targetDatabase, times(1)).ensureUser(any(User.class));
        verify(targetDatabase, times(1)).updateUserData(any(SavedUser.class));
        verify(targetDatabase, times(2)).saveHome(any(Home.class));
        verify(targetDatabase, times(2)).saveWarp(any(Warp.class));
    }

    @Test
    void testImportAllData_Exception() {
        // Arrange
        when(sourceDatabase.getPublicHomes()).thenThrow(new RuntimeException("Database error"));

        // Act
        CompletableFuture<DatabaseImporter.ImportResult> future = importer.importAllData();
        DatabaseImporter.ImportResult result = future.join();

        // Assert
        assertFalse(result.success);
        assertEquals("Database error", result.errorMessage);
        assertEquals(0, result.getTotalImported());
    }

    @Test
    void testImportResult_ToString() {
        // Arrange
        DatabaseImporter.ImportResult result = new DatabaseImporter.ImportResult();
        result.success = true;
        result.usersImported = 5;
        result.homesImported = 10;
        result.warpsImported = 3;
        result.positionsImported = 8;
        result.cooldownsImported = 2;

        // Act
        String resultString = result.toString();

        // Assert
        assertTrue(resultString.contains("Import successful!"));
        assertTrue(resultString.contains("Users: 5"));
        assertTrue(resultString.contains("Homes: 10"));
        assertTrue(resultString.contains("Warps: 3"));
        assertTrue(resultString.contains("Positions: 8"));
        assertTrue(resultString.contains("Cooldowns: 2"));
        assertTrue(resultString.contains("Total: 28"));
    }

    @Test
    void testImportResult_ToString_Failed() {
        // Arrange
        DatabaseImporter.ImportResult result = new DatabaseImporter.ImportResult();
        result.success = false;
        result.errorMessage = "Connection failed";

        // Act
        String resultString = result.toString();

        // Assert
        assertTrue(resultString.contains("Import failed: Connection failed"));
    }

    @Test
    void testImportResult_GetTotalImported() {
        // Arrange
        DatabaseImporter.ImportResult result = new DatabaseImporter.ImportResult();
        result.usersImported = 1;
        result.homesImported = 2;
        result.warpsImported = 3;
        result.positionsImported = 4;
        result.cooldownsImported = 5;

        // Act & Assert
        assertEquals(15, result.getTotalImported());
    }

    private List<Home> createMockHomes() {
        List<Home> homes = new ArrayList<>();
        User mockUser = User.of(UUID.randomUUID(), "TestUser");

        // Create mock homes (simplified - in real tests you'd use proper Home.Builder)
        Home home1 = mock(Home.class);
        when(home1.getOwner()).thenReturn(mockUser);
        when(home1.isPublic()).thenReturn(true);

        Home home2 = mock(Home.class);
        when(home2.getOwner()).thenReturn(mockUser);
        when(home2.isPublic()).thenReturn(false);

        homes.add(home1);
        homes.add(home2);
        return homes;
    }

    private List<Warp> createMockWarps() {
        List<Warp> warps = new ArrayList<>();

        Warp warp1 = mock(Warp.class);
        Warp warp2 = mock(Warp.class);

        warps.add(warp1);
        warps.add(warp2);
        return warps;
    }

    private SavedUser createMockSavedUser() {
        User user = User.of(UUID.randomUUID(), "TestUser");
        return new SavedUser(user, 10, false);
    }
}
