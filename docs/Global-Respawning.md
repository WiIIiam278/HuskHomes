HuskHomes supports global respawning, an advanced setting for networks of multiple servers that wish for players to have just one bed respawn, rather than per-server bed respawns.

To enable global respawn, your network must first be correctly configured in `cross_server` mode. Then, set `global_respawning` to `true` under the `cross_server` section.

## How it works
* When a player interacts with a bed or respawn anchor, HuskHomes will update the user's global respawn position to match their respawn position on that server.
* When a player dies, the player will be sent to the server they set their spawn in with a special `RESPAWN` teleport type.
* When the player arrives at their respawn server, the game will finsih the teleport to their respawn position. If their respawn position is missing or obstructed, the player will instead be moved to the world spawn on that server.

## Limitation
* Plugins, commands and other ways in which a player's spawn may be updated will have no effect on a user's global respawn position, due to Spigot not having an event for this. This limitation may be removed in the future with extended support for Paper.