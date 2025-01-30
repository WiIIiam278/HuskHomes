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

package net.william278.huskhomes.util;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.Audiences;
import net.kyori.adventure.platform.AudienceProvider;
import net.william278.huskhomes.user.ConsoleUser;
import net.william278.huskhomes.user.OnlineUser;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Interface for providing the {@link ConsoleUser} and {@link Audiences} instances
 *
 * @since 4.8
 */
public interface AudiencesProvider {

    /**
     * Get the {@link Audiences} instance.
     *
     * @return the {@link Audiences} instance
     * @since 4.8
     */
    @NotNull
    AudienceProvider getAudiences();

    /**
     * Get the {@link Audience} instance for the given {@link OnlineUser}.
     *
     * @param user the {@link OnlineUser} to get the {@link Audience} for
     * @return the {@link Audience} instance
     */
    @NotNull
    default Audience getAudience(@NotNull UUID user) {
        return getAudiences().player(user);
    }

    /**
     * Get the {@link ConsoleUser} instance.
     *
     * @return the {@link ConsoleUser} instance
     * @since 4.8
     */
    @NotNull
    default ConsoleUser getConsole() {
        return ConsoleUser.wrap(getAudiences().console());
    }

}
