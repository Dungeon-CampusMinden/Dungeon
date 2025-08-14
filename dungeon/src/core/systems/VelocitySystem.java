package core.systems;

import com.badlogic.gdx.Gdx;
import core.Entity;
import core.Game;
import core.System;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.level.Tile;
import core.level.utils.LevelElement;
import core.utils.Direction;
import core.utils.Point;
import core.utils.Vector2;
import core.utils.components.MissingComponentException;

/**
 * The VelocitySystem controls the movement of the entities in the game.
 *
 * <p>Entities with the {@link VelocityComponent}, {@link PositionComponent}, and {@link
 * DrawComponent} will be processed by this system.
 *
 * <p>The system will take the {@link VelocityComponent#currentVelocity()} and calculate the new
 * position of the entity based on their current position stored in the {@link PositionComponent}.
 * If the new position is a valid position, which means the tile they would stand on is accessible,
 * the new position will be set. If the new position is walled, the {@link
 * VelocityComponent#onWallHit()} callback will be executed.
 *
 * <p>This system will also queue the corresponding run or idle animation.
 *
 * <p>At the end, the {@link VelocityComponent#currentVelocity(Vector2)} will be set to 0.
 *
 * @see VelocityComponent
 * @see DrawComponent
 * @see PositionComponent
 * @see core.level.elements.ILevel
 */
public final class VelocitySystem extends System {

  // default time an Animation should be enqueued
  private static final int DEFAULT_FRAME_TIME = 1;

  /** Create a new VelocitySystem. */
  public VelocitySystem() {
    super(VelocityComponent.class, PositionComponent.class, DrawComponent.class);
  }

  /** Updates the position of all entities based on their velocity. */
  @Override
  public void execute() {
    filteredEntityStream(VelocityComponent.class, PositionComponent.class, DrawComponent.class)
        .map(this::buildDataObject)
        .forEach(this::updatePosition);
  }

  private void updatePosition(VSData vsd) {
    Vector2 originalVelocity = vsd.vc.currentVelocity();
    Vector2 velocity = originalVelocity;

    float maxSpeed = Math.max(Math.abs(vsd.vc.velocity().x()), Math.abs(vsd.vc.velocity().y()));
    // Limit velocity to maxSpeed (primarily for diagonal movement)
    if (velocity.length() > maxSpeed) {
      velocity = velocity.normalize();
      velocity = velocity.scale(maxSpeed);
    }

    if (Gdx.graphics != null) {
      velocity = velocity.scale(Gdx.graphics.getDeltaTime());
    }

    float newX = vsd.pc.position().x() + velocity.x();
    float newY = vsd.pc.position().y() + velocity.y();
    boolean hitWall = false;
    boolean canEnterOpenPits =
        vsd.e.fetch(VelocityComponent.class).map(VelocityComponent::canEnterOpenPits).orElse(false);
    try {
      if (this.isAccessible(Game.tileAT(new Point(newX, newY)), canEnterOpenPits)) {
        // no change in direction
        vsd.pc.position(new Point(newX, newY));
        this.movementAnimation(vsd);
      } else if (this.isAccessible(
          Game.tileAT(new Point(newX, vsd.pc.position().y())), canEnterOpenPits)) {
        // redirect not moving along y
        hitWall = true;
        vsd.pc.position(new Point(newX, vsd.pc.position().y()));
        this.movementAnimation(vsd);
        vsd.vc.currentVelocity(Vector2.of(velocity.x(), 0.0f));
      } else if (this.isAccessible(
          Game.tileAT(new Point(vsd.pc.position().x(), newY)), canEnterOpenPits)) {
        // redirect not moving along x
        hitWall = true;
        vsd.pc.position(new Point(vsd.pc.position().x(), newY));
        this.movementAnimation(vsd);
        vsd.vc.currentVelocity(Vector2.of(0.0f, velocity.y()));
      } else {
        hitWall = true;
      }

      if (hitWall) vsd.vc.onWallHit().accept(vsd.e);

      // Friction
      float friction = Game.tileAT(vsd.pc.position()).friction();
      float damp = Math.max(0.0f, 1.0f - friction);
      // If we hit a wall, damp the raw velocity; otherwise damp the movement velocity
      Vector2 toDampen = hitWall ? originalVelocity : velocity;
      float newVX = toDampen.x() * damp;
      if (Math.abs(newVX) < 0.01f) newVX = 0.0f;
      float newVY = toDampen.y() * damp;
      if (Math.abs(newVY) < 0.01f) newVY = 0.0f;

      vsd.vc.currentVelocity(Vector2.of(newVX, newVY));
    } catch (NullPointerException e) {
      // for some reason the entity is out of bound
      vsd.pc.position(PositionComponent.ILLEGAL_POSITION);
      LOGGER.warning("Entity " + e + " is out of bound");
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

  private void movementAnimation(VSData vsd) {
    float x = vsd.vc.currentVelocity().x();
    float y = vsd.vc.currentVelocity().y();

    // move
    if (x != 0 || y != 0) {
      Direction newDirection = Direction.NONE;
      if (x > 0) {
        newDirection = Direction.RIGHT;
      } else if (x < 0) {
        newDirection = Direction.LEFT;
      } else if (y > 0) {
        newDirection = Direction.UP;
      } else if (y < 0) {
        newDirection = Direction.DOWN;
      }
      vsd.dc.sendSignal("move", newDirection);
      vsd.pc.viewDirection(newDirection);
      vsd.vc.previousVelocity(Vector2.of(x, y));
    }
    // idle
    else {
      // each drawComponent has an idle animation, so no check is needed
      vsd.dc.sendSignal("idle", vsd.pc.viewDirection());
    }
  }

  private VSData buildDataObject(Entity e) {
    VelocityComponent vc =
        e.fetch(VelocityComponent.class)
            .orElseThrow(() -> MissingComponentException.build(e, VelocityComponent.class));

    PositionComponent pc =
        e.fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(e, PositionComponent.class));

    DrawComponent dc =
        e.fetch(DrawComponent.class)
            .orElseThrow(() -> MissingComponentException.build(e, DrawComponent.class));

    return new VSData(e, vc, pc, dc);
  }

  private record VSData(Entity e, VelocityComponent vc, PositionComponent pc, DrawComponent dc) {}
}
