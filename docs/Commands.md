HuskHomes provides a range of commands for you to use. This page will detail the permissions for each command to [let you manage access](managing-access) to the plugin's different features.

## Commands & Permissions Table
> **[[Command Conflicts]]:** If you have multiple plugins providing similar commands, you may need to [create aliases](command-conflicts) to ensure HuskHomes' commands take priority.

This is a table of HuskHomes' commands and their required permission nodes. Additional permissions provided by the plugin to control other variables are detailed further below!

| Command                                                         | Description                                         | Base Permission&dagger;       | Default&ddagger; |
|-----------------------------------------------------------------|-----------------------------------------------------|-------------------------------|:----------------:|
| `/home <name>`                                                  | Teleport to a home                                  | `huskhomes.command.home`      |        ✅         |
| `/homelist [page]`                                              | Get a list of your homes                            | `huskhomes.command.homelist`  |        ✅         |
| `/sethome <name>`                                               | Set a new home with given name                      | `huskhomes.command.sethome`   |        ✅         |
| `/delhome <name>`                                               | Delete a home you previously set                    | `huskhomes.command.delhome`   |        ✅         |
| `/edithome <name> [rename/description/relocate/privacy] [args]` | Edit one of your homes                              | `huskhomes.command.edithome`  |        ✅         |
| `/phome [<owner_name.home_name>]`                               | Teleport to a public home                           | `huskhomes.command.phome`     |        ✅         |
| `/phomelist [page]`                                             | View the list of public homes                       | `huskhomes.command.phomelist` |        ✅         |
| `/warp <name>`                                                  | Teleport to a warp                                  | `huskhomes.command.warp`      |        ✅         |
| `/warplist [page]`                                              | View the list of warps                              | `huskhomes.command.warplist`  |        ✅         |
| `/setwarp <name>`                                               | Set a new warp with given name                      | `huskhomes.command.setwarp`   |        ❌         |
| `/delwarp <name>`                                               | Delete a warp                                       | `huskhomes.command.delwarp`   |        ❌         |
| `/editwarp <name> [rename/description/relocate] [args]`         | Edit a warp                                         | `huskhomes.command.editwarp`  |        ❌         |
| `/tp <target> [destination]`                                    | Teleport to another player or location              | `huskhomes.command.tp`        |        ❌         |
| `/tphere <player>`                                              | Teleport another player to you                      | `huskhomes.command.tphere`    |        ❌         |
| `/tpa <player>`                                                 | Request to teleport to another player               | `huskhomes.command.tpa`       |        ✅         |
| `/tpahere <player>`                                             | Request another player to teleport to you           | `huskhomes.command.tpahere`   |        ✅         |
| `/tpaccept [player]`                                            | Accept a teleport request                           | `huskhomes.command.tpaccept`  |        ✅         |
| `/tpdecline [player]`                                           | Decline a teleport request                          | `huskhomes.command.tpdecline` |        ✅         |
| `/rtp [player] [world]`                                         | Teleport randomly into the wild                     | `huskhomes.command.rtp`       |        ✅         |
| `/tpignore`                                                     | Ignore incoming teleport requests                   | `huskhomes.command.tpignore`  |        ✅         |
| `/tpoffline <player>`                                           | Teleport to where a player was last online          | `huskhomes.command.tpoffline` |        ❌         |
| `/tpall`                                                        | Teleport everyone to your position                  | `huskhomes.command.tpall`     |        ❌         |
| `/tpaall`                                                       | Request that everyone teleports to you              | `huskhomes.command.tpaall`    |        ❌         |
| `/spawn`                                                        | Teleport to spawn                                   | `huskhomes.command.spawn`     |        ✅         |
| `/setspawn`                                                     | Set the spawn position                              | `huskhomes.command.setspawn`  |        ❌         |
| `/back`                                                         | Return to your previous position, or where you died | `huskhomes.command.back`      |        ✅         |
| `/huskhomes [about/help/reload/import/update]`                  | View plugin information & reload configs            | `huskhomes.command.huskhomes` |        ✅         |

* &dagger; **Base Permission** &mdash; Required permission for basic command execution; some commands require additional permissions for certain functions ([See below&hellip;](#other-permissions))
* &ddagger; **Default** &mdash; ✅ = Accessible by all players by default &mdash; ❌ = Accessible only by server operators by default.

### Command Aliases
The following commands have aliases that can also be used for convenience:

| Command      | Aliases                      |
|--------------|------------------------------|
| `/homelist`  | `/homes`                     |
| `/phome`     | `/publichome`                |
| `/phomelist` | `/phomes`, `/publichomelist` |
| `/warplist`  | `/warps`                     |
| `/tp`        | `/tpo`                       |
| `/tpaccept`  | `/tpyes`                     |
| `/tpdecline` | `/tpno`, `/tpdeny`           |

## Disabling Commands

If you'd like to disable a command, add it to the `disabled_commands` section of your config file as detailed below. Note that on servers running Paper for versions earlier than Minecraft 1.19.4, or servers just running plain Spigot instead of Paper (not recommended&mdash;please upgrade to Paper!), disabled commands will still be _registered_ but a disabled command message will appear. On servers running Paper for Minecraft 1.19.4+, HuskHomes will be registered as a Paper Plugin (and will appear as such in the `/plugins` menu) and disabled commands will not be registered at all.

<details>
<summary>Disabling a command in config.yml</summary>

```yaml
# Disabled commands (e.g. ['/home', '/warp'] to disable /home and /warp)
disabled_commands: [ '/rtp' ]
```

</details>

## Home Limits
You can modify the maximum number of homes, the allotment of free homes and the number of public homes a user can set through permission nodes.

* `huskhomes.max_homes.<amount>` — Determines the max number of homes a user can set
* `huskhomes.free_homes.<amount>` — Determines the allotment of homes the user can set for free, before they have to pay&dagger;
* `huskhomes.max_public_homes.<amount>` — Determines the maximum number of homes a user can make public

&dagger;Only effective on servers that make use of the economy hook.

If users have multiple permission nodes (i.e. from being in multiple permission groups), HuskHomes will accept the highest. If you prefer that the nodes _stack_, you can set the `stack_permission_limits` setting in the plugin config file to `true` (under `general`).

Note that these permission-set values override the values set in the plugin config (`max_homes`, `max_public_homes` under `general` and `free_homes` under `economy`).

## Other Permissions

<details>
<summary>Return to where you died with /back</summary>

This permission controls whether users can return to where they died. Note that return by death must be enabled in the
config for this to work.

| Permission                     | Command | Description                           |
|--------------------------------|---------|---------------------------------------|
| `huskhomes.command.back.death` | `/back` | Use /back to return to where you died |
</details>

<details>
<summary>Home privacy</summary>

These permissions allow you to make a home public/private (toggling its privacy). There are also permissions that let you use, edit and delete homes that have not been set publicly.

| Permission                           | Command                                                                             | Description                                  |
|--------------------------------------|-------------------------------------------------------------------------------------|----------------------------------------------|
| `huskhomes.command.edithome.privacy` | `/edithome <name> privacy [public/private]`                                         | Modify the privacy of a home                 |
| `huskhomes.command.home.other`       | `/homelist <player> [page]`                                                         | View a list of a user's homes                |
| `huskhomes.command.home.other`       | `/home [<owner_name>.<home_name>]`                                                  | Teleport to a user's home, public or private |
| `huskhomes.command.edithome.other`   | `/edithome [<owner_name>.<home_name>] [rename/description/relocate/privacy] [args]` | Edit a user's home                           |
| `huskhomes.command.delhome.other`    | `/delhome [<owner_name>.<home_name>]`                                               | Delete a user's home                         |
</details>


<details>
<summary>Cooldown, warmup and economy bypasses</summary>

These permissions let you bypass teleportation warmup checks, rtp cooldown checks and economy checks

| Permission                         | Description                                |
|------------------------------------|--------------------------------------------|
| `huskhomes.bypass_teleport_warmup` | Bypass timed teleportation warmups&dagger; |
| `huskhomes.bypass_economy_checks`  | Bypass economy checks                      |
| `huskhomes.rtp.bypass_cooldown`    | Bypass the cooldown on /rtp&ddagger;       |

&dagger;This is not effective when the teleport warmup time is set `<= 0` in the config file.

&ddagger;This is not effective when the /rtp cooldown time is set `<= 0` in the config file.
</details>

<details>
<summary>Advanced teleportation</summary>

These permissions allow you to use /tp and /rtp to teleport other players remotely and to coordinates.

| Permission                         | Command                                     | Description                            |
|------------------------------------|---------------------------------------------|----------------------------------------|
| `huskhomes.command.tp.other`       | `/tp [player] [target] `                    | Teleport another player                |
| `huskhomes.command.tp.coordinates` | `/tp [player] <x> <y> <z> [world] [server]` | Teleport to a set of coordinates.      |
| `huskhomes.command.rtp.other`      | `/rtp [player] [world]`                     | Randomly teleport another player.      |
| `huskhomes.command.rtp.world`      | `/rtp [player] [world]`                     | Randomly teleport in a specific world. |
| `huskhomes.command.spawn.other`    | `/spawn [player]`                           | Teleport another player to spawn.      |
| `huskhomes.command.warp.other`     | `/warp [name] [player]`                     | Teleport another player to a warp.     |
</details>

<details>
<summary>/huskhomes command arguments</summary>

These permissions control what arguments of the /huskhomes command a user may use.

| Permission                           | Command                          | Description                                |
|--------------------------------------|----------------------------------|--------------------------------------------|
| `huskhomes.command.huskhomes.help`   | `/huskhomes help [page]`         | View a list of HuskHomes commands          |
| `huskhomes.command.huskhomes.about`  | `/huskhomes [about]`             | View the plugin about menu                 |
| `huskhomes.command.huskhomes.reload` | `/huskhomes reload`              | Reload the plugin config and message files |
| `huskhomes.command.huskhomes.import` | `/huskhomes import [list/start]` | Import data from other plugins/mods        |
| `huskhomes.command.huskhomes.update` | `/huskhomes update`              | Check for updates                          |
</details>