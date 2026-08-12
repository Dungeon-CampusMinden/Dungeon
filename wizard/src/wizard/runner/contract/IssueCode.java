package wizard.runner.contract;

/** Stable issue-code coverage for the language-neutral project-validation contract. */
public enum IssueCode {
  /** Asset bytes do not match their declared media type. */
  ASSET_CONTENT_MISMATCH,
  /** A declared asset is unused. */
  ASSET_DECLARED_UNUSED,
  /** An asset file in the project is unreferenced. */
  ASSET_FILE_UNREFERENCED,
  /** An asset's content hash does not match its path. */
  ASSET_HASH_MISMATCH,
  /** A referenced asset is missing. */
  ASSET_MISSING,
  /** Asset paths collide after portable normalization. */
  ASSET_PATH_DUPLICATE,
  /** An asset path is unsafe. */
  ASSET_PATH_UNSAFE,
  /** The DEER format version is unsupported. */
  FORMAT_VERSION_UNSUPPORTED,
  /** The riddle graph contains a cycle. */
  GRAPH_CYCLE,
  /** A graph edge is invalid. */
  GRAPH_EDGE_INVALID,
  /** A graph node has no path to the end. */
  GRAPH_NODE_NO_PATH_TO_END,
  /** A graph node is unreachable from the start. */
  GRAPH_NODE_UNREACHABLE,
  /** The graph does not match the supported profile. */
  GRAPH_PROFILE_INVALID,
  /** A riddle does not have the required graph binding. */
  GRAPH_RIDDLE_UNREACHABLE,
  /** An entity identifier is duplicated. */
  ID_DUPLICATE,
  /** Input changed while the read-only Runner pipeline was in progress. */
  INPUT_CHANGED_DURING_RUN,
  /** The DEER file exceeds its byte limit. */
  INPUT_DEER_TOO_LARGE,
  /** The DEER file cannot be read. */
  INPUT_DEER_UNREADABLE,
  /** The Wizard project root is invalid. */
  INPUT_PROJECT_INVALID,
  /** The DEER file starts with a UTF-8 byte-order mark. */
  INPUT_UTF8_BOM,
  /** The DEER file is not valid UTF-8. */
  INPUT_UTF8_INVALID,
  /** An unexpected internal failure occurred. */
  INTERNAL_ERROR,
  /** A JSON object contains a duplicate key. */
  JSON_DUPLICATE_KEY,
  /** The DEER document is not valid JSON. */
  JSON_PARSE_INVALID,
  /** The DEER document contains invalid Unicode. */
  JSON_UNICODE_INVALID,
  /** The player-count range is invalid. */
  PLAYER_COUNT_INVALID,
  /** A referenced identifier does not exist. */
  REFERENCE_UNKNOWN,
  /** Input exceeds a Runner validation or runtime capacity. */
  RUNNER_CAPACITY_EXCEEDED,
  /** The DEER document violates its schema. */
  SCHEMA_INVALID,
  /** Surface cardinality is invalid. */
  SURFACE_CARDINALITY_INVALID,
  /** A surface is incompatible with its binding. */
  SURFACE_INCOMPATIBLE,
  /** A surface has invalid ownership. */
  SURFACE_OWNERSHIP_INVALID,
  /** Player-facing text exceeds its warning threshold. */
  TEXT_LONG
}
