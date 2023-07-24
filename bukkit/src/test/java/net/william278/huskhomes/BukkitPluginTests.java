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

package net.william278.huskhomes;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import de.themoep.minedown.adventure.MineDown;
import net.william278.huskhomes.command.BukkitCommand;
import net.william278.huskhomes.command.Command;
import net.william278.huskhomes.position.*;
import net.william278.huskhomes.user.BukkitUser;
import net.william278.huskhomes.user.ConsoleUser;
import net.william278.huskhomes.user.OnlineUser;
import net.william278.huskhomes.user.User;
import net.william278.huskhomes.util.BukkitAdapter;
import net.william278.huskhomes.util.TransactionResolver;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Uses MockBukkit to test the plugin on a mock Paper server implementing the Bukkit 1.16 API.
 */
@DisplayName("Bukkit Plugin Tests (1.16.5)")
public class BukkitPluginTests {

    private static ServerMock server;
    private static BukkitHuskHomes plugin;

    @BeforeAll
    @DisplayName("Test Plugin Setup")
    public static void setUpPlugin() {
        server = MockBukkit.mock();
        server.addSimpleWorld("world");
        plugin = MockBukkit.load(BukkitHuskHomes.class);
    }

    @Nested
    @DisplayName("Plugin Initialization Tests")
    public class ValidationTests {

        @Test
        @DisplayName("Test Plugin Enables")
        public void testPluginEnables() {
            Assertions.assertTrue(plugin.isEnabled());
        }

        @Test
        @DisplayName("Test Command Registration")
        public void testCommandRegistration() {
            for (BukkitCommand.Type command : BukkitCommand.Type.values()) {
                Assertions.assertNotNull(plugin.getCommand(command.createCommand(plugin).getName()));
            }
            Assertions.assertEquals(BukkitCommand.Type.values().length, plugin.getCommands().size());
        }

        @Test
        @DisplayName("Test Player Adapter")
        public void testPlayerAdaption() {
            PlayerMock player = server.addPlayer();
            Assertions.assertNotNull(BukkitUser.adapt(player, plugin));
        }

        @Test
        @DisplayName("Config Reload & Read Test")
        public void testConfigReload() {
            plugin.reloadConfig();
            Assertions.assertNotNull(plugin.getConfig());
        }

    }

    @Nested
    @DisplayName("Locale Formatting Tests")
    public class LocaleFormattingTests {

        @Test
        @DisplayName("Test Message Formatting")
        public void testMessageFormatting() {
            PlayerMock player = server.addPlayer();

            final MineDown simpleLocale = plugin.getLocales()
                    .getLocale("error_in_game_only")
                    .orElseThrow(() -> new IllegalStateException("Failed to load locale"));
            final String simpleLocaleText = plugin.getLocales().getRawLocale("error_in_game_only")
                    .orElseThrow(() -> new IllegalStateException("Failed to load raw locale"));
            BukkitUser.adapt(player, plugin).sendMessage(simpleLocale);
            player.assertSaid(simpleLocaleText);
        }

        @Test
        @DisplayName("Test Message Dispatching")
        public void testMessageDispatching() {
            final BukkitUser user = BukkitUser.adapt(server.addPlayer(), plugin);

            final MineDown locale = plugin.getLocales()
                    .getLocale("teleporting_action_bar_warmup", Integer.toString(3))
                    .orElseThrow(() -> new IllegalStateException("Failed to load locale"));
            user.sendActionBar(locale);
            user.sendMessage(locale);
            user.sendTitle(locale, false);
            user.sendTitle(locale, true);
        }

        @Test
        @DisplayName("Test Locale Loading")
        public void testLocalesLoading() {
            final Map<String, String> rawLocales = plugin.getLocales().rawLocales;
            Assertions.assertTrue(rawLocales.size() > 0);
            rawLocales.forEach((key, value) -> Assertions.assertNotNull(value));
        }

        @Test
        @DisplayName("Test Locale Parsing")
        public void testLocaleParsing() {
            final Map<String, String> rawLocales = plugin.getLocales().rawLocales;
            BukkitUser user = BukkitUser.adapt(server.addPlayer(), plugin);
            rawLocales.forEach((key, value) -> {
                Optional<MineDown> locale = plugin.getLocales().getLocale(key);
                Assertions.assertTrue(locale.isPresent());
                user.sendMessage(locale.get());
            });
        }

    }

    @Nested
    @DisplayName("Command Execution Tests")
    public class CommandExecutionTests {

        @DisplayName("Test User Execution")
        @ParameterizedTest(name = "/{2} Command")
        @MethodSource("provideUserExecutedCommands")
        public void testUserExecution(@NotNull Command command, @NotNull OnlineUser user,
                                      @NotNull @SuppressWarnings("unused") String commandName) {
            Assertions.assertNotNull(command);
            Assertions.assertNotNull(user);
            command.execute(user, new String[0]);
        }

        @DisplayName("Test Console Execution")
        @ParameterizedTest(name = "/{2} Command")
        @MethodSource("provideConsoleExecutedCommands")
        public void testUserExecution(@NotNull Command command, @NotNull ConsoleUser console,
                                      @NotNull @SuppressWarnings("unused") String commandName) {
            Assertions.assertNotNull(command);
            Assertions.assertNotNull(console);
            command.execute(console, new String[0]);
        }

        public static Stream<Arguments> provideUserExecutedCommands() {
            final List<Command> commands = plugin.getCommands();
            final PlayerMock player = server.addPlayer();
            player.setOp(true);

            final BukkitUser playerUser = BukkitUser.adapt(player, plugin);
            return commands.stream()
                    .flatMap(command -> Stream.of(Arguments.of(command, playerUser, command.getName())));
        }

        public static Stream<Arguments> provideConsoleExecutedCommands() {
            final List<Command> commands = plugin.getCommands();
            final ConsoleUser console = plugin.getConsole();
            return commands.stream()
                    .flatMap(command -> Stream.of(Arguments.of(command, console, command.getName())));
        }

    }

    @Nested
    @DisplayName("Validator Tests")
    public class ValidatorTests {

        @DisplayName("Test Validator Accepts Valid Names")
        @ParameterizedTest(name = "Valid Name: \"{0}\"")
        @ValueSource(strings = {
                "ValidName", "Valid_Name", "Valid-Name", "ValidN4me", "ValidName123", "VN-123", "ValidName_123", "V"
        })
        public void testValidNameIsValid(@NotNull String name) {
            Assertions.assertTrue(plugin.getValidator().isValidName(name));
        }

        @DisplayName("Test Validator Rejects Invalid Names")
        @ParameterizedTest(name = "Invalid Name: \"{0}\"")
        @ValueSource(strings = {
                "Invalid Name", "•♣♠", "Invali♣Name", "Invalid.Name", "", " ", "\0", "InvalidName12345678901234567890"
        })
        public void testInvalidNameIsInvalid(@NotNull String name) {
            Assertions.assertFalse(plugin.getValidator().isValidName(name));
        }

        @DisplayName("Test Validator Accepts Valid Descriptions")
        @ParameterizedTest(name = "Valid Description: \"{0}\"")
        @ValueSource(strings = {
                "Lorem ipsum text", "Lorem ipsum text with special characters •♣♠",
                "Lorem ipsum text with special characters •♣♠ and numbers 1234567890",
                "Lorem ipsum text with special characters •♣♠ and numbers 1234567890 and whitespace",
                "Lorem ipsum text with special characters •♣♠ and numbers 1234567890 and whitespace and a very long"
                        + " description that is 255 characters long and should be accepted by the validator"
        })
        public void testValidDescriptionIsValid(@NotNull String description) {
            Assertions.assertTrue(plugin.getValidator().isValidDescription(description));
        }

        @DisplayName("Test Validator Rejects Invalid Descriptions")
        @ParameterizedTest(name = "Invalid Description: \"{0}\"")
        @ValueSource(strings = {
                "Lorem ipsum text with special characters •♣♠ and numbers 1234567890 and whitespace and"
                        + "a very long description that is more than 256 characters long and should be rejected "
                        + "by the validator because it is far, far too long and thus exceeds the maximum length "
                        + "of 255 characters that are allowed for a description of a home or a warp."
        })
        public void testInvalidDescriptionIsInvalid(@NotNull String description) {
            Assertions.assertFalse(plugin.getValidator().isValidDescription(description));
        }

    }

    @Nested
    @DisplayName("Cooldown Tests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    public class CooldownTests {
        private static User player;
        private static final TransactionResolver.Action ACTION = TransactionResolver.Action.HOME_TELEPORT;
        private static final Duration DURATION = Duration.ofSeconds(100);

        @BeforeAll
        public static void setup() {
            player = BukkitUser.adapt(server.addPlayer(), plugin);
        }

        @DisplayName("Test Applying Cooldown")
        @Order(1)
        @Test
        public void testApplyingCooldown() {
            plugin.getDatabase().ensureUser(player);
            Assertions.assertTrue(plugin.getDatabase().getUserData(player.getUuid()).isPresent());

            plugin.getDatabase().setCooldown(ACTION, player, Instant.now().plus(DURATION));
            Assertions.assertTrue(plugin.getDatabase().getCooldown(ACTION, player).isPresent());
        }

        @DisplayName("Test Removing Cooldown")
        @Order(2)
        @Test
        public void testRemovingCooldown() {
            Assertions.assertTrue(plugin.getDatabase().getCooldown(ACTION, player).isPresent());
            plugin.getDatabase().removeCooldown(ACTION, player);
            Assertions.assertFalse(plugin.getDatabase().getCooldown(ACTION, player).isPresent());
        }

        @DisplayName("Test Cooldown Expiration")
        @Order(3)
        @Test
        public void testCooldownExpiration() {
            Assertions.assertTrue(plugin.getDatabase().getCooldown(ACTION, player).isEmpty());
            plugin.getDatabase().setCooldown(ACTION, player, Instant.now().minus(DURATION));

            final Optional<Instant> cooldown = plugin.getDatabase().getCooldown(ACTION, player);
            Assertions.assertTrue(cooldown.isPresent());
            Assertions.assertTrue(cooldown.get().isBefore(Instant.now()));
        }

    }


    @Nested
    @DisplayName("Warp Tests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    public class WarpTests {
        private static final List<String> WARP_NAMES = List.of(
                "warple",
                "example",
                "exmaination",
                "123234__44",
                "a",
                "1"
        );

        @DisplayName("Test Warp Creation")
        @ParameterizedTest(name = "Warp Name: \"{0}\"")
        @MethodSource("provideWarpData")
        @Order(1)
        public void testWarpCreation(@NotNull String name, @NotNull Position position) {
            plugin.getManager().warps().createWarp(name, position);
            Assertions.assertTrue(plugin.getDatabase().getWarp(name).isPresent());
            Assertions.assertTrue(plugin.getManager().warps()
                    .getWarps().stream()
                    .anyMatch(warp -> warp.equals(name)));
        }

        // test warp renaming
        @DisplayName("Test Warp Renaming")
        @ParameterizedTest(name = "Rename: \"{0}\" > \"{0}2\"")
        @MethodSource("provideWarpData")
        @Order(2)
        @SuppressWarnings("unused")
        public void testWarpRenaming(@NotNull String name, @NotNull Position position) {
            final String newName = (name + "2");
            plugin.getManager().warps().setWarpName(name, newName);
            Assertions.assertTrue(plugin.getDatabase().getWarp(newName).isPresent());
            Assertions.assertTrue(plugin.getManager().warps()
                    .getWarps().stream()
                    .anyMatch(warp -> warp.equals(newName)));

            // Rename back to original name
            plugin.getManager().warps().setWarpName(newName, name);
            Assertions.assertTrue(plugin.getDatabase().getWarp(name).isPresent());
            Assertions.assertTrue(plugin.getManager().warps()
                    .getWarps().stream()
                    .anyMatch(warp -> warp.equals(name)));
        }

        @DisplayName("Test Warp Edit Description")
        @ParameterizedTest(name = "Edit Description: \"{0}\"")
        @MethodSource("provideWarpData")
        @Order(3)
        @SuppressWarnings("unused")
        public void testWarpChangingDescription(@NotNull String name, @NotNull Position position) {
            final String description = "This is a test description for " + name + ".";
            plugin.getManager().warps().setWarpDescription(name, description);
            final Optional<String> warpDescription = plugin.getDatabase().getWarp(name)
                    .map(Warp::getMeta)
                    .map(PositionMeta::getDescription);
            Assertions.assertTrue(warpDescription.isPresent());
            Assertions.assertEquals(description, warpDescription.get());
        }

        @DisplayName("Test Warp Relocate")
        @ParameterizedTest(name = "Relocate: \"{0}\"")
        @MethodSource("provideWarpData")
        @Order(4)
        public void testWarpRelocation(@NotNull String name, @NotNull Position position) {
            final World world = BukkitAdapter.adaptWorld(server.getWorld("world")).orElseThrow();
            final Position newPosition = Position.at(
                    position.getX() + 10, position.getY() + 10, position.getZ() + 10,
                    0, 0,
                    world, plugin.getServerName()
            );
            plugin.getManager().warps().setWarpPosition(name, newPosition);

            final Optional<Position> warpPosition = plugin.getDatabase().getWarp(name).map(warp -> warp);
            Assertions.assertTrue(warpPosition.isPresent());
            Assertions.assertEquals(newPosition.getX(), warpPosition.get().getX());
            Assertions.assertEquals(newPosition.getY(), warpPosition.get().getY());
            Assertions.assertEquals(newPosition.getZ(), warpPosition.get().getZ());
        }

        @DisplayName("Test Warp Overwrite")
        @ParameterizedTest(name = "Overwrite: \"{0}\"")
        @MethodSource("provideWarpData")
        @Order(5)
        public void testWarpOverwrite(@NotNull String name, @NotNull Position position) {
            final World world = BukkitAdapter.adaptWorld(server.getWorld("world")).orElseThrow();
            final Position newPosition = Position.at(
                    position.getX() + 10, position.getY() + 10, position.getZ() + 10,
                    0, 0,
                    world, plugin.getServerName()
            );
            plugin.getManager().warps().createWarp(name, newPosition, true);

            final Optional<Position> warpPosition = plugin.getDatabase().getWarp(name).map(warp -> warp);
            Assertions.assertTrue(warpPosition.isPresent());
            Assertions.assertEquals(newPosition.getX(), warpPosition.get().getX());
            Assertions.assertEquals(newPosition.getY(), warpPosition.get().getY());
            Assertions.assertEquals(newPosition.getZ(), warpPosition.get().getZ());
        }

        @DisplayName("Test Warp Deletion")
        @ParameterizedTest(name = "Delete Warp: \"{0}\"")
        @MethodSource("provideWarpData")
        @Order(6)
        @SuppressWarnings("unused")
        public void testWarpDeletion(@NotNull String name, @NotNull Position position) {
            plugin.getManager().warps().deleteWarp(name);
            Assertions.assertFalse(plugin.getDatabase().getWarp(name).isPresent());
            Assertions.assertFalse(plugin.getManager().warps()
                    .getWarps().stream()
                    .anyMatch(warp -> warp.equals(name)));

            plugin.getManager().warps().createWarp(name, position);
            Assertions.assertTrue(plugin.getDatabase().getWarp(name).isPresent());
        }

        @DisplayName("Test Querying Warps Case-Insensitively")
        @ParameterizedTest(name = "Query: \"{0}\"")
        @MethodSource("provideWarpData")
        @Order(8)
        @SuppressWarnings("unused")
        public void testWarpCaseInsensitiveQuery(@NotNull String name, @NotNull Position position) {
            final String nameUpper = name.toUpperCase();
            final Optional<Warp> nameUpperWarp = plugin.getDatabase().getWarp(nameUpper, true);
            Assertions.assertTrue(nameUpperWarp.isPresent());
            Assertions.assertEquals(name, nameUpperWarp.get().getName());

            final String nameLower = name.toLowerCase();
            final Optional<Warp> nameLowerWarp = plugin.getDatabase().getWarp(nameLower, true);
            Assertions.assertTrue(nameLowerWarp.isPresent());
            Assertions.assertEquals(name, nameLowerWarp.get().getName());
        }

        @DisplayName("Test Deleting All Warps")
        @Order(9)
        @Test
        public void testWarpDeleteAll() {
            final int deleted = plugin.getManager().warps().deleteAllWarps();
            Assertions.assertTrue(plugin.getManager().warps().getWarps().isEmpty());
            Assertions.assertTrue(plugin.getDatabase().getWarps().isEmpty());
            Assertions.assertEquals(WARP_NAMES.size(), deleted);
        }

        @NotNull
        public static Stream<Arguments> provideWarpData() {
            final World world = BukkitAdapter.adaptWorld(server.getWorld("world")).orElseThrow();
            final Position position = Position.at(0, 0, 0, 0, 0, world, plugin.getServerName());
            return WARP_NAMES.stream()
                    .flatMap(name -> Stream.of(Arguments.of(name, position)));
        }

    }

    @Nested
    @DisplayName("Home Tests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    public class HomeTests {
        private static final List<String> HOME_NAMES = List.of(
                "mr_home",
                "homble",
                "seaside",
                "hovel",
                "tokyo-3",
                "testington",
                "h"
        );
        private static BukkitUser homeOwner;

        @DisplayName("Ensure User Data")
        @BeforeAll
        public static void createHomeUser() {
            homeOwner = BukkitUser.adapt(server.addPlayer("TestUser278"), plugin);
            plugin.getDatabase().ensureUser(homeOwner);
            Assertions.assertTrue(plugin.getDatabase().getUserData(homeOwner.getUuid()).isPresent());
        }

        @DisplayName("Test Home Creation")
        @ParameterizedTest(name = "Home Name: \"{1}\"")
        @MethodSource("provideHomeData")
        @Order(1)
        public void testHomeCreation(@NotNull OnlineUser owner, @NotNull String name, @NotNull Position position) {
            plugin.getManager().homes().createHome(owner, name, position);
            Assertions.assertTrue(plugin.getDatabase().getHome(owner, name).isPresent());
            Assertions.assertTrue(plugin.getManager().homes()
                    .getUserHomes()
                    .get(owner.getUsername()).stream()
                    .anyMatch(home -> home.equals(name)));
        }

        @DisplayName("Test Home Renaming")
        @ParameterizedTest(name = "Rename: \"{1}\" > \"{1}2\"")
        @MethodSource("provideHomeData")
        @Order(2)
        @SuppressWarnings("unused")
        public void testHomeRenaming(@NotNull OnlineUser owner, @NotNull String name, @NotNull Position position) {
            final String newName = "new_" + name;
            plugin.getManager().homes().setHomeName(owner, name, newName);
            Assertions.assertTrue(plugin.getDatabase().getHome(owner, newName).isPresent());
            Assertions.assertFalse(plugin.getDatabase().getHome(owner, name).isPresent());
            Assertions.assertTrue(plugin.getManager().homes()
                    .getUserHomes()
                    .get(owner.getUsername()).stream()
                    .anyMatch(home -> home.equals(newName)));
            Assertions.assertFalse(plugin.getManager().homes()
                    .getUserHomes()
                    .get(owner.getUsername()).stream()
                    .anyMatch(home -> home.equals(name)));

            // Rename back to original name
            plugin.getManager().homes().setHomeName(owner, newName, name);
            Assertions.assertTrue(plugin.getDatabase().getHome(owner, name).isPresent());
            Assertions.assertTrue(plugin.getManager().homes()
                    .getUserHomes()
                    .get(owner.getUsername()).stream()
                    .anyMatch(home -> home.equals(name)));
        }

        @DisplayName("Test Home Description")
        @ParameterizedTest(name = "Edit Description: \"{1}\"")
        @MethodSource("provideHomeData")
        @Order(3)
        @SuppressWarnings("unused")
        public void testHomeDescription(@NotNull OnlineUser owner, @NotNull String name, @NotNull Position position) {
            final String description = "This is a test description for the home " + name + "!";
            plugin.getManager().homes().setHomeDescription(owner, name, description);
            Assertions.assertTrue(plugin.getDatabase().getHome(owner, name).isPresent());

            final Optional<String> homeDescription = plugin.getDatabase().getHome(owner, name)
                    .map(Home::getMeta)
                    .map(PositionMeta::getDescription);
            Assertions.assertTrue(homeDescription.isPresent());
            Assertions.assertEquals(description, homeDescription.get());
        }

        @DisplayName("Test Home Relocation")
        @ParameterizedTest(name = "Relocate: \"{1}\"")
        @MethodSource("provideHomeData")
        @Order(4)
        public void testHomeRelocation(@NotNull OnlineUser owner, @NotNull String name, @NotNull Position position) {
            final World world = BukkitAdapter.adaptWorld(server.getWorld("world")).orElseThrow();
            final Position newPosition = Position.at(
                    position.getX() + 10, position.getY() + 10, position.getZ() + 10,
                    0, 0,
                    world, plugin.getServerName()
            );
            plugin.getManager().homes().setHomePosition(owner, name, newPosition);
            Assertions.assertTrue(plugin.getDatabase().getHome(owner, name).isPresent());

            final Optional<Position> homePosition = plugin.getDatabase().getHome(owner, name).map(home -> home);
            Assertions.assertTrue(homePosition.isPresent());
            Assertions.assertEquals(newPosition.getX(), homePosition.get().getX());
            Assertions.assertEquals(newPosition.getY(), homePosition.get().getY());
            Assertions.assertEquals(newPosition.getZ(), homePosition.get().getZ());
        }

        @DisplayName("Test Home Overwrite")
        @ParameterizedTest(name = "Overwrite: \"{1}\"")
        @MethodSource("provideHomeData")
        @Order(5)
        public void testHomeOverwrite(@NotNull OnlineUser owner, @NotNull String name, @NotNull Position position) {
            final World world = BukkitAdapter.adaptWorld(server.getWorld("world")).orElseThrow();
            final Position newPosition = Position.at(
                    position.getX() + 10, position.getY() + 10, position.getZ() + 10,
                    0, 0,
                    world, plugin.getServerName()
            );
            plugin.getManager().homes().setHomePosition(owner, name, newPosition);
            Assertions.assertTrue(plugin.getDatabase().getHome(owner, name).isPresent());

            final Optional<Position> homePosition = plugin.getDatabase().getHome(owner, name).map(home -> home);
            Assertions.assertTrue(homePosition.isPresent());
            Assertions.assertEquals(newPosition.getX(), homePosition.get().getX());
            Assertions.assertEquals(newPosition.getY(), homePosition.get().getY());
            Assertions.assertEquals(newPosition.getZ(), homePosition.get().getZ());
        }

        @DisplayName("Test Making Home Public")
        @ParameterizedTest(name = "Make Public: \"{1}\"")
        @MethodSource("provideHomeData")
        @Order(6)
        @SuppressWarnings("unused")
        public void testHomeMakePublic(@NotNull OnlineUser owner, @NotNull String name, @NotNull Position p) {
            plugin.getManager().homes().setHomePrivacy(owner, name, true);
            Assertions.assertTrue(plugin.getDatabase().getHome(owner, name).isPresent());

            final Optional<Boolean> homePrivacy = plugin.getDatabase().getHome(owner, name).map(Home::isPublic);
            Assertions.assertTrue(homePrivacy.isPresent());
            Assertions.assertTrue(homePrivacy.get());
            Assertions.assertTrue(plugin.getManager().homes().getPublicHomes().get(owner.getUsername()).contains(name));
        }

        @DisplayName("Test Making Home Private")
        @ParameterizedTest(name = "Make Private: \"{1}\"")
        @MethodSource("provideHomeData")
        @Order(7)
        @SuppressWarnings("unused")
        public void testHomeMakePrivate(@NotNull OnlineUser owner, @NotNull String name, @NotNull Position p) {
            plugin.getManager().homes().setHomePrivacy(owner, name, false);
            Assertions.assertTrue(plugin.getDatabase().getHome(owner, name).isPresent());

            final Optional<Boolean> homePrivacy = plugin.getDatabase().getHome(owner, name).map(Home::isPublic);
            Assertions.assertTrue(homePrivacy.isPresent());
            Assertions.assertFalse(homePrivacy.get());
            Assertions.assertFalse(plugin.getManager().homes().getPublicHomes()
                    .getOrDefault(owner.getUsername(), List.of())
                    .contains(name));
        }

        @DisplayName("Test Querying Homes Case-Insensitively")
        @ParameterizedTest(name = "Query: \"{1}\"")
        @MethodSource("provideHomeData")
        @Order(8)
        @SuppressWarnings("unused")
        public void testWarpCaseInsensitiveQuery(@NotNull OnlineUser owner, @NotNull String name, @NotNull Position p) {
            final String nameUpper = name.toUpperCase();
            final Optional<Home> nameUpperWarp = plugin.getDatabase().getHome(owner, nameUpper, true);
            Assertions.assertTrue(nameUpperWarp.isPresent());
            Assertions.assertEquals(name, nameUpperWarp.get().getName());

            final String nameLower = name.toLowerCase();
            final Optional<Home> nameLowerWarp = plugin.getDatabase().getHome(owner, nameLower, true);
            Assertions.assertTrue(nameLowerWarp.isPresent());
            Assertions.assertEquals(name, nameLowerWarp.get().getName());
        }

        @DisplayName("Test Home Deletion")
        @ParameterizedTest(name = "Delete: \"{1}\"")
        @MethodSource("provideHomeData")
        @Order(9)
        public void testHomeDeletion(@NotNull OnlineUser owner, @NotNull String name, @NotNull Position position) {
            plugin.getManager().homes().deleteHome(owner, name);
            Assertions.assertFalse(plugin.getDatabase().getHome(owner, name).isPresent());
            Assertions.assertFalse(plugin.getManager().homes().getUserHomes()
                    .getOrDefault(owner.getUsername(), List.of())
                    .contains(name));

            plugin.getManager().homes().createHome(owner, name, position);
        }

        @DisplayName("Test Deleting All Homes")
        @Order(10)
        @Test
        public void testDeleteAllHomes() {
            final int deleted = plugin.getManager().homes().deleteAllHomes(homeOwner);
            Assertions.assertTrue(plugin.getDatabase().getHomes(homeOwner).isEmpty());
            Assertions.assertTrue(plugin.getManager().homes().getUserHomes().get(homeOwner.getUsername()).isEmpty());
            Assertions.assertEquals(HOME_NAMES.size(), deleted);
        }

        @NotNull
        public static Stream<Arguments> provideHomeData() {
            final World world = BukkitAdapter.adaptWorld(server.getWorld("world")).orElseThrow();
            final Position position = Position.at(0, 0, 0, 0, 0, world, plugin.getServerName());
            return HOME_NAMES.stream()
                    .flatMap(name -> Stream.of(Arguments.of(homeOwner, name, position)));
        }

    }
}
