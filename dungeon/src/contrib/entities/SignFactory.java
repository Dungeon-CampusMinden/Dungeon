package contrib.entities;

import contrib.components.InteractionComponent;
import contrib.components.SignComponent;
import contrib.hud.DialogUtils;
import core.Entity;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.utils.Point;
import core.utils.components.MissingComponentException;
import core.utils.components.draw.animation.Animation;
import core.utils.components.path.SimpleIPath;
import java.util.function.BiConsumer;

/**
 * The SignFactory class is responsible for creating sign entities in the game.
 *
 * @see SignComponent SignComponent
 * @see DialogUtils DialogUtils
 */
public class SignFactory {

  private static final float DEFAULT_INTERACTION_RADIUS = 2.5f;
  private static final Animation SIGN_TEXTURE =
      new Animation(new SimpleIPath("objects/mailbox/mailbox_2.png"));

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
   * @param onInteract The function to execute when the sign is interacted with.
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
}
