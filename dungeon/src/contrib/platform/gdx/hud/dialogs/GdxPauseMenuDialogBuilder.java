package contrib.platform.gdx.hud.dialogs;

import com.badlogic.gdx.scenes.scene2d.Group;
import contrib.hud.UIUtils;
import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.HeadlessDialogGroup;
import core.Game;

/**
 * Builds the libGDX-backed pause menu dialog.
 */
public final class GdxPauseMenuDialogBuilder {

  private GdxPauseMenuDialogBuilder() {}

  /**
   * Builds a Scene2D pause menu from the given context.
   *
   * @param ctx the dialog context
   * @return the Scene2D group representing the pause menu
   */
  public static Group build(DialogContext ctx) {
    if (Game.isHeadless()) {
      return new HeadlessDialogGroup();
    }

    return PauseDialog.create(UIUtils.defaultSkin());
  }
}
