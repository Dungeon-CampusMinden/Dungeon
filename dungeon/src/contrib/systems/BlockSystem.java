package contrib.systems;

import contrib.components.BlockComponent;
import core.Entity;
import core.Game;
import core.System;
import core.components.PositionComponent;
import core.level.DungeonLevel;
import core.utils.Point;
import core.utils.components.MissingComponentException;
import java.util.HashMap;
import java.util.function.Consumer;

/**
 * The BlockSystem manages the pathfinding by adding and removing tiles based on {@link
 * BlockComponent}-Entities.
 *
 * <p>Entities with the {@link BlockComponent} and {@link PositionComponent} will be processed by
 * this system.
 *
 * <p>The system takes the {@link PositionComponent#position()} of an entity and removes the tile
 * where the entity is placed from the pathfinding.
 *
 * <p>This system also checks for updated positions. If an entity moves, the tiles will be freed and
 * blocked accordingly.
 *
 * <p>If a {@link BlockComponent} gets removed, this system will free the respective tile.
 *
 * @see BlockComponent
 */
public final class BlockSystem extends System {

  private HashMap<PositionComponent, Point> oldPositions;
  private final Consumer<Entity> onRemove =
      entity -> {
        BSData data = buildDataObject(entity);
        oldPositions.remove(data.pc);
        ((DungeonLevel) Game.currentLevel().orElse(null))
            .addToPathfinding(Game.tileAt(data.pc.position()).orElse(null));
      };

  private final Consumer<Entity> onAdd =
      entity -> {
        BSData data = buildDataObject(entity);
        oldPositions.put(data.pc, data.pc.position());
        ((DungeonLevel) Game.currentLevel().orElse(null))
            .removeFromPathfinding(Game.tileAt(data.pc.position()).orElse(null));
      };

  /** Creates a new BlockSystem. */
  public BlockSystem() {
    super(BlockComponent.class, PositionComponent.class);
    this.onEntityAdd = onAdd;
    this.onEntityRemove = onRemove;
    oldPositions = new HashMap<>();
  }

  /**
   * Executes the system, processing all entities with {@link BlockComponent} and {@link
   * PositionComponent}.
   *
   * <p>It updates the pathfinding if entities have moved since the last execution.
   */
  @Override
  public void execute() {
    filteredEntityStream(BlockComponent.class, PositionComponent.class)
        .map(this::buildDataObject)
        .forEach(this::updateTiles);
  }

  /**
   * Updates the pathfinding by comparing the old and current positions of an entity.
   *
   * <p>If an entity has moved, it frees the old tile and blocks the new tile accordingly.
   *
   * @param data The data object containing entity information.
   */
  private void updateTiles(BSData data) {
    Point currentP = data.pc.position();
    Point oldP = oldPositions.get(data.pc);
    if (currentP.equals(oldP)) return;
    ((DungeonLevel) Game.currentLevel().orElse(null))
        .addToPathfinding(Game.tileAt(oldP).orElse(null));
    ((DungeonLevel) Game.currentLevel().orElse(null))
        .removeFromPathfinding(Game.tileAt(currentP).orElse(null));
    oldPositions.put(data.pc, currentP);
  }

  private BSData buildDataObject(Entity e) {
    PositionComponent pc =
        e.fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(e, PositionComponent.class));
    return new BSData(e, pc);
  }

  private record BSData(Entity e, PositionComponent pc) {}
}
