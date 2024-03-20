HuskHomes (v4.0.5+) can register a hook providing a number of placeholders that will be replaced with their appropriate values.

On Spigot/Paper, this requires [PlaceholderAPI](https://github.com/PlaceholderAPI/PlaceholderAPI); on Fabric, [Fabric PlaceholderAPI](https://modrinth.com/mod/placeholder-api) is needed instead.

## List of placeholders
| Placeholder                        | Description                                                         | Example Value       |
|------------------------------------|---------------------------------------------------------------------|---------------------|
| `%huskhomes_homes_count%`          | The number of homes this user has set                               | 3                   |
| `%huskhomes_max_homes%`            | The maximum number of homes this user can set                       | 10                  |
| `%huskhomes_max_public_homes%`     | The number of homes this user can make public                       | 5                   |
| `%huskhomes_free_home_slots%`      | The number of homes this user can make for free&dagger;             | 5                   |
| `%huskhomes_home_slots%`           | The number of additional home slots this user has purchased&dagger; | 2                   |
| `%huskhomes_homes_list%`           | A comma-separated list of this user's homes                         | home, castle, tower |
| `%huskhomes_public_homes_count%`   | The number of homes this user has set to public                     | 3                   |
| `%huskhomes_public_homes_list%`    | A comma-separated list of this user's public homes                  | castle, tower       |
| `%huskhomes_ignoring_tp_requests%` | Whether this user is ignoring teleport requests                     | true                |

&dagger;Only effective on servers that make use of the [[Economy Hook]].