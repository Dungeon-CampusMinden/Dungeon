package contrib.modules.puzzle;

import contrib.components.UIComponent;
import contrib.hud.UIUtils;
import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.DialogContextKeys;
import contrib.hud.dialogs.DialogFactory;
import contrib.hud.dialogs.DialogType;
import contrib.item.Item;
import contrib.item.ItemRegistry;
import core.Entity;
import core.utils.components.draw.animation.Animation;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * An {@link Item} that represents a single piece of a {@link Puzzle}.
 *
 * <p>Each piece is bound to its parent puzzle via a runtime {@link #puzzleId()} (looked up through
 * {@link PuzzleMaker#lookup(String)}) and a {@link #pieceIndex()} that identifies which polygonal
 * slot of the parent puzzle this piece belongs to.
 *
 * <p>Both the inventory and the world animation point at the deterministic virtual texture path
 * {@link PuzzleTextureGenerator#texturePath(String, int)}. The actual texture is materialized
 * lazily by {@link PuzzleTextureGenerator#ensureRegistered(Puzzle)} on every client, so puzzle
 * pieces are rendered with their actual image fragment instead of a generic puzzle-piece sprite,
 * even in multiplayer.
 *
 * <p>Unlike most items, {@link #use(Entity)} does <b>not</b> consume the item. Instead it opens the
 * {@link PuzzleDialog} for the calling hero, so the piece stays in the inventory and can still be
 * dragged onto its slot inside the puzzle UI.
 */
public class PuzzlePieceItem extends Item {

  /** Item-data key carrying the parent puzzle id. */
  public static final String DATA_KEY_PUZZLE_ID = "puzzleId";

  /** Item-data key carrying this piece's 0-based index. */
  public static final String DATA_KEY_PIECE_INDEX = "pieceIndex";

  /** Item-data key carrying the source image path of the parent puzzle. */
  public static final String DATA_KEY_IMAGE_PATH = "imagePath";

  /** Item-data key carrying the total piece count of the parent puzzle. */
  public static final String DATA_KEY_PIECE_COUNT = "pieceCount";

  /** Item-data key carrying the RNG seed of the parent puzzle. */
  public static final String DATA_KEY_SEED = "seed";

  static {
    ItemRegistry.register(PuzzlePieceItem.class, PuzzlePieceItem::fromData);
  }

  /**
   * Forces the static initializer of this class to run, registering the item with {@link
   * ItemRegistry}. Call once at startup if you create instances reflectively or want to avoid the
   * "not registered" warning logged by {@link Item}.
   */
  public static void ensureRegistration() {
    // No-op; class-loading triggers the static block above.
  }

  private final String puzzleId;
  private final int pieceIndex;
  private final IPath imagePath;
  private final int pieceCount;
  private final long seed;

  /**
   * Creates a new puzzle piece item.
   *
   * @param puzzleId the runtime id of the parent {@link Puzzle}
   * @param pieceIndex the index of this piece inside the puzzle (matches {@link Puzzle#polygons()})
   * @param imagePath path of the source image the parent puzzle is sliced from
   * @param pieceCount total number of pieces of the parent puzzle
   * @param seed RNG seed of the parent puzzle (used so clients can re-slice deterministically)
   */
  public PuzzlePieceItem(
      String puzzleId, int pieceIndex, IPath imagePath, int pieceCount, long seed) {
    super(
        "A Puzzle Piece",
        "There must be more of them...\n[Use] to see the puzzle.",
        new Animation(new SimpleIPath(PuzzleTextureGenerator.texturePath(puzzleId, pieceIndex))));
    this.puzzleId = puzzleId;
    this.pieceIndex = pieceIndex;
    this.imagePath = imagePath;
    this.pieceCount = pieceCount;
    this.seed = seed;
  }

  /**
   * @return the runtime id of the parent {@link Puzzle}
   */
  public String puzzleId() {
    return puzzleId;
  }

  /**
   * @return the index of this piece in the parent puzzle (matches {@link Puzzle#polygons()})
   */
  public int pieceIndex() {
    return pieceIndex;
  }

  /**
   * @return path of the source image the parent puzzle is sliced from
   */
  public IPath imagePath() {
    return imagePath;
  }

  /**
   * @return total number of pieces of the parent puzzle
   */
  public int pieceCount() {
    return pieceCount;
  }

  /**
   * @return RNG seed of the parent puzzle
   */
  public long seed() {
    return seed;
  }

  @Override
  public Map<String, String> itemData() {
    Map<String, String> data = new LinkedHashMap<>();
    data.put(DATA_KEY_PUZZLE_ID, puzzleId);
    data.put(DATA_KEY_PIECE_INDEX, Integer.toString(pieceIndex));
    data.put(DATA_KEY_IMAGE_PATH, imagePath.pathString());
    data.put(DATA_KEY_PIECE_COUNT, Integer.toString(pieceCount));
    data.put(DATA_KEY_SEED, Long.toString(seed));
    return data;
  }

  /**
   * Item factory used by {@link ItemRegistry} on the receiving end of a network or persistence
   * payload. Ensures the parent puzzle is registered (so {@link PuzzleDialog} can look it up) and
   * the generated piece textures are materialized, so the {@link Animation} created by the
   * constructor finds its texture on first draw.
   *
   * @param data item data map as produced by {@link #itemData()}
   * @return the reconstructed puzzle piece
   */
  private static PuzzlePieceItem fromData(Map<String, String> data) {
    String puzzleId = require(data, DATA_KEY_PUZZLE_ID);
    int pieceIndex = Integer.parseInt(require(data, DATA_KEY_PIECE_INDEX));
    IPath imagePath = new SimpleIPath(require(data, DATA_KEY_IMAGE_PATH));
    int pieceCount = Integer.parseInt(require(data, DATA_KEY_PIECE_COUNT));
    long seed = Long.parseLong(require(data, DATA_KEY_SEED));

    Puzzle puzzle = PuzzleMaker.ensurePuzzle(puzzleId, imagePath, pieceCount, seed);
    PuzzleTextureGenerator.ensureRegistered(puzzle);

    return new PuzzlePieceItem(puzzleId, pieceIndex, imagePath, pieceCount, seed);
  }

  private static String require(Map<String, String> data, String key) {
    String value = data.get(key);
    if (value == null) {
      throw new IllegalArgumentException(
          "PuzzlePieceItem itemData is missing required key '" + key + "'");
    }
    return value;
  }

  /**
   * Opens the {@link PuzzleDialog} for the calling hero. The item is intentionally NOT removed from
   * the inventory (no call to {@code super.use(user)}), so it can still be dragged inside the
   * puzzle UI.
   *
   * @param user the entity using this item
   */
  @Override
  public void use(final Entity user) {
    DialogContext ctx =
        DialogContext.builder()
            .type(DialogType.DefaultTypes.PUZZLE)
            .put(PuzzleDialog.KEY_PUZZLE_ID, puzzleId)
            .put(PuzzleDialog.KEY_HERO_ID, user.id())
            .build();

    UIComponent ui = DialogFactory.show(ctx, user.id());

    ui.registerCallback(DialogContextKeys.ON_CLOSE, payload -> UIUtils.closeDialog(ui));
    ui.registerCallback(
        DialogContextKeys.ON_COMPLETE,
        payload -> {
          PuzzleMaker.lookup(puzzleId).ifPresent(p -> p.tryFireCallback(user));
        });
  }
}
