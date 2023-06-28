HuskHomes supports restricting certain actions behind an economic cost. Players must have enough money in their account to perform the action, otherwise the action will not be allowed. The cost of the action will be deducted from the player's account just before the action is performed.

## Setup
<details>
<summary>Spigot & Paper setup</summary>

> **Applies to:** Spigot, Paper

To enable the Economy Hook on Sponge server, install both [Vault](https://www.spigotmc.org/resources/vault.34315/) and a compatible economy plugin. Then, set `enabled` to `true` under the `economy` section of [`config.yml`](config-files).
</details>

<details>
<summary>Sponge setup</summary>

> **Applies to:** Sponge

To enable the Economy Hook on Sponge server, you require a mod installed for managing player economy accounts through the Sponge economy API. Then, set `enabled` to `true` under the `economy` section of [`config.yml`](config-files).
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
| `additional_home_slot`    | When a user wants to buy another home slot                  |    `$100.00` |
| `make_home_public`        | When a user wants to make their home public                 |     `$50.00` |
| `random_teleport`         | When a user executes /rtp                                   |     `$25.00` |
| `back_command`            | When a user executes /back to return to their last position |      `$0.00` |
| `home_teleport`           | When a user executes /home to teleport to a home            |      `$0.00` |
| `public_home_teleport`    | When a user uses /phome to teleport to a public home        |      `$0.00` |
| `warp_teleport`           | When a user uses /warp to teleport to a warp                |      `$0.00` |
| `spawn_teleport`          | When a user uses /spawn to teleport to spawn                |      `$0.00` |
| `send_teleport_request`   | When a user sends a teleport request                        |      `$0.00` |
| `accept_teleport_request` | When a user accepts an incoming teleport request            |      `$0.00` |


### Example config
> **Warning:** You must specify a decimal monetary value in the config.yml. (i.e. `100.00` is valid, but `100` is not.)

Economy costs are defined under `costs` in the `economy` section of [`config.yml`](config-files).

<details>
<summary>Defining economy costs (config.yml)</summary>

```yaml
economy:
  # Enable economy plugin integration (requires Vault)
  enabled: true
  # Charge money for perform certain actions. Docs: https://william278.net/docs/huskhomes/economy-hook/
  costs:
      additional_home_slot: 100.0
      make_home_public: 50.0
      random_teleport: 25.0
      back_command: 0.0
      home_teleport: 0.0
      public_home_teleport: 0.0
      warp_teleport: 0.0
      spawn_teleport: 0.0
      send_teleport_request: 0.0
      accept_teleport_request: 0.0
```
</details>