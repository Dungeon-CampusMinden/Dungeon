package systems;

import components.PathComponent;
import contrib.utils.components.ai.AIUtils;
import core.Entity;
import core.Game;
import core.System;
import core.utils.components.MissingComponentException;

/**
 * The PathSystem class extends the System class and is responsible for controlling the Hero's
 * movement. It processes entities with the PathComponent. This class is part of the contrib.systems
 * package.
 */
public class PathSystem extends System {

  private final int delay = Game.frameRate();

  /**
   * The constructor for the PathSystem class. It calls the superclass constructor with the
   * PathComponent class as an argument.
   */
  public PathSystem() {
    super(PathComponent.class);
  }

  /**
   * The execute method is overridden from the System class. It applies the executePath method to
   * each entity in the entity stream.
   */
  @Override
  public void execute() {
    this.entityStream().forEach(this::executePath);
  }

  /**
   * The executePath method is responsible for moving the entity along the path. It fetches the
   * PathComponent of the entity and throws an exception if it is missing. If the path is null or
   * has no elements, the method returns without doing anything. Otherwise, it moves the entity
   * along the path and updates the time since the last update.
   *
   * @param entity The entity to be moved.
   */
  private void executePath(Entity entity) {
    PathComponent path =
        entity
            .fetch(PathComponent.class)
            .orElseThrow(() -> MissingComponentException.build(entity, PathComponent.class));

    if (path.path() == null || path.path().getCount() == 0) {
      return;
    }

    AIUtils.move(entity, path.path());
  }
}
