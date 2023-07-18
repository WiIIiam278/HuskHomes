HuskHomes stores player data, homes, warps, and teleports in a database of your choosing. For cross-server setups, you'll need to configure a MySQL or MariaDB database to allow homes to be accessed globally.

## Database types
> **Warning:** There is currently no automatic way of migrating between _database_ types. Changing the database type will cause data to be lost.

| Type      | File or Server | Description                                                                 | Supports cross-server |
|:----------|----------------|:----------------------------------------------------------------------------|:---------------------:|
| `SQLITE`  | File           | A file-based database. This is the default (recommended) option.            |           ❌           |
| `H2`      | File           | A file-based database, similar to SQLITE, with slightly faster query times. |           ❌           |
| `MYSQL`   | Server         | A database hosted on a MySQL server.                                        |           ✅           |
| `MARIADB` | Server         | A database hosted on a MariaDB server.                                      |           ✅           |

### Cross-server
If you are using HuskHomes on a cross-server network, you will need to use a database type that supports cross-server setups. This is because cross-server setups require a single database to be shared between all servers so that HuskHomes can access the same data on each server.

## Configuring
To change the database type, navigate to your [`config.yml`](Config-Files) file and modify the properties under `database`.

<details>
<summary>Database options (config.yml)</summary>

```yaml
database:
  # Type of database to use (SQLITE, H2, MYSQL or MARIADB)
  type: SQLITE
  mysql:
    credentials:
      # Specify credentials here if you are using MYSQL or MARIADB as your database type
      host: localhost
      port: 3306
      database: HuskHomes
      username: root
      password: pa55w0rd
      parameters: ?autoReconnect=true&useSSL=false&useUnicode=true&characterEncoding=UTF-8
    connection_pool:
      # MYSQL / MARIADB database Hikari connection pool properties. Don't modify this unless you know what you're doing!
      size: 12
      idle: 12
      lifetime: 1800000
      keepalive: 30000
      timeout: 20000
```
</details>

### Credentials (MariaDB & MySQL)
You will need to specify the credentials (hostname, port, username, password and the database) if you are using MariaDB or MySQL. These credentials are used to connect to your database server.

Additionally, you can modify the HikariCP connection pool properties if you know what you're doing. The default values should be fine for most users.