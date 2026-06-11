package contrib.modules.puzzle;

import com.badlogic.gdx.scenes.scene2d.Group;
import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.HeadlessDialogGroup;
import core.Game;

/**
 * Builder hook for the puzzle {@link contrib.hud.dialogs.DialogType}.
 *
 * <p>The actual UI lives in {@link PuzzleUI}; this class only resolves the {@link Puzzle} via
 * {@link PuzzleMaker#lookup(String)} from the {@code puzzleId} stored in the {@link DialogContext},
 * so that the (non-serializable) {@link Puzzle} reference and its {@code Runnable onComplete} never
 * have to live inside the serializable context.
 */
public final class PuzzleDialog {

  /** Context key (String) holding the {@link Puzzle#id() puzzle id}. */
  public static final String KEY_PUZZLE_ID = "puzzleId";

  /** Context key (Integer) holding the id of the hero entity that opened the dialog. */
  public static final String KEY_HERO_ID = "puzzleHeroId";

  private PuzzleDialog() {}

  /**
   * Dialog builder registered with {@link contrib.hud.dialogs.DialogFactory} for {@link
   * contrib.hud.dialogs.DialogType.DefaultTypes#PUZZLE}.
   *
   * @param ctx the dialog context (must contain {@link #KEY_PUZZLE_ID} and {@link #KEY_HERO_ID})
   * @return a fresh {@link PuzzleUI}, or a {@link HeadlessDialogGroup} when running headless
   */
  public static Group build(DialogContext ctx) {
    if (Game.isHeadless()) return new HeadlessDialogGroup();

    String puzzleId = ctx.require(KEY_PUZZLE_ID, String.class);
    int heroId = ctx.require(KEY_HERO_ID, Integer.class);
    Puzzle puzzle =
        PuzzleMaker.lookup(puzzleId)
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "PuzzleDialog: no puzzle registered for id '" + puzzleId + "'"));
    return new PuzzleUI(puzzle, heroId, ctx.dialogId());
  }
}
