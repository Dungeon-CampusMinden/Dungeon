package contrib.systems;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import contrib.components.CollideComponent;
import core.Entity;
import core.System;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.systems.CameraSystem;
import core.utils.Point;
import core.utils.Vector2;
import core.utils.components.MissingComponentException;
import core.utils.components.draw.animation.Animation;

/**
 * A debug system that visually overlays entity information on top of the game world.
 *
 * <p>This system draws the following for all entities with a {@link PositionComponent}:
 *
 * <ul>
 *   <li>A filled dot at the entity's position.
 *   <li>An arrow indicating the entity's view direction.
 *   <li>A green rectangle around the entity's sprite/texture if it has a {@link DrawComponent}.
 *   <li>A red rectangle around the entity's collider hitbox if it has a {@link CollideComponent}.
 * </ul>
 *
 * <p>The system uses a {@link com.badlogic.gdx.graphics.glutils.ShapeRenderer} to draw shapes. Make
 * sure the {@link ShapeRenderer} projection matrix matches the camera used in the main rendering
 * system to ensure alignment with sprites.
 *
 * <p>This system is intended for debugging purposes only and can help visualize positions, sprite
 * boundaries, collider bounds, and view directions of entities.
 */
public class DebugDrawSystem extends System {

  private final ShapeRenderer SHAPE_RENDERER = new ShapeRenderer();

  @Override
  public void execute() {
    SHAPE_RENDERER.setProjectionMatrix(CameraSystem.camera().combined);
    filteredEntityStream(PositionComponent.class).forEach(this::drawPosition);
  }

  private void drawPosition(Entity entity) {
    PositionComponent pc =
        entity
            .fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(entity, PositionComponent.class));
    Point position = pc.position();
    Vector2 view = pc.viewDirection(); // normalized

    // --- filled dot for position ---
    SHAPE_RENDERER.begin(ShapeRenderer.ShapeType.Filled);
    SHAPE_RENDERER.setColor(Color.ORANGE);
    SHAPE_RENDERER.circle(position.x(), position.y(), 0.3f);
    SHAPE_RENDERER.end();

    // --- arrow for view direction ---
    float arrowLength = 0.5f; // tune this to your tile size
    float endX = position.x() + view.x() * arrowLength;
    float endY = position.y() + view.y() * arrowLength;

    SHAPE_RENDERER.begin(ShapeRenderer.ShapeType.Line);
    SHAPE_RENDERER.setColor(Color.YELLOW);
    SHAPE_RENDERER.line(position.x(), position.y(), endX, endY);

    // optional: tiny "arrowhead"
    float headSize = 0.1f;
    SHAPE_RENDERER.line(endX, endY, endX - view.y() * headSize, endY + view.x() * headSize);
    SHAPE_RENDERER.line(endX, endY, endX + view.y() * headSize, endY - view.x() * headSize);
    SHAPE_RENDERER.end();

    if (entity.isPresent(DrawComponent.class)) drawTextureSize(entity, pc);
    if (entity.isPresent(CollideComponent.class)) drawColideHitbox(entity, pc);
  }

  /**
   * Draw a red rectangle around the hitbox of the entity.
   *
   * @param entity Entity to draw the rectangle for.
   * @param pc PositionComponent of the entity.
   */
  private void drawColideHitbox(Entity entity, PositionComponent pc) {
    CollideComponent cc =
        entity
            .fetch(CollideComponent.class)
            .orElseThrow(() -> MissingComponentException.build(entity, CollideComponent.class));

    Point bottomLeft = cc.bottomLeft(entity);
    Point topRight = cc.topRight(entity);

    float width = topRight.x() - bottomLeft.x();
    float height = topRight.y() - bottomLeft.y();

    SHAPE_RENDERER.begin(ShapeRenderer.ShapeType.Line);
    SHAPE_RENDERER.setColor(Color.RED);
    SHAPE_RENDERER.rect(bottomLeft.x(), bottomLeft.y(), width, height);
    SHAPE_RENDERER.end();
  }

  /**
   * Draw a green rectangle around the texture of the entity.
   *
   * @param entity Entity to draw the rectangle for.
   * @param pc PositionComponent of the entity.
   */
  private void drawTextureSize(Entity entity, PositionComponent pc) {
    DrawComponent dc =
        entity
            .fetch(DrawComponent.class)
            .orElseThrow(() -> MissingComponentException.build(entity, DrawComponent.class));
    Animation a = dc.currentAnimation();
    Point position = pc.position();

    float width = a.getWidth();
    float height = a.getHeight();

    float x = position.x();
    float y = position.y();

    if (a.getConfig().centered()) {
      x -= width / 2f;
      y -= height / 2f;
    }

    SHAPE_RENDERER.begin(ShapeRenderer.ShapeType.Line);
    SHAPE_RENDERER.setColor(Color.GREEN);
    SHAPE_RENDERER.rect(x, y, width, height);
    SHAPE_RENDERER.end();
  }
}
