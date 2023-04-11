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

package net.william278.huskhomes.hook;

import com.djrapitops.plan.capability.CapabilityService;
import com.djrapitops.plan.extension.CallEvents;
import com.djrapitops.plan.extension.DataExtension;
import com.djrapitops.plan.extension.ExtensionService;
import com.djrapitops.plan.extension.annotation.*;
import com.djrapitops.plan.extension.icon.Color;
import com.djrapitops.plan.extension.icon.Family;
import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.database.Database;
import net.william278.huskhomes.position.Home;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.user.SavedUser;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.logging.Level;

/**
 * Hooks into Plan to provide the {@link PlanDataExtension} with stats on the web panel
 */
public class PlanHook extends Hook {

    private final HuskHomes plugin;

    public PlanHook(@NotNull HuskHomes plugin) {
        super(plugin, "Plan");
        this.plugin = plugin;
    }

    @Override
    public void initialize()  {
        if (!areAllCapabilitiesAvailable()) {
            return;
        }
        registerDataExtension();
        handlePlanReload();
    }


    private boolean areAllCapabilitiesAvailable() {
        CapabilityService capabilities = CapabilityService.getInstance();
        return capabilities.hasCapability("DATA_EXTENSION_VALUES");
    }

    private void registerDataExtension() {
        try {
            ExtensionService.getInstance().register(new PlanDataExtension(plugin.getDatabase(), plugin.getSettings().doCrossServer()));
        } catch (IllegalStateException planIsNotEnabled) {
            plugin.log(Level.SEVERE, "Plan extension hook failed to register. Plan is not enabled.", planIsNotEnabled);
            // Plan is not enabled, handle exception
        } catch (IllegalArgumentException dataExtensionImplementationIsInvalid) {
            plugin.log(Level.SEVERE, "Plan extension hook failed to register. Data hook implementation is invalid.", dataExtensionImplementationIsInvalid);
            // The DataExtension implementation has an implementation error, handle exception
        }
    }

    // Re-register the extension when plan enables
    private void handlePlanReload() {
        CapabilityService.getInstance().registerEnableListener(isPlanEnabled -> {
            if (isPlanEnabled) {
                registerDataExtension();
            }
        });
    }

    @PluginInfo(
            name = "HuskHomes",
            iconName = "home",
            iconFamily = Family.SOLID,
            color = Color.LIGHT_BLUE
    )
    @SuppressWarnings("unused")
    protected static class PlanDataExtension implements DataExtension {

        private Database database;

        private boolean crossServer;

        private static final String UNKNOWN_STRING = "N/A";

        protected PlanDataExtension(@NotNull Database database, boolean crossServer) {
            this.database = database;
        }

        protected PlanDataExtension() {
        }

        @Override
        public CallEvents[] callExtensionMethodsOn() {
            return new CallEvents[]{
                    CallEvents.PLAYER_JOIN,
                    CallEvents.PLAYER_LEAVE
            };
        }

        @BooleanProvider(
                text = "Has Data",
                description = "Whether this user has HuskHomes data.",
                iconName = "bell",
                iconFamily = Family.SOLID,
                conditionName = "hasData",
                hidden = true
        )
        public boolean getHasUserData(@NotNull UUID uuid) {
            return database.getUserData(uuid).isPresent();
        }

        @NumberProvider(
                text = "Home Count",
                description = "The number of homes this user has set.",
                iconName = "home",
                iconFamily = Family.SOLID,
                priority = 5
        )
        @Conditional("hasData")
        public long getHomeCount(@NotNull UUID uuid) {
            return database.getUserData(uuid)
                    .map(userData -> (long) database.getHomes(userData.getUser()).size())
                    .orElse(0L);
        }

        @NumberProvider(
                text = "Public Homes",
                description = "The number of homes this user has made public.",
                iconName = "sun",
                iconFamily = Family.SOLID,
                priority = 4
        )
        @Conditional("hasData")
        public long getPublicHomeCount(@NotNull UUID uuid) {
            return database.getUserData(uuid)
                    .map(userData -> database.getHomes(userData.getUser()).stream()
                            .filter(Home::isPublic).count())
                    .orElse(0L);
        }

        @NumberProvider(
                text = "Home Slots",
                description = "The number of extra home slots this user has purchased.",
                iconName = "money-check-alt",
                iconFamily = Family.SOLID,
                priority = 3
        )
        @Conditional("hasData")
        public long getPurchasedHomeSlots(@NotNull UUID uuid) {
            return database.getUserData(uuid)
                    .map(userData -> (long) userData.getHomeSlots())
                    .orElse(0L);
        }

        @BooleanProvider(
                text = "Ignoring /tpa Requests",
                description = "Whether this player is ignoring /tpa requests.",
                iconName = "phone-slash",
                iconFamily = Family.SOLID,
                priority = 2
        )
        @Conditional("hasData")
        public boolean isIgnoringTeleportRequests(@NotNull UUID uuid) {
            return database.getUserData(uuid)
                    .map(SavedUser::isIgnoringTeleports)
                    .orElse(false);
        }

        @StringProvider(
                text = "Offline Position",
                description = "The location where this user logged out.",
                iconName = "door-open",
                iconFamily = Family.SOLID,
                priority = 1
        )
        @Conditional("hasData")
        public String getOfflinePosition(@NotNull UUID uuid) {
            return database.getUserData(uuid)
                    .map(userData -> database.getOfflinePosition(userData.getUser())
                            .map(Position::toString)
                            .orElse(UNKNOWN_STRING))
                    .orElse(UNKNOWN_STRING);
        }

    }
}