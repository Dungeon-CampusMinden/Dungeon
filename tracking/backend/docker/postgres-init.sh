#!/bin/sh
set -eu

owner_password=$(tr -d '\r\n' < /run/secrets/tracking_owner_password)
runtime_password=$(tr -d '\r\n' < /run/secrets/tracking_runtime_password)

if [ -z "$owner_password" ] || [ -z "$runtime_password" ]; then
  echo "Tracking database role secrets must not be empty" >&2
  exit 1
fi

psql --set=ON_ERROR_STOP=1 \
  --username "$POSTGRES_USER" \
  --dbname "$POSTGRES_DB" \
  --set=owner_password="$owner_password" \
  --set=runtime_password="$runtime_password" <<'SQL'
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
