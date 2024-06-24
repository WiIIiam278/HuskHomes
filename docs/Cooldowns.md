HuskHomes supports enforcing a cooldown against certain actions. Players must wait a certain amount of time between performing the action, otherwise the action will not be allowed. The cooldown will be enforced just before the action is performed, and the player will be notified of the remaining cooldown time if they attempt to perform the action before the cooldown has expired.

## Configuring cooldowns
Cooldowns can be configured in the `cooldowns` section of [`config.yml`](config-files). To enable cooldowns, set `enabled` to `true` and define `cooldown_times` for actions you want to apply cooldowns to. The cooldown time is an integer value, defined in seconds.

### Bypassing cooldowns
Players with the `huskhomes.bypass_cooldowns` [permission node](commands) bypass cooldowns and can perform them immediately.

### Table of actions
| Action                    | Description                                                 |
|---------------------------|-------------------------------------------------------------|
| `ADDITIONAL_HOME_SLOT`    | When a user wants to buy another home slot                  |
| `MAKE_HOME_PUBLIC`        | When a user wants to make their home public                 |
| `RANDOM_TELEPORT`         | When a user executes /rtp                                   |
| `BACK_COMMAND`            | When a user executes /back to return to their last position |
| `HOME_TELEPORT`           | When a user executes /home to teleport to a home            |
| `PUBLIC_HOME_TELEPORT`    | When a user uses /phome to teleport to a public home        |
| `WARP_TELEPORT`           | When a user uses /warp to teleport to a warp                |
| `SPAWN_TELEPORT`          | When a user uses /spawn to teleport to spawn                |
| `SEND_TELEPORT_REQUEST`   | When a user sends a teleport request                        |
| `ACCEPT_TELEPORT_REQUEST` | When a user accepts an incoming teleport request            |

### Example config
Cooldowns are defined under `cooldown_times` in the `cooldowns` section of [`config.yml`](config-files). By default, only a cooldown for `random_teleport` is defined. Add the other actions to this section of the file and associate a cooldown with them to enable them.

<details>
<summary>Defining cooldowns (config.yml)</summary>

```yaml
# Action cooldown settings. Docs: https://william278.net/docs/huskhomes/cooldowns
cooldowns:
  # Whether to apply a cooldown between performing certain actions
  enabled: true
  # Map of cooldown times to actions
  cooldown_times:
    RANDOM_TELEPORT: 600
```
</details>
