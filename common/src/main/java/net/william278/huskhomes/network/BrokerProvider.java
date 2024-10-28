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

import net.william278.huskhomes.HuskHomes;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface BrokerProvider {

    Optional<Broker> getBroker();

    void setBroker(@NotNull Broker broker);

    void closeBroker();

    void setupPluginMessagingChannels();

    default void loadBroker() {
        if (!getPlugin().getSettings().getCrossServer().isEnabled()) {
            return;
        }

        switch (getPlugin().getSettings().getCrossServer().getBrokerType()) {
            case REDIS -> setBroker(new RedisBroker(getPlugin()));
            case PLUGIN_MESSAGE -> setBroker(new PluginMessageBroker(getPlugin()));
        }
        getBroker().ifPresent(Broker::initialize);
    }

    @NotNull
    HuskHomes getPlugin();


}
