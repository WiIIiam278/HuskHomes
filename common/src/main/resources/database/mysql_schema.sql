# Set the storage engine
SET DEFAULT_STORAGE_ENGINE = INNODB;

# Enable foreign key constraints
SET FOREIGN_KEY_CHECKS = 1;

# Create the positions table if it does not exist
CREATE TABLE IF NOT EXISTS `%positions_table%`
(
    `id`          integer      NOT NULL AUTO_INCREMENT,
    `x`           double       NOT NULL,
    `y`           double       NOT NULL,
    `z`           double       NOT NULL,
    `yaw`         float        NOT NULL,
    `pitch`       float        NOT NULL,
    `world_name`  varchar(255) NOT NULL,
    `world_uuid`  char(36)     NOT NULL,
    `server_name` varchar(255) NOT NULL,

    PRIMARY KEY (`id`)
) CHARACTER SET utf8
  COLLATE utf8_unicode_ci;

# Create the players table if it does not exist
CREATE TABLE IF NOT EXISTS `%players_table%`
(
    `uuid`              char(36)    NOT NULL UNIQUE,
    `username`          varchar(16) NOT NULL,
    `last_position`     integer              DEFAULT NULL,
    `offline_position`  integer              DEFAULT NULL,
    `respawn_position`  integer              DEFAULT NULL,
    `home_slots`        integer     NOT NULL DEFAULT 0,
    `ignoring_requests` boolean     NOT NULL DEFAULT FALSE,

    PRIMARY KEY (`uuid`),
    FOREIGN KEY (`last_position`) REFERENCES `%positions_table%` (`id`) ON DELETE SET NULL ON UPDATE NO ACTION,
    FOREIGN KEY (`offline_position`) REFERENCES `%positions_table%` (`id`) ON DELETE SET NULL ON UPDATE NO ACTION,
    FOREIGN KEY (`respawn_position`) REFERENCES `%positions_table%` (`id`) ON DELETE SET NULL ON UPDATE NO ACTION
) CHARACTER SET utf8
  COLLATE utf8_unicode_ci;

# Create the cooldowns table if it does not exist
CREATE TABLE IF NOT EXISTS `%cooldowns_table%`
(
    `id`              integer      NOT NULL AUTO_INCREMENT,
    `player_uuid`     char(36)     NOT NULL,
    `type`            varchar(255) NOT NULL,
    `start_timestamp` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `end_timestamp`   datetime     NOT NULL,

    PRIMARY KEY (`id`),
    FOREIGN KEY (`player_uuid`) REFERENCES `%players_table%` (`uuid`) ON DELETE CASCADE ON UPDATE CASCADE
) CHARACTER SET utf8
  COLLATE utf8_unicode_ci;

# Create the current cross-server teleports table if it does not exist
CREATE TABLE IF NOT EXISTS `%teleports_table%`
(
    `player_uuid`    char(36) NOT NULL UNIQUE,
    `destination_id` integer  NOT NULL,
    `type`           integer  NOT NULL DEFAULT 0,

    PRIMARY KEY (`player_uuid`),
    FOREIGN KEY (`player_uuid`) REFERENCES `%players_table%` (`uuid`) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (`destination_id`) REFERENCES `%positions_table%` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION
) CHARACTER SET utf8
  COLLATE utf8_unicode_ci;

# Create the saved positions table if it does not exist
CREATE TABLE IF NOT EXISTS `%saved_positions_table%`
(
    `id`          integer      NOT NULL AUTO_INCREMENT,
    `position_id` integer      NOT NULL,
    `name`        varchar(64)  NOT NULL,
    `description` varchar(255) NOT NULL,
    `tags`        mediumtext            DEFAULT NULL,
    `timestamp`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (`id`),
    FOREIGN KEY (`position_id`) REFERENCES `%positions_table%` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION
) CHARACTER SET utf8
  COLLATE utf8_unicode_ci;

# Create the homes table if it does not exist
CREATE TABLE IF NOT EXISTS `%homes_table%`
(
    `uuid`              char(36) NOT NULL UNIQUE,
    `saved_position_id` integer  NOT NULL,
    `owner_uuid`        char(36) NOT NULL,
    `public`            boolean  NOT NULL DEFAULT FALSE,

    PRIMARY KEY (`uuid`),
    FOREIGN KEY (`owner_uuid`) REFERENCES `%players_table%` (`uuid`) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (`saved_position_id`) REFERENCES `%saved_positions_table%` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION
) CHARACTER SET utf8
  COLLATE utf8_unicode_ci;

# Create the warps table if it does not exist
CREATE TABLE IF NOT EXISTS `%warps_table%`
(
    `uuid`              char(36) NOT NULL UNIQUE,
    `saved_position_id` integer  NOT NULL,

    PRIMARY KEY (`uuid`),
    FOREIGN KEY (`saved_position_id`) REFERENCES `%saved_positions_table%` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION
) CHARACTER SET utf8
  COLLATE utf8_unicode_ci;