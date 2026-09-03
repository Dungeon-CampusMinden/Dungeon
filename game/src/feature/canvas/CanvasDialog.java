package feature.canvas;

import com.badlogic.gdx.scenes.scene2d.Group;
import engine.Game;
import engine.utils.logging.DungeonLogger;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.HeadlessDialogGroup;
import java.util.List;

/**
 * Builder hook for the canvas {@link feature.hud.dialogs.DialogType}.
 *
 * <p>The server transports the authoritative node states and layout. The local definition is
 * resolved only to create callback-bearing prototypes; failure degrades to state-only nodes.
 */
public final class CanvasDialog {

  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(CanvasDialog.class);

  /** Context key (String) holding the {@link CanvasDefinition#id() canvas id}. */
  public static final String KEY_CANVAS_ID = "canvasId";

  /** Context key (Integer) holding the id of the hero entity that opened the canvas. */
  public static final String KEY_HERO_ID = "canvasHeroId";

  /** Context key ({@link CanvasSnapshot}) holding the server provided default nodes. */
  public static final String KEY_DEFAULT_NODES = "canvasDefaultNodes";

  /** Context key ({@link CanvasLayout}) holding the transported visual configuration. */
  public static final String KEY_LAYOUT = "canvasLayout";

  /** Context key (String) holding the class that statically defines the canvas. */
  public static final String KEY_PROVIDER_CLASS = "canvasProviderClass";

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
    int heroId = ctx.find(KEY_HERO_ID, Integer.class).orElse(-1);
    String providerClass = ctx.find(KEY_PROVIDER_CLASS, String.class).orElse(null);
    CanvasLayout transportedLayout =
        ctx.find(KEY_LAYOUT, CanvasLayout.class).orElseGet(CanvasLayout::defaults);

    CanvasDefinition definition = CanvasMaker.resolve(canvasId, providerClass).orElse(null);
    CanvasLayout layout = definition == null ? transportedLayout : definition.layout();
    List<CanvasNode> prototypes = List.of();
    if (definition != null) {
      try {
        prototypes = definition.currentNodes(new CanvasContext(canvasId, heroId, true));
      } catch (RuntimeException e) {
        LOGGER.warn(
            "Could not create prototypes for canvas '{}'; callbacks will be unavailable", canvasId);
      }
    } else {
      LOGGER.warn(
          "No CanvasDefinition available for id '{}'; callbacks will be unavailable", canvasId);
    }
    return new CanvasUI(canvasId, layout, defaults, ctx.dialogId(), prototypes);
  }
}
