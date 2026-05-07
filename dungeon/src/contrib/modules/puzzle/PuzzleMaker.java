package contrib.modules.puzzle;

import contrib.components.InventoryComponent;
import contrib.item.concreteItem.HintItem;
import core.Entity;
import core.utils.components.path.IPath;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Factory and runtime registry for {@link Puzzle} instances.
 *
 * <p>The static {@link #makePuzzle} overloads create a fully-initialized {@link Puzzle} (including
 * one {@link PuzzlePieceItem} per piece and the initial polygon slicing of the source image), and
 * register the resulting instance under a generated id so the {@link PuzzleDialog} can look it up
 * later.
 *
 * <p>The image is sliced into arbitrary convex polygons by {@link PuzzleSlicer}; you only specify
 * how many pieces you want (no rows / cols anymore). The slicing of a generic {@code N} pieces is
 * intentionally not exact (it can produce slivers / silly layouts), which is why the {@code seed}
 * parameter exists. Set {@code debug = true} to expose seed-tweaking controls in the puzzle UI.
 *
 * <p>Typical usage from a level setup:
 *
 * <pre>{@code
 * Puzzle p = PuzzleMaker.makePuzzle(
 *     new SimpleIPath("images/my_picture.png"),
 *     12,                                       // total piece count (>= 2)
 *     new SimpleIPath("items/rpg/item_paper.png"),
 *     solver -> door.open());                    // completion callback
 *
 * List<Item> items = p.items();
 * for (int i = 0; i < items.size(); i++) {
 *   Game.add(WorldItemBuilder.buildWorldItemSimpleInteraction(items.get(i), itemLocations.get(i)));
 * }
 * }</pre>
 */
public final class PuzzleMaker {

  private static final Map<String, Puzzle> REGISTRY = new ConcurrentHashMap<>();

  private PuzzleMaker() {}

  /**
   * Convenience overload using {@link System#nanoTime()} as seed and {@code debug = false}.
   *
   * @param imagePath path to the source image
   * @param pieceCount total number of puzzle pieces (must be {@code >= 2})
   * @param worldSprite world sprite for the dropped piece items
   * @param onComplete callback fired exactly once when all pieces are placed correctly
   * @return the created puzzle
   */
  public static Puzzle makePuzzle(
      IPath imagePath,
      int pieceCount,
      IPath worldSprite,
      BiConsumer<Puzzle, Entity> onComplete) {
    return makePuzzle(imagePath, pieceCount, worldSprite, onComplete, System.nanoTime(), false);
  }

  /**
   * Convenience overload with explicit seed and {@code debug = false}.
   *
   * @param imagePath path to the source image
   * @param pieceCount total number of puzzle pieces (must be {@code >= 2})
   * @param worldSprite world sprite for the dropped piece items
   * @param onComplete callback fired exactly once when all pieces are placed correctly
   * @param seed RNG seed used for deterministic slicing and scatter
   * @return the created puzzle
   */
  public static Puzzle makePuzzle(
      IPath imagePath,
      int pieceCount,
      IPath worldSprite,
      BiConsumer<Puzzle, Entity> onComplete,
      long seed) {
    return makePuzzle(imagePath, pieceCount, worldSprite, onComplete, seed, false);
  }

  /**
   * Creates a puzzle with explicit seed and debug flag.
   *
   * <p>The actual polygon slicing is performed lazily on the first call to {@link
   * Puzzle#regenerate(long, int, int)} (typically by the puzzle UI when it knows the loaded image
   * dimensions). Until then, {@link Puzzle#polygons()} is empty.
   *
   * @param imagePath path to the source image
   * @param pieceCount total number of puzzle pieces (must be {@code >= 2})
   * @param worldSprite world sprite for the dropped piece items
   * @param onComplete callback fired exactly once when all pieces are placed correctly
   * @param seed RNG seed used for deterministic slicing and scatter
   * @param debug whether the puzzle UI should expose seed-tweaking controls
   * @return the created puzzle
   */
  public static Puzzle makePuzzle(
      IPath imagePath,
      int pieceCount,
      IPath worldSprite,
      BiConsumer<Puzzle, Entity> onComplete,
      long seed,
      boolean debug) {
    Objects.requireNonNull(imagePath, "imagePath");
    Objects.requireNonNull(worldSprite, "worldSprite");
    if (pieceCount < 2) {
      throw new IllegalArgumentException("pieceCount must be >= 2");
    }

    PuzzlePieceItem.ensureRegistration();

    BiConsumer<Puzzle, Entity> effectiveOnComplete =
        onComplete == null ? defaultOnComplete() : onComplete;

    String id = "puzzle-" + UUID.randomUUID();
    Puzzle puzzle =
        new Puzzle(id, imagePath, worldSprite, pieceCount, seed, debug, effectiveOnComplete);
    for (int i = 0; i < pieceCount; i++) {
      puzzle.addPiece(new PuzzlePieceItem(id, i, worldSprite));
    }
    REGISTRY.put(id, puzzle);
    return puzzle;
  }

  /**
   * Returns the default {@code onComplete} callback that is installed by {@link #makePuzzle} when
   * the caller omits one (passes {@code null}).
   *
   * <p>The default behavior is:
   *
   * <ol>
   *   <li>Remove every {@link PuzzlePieceItem} of the solved puzzle from the solving entity's
   *       inventory (via {@link Puzzle#removeItems(Entity)}).
   *   <li>Add a {@link HintItem} carrying the puzzle's base image to the solving entity's
   *       inventory, so the player can re-view the assembled picture from now on.
   * </ol>
   *
   * <p>If the solving entity is {@code null} or has no {@link InventoryComponent}, the {@link
   * HintItem} is silently not added.
   *
   * @return the default completion callback
   */
  private static BiConsumer<Puzzle, Entity> defaultOnComplete() {
    return (puzzle, solver) -> {
      puzzle.removeItems(solver);
      if (solver == null) return;
      solver
          .fetch(InventoryComponent.class)
          .ifPresent(inv -> inv.add(new HintItem(puzzle.imagePath())));
    };
  }

  /**
   * Looks up a puzzle by its runtime id.
   *
   * @param id the puzzle id (see {@link Puzzle#id()})
   * @return the puzzle if registered, otherwise {@link Optional#empty()}
   */
  public static Optional<Puzzle> lookup(String id) {
    return Optional.ofNullable(REGISTRY.get(id));
  }

  /**
   * Removes a puzzle from the registry. Useful when the puzzle is solved and no longer needed, to
   * avoid leaking memory across long sessions.
   *
   * @param id the puzzle id
   */
  public static void unregister(String id) {
    REGISTRY.remove(id);
  }
}
