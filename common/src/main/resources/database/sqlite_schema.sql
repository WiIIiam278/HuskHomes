-- Create the metadata table if it does not exist
CREATE TABLE IF NOT EXISTS `%meta_data%`
(
    `schema_version` integer NOT NULL,

    PRIMARY KEY (`schema_version`)
);

-- Create the positions table if it does not exist
CREATE TABLE IF NOT EXISTS `%position_data%`
(
    `id`          integer      NOT NULL,
    `x`           double       NOT NULL,
    `y`           double       NOT NULL,
    `z`           double       NOT NULL,
    `yaw`         float        NOT NULL,
    `pitch`       float        NOT NULL,
    `world_name`  varchar(255) NOT NULL,
    `world_uuid`  char(36)     NOT NULL,
    `server_name` varchar(255) NOT NULL,

    PRIMARY KEY (`id`)
);

-- Create the players table if it does not exist
CREATE TABLE IF NOT EXISTS `%player_data%`
(
    `uuid`              char(36)    NOT NULL UNIQUE,
    `username`          varchar(16) NOT NULL,
    `last_position`     integer              DEFAULT NULL,
    `offline_position`  integer              DEFAULT NULL,
    `respawn_position`  integer              DEFAULT NULL,
    `home_slots`        integer     NOT NULL DEFAULT 0,
    `ignoring_requests` boolean     NOT NULL DEFAULT FALSE,

    PRIMARY KEY (`uuid`),
    FOREIGN KEY (`last_position`) REFERENCES `%position_data%` (`id`) ON DELETE SET NULL ON UPDATE NO ACTION,
    FOREIGN KEY (`offline_position`) REFERENCES `%position_data%` (`id`) ON DELETE SET NULL ON UPDATE NO ACTION,
    FOREIGN KEY (`respawn_position`) REFERENCES `%position_data%` (`id`) ON DELETE SET NULL ON UPDATE NO ACTION
);
CREATE INDEX IF NOT EXISTS `%player_data%_username` ON `%player_data%` (`username`);

-- Create the cooldowns table if it does not exist
CREATE TABLE IF NOT EXISTS `%player_cooldowns_data%`
(
    `id`              integer      NOT NULL,
    `player_uuid`     char(36)     NOT NULL,
    `type`            varchar(255) NOT NULL,
    `start_timestamp` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `end_timestamp`   datetime     NOT NULL,

    PRIMARY KEY (`id`),
    FOREIGN KEY (`player_uuid`) REFERENCES `%player_data%` (`uuid`) ON DELETE CASCADE ON UPDATE CASCADE
);
CREATE INDEX IF NOT EXISTS `%player_cooldowns_data%_player_uuid` ON `%player_cooldowns_data%` (`player_uuid`);

-- Create the current cross-server teleports table if it does not exist
CREATE TABLE IF NOT EXISTS `%teleport_data%`
(
    `player_uuid`    char(36) NOT NULL UNIQUE,
    `destination_id` integer  NOT NULL,
    `type`           integer  NOT NULL DEFAULT 0,

    PRIMARY KEY (`player_uuid`),
    FOREIGN KEY (`player_uuid`) REFERENCES `%player_data%` (`uuid`) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (`destination_id`) REFERENCES `%position_data%` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION
);

-- Create the saved positions table if it does not exist
CREATE TABLE IF NOT EXISTS `%saved_position_data%`
(
    `id`          integer      NOT NULL,
    `position_id` integer      NOT NULL,
    `name`        varchar(64)  NOT NULL,
    `description` varchar(255) NOT NULL,
    `tags`        mediumtext            DEFAULT NULL,
    `timestamp`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (`id`),
    FOREIGN KEY (`position_id`) REFERENCES `%position_data%` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION
);
CREATE INDEX IF NOT EXISTS `%saved_position_data%_name` ON `%saved_position_data%` (`name`);

-- Create the homes table if it does not exist
CREATE TABLE IF NOT EXISTS `%home_data%`
(
    `uuid`              char(36) NOT NULL UNIQUE,
    `saved_position_id` integer  NOT NULL,
    `owner_uuid`        char(36) NOT NULL,
    `public`            boolean  NOT NULL DEFAULT FALSE,

    PRIMARY KEY (`uuid`),
    FOREIGN KEY (`owner_uuid`) REFERENCES `%player_data%` (`uuid`) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (`saved_position_id`) REFERENCES `%saved_position_data%` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION
);
CREATE INDEX IF NOT EXISTS `%home_data%_owner_uuid` ON `%home_data%` (`owner_uuid`);

-- Create the warps table if it does not exist
CREATE TABLE IF NOT EXISTS `%warp_data%`
(
    `uuid`              char(36) NOT NULL UNIQUE,
    `saved_position_id` integer  NOT NULL,

    PRIMARY KEY (`uuid`),
    FOREIGN KEY (`saved_position_id`) REFERENCES `%saved_position_data%` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION
);