-- Set the storage engine
SET DEFAULT_STORAGE_ENGINE = InnoDB;

-- Enable foreign key constraints
SET FOREIGN_KEY_CHECKS = 1;

-- Create the metadata table if it does not exist
CREATE TABLE IF NOT EXISTS `%meta_data%`
(
    `schema_version` integer NOT NULL,

    PRIMARY KEY (`schema_version`)
);

-- Create the positions table if it does not exist
CREATE TABLE IF NOT EXISTS `%positions_table%`
(
    `id`          INTEGER      NOT NULL AUTO_INCREMENT,
    `x`           DOUBLE       NOT NULL,
    `y`           DOUBLE       NOT NULL,
    `z`           DOUBLE       NOT NULL,
    `yaw`         FLOAT        NOT NULL,
    `pitch`       FLOAT        NOT NULL,
    `world_name`  VARCHAR(255) NOT NULL,
    `world_uuid`  CHAR(36)     NOT NULL,
    `server_name` VARCHAR(255) NOT NULL,

    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- Create the players table if it does not exist
CREATE TABLE IF NOT EXISTS `%players_table%`
(
    `uuid`              CHAR(36)    NOT NULL,
    `username`          VARCHAR(16) NOT NULL,
    `last_position`     INTEGER              DEFAULT NULL,
    `offline_position`  INTEGER              DEFAULT NULL,
    `respawn_position`  INTEGER              DEFAULT NULL,
    `home_slots`        INTEGER     NOT NULL DEFAULT 0,
    `ignoring_requests` BOOLEAN     NOT NULL DEFAULT FALSE,

    PRIMARY KEY (`uuid`),
    FOREIGN KEY (`last_position`) REFERENCES `%positions_table%` (`id`) ON DELETE SET NULL ON UPDATE NO ACTION,
    FOREIGN KEY (`offline_position`) REFERENCES `%positions_table%` (`id`) ON DELETE SET NULL ON UPDATE NO ACTION,
    FOREIGN KEY (`respawn_position`) REFERENCES `%positions_table%` (`id`) ON DELETE SET NULL ON UPDATE NO ACTION
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
CREATE INDEX IF NOT EXISTS `%players_table%_username` ON `%players_table%` (`username`);

-- Create the cooldowns table if it does not exist
CREATE TABLE IF NOT EXISTS `%cooldowns_table%`
(
    `id`              INTEGER      NOT NULL AUTO_INCREMENT,
    `player_uuid`     CHAR(36)     NOT NULL,
    `type`            VARCHAR(255) NOT NULL,
    `start_timestamp` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `end_timestamp`   DATETIME     NOT NULL,

    PRIMARY KEY (`id`),
    FOREIGN KEY (`player_uuid`) REFERENCES `%players_table%` (`uuid`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
CREATE INDEX IF NOT EXISTS `%cooldowns_table%_player_uuid` ON `%cooldowns_table%` (`player_uuid`);

-- Create the current cross-server teleports table if it does not exist
CREATE TABLE IF NOT EXISTS `%teleports_table%`
(
    `player_uuid`    CHAR(36) NOT NULL,
    `destination_id` INTEGER  NOT NULL,
    `type`           INTEGER  NOT NULL DEFAULT 0,

    PRIMARY KEY (`player_uuid`),
    FOREIGN KEY (`player_uuid`) REFERENCES `%players_table%` (`uuid`) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (`destination_id`) REFERENCES `%positions_table%` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- Create the saved positions table if it does not exist
CREATE TABLE IF NOT EXISTS `%saved_positions_table%`
(
    `id`          INTEGER      NOT NULL AUTO_INCREMENT,
    `position_id` INTEGER      NOT NULL,
    `name`        VARCHAR(64)  NOT NULL,
    `description` VARCHAR(255) NOT NULL,
    `tags`        MEDIUMTEXT            DEFAULT NULL,
    `timestamp`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (`id`),
    FOREIGN KEY (`position_id`) REFERENCES `%positions_table%` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
CREATE INDEX IF NOT EXISTS `%saved_positions_table%_name` ON `%saved_positions_table%` (`name`);

-- Create the homes table if it does not exist
CREATE TABLE IF NOT EXISTS `%homes_table%`
(
    `uuid`              CHAR(36) NOT NULL,
    `saved_position_id` INTEGER  NOT NULL,
    `owner_uuid`        CHAR(36) NOT NULL,
    `public`            BOOLEAN  NOT NULL DEFAULT FALSE,

    PRIMARY KEY (`uuid`),
    FOREIGN KEY (`owner_uuid`) REFERENCES `%players_table%` (`uuid`) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (`saved_position_id`) REFERENCES `%saved_positions_table%` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
CREATE INDEX IF NOT EXISTS `%homes_table%_owner_uuid` ON `%homes_table%` (`owner_uuid`);

-- Create the warps table if it does not exist
CREATE TABLE IF NOT EXISTS `%warps_table%`
(
    `uuid`              CHAR(36) NOT NULL,
    `saved_position_id` INTEGER  NOT NULL,

    PRIMARY KEY (`uuid`),
    FOREIGN KEY (`saved_position_id`) REFERENCES `%saved_positions_table%` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;