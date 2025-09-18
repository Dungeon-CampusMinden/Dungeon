package contrib.entities;

import contrib.components.InteractionComponent;
import contrib.components.ShowImageComponent;
import contrib.utils.components.showImage.ShowImageText;
import core.Entity;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.utils.Point;
import core.utils.Vector2;
import core.utils.components.path.SimpleIPath;
import java.util.function.BiConsumer;

public class ShowImageFactory {

  private static final float INTERACTION_RADIUS = 1.5f;
  private static final float X_OFFSET = 0.5f;
  private static final float Y_OFFSET = 0.25f;

  /**
   * Creates a keypad at the designated position.
   *
   * @param pos The position where the lever will be created.
   * @return The created keypad entity.
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
    sic.onCloseAction = onClose;
    sic.maxSize = maxSize;
    sic.textConfig = text;
    entity.add(sic);

    entity.add(new InteractionComponent(radius, true, (e, who) -> sic.isUIOpen = true));
    return entity;
  }

  public static Entity createShowImage(
      Point pos,
      String spriteImage,
      String imagePath,
      BiConsumer<Entity, Entity> onClose,
      float maxSize,
      float radius) {
    return createShowImage(pos, spriteImage, imagePath, onClose, maxSize, radius, null);
  }

  public static Entity createShowImage(
      Point pos,
      String spriteImage,
      String imagePath,
      BiConsumer<Entity, Entity> onClose,
      float maxSize) {
    return createShowImage(pos, spriteImage, imagePath, onClose, maxSize, INTERACTION_RADIUS, null);
  }

  public static Entity createShowImage(
      Point pos, String spriteImage, String imagePath, float maxSize) {
    return createShowImage(pos, spriteImage, imagePath, null, maxSize, INTERACTION_RADIUS, null);
  }

  public static Entity createShowImage(Point pos, String spriteImage, String imagePath) {
    return createShowImage(pos, spriteImage, imagePath, null, 0.85f, INTERACTION_RADIUS, null);
  }
}
