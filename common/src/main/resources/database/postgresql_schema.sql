/* Create the positions table if it does not exist */
CREATE TABLE IF NOT EXISTS "%positions_table%"
(
    "id"          SERIAL,
    "x"           double precision NOT NULL,
    "y"           double precision NOT NULL,
    "z"           double precision NOT NULL,
    "yaw"         real             NOT NULL,
    "pitch"       real             NOT NULL,
    "world_name"  varchar(255)     NOT NULL,
    "world_uuid"  char(36)         NOT NULL,
    "server_name" varchar(255)     NOT NULL,

    PRIMARY KEY ("id")
);

/* Create the players table if it does not exist */
CREATE TABLE IF NOT EXISTS "%players_table%"
(
    "uuid"              char(36)    NOT NULL UNIQUE,
    "username"          varchar(16) NOT NULL,
    "last_position"     integer              DEFAULT NULL,
    "offline_position"  integer              DEFAULT NULL,
    "respawn_position"  integer              DEFAULT NULL,
    "home_slots"        integer     NOT NULL DEFAULT 0,
    "ignoring_requests" boolean     NOT NULL DEFAULT FALSE,

    PRIMARY KEY ("uuid"),
    FOREIGN KEY ("last_position") REFERENCES "%positions_table%" ("id") ON DELETE SET NULL ON UPDATE NO ACTION,
    FOREIGN KEY ("offline_position") REFERENCES "%positions_table%" ("id") ON DELETE SET NULL ON UPDATE NO ACTION,
    FOREIGN KEY ("respawn_position") REFERENCES "%positions_table%" ("id") ON DELETE SET NULL ON UPDATE NO ACTION
);

/* Create the cooldowns table if it does not exist */
CREATE TABLE IF NOT EXISTS "%cooldowns_table%"
(
    "id"              SERIAL,
    "player_uuid"     char(36)     NOT NULL,
    "type"            varchar(255) NOT NULL,
    "start_timestamp" timestamp     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "end_timestamp"   timestamp     NOT NULL,

    PRIMARY KEY ("id"),
    FOREIGN KEY ("player_uuid") REFERENCES "%players_table%" ("uuid") ON DELETE CASCADE ON UPDATE CASCADE
);

/* Create the current cross-server teleports table if it does not exist */
CREATE TABLE IF NOT EXISTS "%teleports_table%"
(
    "player_uuid"    char(36) NOT NULL UNIQUE,
    "destination_id" integer  NOT NULL,
    "type"           integer  NOT NULL DEFAULT 0,

    PRIMARY KEY ("player_uuid"),
    FOREIGN KEY ("player_uuid") REFERENCES "%players_table%" ("uuid") ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY ("destination_id") REFERENCES "%positions_table%" ("id") ON DELETE CASCADE ON UPDATE NO ACTION
);

/* Create the saved positions table if it does not exist */
CREATE TABLE IF NOT EXISTS "%saved_positions_table%"
(
    "id"          SERIAL,
    "position_id" integer      NOT NULL,
    "name"        varchar(64)  NOT NULL,
    "description" varchar(255) NOT NULL,
    "tags"        text         DEFAULT NULL,
    "timestamp"   timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY ("id"),
    FOREIGN KEY ("position_id") REFERENCES "%positions_table%" ("id") ON DELETE CASCADE ON UPDATE NO ACTION
);

/* Create the homes table if it does not exist */
CREATE TABLE IF NOT EXISTS "%homes_table%"
(
    "uuid"              char(36) NOT NULL UNIQUE,
    "saved_position_id" integer  NOT NULL,
    "owner_uuid"        char(36) NOT NULL,
    "public"            boolean  NOT NULL DEFAULT FALSE,

    PRIMARY KEY ("uuid"),
    FOREIGN KEY ("owner_uuid") REFERENCES "%players_table%" ("uuid") ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY ("saved_position_id") REFERENCES "%saved_positions_table%" ("id") ON DELETE CASCADE ON UPDATE NO ACTION
);

/* Create the warps table if it does not exist */
CREATE TABLE IF NOT EXISTS "%warps_table%"
(
    "uuid"              char(36) NOT NULL UNIQUE,
    "saved_position_id" integer  NOT NULL,

    PRIMARY KEY ("uuid"),
    FOREIGN KEY ("saved_position_id") REFERENCES "%saved_positions_table%" ("id") ON DELETE CASCADE ON UPDATE NO ACTION
);