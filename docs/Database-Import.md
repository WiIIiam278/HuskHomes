HuskHomes provides a powerful database import system that allows you to migrate data between different database types seamlessly. This feature is particularly useful when upgrading from SQLite to MySQL, or migrating between any supported database types.

## Overview

The database import system allows you to transfer all your HuskHomes data from one database type to another without losing any information. This includes:

- **Users** - All player data and settings
- **Homes** - Both public and private homes
- **Warps** - All server warps
- **User Positions** - Last positions, offline positions, and respawn points
- **Cooldowns** - Active player cooldowns

## Supported Database Types

The import system supports migration between any of the following database types:

- **SQLITE** - SQLite database (file-based)
- **H2** - H2 database (file-based)
- **MYSQL** - MySQL database
- **MARIADB** - MariaDB database
- **POSTGRESQL** - PostgreSQL database

## Prerequisites

Before performing a database import, ensure that:

1. **Target database is configured** in your `config.yml`
2. **Database credentials are correct** (for MySQL/MariaDB/PostgreSQL)
3. **Target database is accessible** and running
4. **You have a backup** of your current data
5. **Proper permissions** are set up

### Example MySQL Configuration

```yaml
database:
  type: MYSQL
  credentials:
    host: localhost
    port: 3306
    database: huskhomes
    username: huskhomes_user
    password: your_secure_password
    parameters: "?autoReconnect=true&useSSL=false&useUnicode=true&characterEncoding=UTF-8"
```

## Command Usage

### Basic Syntax

```
/huskhomes importdb <source_type> <target_type> [confirm]
```

### Parameters

- `<source_type>` - The database type you're importing FROM
- `<target_type>` - The database type you're importing TO
- `[confirm]` - Required confirmation parameter to execute the import

### Permission

To use the database import command, you need the following permission:

```
huskhomes.command.huskhomes.importdb
```

## Step-by-Step Tutorial

### Step 1: Preview the Import

First, run the command without the `confirm` parameter to see the warning and instructions:

```bash
/huskhomes importdb SQLITE MYSQL
```

This will display:
- A warning about the operation
- Information about potential data overwriting
- The exact command to run for confirmation

### Step 2: Execute the Import

After reviewing the warning, confirm the import by adding the `confirm` parameter:

```bash
/huskhomes importdb SQLITE MYSQL confirm
```

### Step 3: Monitor Progress

The system will provide real-time feedback during the import process:

```
[HuskHomes] Starting database import from SQLite to MySQL…
[HuskHomes] Connecting to databases…
[HuskHomes] Starting data import process…
[HuskHomes] Discovering users in source database…
[HuskHomes] Found 15 users to import
[HuskHomes] Importing users…
[HuskHomes] ✓ Imported 15 users
[HuskHomes] Importing warps…
[HuskHomes] ✓ Imported 4 warps
[HuskHomes] Importing homes…
[HuskHomes] ✓ Imported 45 homes
[HuskHomes] Importing user positions…
[HuskHomes] ✓ Imported 23 positions
[HuskHomes] Importing cooldowns…
[HuskHomes] ✓ Imported 12 cooldowns
[HuskHomes] ✓ Database import completed successfully!
[HuskHomes] Imported: 15 users, 45 homes, 4 warps, 23 positions, 12 cooldowns
```

## Common Migration Scenarios

### SQLite to MySQL Migration

This is the most common migration scenario when servers grow and need a more robust database solution.

```bash
# 1. Configure MySQL in config.yml
# 2. Preview the import
/huskhomes importdb SQLITE MYSQL

# 3. Execute the import
/huskhomes importdb SQLITE MYSQL confirm

# 4. Update config.yml to use MySQL as primary database
# 5. Restart the server
```

### H2 to PostgreSQL Migration

For servers moving to PostgreSQL for better performance and features.

```bash
# 1. Configure PostgreSQL in config.yml
# 2. Execute the migration
/huskhomes importdb H2 POSTGRESQL confirm
```

### MySQL to SQLite (Downgrade)

Sometimes you might want to move back to a simpler database solution.

```bash
# 1. Configure SQLite in config.yml (or use default)
# 2. Execute the migration
/huskhomes importdb MYSQL SQLITE confirm
```

## Import Process Details

The import system follows a specific order to ensure data integrity:

1. **User Discovery** - Finds all users in the source database
2. **User Import** - Imports user accounts and settings
3. **Warp Import** - Transfers all server warps
4. **Home Import** - Imports all player homes (public and private)
5. **Position Import** - Transfers user position data
6. **Cooldown Import** - Imports active cooldowns

## Safety Features

### Double Confirmation System

The import system requires explicit confirmation to prevent accidental execution:

- First command shows warnings and instructions
- Second command with `confirm` parameter actually executes the import

### Data Validation

Before starting the import, the system validates:

- Database type compatibility
- Source and target database connections
- Permission levels
- Configuration correctness

### Non-Destructive Operation

The import process:

- Does not modify the source database
- Only adds data to the target database
- Preserves all original data relationships
- Maintains data integrity throughout the process

## Troubleshooting

### Common Issues and Solutions

#### "Failed to connect to source database"

**Cause**: Source database configuration issues or file not found.

**Solution**:
- Verify the source database file exists (for SQLite/H2)
- Check database credentials and connectivity
- Ensure the source database type matches your current configuration

#### "Failed to connect to target database"

**Cause**: Target database configuration problems.

**Solution**:
- Verify target database configuration in `config.yml`
- Test database connectivity manually
- Ensure the target database exists and is accessible
- Check firewall and network settings

#### "Invalid database type"

**Cause**: Unsupported or misspelled database type.

**Solution**:
- Use only supported types: `SQLITE`, `H2`, `MYSQL`, `MARIADB`, `POSTGRESQL`
- Ensure correct capitalization
- Check for typos in the command

#### "Source and target database types cannot be the same"

**Cause**: Attempting to import from and to the same database type.

**Solution**:
- Use different database types for source and target
- Verify you're specifying the correct migration path

### Performance Considerations

- **Large datasets** may take several minutes to import
- **Network latency** can affect remote database imports
- **Server resources** are used during the import process
- **Async execution** prevents server blocking during import

## Best Practices

### Before Import

1. **Create a full backup** of your current database
2. **Test the target database connection** manually
3. **Verify sufficient disk space** for the target database
4. **Schedule during low-traffic periods** to minimize impact
5. **Inform players** about potential brief service interruption

### During Import

1. **Monitor the progress** messages for any issues
2. **Don't restart the server** during the import process
3. **Keep the console accessible** for troubleshooting
4. **Avoid running other intensive operations** simultaneously

### After Import

1. **Verify the import results** by checking player data
2. **Update your config.yml** to use the new database as primary
3. **Restart the server** to ensure all changes take effect
4. **Test core functionality** (homes, warps, teleportation)
5. **Keep the old database** as backup until you're confident

## Advanced Usage

### Scripted Migrations

For server administrators managing multiple servers, you can script the migration process:

```bash
#!/bin/bash
# Example migration script

echo "Starting HuskHomes database migration..."

# Execute the import
screen -S minecraft -p 0 -X stuff "/huskhomes importdb SQLITE MYSQL confirm$(printf \\r)"

# Wait for completion (adjust time as needed)
sleep 300

# Update configuration and restart
# (Add your server restart logic here)

echo "Migration completed!"
```

### Batch Operations

When migrating multiple servers, consider:

- **Staggered migrations** to avoid overwhelming database servers
- **Monitoring tools** to track progress across servers
- **Rollback procedures** in case of issues
- **Communication plans** for player notifications

## Security Considerations

### Database Credentials

- Use **strong passwords** for database accounts
- Create **dedicated database users** with minimal required permissions
- **Restrict network access** to database servers
- **Use SSL/TLS** for remote database connections when possible

### Access Control

- **Limit the import permission** to trusted administrators only
- **Monitor command usage** through server logs
- **Audit database access** regularly
- **Use principle of least privilege** for all database operations

## Integration with Other Systems

### Backup Systems

The database import feature works well with:

- **Automated backup solutions**
- **Database replication systems**
- **Disaster recovery procedures**
- **Version control for configurations**

### Monitoring

Consider integrating with:

- **Server monitoring tools** to track import progress
- **Database monitoring** to watch performance during imports
- **Log aggregation systems** for centralized troubleshooting
- **Alerting systems** for import completion or failure notifications

## Conclusion

The HuskHomes database import system provides a robust, safe, and user-friendly way to migrate your data between different database types. With its comprehensive safety features, real-time progress reporting, and extensive validation, you can confidently upgrade your database infrastructure without losing any player data.

Remember to always backup your data before performing any migration, and test the process in a development environment when possible. The import system is designed to be reliable and safe, but proper preparation ensures the smoothest possible migration experience.
