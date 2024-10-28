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
