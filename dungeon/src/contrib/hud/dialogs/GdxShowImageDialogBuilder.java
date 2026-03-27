package contrib.hud.dialogs;

import com.badlogic.gdx.scenes.scene2d.Group;
import contrib.components.ShowImageComponent;
import contrib.hud.image.ShowImageUI;
import contrib.utils.components.showImage.TransitionSpeed;
import core.Game;

/**
 * Builds the libGDX-backed image popup dialog.
 */
public final class GdxShowImageDialogBuilder {

  private GdxShowImageDialogBuilder() {}

  /**
   * Builds a Scene2D image popup from the given dialog context.
   *
   * @param ctx the dialog context
   * @return the Scene2D group representing the image popup
   */
  public static Group build(DialogContext ctx) {
    if (Game.isHeadless()) {
      return new HeadlessDialogGroup("ShowImage", null);
    }

    String imagePath = ctx.require(DialogContextKeys.IMAGE, String.class);
    ShowImageComponent component = new ShowImageComponent(imagePath);

    ctx.find(DialogContextKeys.IMAGE_TRANSITION_SPEED, TransitionSpeed.class)
      .ifPresent(component::transitionSpeed);

    return new ShowImageUI(component);
  }
}
