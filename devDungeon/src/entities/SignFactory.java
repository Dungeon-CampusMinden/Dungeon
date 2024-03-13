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
import java.util.function.BiConsumer;

/**
 * The SignFactory class is responsible for creating sign entities in the game. It defines the
 * default interaction radius and the texture for the sign.
 */
public class SignFactory {

  private static final float DEFAULT_INTERACTION_RADIUS = 2.5f;
  private static final Animation SIGN_TEXTURE =
      Animation.fromSingleImage(
          new SimpleIPath("objects/mailbox/mailbox_2.png")); // TODO: Change to sign texture

  /**
   * Creates a sign entity with a default title at a given position.
   *
   * @param text The text of the sign.
   * @param pos The position where the sign will be created.
   * @return The created sign entity.
   * @see #createSign(String, String, Point, BiConsumer) createSign
   * @see SignComponent#DEFAULT_TITLE
   */
  public static Entity createSign(String text, Point pos) {
    return createSign(text, SignComponent.DEFAULT_TITLE, pos, (a, b) -> {});
  }

  /**
   * Creates a sign entity at a given position.
   *
   * @param text The text of the sign.
   * @param title The title of the sign.
   * @param pos The position where the sign will be created.
   * @return The created sign entity.
   * @see SignComponent
   */
  public static Entity createSign(
      String text, String title, Point pos, BiConsumer<Entity, Entity> onInteract) {
    Entity sign = new Entity("sign");

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
              onInteract.accept(entity, who);
              sc.showDialog();
            }));

    return sign;
  }

  /**
   * Displays a text popup with a default width, height, and alignment.
   *
   * @param text The text of the popup.
   * @param title The title of the popup.
   * @return The popup entity.
   * @see #showTextPopup(String, String, int, int, int) showTextPopup
   * @see SignComponent#DEFAULT_WIDTH
   * @see SignComponent#DEFAULT_HEIGHT
   * @see SignComponent#DEFAULT_ALIGNMENT
   */
  public static Entity showTextPopup(String text, String title) {
    return showTextPopup(
        text,
        title,
        SignComponent.DEFAULT_WIDTH,
        SignComponent.DEFAULT_HEIGHT,
        SignComponent.DEFAULT_ALIGNMENT);
  }

  /**
   * Displays a text popup with a specified width, height, and alignment.
   *
   * @param text The text of the popup.
   * @param title The title of the popup.
   * @param width The width of the popup.
   * @param height The height of the popup.
   * @param alignment The alignment of the popup.
   * @return The popup entity.
   * @see TextDialog#textDialog(String, String, String, int, int, int) TextDialog#textDialog
   */
  public static Entity showTextPopup(
      String text, String title, int width, int height, int alignment) {
    return TextDialog.textDialog(
        text, SignComponent.DEFAULT_BUTTON_TEXT, title, width, height, alignment);
  }
}
