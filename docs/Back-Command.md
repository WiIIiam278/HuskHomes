The /back [command](commands) lets users teleport to a position they were at previously. This includes:

* Where the player was before they last used another HuskHomes teleportation command (on by default)
* Where the player was before they last died (off by default, requires permission)
* Where the player was before they last used another plugin's teleport command (off by default)

## Configuration
Through the config file and permissions, you can choose which last positions to let users return to.

<details>
<summary>Back Command &ndash; config.yml</summary>

```yaml
# Settings for the /back command
back_command:
  # Whether /back should work to teleport the user to where they died
  return_by_death: true
  # Whether /back should work with other plugins that use the PlayerTeleportEvent (can conflict)
  save_on_teleport_event: false
  # List of world names where the /back command cannot RETURN the player to.
  # A user's last position won't be updated if they die or teleport from these worlds, but they still will be able to use the command while IN the world
  restricted_worlds: []
```
</details>

## Restricting /back
There are several options for restricting the use of `/back`.

### Letting users return to a previous position
Being able to return to where a user last performed a HuskHomes teleportation command from is the default functionality of `/back`.

To disable this, give the player the `huskhomes.command.back.previous` permission node, set to the value `false`.

### Letting users "return by death"
To enable "return by death" &ndash; where a user can return to where they most recently died with /back &ndash; first enable `return_by_death` under `config.yml`

<details>
<summary>Return By Death &ndash; config.yml</summary>

```yaml
back_command:
  # Whether /back should work to teleport the user to where they died
  return_by_death: true
```
</details>

Then, grant the player the `huskhomes.command.back.death` permission node.

### Disabling using /back to return to certain worlds
Add world names to the `restricted_worlds` list under `back_command` in the config file and the plugin will no longer save last positions within the listed worlds.

This will stop the plugin from letting users _return_ to certain specific worlds. It will _not_, however, prevent users from using the `/back` command _in_ these worlds. To stop users from using `/back` in specific worlds, use [LuckPerms Contextual Permissions](https://luckperms.net/wiki/Context).

<details>
<summary>Restricted Worlds &ndash; config.yml</summary>

```yaml
back_command:
  # List of world names where the /back command cannot RETURN the player to.
  # A user's last position won't be updated if they die or teleport from these worlds, but they still will be able to use the command while IN the world
  restricted_worlds: []
```
</details>

### Returning from other plugins' teleports
> **Warning:** This may cause conflicts with the behaviour of some plugins, please take caution when enabling this if you have lots of plugins installed!

To use `/back` to return to where other plugins' teleported a user from, enable `save_on_teleport_event`.

<details>
<summary>Teleport Event &ndash; config.yml</summary>

```yaml
back_command:
  # Whether /back should work with other plugins that use the PlayerTeleportEvent (can conflict)
  save_on_teleport_event: false
```
</details>