package contrib.utils.components.interaction;

import core.Entity;
import java.util.function.BiConsumer;

public class Interaction implements BiConsumer<Entity, Entity> {

  private String name;
  private BiConsumer<Entity, Entity> onInteraction;

  public Interaction(String name, BiConsumer<Entity, Entity> onInteraction) {
    this.name = name;
    this.onInteraction = onInteraction;
  }

  @Override
  public void accept(Entity object, Entity interactor) {
    onInteraction.accept(object, interactor);
  }

  public String name() {
    return this.name;
  }
}
