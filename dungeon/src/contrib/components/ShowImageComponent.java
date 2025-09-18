package contrib.components;

import contrib.utils.components.showImage.ShowImageText;
import core.Component;
import core.Entity;
import java.util.function.BiConsumer;

public class ShowImageComponent implements Component {

  public String imagePath;
  public ShowImageText textConfig;
  public boolean isUIOpen = false;
  public BiConsumer<Entity, Entity> onOpenAction;
  public BiConsumer<Entity, Entity> onCloseAction;
  public Entity overlay;

  /** Defines the maximum size the image should occupy on the screen, in its biggest axis. */
  public float maxSize = 0.85f;

  public ShowImageComponent(String imagePath) {
    this.imagePath = imagePath;
  }

  public void onOpen(Entity entity, Entity overlay) {
    if (onOpenAction != null) {
      onOpenAction.accept(entity, overlay);
    }
  }

  public void onClose(Entity entity, Entity overlay) {
    if (onCloseAction != null) {
      onCloseAction.accept(entity, overlay);
    }
  }
}
