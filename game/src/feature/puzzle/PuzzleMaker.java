package feature.puzzle;

import engine.Entity;
import engine.utils.components.path.IPath;
import feature.components.InventoryComponent;
import feature.inventory.items.HintItem;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import rooms.lasthour.util.translation.TranslationKey;

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
 *   Game.add(WorldItemBuilder.buildWorldItem(items.get(i), itemLocations.get(i)));
 * }
 * }</pre>
 */
public final class PuzzleMaker {

  private static final Map<String, Puzzle> REGISTRY = new ConcurrentHashMap<>();
  private static final Map<String, BiConsumer<Puzzle, Entity>> FAMILY_COMPLETION_CALLBACKS =
      new ConcurrentHashMap<>();

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
    return makePuzzle(imagePath, pieceCount, onComplete, seed, debug, null);
  }

  /**
   * Creates a puzzle with an optional shared piece family identifier.
   *
   * <p>Puzzles with the same family identifier may contribute pieces to one another's UI. This
   * supports alternate presentations of the same puzzle, such as language-specific variants.
   *
   * @param imagePath path to the source image
   * @param pieceCount total number of puzzle pieces (must be {@code >= 2})
   * @param onComplete callback fired exactly once when all pieces are placed correctly
   * @param seed RNG seed used for deterministic slicing and scatter
   * @param debug whether the puzzle UI should expose seed-tweaking controls
   * @param pieceFamilyId stable identifier shared by puzzle variants, or null to use this puzzle's
   *     deterministic ID
   * @return the created puzzle
   */
  public static Puzzle makePuzzle(
      IPath imagePath,
      int pieceCount,
      BiConsumer<Puzzle, Entity> onComplete,
      long seed,
      boolean debug,
      String pieceFamilyId) {
    return makePuzzle(imagePath, pieceCount, onComplete, seed, debug, pieceFamilyId, null);
  }

  /**
   * Creates a puzzle whose id additionally depends on a scope, so puzzles with the same image,
   * piece count and seed but different scopes are separate puzzles with their own completion
   * callbacks, e.g. two prefab instances showing the same image.
   *
   * @param imagePath path to the source image
   * @param pieceCount total number of puzzle pieces (must be {@code >= 2})
   * @param onComplete callback fired exactly once when all pieces are placed correctly
   * @param seed RNG seed used for deterministic slicing and scatter
   * @param debug whether the puzzle UI should expose seed-tweaking controls
   * @param pieceFamilyId stable identifier shared by puzzle variants, or null to use this puzzle's
   *     deterministic ID
   * @param scope stable value mixed into the puzzle id, or null for none
   * @return the created puzzle
   */
  public static Puzzle makePuzzle(
      IPath imagePath,
      int pieceCount,
      BiConsumer<Puzzle, Entity> onComplete,
      long seed,
      boolean debug,
      String pieceFamilyId,
      String scope) {
    Objects.requireNonNull(imagePath, "imagePath");
    if (pieceCount < 2) {
      throw new IllegalArgumentException("pieceCount must be >= 2");
    }

    PuzzlePieceItem.ensureRegistration();

    String id = deterministicId(imagePath, pieceCount, seed, scope);
    String effectivePieceFamilyId =
        pieceFamilyId == null ? id : requirePieceFamilyId(pieceFamilyId);
    BiConsumer<Puzzle, Entity> effectiveOnComplete;
    if (onComplete == null) {
      effectiveOnComplete =
          FAMILY_COMPLETION_CALLBACKS.getOrDefault(effectivePieceFamilyId, defaultOnComplete());
    } else {
      effectiveOnComplete = onComplete;
      FAMILY_COMPLETION_CALLBACKS.put(effectivePieceFamilyId, onComplete);
      REGISTRY.values().stream()
          .filter(puzzle -> puzzle.pieceFamilyId().equals(effectivePieceFamilyId))
          .forEach(puzzle -> puzzle.onComplete(onComplete));
    }
    return REGISTRY.computeIfAbsent(
        id,
        unused -> {
          Puzzle p =
              new Puzzle(
                  id,
                  effectivePieceFamilyId,
                  imagePath,
                  pieceCount,
                  seed,
                  debug,
                  effectiveOnComplete);
          PuzzleTextureGenerator.ensureRegistered(p);
          for (int i = 0; i < pieceCount; i++) {
            p.addPiece(
                new PuzzlePieceItem(id, i, imagePath, pieceCount, seed, effectivePieceFamilyId));
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
    return ensurePuzzle(id, imagePath, pieceCount, seed, id);
  }

  /**
   * Looks up or reconstructs a puzzle with an explicit compatible-piece family.
   *
   * @param id runtime puzzle identifier
   * @param imagePath source image path
   * @param pieceCount total number of pieces
   * @param seed deterministic slicing seed
   * @param pieceFamilyId stable identifier shared by compatible variants
   * @return existing or freshly reconstructed puzzle
   */
  public static Puzzle ensurePuzzle(
      String id, IPath imagePath, int pieceCount, long seed, String pieceFamilyId) {
    Objects.requireNonNull(id, "id");
    Objects.requireNonNull(imagePath, "imagePath");
    if (pieceCount < 2) {
      throw new IllegalArgumentException("pieceCount must be >= 2");
    }
    String effectivePieceFamilyId = requirePieceFamilyId(pieceFamilyId);
    PuzzlePieceItem.ensureRegistration();
    return REGISTRY.computeIfAbsent(
        id,
        unused -> {
          Puzzle p =
              new Puzzle(
                  id,
                  effectivePieceFamilyId,
                  imagePath,
                  pieceCount,
                  seed,
                  false,
                  FAMILY_COMPLETION_CALLBACKS.getOrDefault(
                      effectivePieceFamilyId, defaultOnComplete()));
          // Register the @gen piece textures before constructing the items (see makePuzzle).
          PuzzleTextureGenerator.ensureRegistered(p);
          for (int i = 0; i < pieceCount; i++) {
            p.addPiece(
                new PuzzlePieceItem(id, i, imagePath, pieceCount, seed, effectivePieceFamilyId));
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
  public static BiConsumer<Puzzle, Entity> defaultOnComplete() {
    return (puzzle, solver) -> {
      HintItem hintItem = completionHint(puzzle, solver);
      puzzle.removeItems(solver);
      if (solver == null) return;
      solver.fetch(InventoryComponent.class).ifPresent(inv -> inv.add(hintItem));
    };
  }

  private static HintItem completionHint(Puzzle puzzle, Entity solver) {
    String name = null;
    String description = null;
    if (solver != null) {
      Optional<InventoryComponent> inventory = solver.fetch(InventoryComponent.class);
      if (inventory.isPresent()) {
        for (var item : inventory.get().items()) {
          if (item instanceof PuzzlePieceItem piece
              && piece.pieceFamilyId().equals(puzzle.pieceFamilyId())
              && (piece.completionHintName() != null
                  || piece.completionHintDescription() != null)) {
            name = piece.completionHintName();
            description = piece.completionHintDescription();
            break;
          }
        }
      }
    }
    if (name == null && description == null) {
      return new HintItem(puzzle.imagePath());
    }
    return new HintItem(
        puzzle.imagePath(),
        name == null ? TranslationKey.HintItemName : name,
        description == null ? TranslationKey.HintItemDescription : description);
  }

  /**
   * Derives a stable puzzle id from its identifying inputs. The same combination of image, piece
   * count and seed always yields the same id, which is what lets the server and every client agree
   * on the {@code @gen/puzzle/<id>/...} texture paths without round-tripping the id explicitly.
   *
   * @param imagePath the source image path of the puzzle
   * @param pieceCount the number of pieces the puzzle is split into
   * @param seed the seed used to slice the puzzle
   * @param scope additional identifying value, or null for none
   * @return the deterministic puzzle id
   */
  private static String deterministicId(IPath imagePath, int pieceCount, long seed, String scope) {
    String raw = imagePath.pathString() + "|" + pieceCount + "|" + seed;
    if (scope != null) raw += "|" + scope;
    UUID uuid = UUID.nameUUIDFromBytes(raw.getBytes(StandardCharsets.UTF_8));
    return "puzzle-" + uuid;
  }

  private static String requirePieceFamilyId(String pieceFamilyId) {
    Objects.requireNonNull(pieceFamilyId, "pieceFamilyId");
    if (pieceFamilyId.isBlank()) {
      throw new IllegalArgumentException("pieceFamilyId must not be blank");
    }
    return pieceFamilyId;
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
      boolean familyStillRegistered =
          REGISTRY.values().stream()
              .anyMatch(puzzle -> puzzle.pieceFamilyId().equals(removed.pieceFamilyId()));
      if (!familyStillRegistered) {
        FAMILY_COMPLETION_CALLBACKS.remove(removed.pieceFamilyId());
      }
    }
  }
}
