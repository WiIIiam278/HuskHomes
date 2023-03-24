package net.william278.huskhomes.config;

import net.william278.annotaml.YamlFile;
import net.william278.annotaml.YamlKey;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

/**
 * Represents a server on a proxied network
 */
@YamlFile(header = """
        ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
        ┃  HuskHomes Server ID config  ┃
        ┃    Developed by William278   ┃
        ┣━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
        ┣╸ This file should contain the ID of this server as defined in your proxy config.
        ┣╸ If you join it using /server alpha, then set it to 'alpha' (case-sensitive)
        ┗╸ You only need to touch this if you're using cross-server mode.""")
public class Server {

    /**
     * Default server identifier
     */
    @NotNull
    public static String getDefaultServerName() {
        try {
            final Path serverDirectory = Path.of(System.getProperty("user.dir"));
            return serverDirectory.getFileName().toString().trim();
        } catch (Exception e) {
            return "server";
        }
    }

    @YamlKey("name")
    private String serverName = getDefaultServerName();

    public Server(@NotNull String serverName) {
        this.serverName = serverName;
    }

    @SuppressWarnings("unused")
    private Server() {
    }

    @Override
    public boolean equals(@NotNull Object other) {
        // If the name of this server matches another, the servers are the same.
        if (other instanceof Server server) {
            return server.getName().equalsIgnoreCase(this.getName());
        }
        return super.equals(other);
    }

    /**
     * Proxy-defined name of this server
     */
    @NotNull
    public String getName() {
        return serverName;
    }

}
