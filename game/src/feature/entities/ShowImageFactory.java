package feature.entities;

import engine.Entity;
import engine.components.DrawComponent;
import engine.components.PositionComponent;
import engine.utils.Point;
import engine.utils.Vector2;
import engine.utils.components.path.SimpleIPath;
import feature.hud.DialogUtils;
import feature.interaction.Interaction;
import feature.interaction.InteractionComponent;
import java.util.function.Consumer;

/**
 * Factory class for creating ShowImage entities.
 *
 * <p>Interacting with such an entity shows the image to the interacting player as an image dialog.
 */
public class ShowImageFactory {

  private static final float INTERACTION_RADIUS = 1.5f;
  private static final float X_OFFSET = 0.5f;
  private static final float Y_OFFSET = 0.25f;

  /**
   * Creates an entity that will show the specified image when interacted with, at the designated
   * position.
   *
   * @param pos the position of the entity
   * @param spriteImage the path to the sprite image representing the entity
   * @param imagePath the path of the image to be shown
   * @param onClose receives the player that closed the image
   * @param radius the interaction radius of the entity
   * @return the entity
   */
  public static Entity createShowImage(
      Point pos, String spriteImage, String imagePath, Consumer<Entity> onClose, float radius) {
    Entity entity = new Entity("show-image");
    entity.add(new PositionComponent(pos.translate(Vector2.of(X_OFFSET, Y_OFFSET))));
    entity.add(new DrawComponent(new SimpleIPath(spriteImage)));
    entity.add(
        new InteractionComponent(
            new Interaction(
                (e, who) ->
                    DialogUtils.showImagePopUp(imagePath, () -> onClose.accept(who), who.id()),
                radius)));
    return entity;
  }

  /**
   * Creates an entity that will show the specified image when interacted with, at the designated
   * position.
   *
   * @param pos the position of the entity
   * @param spriteImage the path to the sprite image representing the entity
   * @param imagePath the path of the image to be shown
   * @return the entity
   */
  public static Entity createShowImage(Point pos, String spriteImage, String imagePath) {
    return createShowImage(pos, spriteImage, imagePath, who -> {}, INTERACTION_RADIUS);
  }
}
