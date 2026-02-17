package contrib.systems;

import contrib.components.AIComponent;
import contrib.components.CollideComponent;
import contrib.components.FlyComponent;
import contrib.components.ProjectileComponent;
import core.Entity;
import core.Game;
import core.System;
import core.components.CameraComponent;
import core.components.PlayerComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.level.Tile;
import core.level.elements.tile.PitTile;
import core.utils.Point;
import core.utils.components.MissingComponentException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Manages the pit system. A pit is an open or closed hole in the ground. If it is open, the player
 * dies (see {@link FallingSystem}). If it is closed, the player can walk over it, but the pit gets
 * opened up after a certain amount of time. The pit system will manage the opening of the pits.
 */
public class PitSystem extends System {
  private final Map<PitTile, Long> pitTimes = new HashMap<>();

  /**
   * Constructor for the PitSystem class. This system processes entities with the PositionComponent
   * and VelocityComponent.
   */
  public PitSystem() {
    super(PositionComponent.class, VelocityComponent.class, CollideComponent.class);
  }

  @Override
  public void execute() {
    processEntities();
    openPits();
  }

  /** Process each entity and add it to the pitTimes map if it's on a PitTile. */
  private void processEntities() {
    filteredEntityStream()
        .filter(entity -> !entity.isPresent(ProjectileComponent.class))
        .filter(entity -> !entity.isPresent(FlyComponent.class))
        .forEach(
            entity -> {
              Tile currentTile = tileAtCenter(entity);

              if (currentTile instanceof PitTile pitTile) {
                // camera focus point entity should not trigger pit
                if (entity.isPresent(CameraComponent.class)
                    && !entity.isPresent(PlayerComponent.class)
                    && !entity.isPresent(AIComponent.class)) return;
                pitTimes.putIfAbsent(pitTile, java.lang.System.currentTimeMillis());
              }
            });
  }

  /** Open pits that have been stepped on for more than their timeToOpen. */
  private void openPits() {

    Iterator<Map.Entry<PitTile, Long>> pitIterator = pitTimes.entrySet().iterator();

    while (pitIterator.hasNext()) {
      Map.Entry<PitTile, Long> pitEntry = pitIterator.next();
      PitTile pitTile = pitEntry.getKey();
      Long stepOnTime = pitEntry.getValue();

      if (hasPitOpenTimeElapsed(stepOnTime, pitTile.timeToOpen())) {
        pitTile.open();
        pitIterator.remove();
      }
    }
  }

  /**
   * Check if the time elapsed since the pit was stepped on is greater than the time to open the
   * pit.
   *
   * @param stepOnTime The time the pit was stepped on.
   * @param timeToOpen The time it takes for the pit to open.
   * @return true if the time elapsed is greater than the time to open, false otherwise.
   */
  private boolean hasPitOpenTimeElapsed(long stepOnTime, long timeToOpen) {
    return java.lang.System.currentTimeMillis() - stepOnTime > timeToOpen;
  }

  private Tile tileAtCenter(Entity entity) {
    CollideComponent cc =
        entity
            .fetch(CollideComponent.class)
            .orElseThrow(() -> MissingComponentException.build(entity, CollideComponent.class));
    Point center = cc.collider().absoluteCenter();
    return Game.tileAt(center).orElse(null);
  }
}
