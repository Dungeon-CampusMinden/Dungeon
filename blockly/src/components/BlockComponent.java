package components;

import core.Component;
import core.Game;
import core.components.PositionComponent;
import core.level.TileLevel;

/**
 * Marks an entity as blocking, preventing the player from entering the tile it occupies.
 *
 * <p>The {@link BlockComponent} interacts with the pathfinding system to modify the accessibility
 * of a tile in the game. When a {@link BlockComponent} is added to an entity, the tile at the
 * entity's position is marked as blocked, and the player is unable to move onto that tile.
 *
 * <p>When the {@link BlockComponent} is created, it removes the corresponding tile from the
 * pathfinding system to ensure that the player cannot find a path through this tile. When the
 * component is discarded, the tile is re-added to the pathfinding system, restoring its
 * accessibility.
 */
public final class BlockComponent implements Component {

  private final PositionComponent pc;

  /**
   * Creates a new {@link BlockComponent}.
   *
   * <p>The position is used to remove the corresponding tile from the pathfinding system.
   *
   * @param pc The {@link PositionComponent} of the entity
   */
  public BlockComponent(final PositionComponent pc) {
    this.pc = pc;
    ((TileLevel) Game.currentLevel()).removeFromPathfinding(Game.tileAT(pc.position()));
  }

  /**
   * Discards this {@link BlockComponent}, restoring the accessibility of the blocked tile.
   *
   * <p>When the component is discarded, the tile at the entity’s position is re-added to the
   * pathfinding system, marking it as accessible again.
   */
  @Override
  public void discard() {
    ((TileLevel) Game.currentLevel()).addToPathfinding(Game.tileAT(pc.position()));
  }
}
