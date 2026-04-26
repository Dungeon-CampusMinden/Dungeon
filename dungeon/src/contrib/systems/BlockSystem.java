package contrib.systems;

import contrib.components.BlockComponent;
import core.Entity;
import core.Game;
import core.System;
import core.components.PositionComponent;
import core.level.Tile;
import core.level.path.DynamicObstacles;
import core.level.utils.Coordinate;
import core.utils.components.MissingComponentException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * The BlockSystem manages dynamic pathfinding obstacles based on {@link BlockComponent}-Entities.
 *
 * <p>Entities with {@link BlockComponent} and {@link PositionComponent} will be processed by this
 * system. The tile coordinate under the entity is registered in {@link DynamicObstacles}.
 *
 * <p>If an entity moves, the old coordinate is unblocked and the new coordinate is blocked. If a
 * {@link BlockComponent} gets removed, the respective coordinate is unblocked.
 */
public final class BlockSystem extends System {

  private final Map<PositionComponent, Coordinate> oldTileCoords = new HashMap<>();

  private final Consumer<Entity> onRemove =
      entity -> {
        final BSData data = buildDataObject(entity);
        final Coordinate c = currentTileCoord(data.pc);
        oldTileCoords.remove(data.pc);
        DynamicObstacles.unblock(c);
      };

  private final Consumer<Entity> onAdd =
      entity -> {
        final BSData data = buildDataObject(entity);
        final Coordinate c = currentTileCoord(data.pc);
        oldTileCoords.put(data.pc, c);
        DynamicObstacles.block(c);
      };

  /** Creates a new BlockSystem. */
  public BlockSystem() {
    super(BlockComponent.class, PositionComponent.class);
    this.onEntityAdd = onAdd;
    this.onEntityRemove = onRemove;
  }

  @Override
  public void execute() {
    filteredEntityStream(BlockComponent.class, PositionComponent.class)
        .map(this::buildDataObject)
        .forEach(this::updateTiles);
  }

  private void updateTiles(final BSData data) {
    final Coordinate current = currentTileCoord(data.pc);
    final Coordinate old = oldTileCoords.get(data.pc);
    if (Objects.equals(current, old)) return;

    DynamicObstacles.unblock(old);
    DynamicObstacles.block(current);
    oldTileCoords.put(data.pc, current);
  }

  private static Coordinate currentTileCoord(final PositionComponent pc) {
    if (pc == null) return null;
    return Game.tileAt(pc.position()).map(Tile::coordinate).orElse(null);
  }

  private BSData buildDataObject(final Entity e) {
    final PositionComponent pc =
        e.fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(e, PositionComponent.class));
    return new BSData(e, pc);
  }

  private record BSData(Entity e, PositionComponent pc) {}
}
