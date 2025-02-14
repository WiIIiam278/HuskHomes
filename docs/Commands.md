HuskHomes provides a range of commands for you to use. This page will detail the permissions for each command to [let you manage access](managing-access) to the plugin's different features.

## Commands & Permissions Table
> **Command Conflicts:** If you have multiple plugins providing similar commands, you may need to [create aliases](command-conflicts) to ensure HuskHomes' commands take priority.

<table align="right" style="width: max-content">
    <thead>
        <tr><th colspan="2">Key</th></tr>
    </thead>
    <tbody>
        <tr><td>✅</td><td>Accessible by all players by default</td></tr>
        <tr><td>❌</td><td>Accessible only by server operators by default</td></tr>
    </tbody>
</table>

This is a table of HuskHomes commands, how to use them, and their required permission nodes. Additional permissions for bypassing teleport warmup and economy checks are detailed below.

<table>
    <thead>
        <tr>
            <th colspan="2">Command</th>
            <th>Description</th>
            <th>Permission</th>
            <th>Default</th>
        </tr>
    </thead>
    <tbody>
        <!-- /home command -->
        <tr><th colspan="5">Home commands</th></tr>
        <tr>
            <td rowspan="2"><code>/home</code></td>
            <td><code>/home [name]</code></td>
            <td>Teleport to one of your homes</td>
            <td><code>huskhomes.command.home</code></td>
            <td align="center">✅</td>
        </tr>
        <tr>
            <td><code>/home [&lt;owner_name.home_name&gt;]</code></td>
            <td>Teleport to another user's home</td>
            <td><code>huskhomes.command.home.other</code></td>
            <td align="center">❌</td>
        </tr>
        <!-- /homelist command -->
        <tr>
            <td rowspan="2"><code>/homelist</code></td>
            <td><code>/homelist [page]</code></td>
            <td>View a list of your homes</td>
            <td><code>huskhomes.command.homelist</code></td>
            <td align="center">✅</td>
        </tr>
        <tr>
            <td><code>/homelist &lt;owner_name&gt; [page]</code></td>
            <td>View a list of another user's homes</td>
            <td><code>huskhomes.command.homelist.other</code></td>
            <td align="center">❌</td>
        </tr>
        <!-- /sethome command -->
        <tr>
            <td><code>/sethome</code></td>
            <td><code>/sethome [name]</code></td>
            <td>Set a new home with a name</td>
            <td><code>huskhomes.command.sethome</code></td>
            <td align="center">✅</td>
        </tr>
        <!-- /delhome command -->
        <tr>
            <td rowspan="3"><code>/delhome</code></td>
            <td><code>/delhome [name]</code></td>
            <td>Delete a home you previously set</td>
            <td rowspan="2"><code>huskhomes.command.delhome</code></td>
            <td rowspan="2" align="center">✅</td>
        </tr>
        <tr>
            <td><code>/delhome all [confirm]</code></td>
            <td>Delete all of your homes</td>
        </tr>
        <tr>
            <td><code>/delhome &lt;owner_name.home_name&gt;</code></td>
            <td>Delete the home of another user</td>
            <td><code>huskhomes.command.delhome.other</code></td>
            <td align="center">❌</td>
        </tr>
        <!-- /edithome command -->
        <tr>
            <td rowspan="6"><code>/edithome</code></td>
            <td><code>/edithome &lt;name&gt;</code></td>
            <td>View and edit information about a home</td>
            <td><code>huskhomes.command.edithome</code></td>
            <td rowspan="5" align="center">✅</td>
        </tr>
        <tr>
            <td><code>/edithome &lt;name&gt; rename &lt;new_name&gt;</code></td>
            <td>Rename a home</td>
            <td><code>huskhomes.command.edithome.rename</code></td>
        </tr>
        <tr>
            <td><code>/edithome &lt;name&gt; description &lt;text&gt;</code></td>
            <td>Set a home's description</td>
            <td><code>huskhomes.command.edithome.description</code></td>
        </tr>
        <tr>
            <td><code>/edithome &lt;name&gt; relocate</code></td>
            <td>Move a home to your current position</td>
            <td><code>huskhomes.command.edithome.relocate</code></td>
        </tr>
        <tr>
            <td><code>/edithome &lt;name&gt; privacy [public|private]</code></td>
            <td>Set a home's privacy (make it public or private)</td>
            <td><code>huskhomes.command.edithome.privacy</code></td>
        </tr>
        <tr>
            <td><code>/edithome &lt;owner_name.home_name&gt; [args]</code></td>
            <td>View and edit another user's home</td>
            <td><code>huskhomes.command.edithome.other</code></td>
            <td align="center">❌</td>
        </tr>
        <!-- /phome command -->
        <tr>
            <td><code>/phome</code></td>
            <td><code>/phome [&lt;owner_name.home_name&gt;]</code></td>
            <td>Teleport to a public home</td>
            <td><code>huskhomes.command.phome</code></td>
            <td align="center">✅</td>
        </tr>
        <!-- /phomelist command -->
        <tr>
            <td><code>/phomelist</code></td>
            <td><code>/phomelist [page]</code></td>
            <td>View the list of public homes</td>
            <td><code>huskhomes.command.phomelist</code></td>
            <td align="center">✅</td>
        </tr>
        <!-- /warp command -->
        <tr><th colspan="5">Warp commands</th></tr>
        <tr>
            <td rowspan="2"><code>/warp</code></td>
            <td><code>/warp [name]</code></td>
            <td>Teleport to a warp</td>
            <td><code>huskhomes.command.warp</code>&dagger;</td>
            <td align="center">✅</td>
        </tr>
        <tr>
            <td><code>/warp &lt;name&gt; &lt;teleporter_name&gt;</code></td>
            <td>Teleport another online user to a warp</td>
            <td><code>huskhomes.command.warp.other</code>&dagger;</td>
            <td align="center">❌</td>
        </tr>
        <!-- /warplist command -->
        <tr>
            <td><code>/warplist</code></td>
            <td><code>/warplist [page]</code></td>
            <td>View the list of warps</td>
            <td><code>huskhomes.command.warplist</code>&dagger;</td>
            <td align="center">✅</td>
        </tr>
        <!-- /setwarp command -->
        <tr>
            <td><code>/setwarp</code></td>
            <td><code>/setwarp &lt;name&gt;</code></td>
            <td>Set a new warp with a name</td>
            <td><code>huskhomes.command.setwarp</code></td>
            <td align="center">❌</td>
        </tr>
        <!-- /delwarp command -->
        <tr>
            <td><code>/delwarp</code></td>
            <td><code>/delwarp &lt;name&gt;</code></td>
            <td>Delete a warp</td>
            <td><code>huskhomes.command.delwarp</code></td>
            <td align="center">❌</td>
        </tr>
        <!-- /editwarp command -->
        <tr>
            <td rowspan="4"><code>/editwarp</code></td>
            <td><code>/editwarp &lt;name&gt;</code></td>
            <td>View and edit information about a warp</td>
            <td><code>huskhomes.command.editwarp</code></td>
            <td rowspan="4" align="center">❌</td>
        </tr>
        <tr>
            <td><code>/editwarp &lt;name&gt; rename &lt;new_name&gt;</code></td>
            <td>Rename a warp</td>
            <td><code>huskhomes.command.editwarp.rename</code></td>
        </tr>
        <tr>
            <td><code>/editwarp &lt;name&gt; description &lt;text&gt;</code></td>
            <td>Set a warp's description</td>
            <td><code>huskhomes.command.editwarp.description</code></td>
        </tr>
        <tr>
            <td><code>/editwarp &lt;name&gt; relocate</code></td>
            <td>Move a warp to your current position</td>
            <td><code>huskhomes.command.editwarp.relocate</code></td>
        </tr>
        <!-- /spawn command -->
        <tr><th colspan="5">Spawn commands</th></tr>
        <tr>
            <td rowspan="2"><code>/spawn</code></td>
            <td><code>/spawn</code></td>
            <td>Teleport to the spawn position</td>
            <td><code>huskhomes.command.spawn</code></td>
            <td align="center">✅</td>
        </tr>
        <tr>
            <td><code>/spawn [teleporter_name]</code></td>
            <td>Teleport another online user to the spawn position</td>
            <td><code>huskhomes.command.spawn.other</code></td>
            <td align="center">❌</td>
        </tr>
        <!-- /setspawn command -->
        <tr>
            <td colspan="2"><code>/setspawn</code></td>
            <td>Set the spawn position to your current location</td>
            <td><code>huskhomes.command.setspawn</code></td>
            <td align="center">❌</td>
        </tr>
        <!-- /tp command -->
        <tr><th colspan="5">Teleport commands</th></tr>
        <tr>
            <td rowspan="4"><code>/tp</code></td>
            <td><code>/tp &lt;target_name&gt;</code></td>
            <td>Teleport to an online user</td>
            <td><code>huskhomes.command.tp</code></td>
            <td align="center">❌</td>
        </tr>
        <tr>
            <td><code>/tp &lt;teleporter_name&gt; &lt;target_name&gt;</code></td>
            <td>Teleport an online user to another online user</td>
            <td><code>huskhomes.command.tp.other</code></td>
            <td align="center">❌</td>
        </tr>
        <tr>
            <td><code>/tp &lt;(x) (y) (z) [yaw] [pitch] [world_name] [server_name]&gt;</code></td>
            <td>Teleport to a set of coordinates</td>
            <td><code>huskhomes.command.tp.coordinates</code></td>
            <td align="center">❌</td>
        </tr>
        <tr>
            <td><code>/tp &lt;teleporter_name&gt; &lt;coordinates&gt;</code></td>
            <td>Teleport an online user to a set of coordinates</td>
            <td><code>huskhomes.command.tp.other</code><br/><code>huskhomes.command.tp.coordinates</code></td>
            <td align="center">❌</td>
        </tr>
        <!-- /tphere command -->
        <tr>
            <td><code>/tphere</code></td>
            <td><code>/tphere &lt;username&gt;</code></td>
            <td>Teleport an online user to your position</td>
            <td><code>huskhomes.command.tphere</code></td>
            <td align="center">❌</td>
        </tr>
        <!-- /tpoffline command -->
        <tr>
            <td><code>/tpoffline</code></td>
            <td><code>/tpoffline &lt;username&gt;</code></td>
            <td>Teleport to where a user last logged out</td>
            <td><code>huskhomes.command.tpoffline</code></td>
            <td align="center">❌</td>
        </tr>
        <!-- /tpall command -->
        <tr>
            <td colspan="2"><code>/tpall</code></td>
            <td>Teleport everyone to your position</td>
            <td><code>huskhomes.command.tpall</code></td>
            <td align="center">❌</td>
        </tr>
        <!-- /tpa command -->
        <tr><th colspan="5">Teleport request commands</th></tr>
        <tr>
            <td><code>/tpa</code></td>
            <td><code>/tpa &lt;username&gt;</code></td>
            <td>Send a request to teleport to another online user</td>
            <td><code>huskhomes.command.tpa</code></td>
            <td align="center">✅</td>
        </tr>
        <!-- /tpahere command -->
        <tr>
            <td><code>/tpahere</code></td>
            <td><code>/tpahere &lt;username&gt;</code></td>
            <td>Send a request asking another online user to teleport to you</td>
            <td><code>huskhomes.command.tpahere</code></td>
            <td align="center">✅</td>
        </tr>
        <!-- /tpaall command -->
        <tr>
            <td colspan="2"><code>/tpaall</code></td>
            <td>Request that everyone teleports to you</td>
            <td><code>huskhomes.command.tpaall</code></td>
            <td align="center">❌</td>
        </tr>
        <!-- /tpaccept command -->
        <tr>
            <td rowspan="2"><code>/tpaccept</code></td>
            <td><code>/tpaccept</code></td>
            <td>Accept the last teleport request you received</td>
            <td rowspan="2"><code>huskhomes.command.tpaccept</code></td>
            <td rowspan="2" align="center">✅</td>
        </tr>
        <tr>
            <td><code>/tpaccept [username]</code></td>
            <td>Accept a teleport request from a specific user</td>
        </tr>
        <!-- /tpdecline command -->
        <tr>
            <td rowspan="2"><code>/tpdecline</code></td>
            <td><code>/tpdecline</code></td>
            <td>Decline the last teleport request you received</td>
            <td rowspan="2"><code>huskhomes.command.tpdecline</code></td>
            <td rowspan="2" align="center">✅</td>
        </tr>
        <tr>
            <td><code>/tpdecline [username]</code></td>
            <td>Decline a teleport request from a specific user</td>
        </tr>
        <!-- /tpignore command -->
        <tr>
            <td colspan="2"><code>/tpignore</code></td>
            <td>Toggle whether to ignore incoming teleport requests</td>
            <td><code>huskhomes.command.tpignore</code></td>
            <td align="center">✅</td>
        </tr>
        <!-- /rtp command -->
        <tr><th colspan="5">Random teleport command</th></tr>
        <tr>
            <td rowspan="5"><code>/rtp</code></td>
            <td><code>/rtp</code></td>
            <td>Teleport randomly into the wild in the current world</td>
            <td><code>huskhomes.command.rtp</code></td>
            <td align="center">✅</td>
        </tr>
        <tr>
            <td><code>/rtp &lt;player&gt;</code></td>
            <td>Teleport another player randomly into the wild</td>
            <td><code>huskhomes.command.rtp.other</code></td>
            <td align="center">❌</td>
        </tr>
        <tr>
            <td><code>/rtp &lt;world&gt;</code></td>
            <td>Teleport randomly in a specified world</td>
            <td><code>huskhomes.command.rtp.world</code></td>
            <td align="center">❌</td>
        </tr>
        <tr>
            <td><code>/rtp &lt;world&gt; &lt;server&gt;</code></td>
            <td>Teleport randomly in a specified world on a specified server</td>
            <td><code>huskhomes.command.rtp.world</code></td>
            <td align="center">❌</td>
        </tr>
        <tr>
            <td><code>/rtp &lt;server&gt;</code></td>
            <td>Teleport randomly in the current world on a specified server</td>
            <td><code>huskhomes.command.rtp.world</code></td>
            <td align="center">❌</td>
        </tr>
        <!-- /back command -->
        <tr><th colspan="5">Back teleport command</th></tr>
        <tr>
            <td rowspan="3" colspan="2"><code>/back</code></td>
            <td>Teleport to your last position (see below)</td>
            <td><code>huskhomes.command.back</code></td>
            <td align="center">✅</td>
        </tr>
        <tr>
            <td>Teleport back to where you last teleported from</td>
            <td><code>huskhomes.command.back.previous</code></td>
            <td align="center">✅</td>
        </tr>
        <tr>
            <td>Teleport back to where you last died</td>
            <td><code>huskhomes.command.back.death</code>&ddagger;</td>
            <td align="center">✅</td>
        </tr>
        <!-- /huskhomes command -->
        <tr><th colspan="5">Plugin management command</th></tr>
        <tr>
            <td rowspan="11"><code>/huskhomes</code></td>
            <td><code>/huskhomes</code></td>
            <td>Use plugin management commands</td>
            <td><code>huskhomes.command.huskhomes</code></td>
            <td align="center">✅</td>
        </tr>
        <tr>
            <td><code>/huskhomes about</code></td>
            <td>View the plugin about menu</td>
            <td><code>huskhomes.command.huskhomes.about</code></td>
            <td align="center">✅</td>
        </tr>
        <tr>
            <td><code>/huskhomes help [page]</code></td>
            <td>View the list of enabled plugin commands</td>
            <td><code>huskhomes.command.huskhomes.help</code></td>
            <td align="center">✅</td>
        </tr>
        <tr>
            <td><code>/huskhomes update</code></td>
            <td>Check for plugin updates</td>
            <td><code>huskhomes.command.huskhomes.update</code></td>
            <td align="center">❌</td>
        </tr>
        <tr>
            <td><code>/huskhomes reload</code></td>
            <td>Reload the plugin locales and config file</td>
            <td><code>huskhomes.command.huskhomes.reload</code></td>
            <td align="center">❌</td>
        </tr>
        <tr>
            <td><code>/huskhomes homeslots &lt;username&gt; [view|set|add|remove]</code></td>
            <td>View &amp; manage a player's home slots (requires economy hook)</td>
            <td><code>huskhomes.command.huskhomes.homeslots</code></td>
            <td align="center">❌</td>
        </tr>
        <tr>
            <td><code>/huskhomes import</code></td>
            <td>Import data from another plugin</td>
            <td><code>huskhomes.command.huskhomes.import</code></td>
            <td align="center">❌</td>
        </tr>
        <tr>
            <td><code>/huskhomes delete player &lt;username&gt; [confirm]</code></td>
            <td>Delete player data from the system database</td>
            <td rowspan="3"><code>huskhomes.command.huskhomes.delete</code></td>
            <td rowspan="3" align="center">❌</td>
        </tr>
        <tr>
            <td><code>/huskhomes delete homes &lt;world_name&gt; [server_name] [confirm]</code></td>
            <td>Delete all homes on a specific world and/or server</td>
        </tr>
        <tr>
            <td><code>/huskhomes delete warps &lt;world_name&gt; [server_name] [confirm]</code></td>
            <td>Delete all warps on a specific world and/or server</td>
        </tr>
        <tr>
            <td><code>/huskhomes status</code></td>
            <td>View the system status debug info screen.</td>
            <td><code>huskhomes.command.huskhomes.status</code></td>
            <td align="center">❌</td>
        </tr>
    </tbody>
</table>

### Notes
&dagger; &mdash; If [Permission Restricted Warps](restricted-warps) are in use, the  `huskhomes.warp.<name>` permission node will also be required to be able to list, use, edit and delete (but not set) warps. The `huskhomes.warp.*` wildcard permission grants the ability to access all warps.

&ddagger; &mdash; Requires `return_by_death` to be enabled in [`config.yml`](config-files).

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

## Disabling commands

If you'd like to disable a command, add it to the `disabled_commands` section of your config file as detailed below and the command will not be registered.

<details>
<summary>Disabling a command in config.yml</summary>

```yaml
# Disabled commands (e.g. ['/home', '/warp'] to disable /home and /warp)
disabled_commands: [ '/rtp' ]
```

</details>

## Home limits
You can modify the maximum number of homes, the allotment of free homes and the number of public homes a user can set through permission nodes.

* `huskhomes.max_homes.<amount>` — Determines the max number of homes a user can set
* `huskhomes.free_homes.<amount>` — Determines the allotment of homes the user can set for free, before they have to pay&dagger;
* `huskhomes.max_public_homes.<amount>` — Determines the maximum number of homes a user can make public

&dagger;Only effective on servers that make use of the economy hook.

If users have multiple permission nodes (i.e. from being in multiple permission groups), HuskHomes will accept the highest. If you prefer that the nodes _stack_, you can set the `stack_permission_limits` setting in the plugin config file to `true` (under `general`).

Note that these permission-set values override the values set in the plugin config (`max_homes`, `max_public_homes` under `general` and `free_homes` under `economy`).

## Teleport warmup times
You can change the teleport warmup time based on a permission node:

* `huskhomes.teleport_warmup.<seconds>` — Determines how long this player has to wait before teleporting.

HuskHomes will always take the highest node value present for this, regardless of the `stack_permission_limits` value.

## Bypass permission nodes

These permissions let you bypass teleportation warmup checks, cooldown, and economy checks

| Description                                | Permission                         | Default |
|--------------------------------------------|------------------------------------|:-------:|
| Bypass timed teleportation warmups&dagger; | `huskhomes.bypass_teleport_warmup` | Not set |
| Bypass [cooldown checks](cooldowns)        | `huskhomes.bypass_cooldowns`       | Not set |
| Bypass [economy checks](economy-hook)      | `huskhomes.bypass_economy_checks`  | Not set |

&dagger;This is not effective when the teleport warmup time is set to `<= 0` in the config file. This permission also bypasses the numerical teleport warmup time permission detailed above.
