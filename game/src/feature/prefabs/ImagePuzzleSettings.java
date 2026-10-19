package feature.prefabs;

import engine.utils.Point;
import engine.utils.Vector2;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Typed settings for an image puzzle prefab, including one placement for every puzzle piece.
 *
 * @param pieceCount number of fragments
 * @param seed deterministic slicing seed
 * @param pieces ordered fragment placements
 */
public record ImagePuzzleSettings(int pieceCount, long seed, List<PuzzlePiecePlacement> pieces) {

  /** Default number of pieces in a newly-authored image puzzle. */
  public static final int DEFAULT_PIECE_COUNT = 8;

  /** Validates puzzle settings. */
  public ImagePuzzleSettings {
    if (pieceCount < 2) throw new IllegalArgumentException("pieceCount must be at least 2");
    pieces = List.copyOf(Objects.requireNonNull(pieces, "pieces"));
    if (pieces.size() != pieceCount) {
      throw new IllegalArgumentException(
          "piece placements must contain exactly " + pieceCount + " entries");
    }
  }

  /** Creates defaults with deterministic, spaced world positions. */
  public static ImagePuzzleSettings defaults() {
    return withDefaultPieces(DEFAULT_PIECE_COUNT, 1337L);
  }

  /** Returns settings initialized with default placements. */
  public static ImagePuzzleSettings withDefaultPieces(int pieceCount, long seed) {
    if (pieceCount < 2) throw new IllegalArgumentException("pieceCount must be at least 2");
    List<PuzzlePiecePlacement> pieces = new ArrayList<>(pieceCount);
    for (int i = 0; i < pieceCount; i++) pieces.add(PuzzlePiecePlacement.defaultFor(i));
    return new ImagePuzzleSettings(pieceCount, seed, pieces);
  }

  /** Returns the settings with the requested piece count, preserving existing placements. */
  public ImagePuzzleSettings withPieceCount(int newPieceCount) {
    if (newPieceCount < 2) throw new IllegalArgumentException("pieceCount must be at least 2");
    List<PuzzlePiecePlacement> resized = new ArrayList<>(newPieceCount);
    int retained = Math.min(pieces.size(), newPieceCount);
    resized.addAll(pieces.subList(0, retained));
    for (int i = retained; i < newPieceCount; i++) {
      resized.add(PuzzlePiecePlacement.defaultFor(i));
    }
    return new ImagePuzzleSettings(newPieceCount, seed, resized);
  }

  /** Returns the settings with another deterministic slicing seed. */
  public ImagePuzzleSettings withSeed(long newSeed) {
    return new ImagePuzzleSettings(pieceCount, newSeed, pieces);
  }

  /** Returns the settings with one piece placement replaced. */
  public ImagePuzzleSettings withPiece(int pieceIndex, PuzzlePiecePlacement placement) {
    Objects.requireNonNull(placement, "placement");
    if (pieceIndex < 0 || pieceIndex >= pieceCount) {
      throw new IndexOutOfBoundsException("pieceIndex must be between 0 and " + (pieceCount - 1));
    }
    List<PuzzlePiecePlacement> updated = new ArrayList<>(pieces);
    updated.set(pieceIndex, placement);
    return new ImagePuzzleSettings(pieceCount, seed, updated);
  }

  /** Translates all piece positions by a world vector. */
  public ImagePuzzleSettings translated(Vector2 translation) {
    Objects.requireNonNull(translation, "translation");
    List<PuzzlePiecePlacement> moved = new ArrayList<>(pieces.size());
    for (PuzzlePiecePlacement piece : pieces) {
      moved.add(piece.withPosition(piece.position().translate(translation)));
    }
    return new ImagePuzzleSettings(pieceCount, seed, moved);
  }

  /** Returns the world position of the requested piece. */
  public Point piecePosition(int pieceIndex) {
    return pieces.get(pieceIndex).position();
  }
}
