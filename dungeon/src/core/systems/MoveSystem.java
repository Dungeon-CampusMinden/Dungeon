package core.systems;

import core.Entity;
import core.Game;
import core.System;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.level.Tile;
import core.level.utils.LevelElement;
import core.utils.Point;
import core.utils.Vector2;
import core.utils.components.MissingComponentException;

public class MoveSystem extends System {

  public MoveSystem() {
    super(VelocityComponent.class, PositionComponent.class);
  }

  @Override
  public void execute() {
    filteredEntityStream().map(this::buildDataObject).forEach(this::updatePosition);
  }

  private void updatePosition(MSData data) {
    Vector2 velocity = data.vc.currentVelocity();
    // Limit velocity to maxSpeed (primarily for diagonal movement)
    if (velocity.length() > data.vc.maxSpeed()) {
      velocity = velocity.normalize();
      velocity = velocity.scale(data.vc.maxSpeed());
    }
    Vector2 sv = velocity.scale(1f / Game.frameRate());
    java.lang.System.out.println(velocity);
    Point oldPos = data.pc.position();
    Point newPos = oldPos.translate(sv);
    boolean canEnterOpenPits = data.vc.canEnterOpenPits();
    if (isAccessible(Game.tileAT(newPos), canEnterOpenPits)) {
      data.pc.position(newPos);
    } else {
      Point xMove = new Point(newPos.x(), oldPos.y());
      Point yMove = new Point(oldPos.x(), newPos.y());
      boolean xAccessible = isAccessible(Game.tileAT(xMove), canEnterOpenPits);
      boolean yAccessible = isAccessible(Game.tileAT(yMove), canEnterOpenPits);
      if (xAccessible) {
        data.pc.position(xMove);
      } else if (yAccessible) {
        data.pc.position(yMove);
      }
      data.vc.onWallHit().accept(data.e);
    }
  }

  /**
   * Small helper function to check if a tile is accessible and also considers if the entity can
   * enter empty tiles.
   *
   * @param tile The tile to check.
   * @param canEnterPitTiles If the entity can enter PIT tiles.
   * @return true if the tile is accessible, false if not.
   */
  private boolean isAccessible(Tile tile, boolean canEnterPitTiles) {
    return tile != null
        && (tile.isAccessible()
            || (canEnterPitTiles && tile.levelElement().equals(LevelElement.PIT)));
  }

  private MSData buildDataObject(Entity e) {
    VelocityComponent vc =
        e.fetch(VelocityComponent.class)
            .orElseThrow(() -> MissingComponentException.build(e, VelocityComponent.class));

    PositionComponent pc =
        e.fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(e, PositionComponent.class));

    return new MSData(e, vc, pc);
  }

  private record MSData(Entity e, VelocityComponent vc, PositionComponent pc) {}
}
