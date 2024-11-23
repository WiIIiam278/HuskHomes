This page addresses a number of miscellaneous troubleshooting topics.

## Players are not being sent between servers on my Velocity proxy.
**Velocity:** Please ensure that you have not disabled the BungeeCord Plugin Messaging Channel on your Velocity proxy config.

The BungeeCord Plugin Messaging Channel is a standard established originally BungeeCord to allow backend server plugins to utilize the Plugin Message protocol provided by Minecraft to perform actions against the proxy, such as changing the player's server. The Velocity proxy implements this standard, too, unless it has been switched off.

In your velocity.toml file, ensure [`bungee-plugin-message-channel`](https://docs.papermc.io/velocity/configuration) is set to `true`