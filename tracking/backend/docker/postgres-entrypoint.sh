#!/bin/sh
set -eu

require_non_blank() {
  if ! printf '%s' "$2" | grep -q '[^[:space:]]'; then
    echo "$1 must contain a non-whitespace character" >&2
    exit 1
  fi
}

require_non_blank POSTGRES_PASSWORD "${POSTGRES_PASSWORD-}"
require_non_blank DUNGEON_TRACKING_OWNER_DATABASE_PASSWORD "${DUNGEON_TRACKING_OWNER_DATABASE_PASSWORD-}"
require_non_blank DUNGEON_TRACKING_RUNTIME_DATABASE_PASSWORD "${DUNGEON_TRACKING_RUNTIME_DATABASE_PASSWORD-}"

exec /usr/local/bin/docker-entrypoint.sh "$@"
