HuskHomes supports restricting certain actions behind an economic cost. Players must have enough money in their account to perform the action, otherwise the action will not be allowed. The cost of the action will be deducted from the player's account just before the action is performed.

## Setup
<details>
<summary>Spigot & Paper setup</summary>

> **Applies to:** Spigot, Paper

To enable the Economy Hook on a Spigot/Paper server, install both [Vault](https://www.spigotmc.org/resources/vault.34315/) and a compatible economy plugin. Then, set `enabled` to `true` under the `economy` section of [`config.yml`](config-files).
</details>

<details>
<summary>Fabric setup</summary>

> **Applies to:** Fabric

To enable the Economy Hook on a Fabric server, the [Impactor Economy](https://modrinth.com/mod/impactor) mod must be installed to provide an API for plugins to perform economy operations. Then, set `enabled` to `true` under the `economy` section of [`config.yml`](config-files).
</details>

### Bypassing economy checks
Players with the `huskhomes.bypass_economy_checks` [permission node](commands) bypass economy checks and can perform economy actions without paying.

## Home slots
With the economy hook enabled, players will need to pay for home slots beyond their initial "free" allotment.

The price will be levied against the player when they attempt to set home and have no free slots left. A warning message will notify the player of this when they have set their last free home slot.

You can configure the number of 'free home slots' a user gets using the `free_home_slots` setting in the `economy` section of the config. The default is `5`.

## Economy actions
You can set the economy cost for the following actions in the `costs` section of the `config.yml` file. Note that this section by default only has the `additional_home_slot`, `make_home_public` and `random_teleport` actions defined. Add the other actions to this section of the file and associate a price with them to enable them.

### Table of actions
| Action                    | Description                                                 | Default Cost |
|---------------------------|-------------------------------------------------------------|-------------:|
| `ADDITIONAL_HOME_SLOT`    | When a user wants to buy another home slot                  |     `100.00` |
| `MAKE_HOME_PUBLIC`        | When a user wants to make their home public                 |      `50.00` |
| `RANDOM_TELEPORT`         | When a user executes /rtp                                   |      `25.00` |
| `BACK_COMMAND`            | When a user executes /back to return to their last position |       `0.00` |
| `HOME_TELEPORT`           | When a user executes /home to teleport to a home            |       `0.00` |
| `PUBLIC_HOME_TELEPORT`    | When a user uses /phome to teleport to a public home        |       `0.00` |
| `WARP_TELEPORT`           | When a user uses /warp to teleport to a warp                |       `0.00` |
| `SPAWN_TELEPORT`          | When a user uses /spawn to teleport to spawn                |       `0.00` |
| `SEND_TELEPORT_REQUEST`   | When a user sends a teleport request                        |       `0.00` |
| `ACCEPT_TELEPORT_REQUEST` | When a user accepts an incoming teleport request            |       `0.00` |

### Example config
> **Warning:** You must specify a decimal monetary value in the config.yml. (i.e. `100.00` is valid, but `100` is not.)

Economy costs are defined under `costs` in the `economy` section of [`config.yml`](config-files).

<details>
<summary>Defining economy costs (config.yml)</summary>

```yaml
# Economy settings. Docs: https://william278.net/docs/huskhomes/economy-hook
economy:
  # Enable economy plugin integration (requires Vault and a compatible Economy plugin)
  enabled: true
  # Map of economy actions to costs.
  economy_costs:
    ADDITIONAL_HOME_SLOT: 100.0
    MAKE_HOME_PUBLIC: 50.0
    RANDOM_TELEPORT: 25.0
```
</details>
