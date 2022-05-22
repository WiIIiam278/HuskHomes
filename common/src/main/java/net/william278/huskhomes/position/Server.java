package net.william278.huskhomes.position;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a server on a proxied network
 */
public class Server {

    /**
     * Proxy-defined name of this server
     */
    @NotNull
    public String name;

    public Server(@NotNull String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Server server) {
            // If the name of this server matches another, the servers are the same.
            return server.name.equalsIgnoreCase(this.name);
        }
        return super.equals(other);
    }
}
