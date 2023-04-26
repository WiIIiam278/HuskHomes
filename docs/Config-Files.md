This page contains the configuration structure for HuskHomes.

## Configuration structure
📁 `plugins/HuskHomes/` (Spigot) OR `config/huskhomes/` (Fabric, Sponge)
  - 📄 `config.yml`: General plugin configuration
  - 📄 `server.yml`: (Cross-server setups only) Server ID configuration
  - 📄 `spawn.yml`: Local saved server spawn position. Use /setspawn to generate this file
  - 📄 `messages-xx-xx.yml`: Plugin locales, formatted in MineDown (see [[Translations]])

## Example files
<details>
<summary>config.yml</summary>

```yaml
# ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
# ┃       HuskHomes Config       ┃
# ┃    Developed by William278   ┃
# ┣━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
# ┣╸ Information: https://william278.net/project/huskhomes
# ┗╸ Documentation: https://william278.net/docs/huskhomes
language: en-gb
check_for_updates: true
database:
  # Database connection settings
  type: SQLITE
  mysql:
    credentials:
      host: localhost
      port: 3306
      database: HuskHomes
      username: root
      password: pa55w0rd
      parameters: ?autoReconnect=true&useSSL=false&useUnicode=true&characterEncoding=UTF-8
    connection_pool:
      # MySQL connection pool properties
      size: 12
      idle: 12
      lifetime: 1800000
      keepalive: 30000
      timeout: 20000
  table_names:
    player_data: huskhomes_users
    saved_position_data: huskhomes_saved_positions
    position_data: huskhomes_position_data
    home_data: huskhomes_homes
    warp_data: huskhomes_warps
    teleport_data: huskhomes_teleports
general:
  # General plugin settings
  max_homes: 10
  max_public_homes: 10
  stack_permission_limits: false
  permission_restrict_warps: false
  overwrite_existing_homes_warps: true
  teleport_warmup_time: 5
  teleport_warmup_display: ACTION_BAR
  teleport_request_expiry_time: 60
  strict_tpa_here_requests: true
  allow_unicode_names: false
  allow_unicode_descriptions: true
  back_command_return_by_death: true
  back_command_save_teleport_event: false
  list_items_per_page: 12
  asynchronous_teleports: true
  play_sound_effects: true
  sound_effects:
    teleportation_cancelled: entity.item.break
    teleportation_warmup: block.note_block.banjo
    teleportation_complete: entity.enderman.teleport
    teleport_request_received: entity.experience_orb.pickup
  brigadier_tab_completion: true
cross_server:
  # Enable teleporting across proxied servers. Requires MySQL
  enabled: false
  messenger_type: PLUGIN_MESSAGE
  cluster_id: ''
  global_spawn:
    enabled: false
    warp_name: Spawn
  global_respawning: false
  redis_credentials:
    host: localhost
    port: 6379
    password: ''
    use_ssl: false
rtp:
  # Random teleport (/rtp) command settings
  cooldown_length: 10
  radius: 5000
  spawn_radius: 500
  distribution_mean: 0.75
  distribution_deviation: 2.0
  restricted_worlds:
  - world_nether
  - world_the_end
economy:
  # Charge for certain actions (requires Vault)
  enabled: false
  # Use this currency for payments (works only with RedisEconomy), defaults to Vault currency
  redis_economy_name: vault
  free_home_slots: 5
  costs:
    back_command: 0.0
    additional_home_slot: 100.0
    random_teleport: 25.0
    make_home_public: 50.0
map_hook:
  # Display public homes/warps on your web map (supports Dynmap and BlueMap)
  enabled: true
  show_public_homes: true
  show_warps: true
# Disabled commands (e.g. ['/home', '/warp'] to disable /home and /warp)
disabled_commands: []
```
</details>

<details>
<summary>spawn.yml</summary>

You should generate this file in-game with the `/setspawn` command.
```yaml
# ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
# ┃ Server /spawn location cache ┃
# ┃ Edit in-game using /setspawn ┃
# ┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
x: 0.0
y: 64.0
z: 0.0
yaw: 180.0
pitch: 0.0
world_name: world
world_uuid: 00000000-0000-0000-0000-000000000000
```
</details>

<details>
<summary>server.yml</summary>

This file is only present if your server uses cross-server mode to run HuskHomes on a proxy network.
```yaml
# ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
# ┃  HuskHomes Server ID config  ┃
# ┃    Developed by William278   ┃
# ┣━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
# ┣╸ This file should contain the ID of this server as defined in your proxy config.
# ┣╸ If you join it using /server alpha, then set it to 'alpha' (case-sensitive)
# ┗╸ You only need to touch this if you're using cross-server mode.
name: beta
```

</details>