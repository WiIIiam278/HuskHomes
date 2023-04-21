HuskHomes supports charging players' Vault economy balances for performing certain actions. This requires the Vault plugin and a compatible economy plugin.

To enable the economy hook, set `enabled` to `true` under the `economy` section of the config. Players with the `huskhomes.bypass.economy` permission bypass economy checks and don't need to pay for things.

## Home slots
With the economy hook enabled, players will need to pay for home slots beyond their initial "free" allotment.

The price will be levied against the player when they attempt to set home and have no free slots left. A warning message will notify the player of this when they have set their last free home slot.

You can configure the number of 'free home slots' a user gets using the `free_home_slots` setting in the `economy` section. The default is `5`.

## Economy actions
You can set prices to charge for these actions.

| Action                 | Description                                 | Default Price |
|------------------------|---------------------------------------------|---------------|
| `ADDITIONAL_HOME_SLOT` | When a user wants to buy another home slot  | `$100.00`     |
| `MAKE_HOME_PUBLIC`     | When a user wants to make their home public | `$50.00`      |
| `RANDOM_TELEPORT`      | When a user executes /rtp                   | `$25.00`      |
| `BACK_COMMAND`         | When a user executes /back                  | `$0.00`       |