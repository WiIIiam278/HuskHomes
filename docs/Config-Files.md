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
# ┣╸ Information: https://william278.net/project/huskhomes/
# ┣╸ Config Help: https://william278.net/docs/huskhomes/config-files/
# ┗╸ Documentation: https://william278.net/docs/huskhomes/
# Locale of the default language file to use. Docs: https://william278.net/docs/huskhomes/translations
language: en-gb
# Whether to automatically check for plugin updates on startup
check_for_updates: true
database:
  # Type of database to use (MYSQL, SQLITE)
  type: SQLITE
  mysql:
    credentials:
      # Specify credentials here if you are using MYSQL as your database type
      host: localhost
      port: 3306
      database: HuskHomes
      username: root
      password: pa55w0rd
      parameters: ?autoReconnect=true&useSSL=false&useUnicode=true&characterEncoding=UTF-8
    connection_pool:
      # MYSQL database Hikari connection pool properties. Don't modify this unless you know what you're doing!
      size: 12
      idle: 12
      lifetime: 1800000
      keepalive: 30000
      timeout: 20000
  # Names of tables to use on your database. Don't modify this unless you know what you're doing!
  table_names:
    position_data: huskhomes_position_data
    warp_data: huskhomes_warps
    saved_position_data: huskhomes_saved_positions
    player_cooldowns_data: huskhomes_user_cooldowns
    home_data: huskhomes_homes
    teleport_data: huskhomes_teleports
    player_data: huskhomes_users
general:
  # The maximum homes a user can create. Override with the huskhomes.max_homes.<number> permission.
  max_homes: 10
  # The maximum public homes a user can create. Override with the huskhomes.max_public_homes.<number> permission.
  max_public_homes: 10
  # Whether permission limits (i.e. huskhomes.max_homes.<number>) should stack if the user inherits multiple nodes.
  stack_permission_limits: false
  # Whether users require a permission (huskhomes.command.warp.<warp_name>) to use warps
  permission_restrict_warps: false
  # Whether running /sethome <name> or /setwarp <name> when a home/warp already exists should overwrite it.
  overwrite_existing_homes_warps: true
  # How long a player has to stand still and not take damage for when teleporting (in seconds)
  teleport_warmup_time: 5
  # Where the teleport warmup timer should display (CHAT, ACTION_BAR, TITLE, SUBTITLE or NONE)
  teleport_warmup_display: ACTION_BAR
  # How long before received teleport requests expire (in seconds)
  teleport_request_expiry_time: 60
  # Whether /tpahere should use the location of the sender when sent. Docs: https://william278.net/docs/huskhomes/strict-tpahere/
  strict_tpa_here_requests: true
  # Whether home or warp names should be case insensitive (i.e. allow /home HomeOne and /home homeone)
  case_insensitive_names: false
  # Whether home or warp names should allow UTF-8 characters (i.e. allow /home 你好)
  allow_unicode_names: false
  # Whether home or warp descriptions should allow UTF-8 characters
  allow_unicode_descriptions: true
  # Whether /back should work to teleport the user to where they died
  back_command_return_by_death: true
  # Whether /back should work with other plugins that use the PlayerTeleportEvent (this can cause conflicts)
  back_command_save_teleport_event: false
  # How many items should be displayed per-page in chat menu lists
  list_items_per_page: 12
  # Whether teleportation should be carried out asynchronously (ensuring chunks load before teleporting)
  asynchronous_teleports: true
  # Whether to play sound effects
  play_sound_effects: true
  # Which sound effects to play for various actions
  sound_effects:
    teleportation_cancelled: entity.item.break
    teleportation_warmup: block.note_block.banjo
    teleportation_complete: entity.enderman.teleport
    teleport_request_received: entity.experience_orb.pickup
  # Whether to provide modern, rich TAB suggestions for commands (if available)
  brigadier_tab_completion: true
cross_server:
  # Enable teleporting across your proxy network. Requires database type to be MYSQL
  enabled: false
  # The type of message broker to use for cross-server communication. Options: PLUGIN_MESSAGE, REDIS
  messenger_type: PLUGIN_MESSAGE
  # Specify a common ID for grouping servers running HuskHomes on your proxy. Don't modify this unless you know what you're doing!
  cluster_id: ''
  global_spawn:
    # Define a single global /spawn for your network via a warp. Docs: https://william278.net/docs/huskhomes/global-spawn/
    enabled: false
    # The name of the warp to use as the global spawn.
    warp_name: Spawn
  # Whether player respawn positions should work cross-server. Docs: https://william278.net/docs/huskhomes/global-respawning/
  global_respawning: false
  redis_credentials:
    # Specify credentials here if you are using REDIS as your messenger_type. Docs: https://william278.net/docs/huskhomes/redis-support/
    host: localhost
    port: 6379
    password: ''
    use_ssl: false
rtp:
  # Radius around the spawn point in which players cannot be random teleported to
  radius: 5000
  # Radius of the spawn area in which players cannot be random teleported to
  spawn_radius: 500
  # Mean of the normal distribution used to calculate the distance from the center of the world
  distribution_mean: 0.75
  # Standard deviation of the normal distribution used to calculate the distance from the center of the world
  distribution_deviation: 2.0
  # List of worlds in which /rtp is disabled. Please note that /rtp does not work well in the nether.
  restricted_worlds:
    - world_nether
    - world_the_end
cooldowns:
  # Whether to apply a cooldown between performing certain actions
  enabled: true
  # Set a cooldown between performing actions (in seconds). Docs: https://william278.net/docs/huskhomes/cooldowns/
  cooldown_times:
    random_teleport: 600
economy:
  # Enable economy plugin integration (requires Vault)
  enabled: false
  # Specify how many homes players can set for free, before they need to pay for more slots
  free_home_slots: 5
  # Charge money for perform certain actions. Docs: https://william278.net/docs/huskhomes/economy-hook/
  costs:
    make_home_public: 50.0
    additional_home_slot: 100.0
    random_teleport: 25.0
map_hook:
  # Display public homes/warps on your Dynmap, BlueMap or Pl3xMap. Docs: https://william278.net/docs/huskhomes/map-hooks
  enabled: true
  # Show public homes on the web map
  show_public_homes: true
  # Show warps on the web map
  show_warps: true
# List of commands to disable (e.g. ['/home', '/warp'] to disable /home and /warp)
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