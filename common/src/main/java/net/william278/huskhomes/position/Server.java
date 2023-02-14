package net.william278.huskhomes.position;

import net.william278.annotaml.YamlFile;
import net.william278.annotaml.YamlKey;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a server on a proxied network
 */
@YamlFile(header = """
        ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
        ┃ Server ID cache. Must match  ┃
        ┃ server name in proxy config. ┃
        ┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛""")
public class Server {

    /**
     * Default (unknown) server identifier
     */
    public static final Server DEFAULT = new Server();

    /**
     * Proxy-defined name of this server
     */
    @YamlKey("server_name")
    public String name = "server";

    public Server(@NotNull String name) {
        this.name = name;
    }

    @SuppressWarnings("unused")
    private Server() {
    }

    @Override
    public boolean equals(@NotNull Object other) {
        // If the name of this server matches another, the servers are the same.
        if (other instanceof Server server) {
            return server.name.equalsIgnoreCase(this.name);
        }
        return super.equals(other);
    }

}
