package feature.puzzle;

import engine.Entity;
import engine.utils.components.path.SimpleIPath;
import feature.components.ItemComponent;
import java.util.HashMap;
import java.util.Map;

/** Shared item metadata and client texture preparation for networked puzzle-piece world items. */
public final class PuzzleNetworkSync {

  /** Spawn/snapshot metadata key containing the deterministic puzzle ID. */
  public static final String PUZZLE_ID = "puzzlePiece.puzzleId";

  /** Spawn/snapshot metadata key containing the source image path. */
  public static final String IMAGE_PATH = "puzzlePiece.imagePath";

  /** Spawn/snapshot metadata key containing the piece count. */
  public static final String PIECE_COUNT = "puzzlePiece.pieceCount";

  /** Spawn/snapshot metadata key containing the deterministic slicing seed. */
  public static final String SEED = "puzzlePiece.seed";

  /** Spawn metadata key containing this fragment's index. */
  public static final String PIECE_INDEX = "puzzlePiece.pieceIndex";

  /** Spawn metadata key containing the compatible puzzle-family identifier. */
  public static final String PIECE_FAMILY_ID = "puzzlePiece.pieceFamilyId";

  static {
    PuzzlePieceItem.ensureRegistration();
  }

  private PuzzleNetworkSync() {}

  /**
   * Returns the identifying metadata for a puzzle-piece world item.
   *
   * @param entity entity to inspect
   * @return empty metadata for other entities
   */
  public static Map<String, String> metadata(Entity entity) {
    ItemComponent component = entity.fetch(ItemComponent.class).orElse(null);
    if (component == null || !(component.item() instanceof PuzzlePieceItem piece)) {
      return Map.of();
    }
    Map<String, String> metadata = new HashMap<>();
    metadata.put(PUZZLE_ID, piece.puzzleId());
    metadata.put(IMAGE_PATH, piece.imagePath().pathString());
    metadata.put(PIECE_COUNT, Integer.toString(piece.pieceCount()));
    metadata.put(SEED, Long.toString(piece.seed()));
    metadata.put(PIECE_INDEX, Integer.toString(piece.pieceIndex()));
    metadata.put(PIECE_FAMILY_ID, piece.pieceFamilyId());
    return Map.copyOf(metadata);
  }

  /**
   * Ensures the matching generated fragment textures are registered before a client builds its draw
   * component from spawn information.
   *
   * @param metadata spawn or snapshot metadata
   */
  public static void ensureTextures(Map<String, String> metadata) {
    if (!metadata.containsKey(PUZZLE_ID)
        && !metadata.containsKey(IMAGE_PATH)
        && !metadata.containsKey(PIECE_COUNT)
        && !metadata.containsKey(SEED)
        && !metadata.containsKey(PIECE_INDEX)) return;
    String puzzleId = require(metadata, PUZZLE_ID);
    String imagePath = require(metadata, IMAGE_PATH);
    int pieceCount = Integer.parseInt(require(metadata, PIECE_COUNT));
    long seed = Long.parseLong(require(metadata, SEED));
    Puzzle puzzle =
        PuzzleMaker.ensurePuzzle(
            puzzleId,
            new SimpleIPath(imagePath),
            pieceCount,
            seed,
            metadata.getOrDefault(PIECE_FAMILY_ID, puzzleId));
    PuzzleTextureGenerator.ensureRegistered(puzzle);
  }

  private static String require(Map<String, String> metadata, String key) {
    String value = metadata.get(key);
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("Puzzle metadata is missing required key '" + key + "'");
    }
    return value;
  }
}
