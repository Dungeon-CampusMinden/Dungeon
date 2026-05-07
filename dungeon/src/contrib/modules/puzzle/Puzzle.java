package contrib.modules.puzzle;

import com.badlogic.gdx.math.Vector2;
import contrib.components.InventoryComponent;
import contrib.item.Item;
import core.Entity;
import core.utils.components.path.IPath;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * In-memory state and configuration of a single jigsaw puzzle instance.
 *
 * <p>Holds the immutable parameters (image, total piece count, world sprite, completion callback,
 * debug flag) and the mutable per-puzzle state (current RNG seed, polygon shape of every piece in
 * image-pixel coordinates, current playfield positions of pieces, which pieces are currently
 * snapped to their correct slot, and whether the completion callback has already been fired).
 *
 * <p>The pieces are obtained by repeatedly splitting the largest current polygon along a chord
 * between two random points on its perimeter (see {@link PuzzleSlicer}). Polygons are recomputed
 * whenever {@link #regenerate(long, int, int)} is called, so the puzzle UI can re-roll the slicing
 * for debugging.
 *
 * <p>One {@link PuzzlePieceItem} is created per piece by {@link PuzzleMaker#makePuzzle}; the UI
 * ({@link PuzzleUI}) reads the parameters from this object, displays only the pieces that are
 * present in the interacting hero's inventory, and writes the dragged positions back here so
 * progress is preserved between attempts.
 */
public class Puzzle {

  private final String id;
  private final IPath imagePath;
  private final IPath worldSprite;
  private final int pieceCount;
  private final boolean debug;
  private final BiConsumer<Puzzle, Entity> onComplete;

  private long seed;

  private final List<PuzzlePieceItem> pieces = new ArrayList<>();

  /**
   * Polygon vertices per piece in image-pixel coordinates ({@code {x0,y0,x1,y1,...}}). Index =
   * piece index. Recomputed by {@link #regenerate(long, int, int)}.
   */
  private List<float[]> polygons = new ArrayList<>();

  /** Local (playfield) position of each piece (key = piece index). */
  private final Map<Integer, Vector2> piecePositions = new HashMap<>();

  /** Indices of pieces that are currently snapped onto their correct slot. */
  private final Set<Integer> placedCorrectly = new HashSet<>();

  private boolean callbackFired = false;

  /**
   * Creates a new {@code Puzzle}. Use {@link PuzzleMaker} to obtain instances.
   *
   * @param id unique runtime id for this puzzle (used as the lookup key in {@link PuzzleMaker})
   * @param imagePath path to the source image that gets sliced into pieces
   * @param worldSprite path to the sprite used for the dropped/world {@link Item} representation of
   *     every piece
   * @param pieceCount total number of pieces ({@code >= 2})
   * @param seed RNG seed used for deterministic slicing and initial scatter
   * @param debug whether the UI should expose debug controls (seed input + reroll button)
   * @param onComplete callback fired exactly once when all pieces are placed correctly (receives
   *     the solving entity, if known)
   */
  Puzzle(
      String id,
      IPath imagePath,
      IPath worldSprite,
      int pieceCount,
      long seed,
      boolean debug,
      BiConsumer<Puzzle, Entity> onComplete) {
    if (pieceCount < 2) {
      throw new IllegalArgumentException("pieceCount must be >= 2");
    }
    this.id = id;
    this.imagePath = imagePath;
    this.worldSprite = worldSprite;
    this.pieceCount = pieceCount;
    this.debug = debug;
    this.seed = seed;
    this.onComplete = onComplete;
  }

  /**
   * @return runtime id of this puzzle (used by {@link PuzzleMaker#lookup(String)})
   */
  public String id() {
    return id;
  }

  /**
   * @return path to the source image
   */
  public IPath imagePath() {
    return imagePath;
  }

  /**
   * @return path to the world sprite used for piece items
   */
  public IPath worldSprite() {
    return worldSprite;
  }

  /**
   * @return total number of pieces
   */
  public int pieceCount() {
    return pieceCount;
  }

  /**
   * @return whether the UI should expose debug controls (seed input + reroll button)
   */
  public boolean debug() {
    return debug;
  }

  /**
   * @return RNG seed used for deterministic piece slicing / scatter
   */
  public long seed() {
    return seed;
  }

  /**
   * @return immutable view of the puzzle pieces in index order (0..{@link #pieceCount()}-1)
   */
  public List<PuzzlePieceItem> pieces() {
    return Collections.unmodifiableList(pieces);
  }

  /**
   * Convenience accessor that returns the pieces typed as {@link Item}, in piece-index order.
   *
   * @return the pieces as a list of {@link Item}s
   */
  public List<Item> items() {
    return new ArrayList<>(pieces);
  }

  /**
   * Adds a piece to this puzzle. Package-private; only used by {@link PuzzleMaker} during
   * construction.
   *
   * @param piece the piece item to append
   */
  void addPiece(PuzzlePieceItem piece) {
    pieces.add(piece);
  }

  /**
   * Returns the polygon vertices of the given piece, in image-pixel coordinates ({@code
   * {x0,y0,x1,y1,...}}). The returned list is parallel to {@link #pieces()}.
   *
   * <p>The returned arrays are the live storage of this puzzle and must not be mutated.
   *
   * @return list of polygons, one per piece
   */
  public List<float[]> polygons() {
    return polygons;
  }

  /**
   * (Re)slices the source image with the given seed. Resets all per-piece progress state (stored
   * positions, snapped state, callback-fired state) so the puzzle can be played again from scratch
   * with a different layout. Used by the debug UI to test alternative seeds.
   *
   * @param newSeed the new RNG seed
   * @param imageWidth source image width in pixels
   * @param imageHeight source image height in pixels
   */
  public void regenerate(long newSeed, int imageWidth, int imageHeight) {
    this.seed = newSeed;
    this.polygons = PuzzleSlicer.slice(imageWidth, imageHeight, pieceCount, newSeed);
    this.piecePositions.clear();
    this.placedCorrectly.clear();
    this.callbackFired = false;
  }

  /**
   * Returns the stored local (playfield) position of the given piece, if any.
   *
   * @param pieceIndex piece index
   * @return the stored position, or empty if the piece has never been placed
   */
  public Optional<Vector2> getPiecePosition(int pieceIndex) {
    Vector2 v = piecePositions.get(pieceIndex);
    return v == null ? Optional.empty() : Optional.of(v.cpy());
  }

  /**
   * Stores the current local (playfield) position of the given piece.
   *
   * @param pieceIndex piece index
   * @param pos new local position
   */
  public void setPiecePosition(int pieceIndex, Vector2 pos) {
    piecePositions.put(pieceIndex, pos.cpy());
  }

  /**
   * Marks a piece as snapped to its correct slot.
   *
   * @param pieceIndex piece index
   */
  public void markPlaced(int pieceIndex) {
    placedCorrectly.add(pieceIndex);
  }

  /**
   * Marks a piece as no longer snapped to its correct slot.
   *
   * @param pieceIndex piece index
   */
  public void unmarkPlaced(int pieceIndex) {
    placedCorrectly.remove(pieceIndex);
  }

  /**
   * Removes all item instances of this puzzle from the given entity's inventory.
   *
   * <p>If the entity has no {@link InventoryComponent}, this method is a no-op.
   *
   * @param entity entity whose inventory should be cleaned up
   */
  public void removeItems(Entity entity) {
    if (entity == null) return;
    entity
        .fetch(InventoryComponent.class)
        .ifPresent(inv -> pieces.forEach(piece -> inv.remove(piece)));
  }

  /**
   * @param pieceIndex piece index
   * @return whether the given piece is currently snapped to its correct slot
   */
  public boolean isPlaced(int pieceIndex) {
    return placedCorrectly.contains(pieceIndex);
  }

  /**
   * @return whether every single piece is snapped to its correct slot
   */
  public boolean isFullySolved() {
    return placedCorrectly.size() == pieceCount();
  }

  /**
   * Tries to fire the {@code onComplete} callback. Idempotent: only the first invocation while
   * {@link #isFullySolved()} is true triggers the callback; further invocations are no-ops.
   *
   * @param solver the entity that solved the puzzle (can be {@code null} if unknown)
   * @return {@code true} iff this invocation actually fired the callback
   */
  public boolean tryFireCallback(Entity solver) {
    if (callbackFired) return false;
    if (!isFullySolved()) return false;
    callbackFired = true;
    if (onComplete != null) onComplete.accept(this, solver);
    return true;
  }
}
