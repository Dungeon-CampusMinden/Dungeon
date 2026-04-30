package contrib.hud.dialogs;

import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import contrib.configuration.KeyboardConfig;
import contrib.hud.UIUtils;
import core.Game;
import core.utils.BaseContainerUI;
import java.util.List;

/**
 * Package-private builder for a sequenced speaker dialogue ("DialogDialog").
 *
 * <p>Renders a {@link DialogScriptView} (parsed from a single dialog script string via {@link
 * DialogScript}) inside a styled dialog frame.
 *
 * <p>User interaction:
 *
 * <ul>
 *   <li>Any mouse click anywhere on the dialog or pressing the configured interact key (see {@link
 *       contrib.configuration.KeyboardConfig#INTERACT_WORLD}) advances the script view.
 *   <li>If the typewriter is still revealing text, advancing skips to the end of the current
 *       entry's text.
 *   <li>Otherwise, the next page is shown.
 *   <li>After the last page has been confirmed, the {@link DialogContextKeys#ON_CONFIRM} callback
 *       is fired.
 * </ul>
 *
 * <p>Use {@link DialogFactory#showDialogDialog} instead of accessing this class directly.
 */
final class DialogDialog {

  /** Distance in pixels from the top edge of the stage to the top of the dialog. */
  private static final float TOP_OFFSET = 100;

  private DialogDialog() {}

  /**
   * Builds a DialogDialog from the given context.
   *
   * <p>On headless servers, returns a {@link HeadlessDialogGroup} placeholder containing all
   * speaker lines concatenated (one per line) so the server can still log/forward the payload.
   *
   * @param ctx The dialog context. Requires {@link DialogContextKeys#DIALOG} as a non-blank {@link
   *     String} script.
   * @return A fully configured DialogDialog or HeadlessDialogGroup.
   */
  static Group build(DialogContext ctx) {
    String script = ctx.require(DialogContextKeys.DIALOG, String.class);
    if (script.isBlank()) {
      throw new DialogCreationException("DialogDialog requires a non-blank dialog script");
    }

    if (Game.isHeadless()) {
      List<DialogEntry> entries =
          DialogScript.parseNonEmpty(script, () -> "DialogDialog script produced no pages");
      return new HeadlessDialogGroup("", DialogScript.toHeadlessText(entries));
    }

    return create(ctx, script);
  }

  private static Group create(DialogContext ctx, String script) {
    Skin skin = UIUtils.defaultSkin();

    HandledDialog dialog =
        new HandledDialog("", skin, (d, id) -> true); // no buttons; advance via input listeners
    DialogDesign.setDialogDefaults(dialog, "");

    DialogScriptView scriptView = new DialogScriptView(script);
    Table content = dialog.getContentTable();
    content.add(scriptView);
    content.row();

    scriptView.setOnSequenceComplete(
        () ->
            DialogCallbackResolver.createButtonCallback(
                    ctx.dialogId(), DialogContextKeys.ON_CONFIRM)
                .accept(null));

    Runnable advance =
        () -> {
          scriptView.advance();
          dialog.pack();
        };

    dialog.setTouchable(Touchable.enabled);
    dialog.addCaptureListener(
        new InputListener() {
          @Override
          public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            advance.run();
            event.stop();
            return true;
          }
        });

    // Key listener on the dialog itself, only the configured interact key advances.
    dialog.addListener(
        new InputListener() {
          @Override
          public boolean keyDown(InputEvent event, int keycode) {
            if (keycode != KeyboardConfig.INTERACT_WORLD.value()) {
              return false;
            }
            advance.run();
            return true;
          }
        });

    // Continuously claim keyboard focus so key input keeps reaching us even after mouse activity.
    dialog.addAction(
        new Action() {
          @Override
          public boolean act(float delta) {
            Stage stage = dialog.getStage();
            if (stage != null) {
              stage.setKeyboardFocus(dialog);
            }
            return false; // run forever
          }
        });

    // Wrap in an actor that clears the local texture cache on stage removal. Textures themselves
    // are owned by the TextureMap and must not be disposed here.
    dialog.pack();
    return new BaseContainerUI(dialog, Align.top, 0f, TOP_OFFSET, false, true) {
      @Override
      protected void setStage(Stage stage) {
        super.setStage(stage);
        if (stage == null) {
          scriptView.disposeCache();
        }
      }
    };
  }
}
