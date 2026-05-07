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

/**
 * An {@link Item} that represents a single piece of a {@link Puzzle}.
 *
 * <p>Each piece is bound to its parent puzzle via a runtime {@link #puzzleId()} (looked up through
 * {@link PuzzleMaker#lookup(String)}) and a {@link #pieceIndex()} that identifies which polygonal
 * slot of the parent puzzle this piece belongs to.
 *
 * <p>Unlike most items, {@link #use(Entity)} does <b>not</b> consume the item. Instead it opens the
 * {@link PuzzleDialog} for the calling hero, so the piece stays in the inventory and can still be
 * dragged onto its slot inside the puzzle UI.
 */
public class PuzzlePieceItem extends Item {

  static {
    ItemRegistry.register(PuzzlePieceItem.class);
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

  /**
   * Creates a new puzzle piece item.
   *
   * @param puzzleId the runtime id of the parent {@link Puzzle}
   * @param pieceIndex the index of this piece inside the puzzle grid (row-major)
   * @param worldSprite the sprite used as both inventory and world animation
   */
  public PuzzlePieceItem(String puzzleId, int pieceIndex, IPath worldSprite) {
    super(
        "A Puzzle Piece",
        "There must be more of them...\n[Use] to see the puzzle.",
        new Animation(worldSprite));
    this.puzzleId = puzzleId;
    this.pieceIndex = pieceIndex;
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
