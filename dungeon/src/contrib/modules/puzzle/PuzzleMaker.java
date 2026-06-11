package contrib.modules.puzzle;

import contrib.components.InventoryComponent;
import contrib.item.concreteItem.HintItem;
import core.Entity;
import core.utils.components.path.IPath;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

/**
 * Factory and runtime registry for {@link Puzzle} instances.
 *
 * <p>The static {@link #makePuzzle} overloads create a fully-initialized {@link Puzzle} (including
 * one {@link PuzzlePieceItem} per piece and the initial polygon slicing of the source image), and
 * register the resulting instance under a deterministic id derived from the source image, piece
 * count and seed so the same puzzle is rebuilt with the same id on every machine. This is the
 * mechanism that makes per-piece textures (registered by {@link PuzzleTextureGenerator}) align
 * across the multiplayer server and its clients without explicit id negotiation.
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
   * @param onComplete callback fired exactly once when all pieces are placed correctly
   * @return the created puzzle
   */
  public static Puzzle makePuzzle(
      IPath imagePath, int pieceCount, BiConsumer<Puzzle, Entity> onComplete) {
    return makePuzzle(imagePath, pieceCount, onComplete, System.nanoTime(), false);
  }

  /**
   * Convenience overload with explicit seed and {@code debug = false}.
   *
   * @param imagePath path to the source image
   * @param pieceCount total number of puzzle pieces (must be {@code >= 2})
   * @param onComplete callback fired exactly once when all pieces are placed correctly
   * @param seed RNG seed used for deterministic slicing and scatter
   * @return the created puzzle
   */
  public static Puzzle makePuzzle(
      IPath imagePath, int pieceCount, BiConsumer<Puzzle, Entity> onComplete, long seed) {
    return makePuzzle(imagePath, pieceCount, onComplete, seed, false);
  }

  /**
   * Creates a puzzle with explicit seed and debug flag.
   *
   * <p>The puzzle id is derived deterministically from {@code imagePath + pieceCount + seed} so the
   * server and all clients agree on the id without explicit synchronisation; this is what lets the
   * generated per-piece texture paths ({@code @gen/puzzle/<id>/<idx>.png}) refer to the same
   * fragments on every machine.
   *
   * <p>On graphical hosts, the per-piece textures are materialized eagerly by {@link
   * PuzzleTextureGenerator#ensureRegistered(Puzzle)}; on headless hosts (dedicated server) this is
   * a no-op and the polygons are sliced lazily when the first {@link PuzzleUI} opens (or when a
   * client first reconstructs a {@link PuzzlePieceItem}).
   *
   * @param imagePath path to the source image
   * @param pieceCount total number of puzzle pieces (must be {@code >= 2})
   * @param onComplete callback fired exactly once when all pieces are placed correctly
   * @param seed RNG seed used for deterministic slicing and scatter
   * @param debug whether the puzzle UI should expose seed-tweaking controls
   * @return the created puzzle
   */
  public static Puzzle makePuzzle(
      IPath imagePath,
      int pieceCount,
      BiConsumer<Puzzle, Entity> onComplete,
      long seed,
      boolean debug) {
    Objects.requireNonNull(imagePath, "imagePath");
    if (pieceCount < 2) {
      throw new IllegalArgumentException("pieceCount must be >= 2");
    }

    PuzzlePieceItem.ensureRegistration();

    BiConsumer<Puzzle, Entity> effectiveOnComplete =
        onComplete == null ? defaultOnComplete() : onComplete;

    String id = deterministicId(imagePath, pieceCount, seed);
    return REGISTRY.computeIfAbsent(
        id,
        unused -> {
          Puzzle p = new Puzzle(id, imagePath, pieceCount, seed, debug, effectiveOnComplete);
          PuzzleTextureGenerator.ensureRegistered(p);
          for (int i = 0; i < pieceCount; i++) {
            p.addPiece(new PuzzlePieceItem(id, i, imagePath, pieceCount, seed));
          }
          return p;
        });
  }

  /**
   * Looks up (or lazily creates) the puzzle with the given id.
   *
   * <p>This is the entry point used by client-side code (notably the {@link PuzzlePieceItem} item
   * factory) to reconstruct a puzzle definition that was first created on the server: the server's
   * deterministic id, source image, piece count and seed are carried over the network as item data,
   * and this method materializes the matching {@link Puzzle} locally so {@link PuzzleDialog} can
   * look it up.
   *
   * @param id the deterministic puzzle id (see {@link Puzzle#id()})
   * @param imagePath the source image path of the puzzle
   * @param pieceCount the total number of pieces of the puzzle
   * @param seed the RNG seed of the puzzle
   * @return the existing or freshly created puzzle
   */
  public static Puzzle ensurePuzzle(String id, IPath imagePath, int pieceCount, long seed) {
    Objects.requireNonNull(id, "id");
    Objects.requireNonNull(imagePath, "imagePath");
    if (pieceCount < 2) {
      throw new IllegalArgumentException("pieceCount must be >= 2");
    }
    PuzzlePieceItem.ensureRegistration();
    return REGISTRY.computeIfAbsent(
        id,
        unused -> {
          Puzzle p = new Puzzle(id, imagePath, pieceCount, seed, false, defaultOnComplete());
          // Register the @gen piece textures before constructing the items (see makePuzzle).
          PuzzleTextureGenerator.ensureRegistered(p);
          for (int i = 0; i < pieceCount; i++) {
            p.addPiece(new PuzzlePieceItem(id, i, imagePath, pieceCount, seed));
          }
          return p;
        });
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
   * Derives a stable puzzle id from its identifying inputs. The same combination of image, piece
   * count and seed always yields the same id, which is what lets the server and every client agree
   * on the {@code @gen/puzzle/<id>/...} texture paths without round-tripping the id explicitly.
   *
   * @param imagePath the source image path of the puzzle
   * @param pieceCount the number of pieces the puzzle is split into
   * @param seed the seed used to slice the puzzle
   * @return the deterministic puzzle id
   */
  private static String deterministicId(IPath imagePath, int pieceCount, long seed) {
    String raw = imagePath.pathString() + "|" + pieceCount + "|" + seed;
    UUID uuid = UUID.nameUUIDFromBytes(raw.getBytes(StandardCharsets.UTF_8));
    return "puzzle-" + uuid;
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
    Puzzle removed = REGISTRY.remove(id);
    if (removed != null) {
      PuzzleTextureGenerator.unregister(id, removed.pieceCount());
    }
  }
}
