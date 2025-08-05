package contrib.systems;

import contrib.components.BlockComponent;
import core.Entity;
import core.Game;
import core.System;
import core.components.PositionComponent;
import core.level.DungeonLevel;
import core.level.Tile;
import core.utils.Point;
import core.utils.components.MissingComponentException;
import java.util.HashMap;
import java.util.Optional;
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

  // Removes the entity's tile from pathfinding if present
  private final Consumer<Entity> onRemove =
      entity -> {
        BSData data = buildDataObject(entity);
        oldPositions.remove(data.pc);
        Game.tileAT(data.pc.position())
            .ifPresent(tile -> ((DungeonLevel) Game.currentLevel()).addToPathfinding(tile));
      };

  // Blocks the entity's current tile from pathfinding if present
  private final Consumer<Entity> onAdd =
      entity -> {
        BSData data = buildDataObject(entity);
        oldPositions.put(data.pc, data.pc.position());
        Game.tileAT(data.pc.position())
            .ifPresent(tile -> ((DungeonLevel) Game.currentLevel()).removeFromPathfinding(tile));
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
   * <p>If the entity has moved, the tile at the old position will be freed (added back to the
   * pathfinding), and the tile at the new position will be blocked (removed from the pathfinding).
   *
   * <p>If no tile exists at either position, that specific update step is skipped.
   *
   * <p>Internally uses {@link Game#tileAT(Point)}, which now returns an {@link Optional<Tile>}.
   */
  private void updateTiles(BSData data) {
    Point currentP = data.pc.position();
    Point oldP = oldPositions.get(data.pc);
    if (currentP.equals(oldP)) return;

    Game.tileAT(oldP)
        .ifPresent(tile -> ((DungeonLevel) Game.currentLevel()).addToPathfinding(tile));

    Game.tileAT(currentP)
        .ifPresent(tile -> ((DungeonLevel) Game.currentLevel()).removeFromPathfinding(tile));

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
