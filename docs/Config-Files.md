This page contains the configuration structure for HuskHomes.

## Configuration structure
📁 `plugins/HuskHomes/` (Spigot) OR `config/huskhomes/` (Fabric)
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
# Database settings
database:
  # Type of database to use (SQLITE, H2, MYSQL, MARIADB, or POSTGRESQL)
  type: SQLITE
  # Specify credentials here if you are using MYSQL, MARIADB, or POSTGRESQL
  credentials:
    host: localhost
    port: 3306
    database: huskhomes
    username: root
    password: pa55w0rd
    parameters: ?autoReconnect=true&useSSL=false&useUnicode=true&characterEncoding=UTF-8
  # MYSQL / MARIADB / POSTGRESQL database Hikari connection pool properties
  # Don't modify this unless you know what you're doing!
  pool_options:
    size: 12
    idle: 12
    lifetime: 1800000
    keep_alive: 30000
    timeout: 20000
  # Names of tables to use on your database. Don't modify this unless you know what you're doing!
  table_names:
    PLAYER_DATA: huskhomes_users
    PLAYER_COOLDOWNS_DATA: huskhomes_user_cooldowns
    POSITION_DATA: huskhomes_position_data
    SAVED_POSITION_DATA: huskhomes_saved_positions
    HOME_DATA: huskhomes_homes
    WARP_DATA: huskhomes_warps
    TELEPORT_DATA: huskhomes_teleports
# General settings
general:
  # The maximum homes a user can create. Override with the huskhomes.max_homes.<number> permission.
  max_homes: 10
  # The maximum public homes a user can create. Override with the huskhomes.max_public_homes.<number> permission.
  max_public_homes: 10
  # Whether permission limits (i.e. huskhomes.max_homes.<number>) should stack if the user inherits multiple nodes.
  stack_permission_limits: false
  # Whether users require a permission (huskhomes.warp.<warp_name>) to use warps
  permission_restrict_warps: false
  # How long a player has to stand still and not take damage for when teleporting (in seconds) 
  teleport_warmup_time: 5
  # Whether the teleport warmup timer should be cancelled if the player takes damage
  teleport_warmup_cancel_on_damage: true
  # Whether the teleport warmup timer should be cancelled if the player moves
  teleport_warmup_cancel_on_move: true
  # Where the teleport warmup timer should display (CHAT, ACTION_BAR, TITLE, SUBTITLE or NONE)
  teleport_warmup_display: ACTION_BAR
  # How long the player should be invulnerable for after teleporting (in seconds)
  teleport_invulnerability_time: 0
  # How long before received teleport requests expire (in seconds)
  teleport_request_expiry_time: 60
  # Whether /tpahere should use the location of the sender when sent. Docs: https://william278.net/docs/huskhomes/strict-tpahere/
  strict_tpa_here_requests: true
  # How many items should be displayed per-page in chat menu lists
  list_items_per_page: 12
  # Whether the user should always be put back at the /spawn point when they die (ignores beds/respawn anchors)
  always_respawn_at_spawn: false
  # Whether teleportation should be carried out async (ensuring chunks load before teleporting)
  teleport_async: true
  # Settings for home and warp names
  names:
    # Whether running /sethome <name> or /setwarp <name> when one already exists should overwrite.
    overwrite_existing: true
    # Whether home or warp names should be case insensitive (i.e. allow /home HomeOne & /home homeone)
    case_insensitive: false
    # Whether home and warp names should be restricted to a regex filter.Set this to false to allow full UTF-8 names (i.e. allow /home 你好).
    restrict: true
    # Regex which home and warp names must match. Names have a max length of 16 characters
    regex: '[a-zA-Z0-9-_]*'
  # Settings for home and warp descriptions
  descriptions:
    # Whether home/warp descriptions should be restricted to a regex filter. Set this to true to restrict UTF-8 usage.
    restrict: false
    # Regex which home and warp descriptions must match. A hard max length of 256 characters is enforced
    regex: \A\p{ASCII}*\z
  # Settings for the /back command
  back_command:
    # Whether /back should work to teleport the user to where they died
    return_by_death: true
    # Whether /back should work with other plugins that use the PlayerTeleportEvent (can conflict)
    save_on_teleport_event: false
    # List of world names where the /back command cannot RETURN the player to.
    # A user's last position won't be updated if they die or teleport from these worlds, but they still will be able to use the command while IN the world
    restricted_worlds: []
  # Settings for sound effects
  sound_effects:
    # Whether to play sound effects
    enabled: true
    # Map of sound effect actions to types
    types:
      TELEPORTATION_COMPLETE: entity.enderman.teleport
      TELEPORTATION_WARMUP: block.note_block.banjo
      TELEPORTATION_CANCELLED: entity.item.break
      TELEPORT_REQUEST_RECEIVED: entity.experience_orb.pickup
# Cross-server settings
cross_server:
  # Whether to enable cross-server mode for teleporting across your proxy network.
  enabled: false
  # The cluster ID, for if you're networking multiple separate groups of HuskHomes-enabled servers.
  # Do not change unless you know what you're doing
  cluster_id: main
  # Type of network message broker to ues for cross-server networking (PLUGIN_MESSAGE or REDIS)
  broker_type: PLUGIN_MESSAGE
  # Settings for if you're using REDIS as your message broker
  redis:
    host: localhost
    port: 6379
    # Password for your Redis server. Leave blank if you're not using a password.
    password: ''
    use_ssl: false
    # Settings for if you're using Redis Sentinels.
    # If you're not sure what this is, please ignore this section.
    sentinel:
      master_name: ''
      # List of host:port pairs
      nodes: []
      password: ''
  # Define a single global /spawn for your network via a warp. Docs: https://william278.net/docs/huskhomes/global-spawn/
  global_spawn:
    # Whether to define a single global /spawn for your network via a warp.
    enabled: false
    # The name of the warp to use as the global spawn.
    warp_name: Spawn
  # Whether player respawn positions should work cross-server. Docs: https://william278.net/docs/huskhomes/global-respawning/
  global_respawning: false
# Random teleport (/rtp) settings.
rtp:
  # Radial region around the /spawn position where players CAN be randomly teleported.
  # If no /spawn has been set, (0, 0) will be used instead.
  region:
    min: 500
    max: 5000
  # Mean of the normal distribution used to calculate the distance from the center of the world
  distribution_mean: 0.75
  # Standard deviation of the normal distribution for distributing players randomly
  distribution_standard_deviation: 2.0
  # List of worlds in which /rtp is disabled. Please note that /rtp does not work well in the nether.
  restricted_worlds:
    - world_nether
    - world_the_end
# Action cooldown settings. Docs: https://william278.net/docs/huskhomes/cooldowns
cooldowns:
  # Whether to apply a cooldown between performing certain actions
  enabled: true
  # Map of cooldown times to actions
  cooldown_times:
    RANDOM_TELEPORT: 600
# Economy settings. Docs: https://william278.net/docs/huskhomes/economy-hook
economy:
  # Enable economy plugin integration (requires Vault and a compatible Economy plugin)
  enabled: true
  # Map of economy actions to costs.
  economy_costs:
    ADDITIONAL_HOME_SLOT: 100.0
    MAKE_HOME_PUBLIC: 50.0
    RANDOM_TELEPORT: 25.0
  # Specify how many homes players can set for free, before they need to pay for more slots
  free_home_slots: 5
# Web map hook settings. Docs: https://william278.net/docs/huskhomes/map-hooks
map_hook:
  # Display public homes/warps on your Dynmap, BlueMap or Pl3xMap
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
# ┃     HuskHomes - Server ID    ┃
# ┃    Developed by William278   ┃
# ┣━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
# ┣╸ This file should contain the ID of this server as defined in your proxy config.
# ┣╸ If you join it using /server alpha, then set it to 'alpha' (case-sensitive)
# ┗╸ You only need to touch this if you're using cross-server mode.

name: beta
```

</details>
