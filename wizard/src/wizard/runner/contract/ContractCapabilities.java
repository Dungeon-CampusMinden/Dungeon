package wizard.runner.contract;

import java.util.Set;

/** Immutable V0.4 Runner formats, capabilities, and operational limits. */
public final class ContractCapabilities {
  /** Supported DEER input versions. */
  public static final Set<String> DEER_FORMAT_VERSIONS = Set.of("0.4");

  /** Maximum DEER file size. */
  public static final int MAX_DEER_BYTES = 1_048_576;

  /** Maximum number of riddles. */
  public static final int MAX_RIDDLES = 64;

  /** Maximum number of authored graph edges. */
  public static final int MAX_GRAPH_EDGES = 4_096;

  /** Maximum number of resources. */
  public static final int MAX_RESOURCES = 512;

  /** Maximum number of hints. */
  public static final int MAX_HINTS = 512;

  /** Maximum number of referenced assets. */
  public static final int MAX_REFERENCED_ASSETS = 128;

  /** Maximum size of one asset. */
  public static final int MAX_ASSET_BYTES = 16_777_216;

  /** Maximum combined size of referenced assets. */
  public static final int MAX_REFERENCED_ASSET_BYTES = 67_108_864;

  /** Maximum image dimension. */
  public static final int MAX_IMAGE_DIMENSION = 8_192;

  /** Maximum decoded image pixels. */
  public static final int MAX_IMAGE_PIXELS = 32_000_000;

  /** Maximum entries below the custom asset directory. */
  public static final int MAX_ASSET_DIRECTORY_ENTRIES = 1_024;

  /** Warning threshold for player-facing text. */
  public static final int MAX_PLAYER_FACING_TEXT_WARNING_CODE_POINTS = 2_000;

  /** Warning threshold for hint text. */
  public static final int MAX_HINT_TEXT_WARNING_CODE_POINTS = 800;

  private ContractCapabilities() {}
}
