package core.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import core.Entity;
import core.Game;
import core.System;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.level.Tile;
import core.level.utils.LevelElement;
import core.utils.Point;
import core.utils.components.MissingComponentException;
import core.utils.components.draw.CoreAnimationPriorities;
import core.utils.components.draw.CoreAnimations;

/**
 * The VelocitySystem controls the movement of the entities in the game.
 *
 * <p>Entities with the {@link VelocityComponent}, {@link PositionComponent}, and {@link
 * DrawComponent} will be processed by this system.
 *
 * <p>The system will take the {@link VelocityComponent#currentXVelocity()} and {@link
 * VelocityComponent#currentYVelocity()} and calculate the new position of the entity based on their
 * current position stored in the {@link PositionComponent}. If the new position is a valid
 * position, which means the tile they would stand on is accessible, the new position will be set.
 * If the new position is walled, the {@link VelocityComponent#onWallHit()} callback will be
 * executed.
 *
 * <p>This system will also queue the corresponding run or idle animation.
 *
 * <p>At the end, the {@link VelocityComponent#currentXVelocity(float)} and {@link
 * VelocityComponent#yVelocity(float)} will be set to 0.
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
    entityStream().map(this::buildDataObject).forEach(this::updatePosition);
  }

  private void updatePosition(VSData vsd) {
    Vector2 velocity = new Vector2(vsd.vc.currentXVelocity(), vsd.vc.currentYVelocity());
    float maxSpeed = Math.max(Math.abs(vsd.vc.xVelocity()), Math.abs(vsd.vc.yVelocity()));
    // Limit velocity to maxSpeed (primarily for diagonal movement)
    if (velocity.len() > maxSpeed) {
      velocity.nor();
      velocity.scl(maxSpeed);
    }
    if (Gdx.graphics != null) {
      velocity.scl(Gdx.graphics.getDeltaTime());
    }

    float newX = vsd.pc.position().x + velocity.x;
    float newY = vsd.pc.position().y + velocity.y;
    boolean hitWall = false;
    boolean canEnterEmptyTiles =
        vsd.e
            .fetch(VelocityComponent.class)
            .map(VelocityComponent::canEnterEmptyTiles)
            .orElse(false);
    try {
      if (isAccessible(Game.tileAT(new Point(newX, newY)), canEnterEmptyTiles)) {
        // no change in direction
        vsd.pc.position(new Point(newX, newY));
        this.movementAnimation(vsd);
      } else if (isAccessible(
          Game.tileAT(new Point(newX, vsd.pc.position().y)), canEnterEmptyTiles)) {
        // redirect not moving along y
        hitWall = true;
        vsd.pc.position(new Point(newX, vsd.pc.position().y));
        this.movementAnimation(vsd);
        vsd.vc.currentYVelocity(0.0f);
      } else if (isAccessible(
          Game.tileAT(new Point(vsd.pc.position().x, newY)), canEnterEmptyTiles)) {
        // redirect not moving along x
        hitWall = true;
        vsd.pc.position(new Point(vsd.pc.position().x, newY));
        this.movementAnimation(vsd);
        vsd.vc.currentXVelocity(0.0f);
      } else {
        hitWall = true;
      }

      if (hitWall) vsd.vc.onWallHit().accept(vsd.e);

      float friction = Game.tileAT(vsd.pc.position()).friction();
      float newVX = vsd.vc.currentXVelocity() * (Math.min(1.0f, 1.0f - friction));
      if (Math.abs(newVX) < 0.01f) newVX = 0.0f;
      float newVY = vsd.vc.currentYVelocity() * (Math.min(1.0f, 1.0f - friction));
      if (Math.abs(newVY) < 0.01f) newVY = 0.0f;

      vsd.vc.currentYVelocity(newVY);
      vsd.vc.currentXVelocity(newVX);
    } catch (NullPointerException e) {
      // for some reason the entity is out of bound
      vsd.pc().position(PositionComponent.ILLEGAL_POSITION);
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
    return tile.isAccessible()
        || (canEnterPitTiles && tile.levelElement().equals(LevelElement.PIT));
  }

  private void movementAnimation(VSData vsd) {
    float x = vsd.vc.currentXVelocity();
    float y = vsd.vc.currentYVelocity();

    // move
    if (x != 0 || y != 0) {
      vsd.dc.deQueueByPriority(CoreAnimationPriorities.RUN.priority());
      if (x > 0) vsd.dc.queueAnimation(CoreAnimations.RUN_RIGHT, CoreAnimations.RUN);
      else if (x < 0) vsd.dc.queueAnimation(CoreAnimations.RUN_LEFT, CoreAnimations.RUN);
      else if (y > 0) vsd.dc.queueAnimation(CoreAnimations.RUN_UP, CoreAnimations.RUN);
      else if (y < 0) vsd.dc.queueAnimation(CoreAnimations.RUN_DOWN, CoreAnimations.RUN);
      vsd.vc.previousXVelocity(x);
      vsd.vc.previousYVelocity(y);

      vsd.dc.deQueueByPriority(CoreAnimationPriorities.IDLE.priority());
    }
    // idle
    else {
      // each drawComponent has an idle animation, so no check is needed
      if (vsd.vc.previousXVelocity() < 0)
        vsd.dc.queueAnimation(
            DEFAULT_FRAME_TIME,
            CoreAnimations.IDLE_LEFT,
            CoreAnimations.IDLE,
            CoreAnimations.IDLE_RIGHT,
            CoreAnimations.IDLE_DOWN,
            CoreAnimations.IDLE_UP);
      else if (vsd.vc.previousXVelocity() > 0)
        vsd.dc.queueAnimation(
            DEFAULT_FRAME_TIME,
            CoreAnimations.IDLE_RIGHT,
            CoreAnimations.IDLE,
            CoreAnimations.IDLE_LEFT,
            CoreAnimations.IDLE_DOWN,
            CoreAnimations.IDLE_UP);
      else if (vsd.vc.previousYVelocity() > 0)
        vsd.dc.queueAnimation(
            DEFAULT_FRAME_TIME,
            CoreAnimations.IDLE_UP,
            CoreAnimations.IDLE,
            CoreAnimations.IDLE_DOWN,
            CoreAnimations.IDLE_LEFT,
            CoreAnimations.IDLE_RIGHT);
      else
        vsd.dc.queueAnimation(
            DEFAULT_FRAME_TIME,
            CoreAnimations.IDLE_DOWN,
            CoreAnimations.IDLE,
            CoreAnimations.IDLE_UP,
            CoreAnimations.IDLE_LEFT,
            CoreAnimations.IDLE_RIGHT);
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
