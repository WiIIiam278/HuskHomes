HuskHomes supports enforcing a cooldown against certain actions. Players must wait a certain amount of time between performing the action, otherwise the action will not be allowed. The cooldown will be enforced just before the action is performed, and the player will be notified of the remaining cooldown time if they attempt to perform the action before the cooldown has expired.

## Configuring cooldowns
Cooldowns can be configured in the `cooldowns` section of [`config.yml`](config-files). To enable cooldowns, set `enabled` to `true` and define `cooldown_times` for actions you want to apply cooldowns to. The cooldown time is an integer value, defined in seconds.

### Bypassing cooldowns
Players with the `huskhomes.bypass_cooldowns` [permission node](commands) bypass cooldowns and can perform them immediately.

### Table of actions
| Action                    | Description                                                 |
|---------------------------|-------------------------------------------------------------|
| `additional_home_slot`    | When a user wants to buy another home slot                  |
| `make_home_public`        | When a user wants to make their home public                 |
| `random_teleport`         | When a user executes /rtp                                   |
| `back_command`            | When a user executes /back to return to their last position |
| `home_teleport`           | When a user executes /home to teleport to a home            |
| `public_home_teleport`    | When a user uses /phome to teleport to a public home        |
| `warp_teleport`           | When a user uses /warp to teleport to a warp                |
| `spawn_teleport`          | When a user uses /spawn to teleport to spawn                |
| `send_teleport_request`   | When a user sends a teleport request                        |
| `accept_teleport_request` | When a user accepts an incoming teleport request            |

### Example config
Cooldowns are defined under `cooldown_times` in the `cooldowns` section of [`config.yml`](config-files). By default, only a cooldown for `random_teleport` is defined. Add the other actions to this section of the file and associate a cooldown with them to enable them.

<details>
<summary>Defining cooldowns (config.yml)</summary>

```yaml
cooldowns:
  # Whether to apply a cooldown between performing certain actions
  enabled: true
  # Set a cooldown between performing actions (in seconds). Docs: https://william278.net/docs/huskhomes/cooldowns/
  cooldown_times:
    additional_home_slot: 0
    make_home_public: 0
    random_teleport: 600
    back_command: 0
    home_teleport: 0
    public_home_teleport: 0
    warp_teleport: 0
    spawn_teleport: 0
    send_teleport_request: 0
    accept_teleport_request: 0
```
</details>
