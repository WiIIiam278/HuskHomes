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
import net.william278.huskhomes.user.User;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

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
        loadUserListRefreshTask();
    }

    /**
     * Start the task that periodically re-sends this server's player list to the network, if it has changed.
     *
     * <p>The list is otherwise only sent on player join and leave. Vanish plugins toggle a player's
     * visibility without firing either event, so a staff member vanishing mid-session would remain
     * visible in the player lists of every other server until the next join or leave happens here.
     *
     * @since 4.11
     */
    default void loadUserListRefreshTask() {
        final int interval = getPlugin().getSettings().getCrossServer().getUserListRefreshSeconds();
        if (interval <= 0 || getBroker().isEmpty()) {
            return;
        }

        final AtomicReference<List<User>> lastSent = new AtomicReference<>();
        getPlugin().getRepeatingTask(() -> getPlugin().runSync(() -> {
            final List<User> users = getPlugin().getLocalUserList();
            if (users.equals(lastSent.get())) {
                return;
            }

            // Any online user can carry the message; vanished users are valid senders
            getPlugin().getOnlineUsers().stream().findAny().ifPresent(sender -> getBroker().ifPresent(broker -> {
                Message.builder()
                        .type(Message.MessageType.UPDATE_USER_LIST)
                        .target(Message.TARGET_ALL, Message.TargetType.SERVER)
                        .payload(Payload.userList(users))
                        .build().send(broker, sender);
                lastSent.set(users);
            }));
        }), interval * 20L).run();
    }

    @NotNull
    HuskHomes getPlugin();


}
