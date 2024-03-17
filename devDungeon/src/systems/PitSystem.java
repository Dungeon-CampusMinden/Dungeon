package systems;

import contrib.components.ProjectileComponent;
import core.Entity;
import core.Game;
import core.System;
import core.components.PositionComponent;
import core.level.Tile;
import core.level.elements.tile.PitTile;
import core.utils.components.MissingComponentException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Manages the pit system. A pit is a open or closed hole in the ground. If it is open, the player
 * dies (see {@link FallingSystem}). If it is closed, the player can walk over it, but the pit gets
 * opened up after a certain amount of time. The pit system will manage the opening of the pits.
 */
public class PitSystem extends System {
  private final Map<PitTile, Long> pitTimes = new HashMap<>();

  /**
   * Constructor for the PitSystem class. This system processes entities with the PositionComponent.
   */
  public PitSystem() {
    super(PositionComponent.class);
  }

  @Override
  public void execute() {
    this.processEntities();
    this.openPits();
  }

  /** Process each entity and add it to the pitTimes map if it's on a PitTile. */
  private void processEntities() {
    this.entityStream()
        .filter(entity -> !entity.isPresent(ProjectileComponent.class))
        .forEach(
            entity -> {
              PositionComponent positionComponent = this.getPositionComponent(entity);
              Tile currentTile = Game.tileAT(positionComponent.position());

              if (currentTile instanceof PitTile pitTile) {
                this.pitTimes.putIfAbsent(pitTile, java.lang.System.currentTimeMillis());
              }
            });
  }

  /** Open pits that have been stepped on for more than their timeToOpen. */
  private void openPits() {
    Iterator<Map.Entry<PitTile, Long>> pitIterator = this.pitTimes.entrySet().iterator();

    while (pitIterator.hasNext()) {
      Map.Entry<PitTile, Long> pitEntry = pitIterator.next();
      PitTile pitTile = pitEntry.getKey();
      Long stepOnTime = pitEntry.getValue();

      if (this.hasPitOpenTimeElapsed(stepOnTime, pitTile.timeToOpen())) {
        pitTile.open();
        pitIterator.remove();
      }
    }
  }

  /**
   * Get the PositionComponent of an entity.
   *
   * @param entity The entity to get the PositionComponent from.
   * @return The PositionComponent of the entity.
   * @throws MissingComponentException If the entity does not have a PositionComponent.
   */
  private PositionComponent getPositionComponent(Entity entity) {
    return entity
        .fetch(PositionComponent.class)
        .orElseThrow(() -> MissingComponentException.build(entity, PositionComponent.class));
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
}
