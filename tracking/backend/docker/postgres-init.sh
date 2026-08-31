#!/bin/sh
set -eu

require_non_blank() {
  if ! printf '%s' "$2" | grep -q '[^[:space:]]'; then
    echo "$1 must contain a non-whitespace character" >&2
    exit 1
  fi
}

require_non_blank DUNGEON_TRACKING_OWNER_DATABASE_PASSWORD "${DUNGEON_TRACKING_OWNER_DATABASE_PASSWORD-}"
require_non_blank DUNGEON_TRACKING_RUNTIME_DATABASE_PASSWORD "${DUNGEON_TRACKING_RUNTIME_DATABASE_PASSWORD-}"

psql --set=ON_ERROR_STOP=1 \
  --username "$POSTGRES_USER" \
  --dbname "$POSTGRES_DB" \
  --set=owner_password="$DUNGEON_TRACKING_OWNER_DATABASE_PASSWORD" \
  --set=runtime_password="$DUNGEON_TRACKING_RUNTIME_DATABASE_PASSWORD" <<'SQL'
CREATE ROLE dungeon_tracking_owner
    LOGIN PASSWORD :'owner_password'
    NOSUPERUSER NOCREATEDB NOCREATEROLE NOREPLICATION;
CREATE ROLE dungeon_tracking_runtime
    LOGIN PASSWORD :'runtime_password'
    NOSUPERUSER NOCREATEDB NOCREATEROLE NOREPLICATION;

ALTER DATABASE dungeon_tracking OWNER TO dungeon_tracking_owner;
REVOKE CONNECT, TEMPORARY ON DATABASE dungeon_tracking FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM PUBLIC;
ALTER SCHEMA public OWNER TO dungeon_tracking_owner;

GRANT CONNECT ON DATABASE dungeon_tracking TO dungeon_tracking_runtime;
GRANT USAGE ON SCHEMA public TO dungeon_tracking_runtime;
SQL
