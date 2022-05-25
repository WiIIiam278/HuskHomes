/* Ensure data gets correctly encoded in UTF-8 */
PRAGMA encoding = 'UTF-8';

/* Ensure foreign key constraints are enforced */
PRAGMA foreign_keys = ON;

/* Create the positions table if it does not exist */
CREATE TABLE IF NOT EXISTS `%positions_table%`
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

/* Create the player data table if it does not exist */
CREATE TABLE IF NOT EXISTS `%players_table%`
(
    `uuid`              char(36)    NOT NULL UNIQUE,
    `username`          varchar(16) NOT NULL,
    `last_position`     integer              DEFAULT NULL,
    `offline_position`  integer              DEFAULT NULL,
    `respawn_position`  integer              DEFAULT NULL,
    `home_slots`        integer     NOT NULL DEFAULT 0,
    `ignoring_requests` boolean     NOT NULL DEFAULT FALSE,
    `rtp_cooldown`      datetime    NOT NULL DEFAULT 0,

    PRIMARY KEY (`uuid`),
    FOREIGN KEY (`last_position`) REFERENCES `%positions_table%` (`id`) ON DELETE SET NULL ON UPDATE NO ACTION,
    FOREIGN KEY (`offline_position`) REFERENCES `%positions_table%` (`id`) ON DELETE SET NULL ON UPDATE NO ACTION,
    FOREIGN KEY (`respawn_position`) REFERENCES `%positions_table%` (`id`) ON DELETE SET NULL ON UPDATE NO ACTION
);

/* Create the current cross-server teleports table if it does not exist */
CREATE TABLE IF NOT EXISTS `%teleports_table%`
(
    `player_uuid`    char(36) NOT NULL UNIQUE,
    `destination_id` integer  NOT NULL,

    PRIMARY KEY (`player_uuid`),
    FOREIGN KEY (`player_uuid`) REFERENCES `%players_table%` (`uuid`) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (`destination_id`) REFERENCES `%positions_table%` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION
);

/* Create the position metadata table if it does not exist */
CREATE TABLE IF NOT EXISTS `%position_metadata_table%`
(
    `id`          integer      NOT NULL,
    `name`        varchar(64)  NOT NULL,
    `description` varchar(255) NOT NULL,
    `timestamp`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (`id`)
);

/* Create the homes table if it does not exist */
CREATE TABLE IF NOT EXISTS `%homes_table%`
(
    `uuid`        char(36) NOT NULL UNIQUE,
    `owner_uuid`  char(36) NOT NULL,
    `position_id` integer  NOT NULL,
    `metadata_id` integer  NOT NULL,
    `public`      boolean  NOT NULL DEFAULT FALSE,

    PRIMARY KEY (`uuid`),
    FOREIGN KEY (`owner_uuid`) REFERENCES `%players_table%` (`uuid`) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (`position_id`) REFERENCES `%positions_table%` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION,
    FOREIGN KEY (`metadata_id`) REFERENCES `%position_metadata_table%` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION
);

/* Create the warps table if it does not exist */
CREATE TABLE IF NOT EXISTS `%warps_table%`
(
    `uuid`        char(36) NOT NULL UNIQUE,
    `position_id` integer  NOT NULL,
    `metadata_id` integer  NOT NULL,

    PRIMARY KEY (`uuid`),
    FOREIGN KEY (`position_id`) REFERENCES `%positions_table%` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION,
    FOREIGN KEY (`metadata_id`) REFERENCES `%position_metadata_table%` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION
);