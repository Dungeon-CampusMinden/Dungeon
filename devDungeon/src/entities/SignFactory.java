package entities;

import components.SignComponent;
import contrib.components.InteractionComponent;
import contrib.hud.dialogs.TextDialog;
import core.Entity;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.utils.Point;
import core.utils.components.MissingComponentException;
import core.utils.components.draw.Animation;
import core.utils.components.path.SimpleIPath;

public class SignFactory {

  private static final float DEFAULT_INTERACTION_RADIUS = 2.5f;
  private static final Animation SIGN_TEXTURE =
      Animation.fromSingleImage(
          new SimpleIPath("objects/mailbox/mailbox_2.png")); // TODO: Change to sign texture

  public static Entity createSign(String text, Point pos) {
    return createSign(text, SignComponent.DEFAULT_TITLE, pos);
  }

  public static Entity createSign(String text, String title, Point pos) {
    Entity sign = new Entity();

    sign.add(new PositionComponent(pos));
    sign.add(new DrawComponent(SIGN_TEXTURE));
    sign.add(new SignComponent(text, title));
    sign.add(
        new InteractionComponent(
            DEFAULT_INTERACTION_RADIUS,
            true,
            (entity, who) -> {
              SignComponent sc =
                  entity
                      .fetch(SignComponent.class)
                      .orElseThrow(
                          () -> MissingComponentException.build(entity, SignComponent.class));
              sc.showDialog();
            }));

    return sign;
  }

  public static Entity showTextPopup(String text, String title) {
    return showTextPopup(
        text,
        title,
        SignComponent.DEFAULT_WIDTH,
        SignComponent.DEFAULT_HEIGHT,
        SignComponent.DEFAULT_ALIGNMENT);
  }

  public static Entity showTextPopup(
      String text, String title, int width, int height, int alignment) {
    return TextDialog.textDialog(
        text, SignComponent.DEFAULT_BUTTON_TEXT, title, width, height, alignment);
  }
}
