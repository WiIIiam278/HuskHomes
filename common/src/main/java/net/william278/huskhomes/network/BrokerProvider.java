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
