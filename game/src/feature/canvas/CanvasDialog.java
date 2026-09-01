package feature.canvas;

import com.badlogic.gdx.scenes.scene2d.Group;
import engine.Game;
import engine.utils.logging.DungeonLogger;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogContextKeys;
import feature.hud.dialogs.HeadlessDialogGroup;

/**
 * Builder hook for the canvas {@link feature.hud.dialogs.DialogType}.
 *
 * <p>The actual UI lives in {@link CanvasUI}. This class only resolves the transported context
 * attributes: the canvas id, the hero that opened the canvas and the {@link CanvasSnapshot} of
 * server provided default nodes.
 *
 * <p>Layout and behaviour of the canvas come from the {@link CanvasDefinition} registered under the
 * canvas id. Definitions are created by level setup code that runs on server and client alike, so
 * the lookup normally succeeds on both. Should it fail, the dialog still opens with default options
 * so a missing definition degrades into a plain canvas instead of an error.
 */
public final class CanvasDialog {

  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(CanvasDialog.class);

  /** Context key (String) holding the {@link CanvasDefinition#id() canvas id}. */
  public static final String KEY_CANVAS_ID = "canvasId";

  /** Context key (Integer) holding the id of the hero entity that opened the canvas. */
  public static final String KEY_HERO_ID = "canvasHeroId";

  /** Context key ({@link CanvasSnapshot}) holding the server provided default nodes. */
  public static final String KEY_DEFAULT_NODES = "canvasDefaultNodes";

  private CanvasDialog() {}

  /**
   * Dialog builder registered with {@link feature.hud.dialogs.DialogFactory} for {@link
   * feature.hud.dialogs.DialogType.DefaultTypes#CANVAS}.
   *
   * @param ctx the dialog context; must contain {@link #KEY_CANVAS_ID}
   * @return a fresh {@link CanvasUI}, or a {@link HeadlessDialogGroup} when running headless
   */
  public static Group build(DialogContext ctx) {
    if (Game.isHeadless()) {
      return new HeadlessDialogGroup();
    }

    String canvasId = ctx.require(KEY_CANVAS_ID, String.class);
    CanvasSnapshot defaults =
        ctx.find(KEY_DEFAULT_NODES, CanvasSnapshot.class).orElseGet(CanvasSnapshot::empty);

    CanvasDefinition definition = CanvasMaker.lookup(canvasId).orElse(null);
    if (definition == null) {
      LOGGER.warn(
          "No CanvasDefinition registered for id '{}'; opening with default layout", canvasId);
      String title = ctx.find(DialogContextKeys.TITLE, String.class).orElse("");
      return new CanvasUI(
          canvasId, title, 900f, 560f, new CanvasOptions(), defaults, ctx.dialogId(), true, true);
    }

    return new CanvasUI(
        canvasId,
        definition.title(),
        definition.areaWidth(),
        definition.areaHeight(),
        definition.options(),
        defaults,
        ctx.dialogId(),
        definition.showResetViewButton(),
        definition.showFitButton());
  }
}
