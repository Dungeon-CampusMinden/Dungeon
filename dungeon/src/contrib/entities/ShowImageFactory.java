package contrib.entities;

import contrib.components.ShowImageComponent;
import contrib.modules.interaction.Interaction;
import contrib.modules.interaction.InteractionComponent;
import contrib.utils.components.showImage.ShowImageText;
import core.Entity;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.utils.Point;
import core.utils.Vector2;
import core.utils.components.path.SimpleIPath;
import java.util.function.BiConsumer;

/** Factory class for creating ShowImage entities. */
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
   * @param onClose action to be performed when the image is closed
   * @param maxSize the maximum size the image should occupy on the screen, in its biggest axis
   * @param radius the interaction radius of the entity
   * @param text optional text configuration for displaying text alongside the image
   * @return the entity
   */
  public static Entity createShowImage(
      Point pos,
      String spriteImage,
      String imagePath,
      BiConsumer<Entity, Entity> onClose,
      float maxSize,
      float radius,
      ShowImageText text) {
    Entity entity = new Entity("show-image");
    entity.add(new PositionComponent(pos.translate(Vector2.of(X_OFFSET, Y_OFFSET))));

    DrawComponent dc = new DrawComponent(new SimpleIPath(spriteImage));
    entity.add(dc);

    ShowImageComponent sic = new ShowImageComponent(imagePath);
    sic.onCloseAction(onClose);
    sic.maxSize(maxSize);
    sic.textConfig(text);
    entity.add(sic);

    entity.add(
        new InteractionComponent(() -> new Interaction((e, who) -> sic.isUIOpen(true), radius)));
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
    return createShowImage(pos, spriteImage, imagePath, null, 0.85f, INTERACTION_RADIUS, null);
  }
}
