package contrib.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import contrib.components.CollideComponent;
import contrib.utils.components.skill.SkillTools;
import core.Entity;
import core.System;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.systems.CameraSystem;
import core.systems.LevelSystem;
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

  private static final Vector2 INV_OFFSET = Vector2.of(-0.5f, -0.25f);
  private static final ShapeRenderer SHAPE_RENDERER = new ShapeRenderer();

  @Override
  public void execute() {
    SHAPE_RENDERER.setProjectionMatrix(CameraSystem.camera().combined);
    filteredEntityStream(PositionComponent.class).forEach(this::drawPosition);
    showTileUnderCursor();
  }

  private void showTileUnderCursor(){
    Point mosPos = SkillTools.cursorPositionAsPoint();
    mosPos = new Point(mosPos.x(), mosPos.y());
    Point tilePos = new Point((int)mosPos.x(), (int)mosPos.y());

    LevelSystem.level().flatMap(level -> level.tileAt(tilePos)).ifPresent(tile -> {
      renderRect(tile.position(), 1, 1, new Color(1, 1, 1, 0.2f));
    });
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
    if (entity.isPresent(CollideComponent.class)) drawCollideHitbox(entity, pc);
    if (entity.isPresent(VelocityComponent.class)) drawMoveHitbox(entity, pc);
  }

  /**
   * Draw a red rectangle around the hitbox of the entity.
   *
   * @param entity Entity to draw the rectangle for.
   * @param pc PositionComponent of the entity.
   */
  private void drawCollideHitbox(Entity entity, PositionComponent pc) {
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

  /**
   * Draws a white rectangle representing the entity's movement hitbox.
   *
   * <p>This is useful for debugging collision and movement boundaries.
   *
   * @param entity The entity whose movement hitbox should be drawn. Must have a {@link
   *     VelocityComponent}.
   * @param pc The {@link PositionComponent} of the entity, used as the reference point for the
   *     hitbox.
   */
  private void drawMoveHitbox(Entity entity, PositionComponent pc) {
    VelocityComponent vc =
        entity
            .fetch(VelocityComponent.class)
            .orElseThrow(() -> MissingComponentException.build(entity, VelocityComponent.class));

    // Compute bottom-left corner of the hitbox
    Point bottomLeft = pc.position().translate(vc.moveboxOffset());
    Vector2 size = vc.moveboxSize();
    float width = size.x();
    float height = size.y();

    SHAPE_RENDERER.begin(ShapeRenderer.ShapeType.Line);
    SHAPE_RENDERER.setColor(Color.WHITE);
    SHAPE_RENDERER.rect(bottomLeft.x(), bottomLeft.y(), width, height);
    SHAPE_RENDERER.end();
  }

  public static void renderRect(Point point, Point point2){
    Vector2 diff = point2.vectorTo(point) ;
    renderRect(point, diff.x(), diff.y(), Color.WHITE);
  }
  public static void renderRect(Point point, float width, float height){
    renderRect(point, width, height, Color.WHITE);
  }
  public static void renderRect(Point point, float width, float height, Color color){
    SHAPE_RENDERER.setProjectionMatrix(CameraSystem.camera().combined);
    SHAPE_RENDERER.begin(ShapeRenderer.ShapeType.Line);
    Gdx.gl.glEnable(GL20.GL_BLEND);
    SHAPE_RENDERER.setColor(color);
    SHAPE_RENDERER.rect(point.x(), point.y(), width, height);
    SHAPE_RENDERER.end();
  }

  public static void renderCircle(Point point, float radius){
    renderCircle(point, radius, Color.WHITE);
  }
  public static void renderCircle(Point point, float radius, Color color){
    point = point.translate(INV_OFFSET);
    SHAPE_RENDERER.setProjectionMatrix(CameraSystem.camera().combined);
    SHAPE_RENDERER.begin(ShapeRenderer.ShapeType.Line);
    Gdx.gl.glEnable(GL20.GL_BLEND);
    SHAPE_RENDERER.setColor(color);
    SHAPE_RENDERER.circle(point.x(), point.y(), radius, 30);
    SHAPE_RENDERER.end();
  }

  public static void renderLine(Point point, Point other){
    renderLine(point, other, Color.WHITE);
  }
  public static void renderLine(Point point, Point other, Color color){
    point = point.translate(INV_OFFSET);
    other = other.translate(INV_OFFSET);
    SHAPE_RENDERER.setProjectionMatrix(CameraSystem.camera().combined);
    SHAPE_RENDERER.begin(ShapeRenderer.ShapeType.Line);
    Gdx.gl.glEnable(GL20.GL_BLEND);
    SHAPE_RENDERER.setColor(color);
    SHAPE_RENDERER.line(point.x(), point.y(), other.x(), other.y());
    SHAPE_RENDERER.end();
  }

  public static void renderArrow(Point point, Point other){
    renderArrow(point, other, Color.WHITE);
  }
  public static void renderArrow(Point point, Point other, Color color){
    //Arrow base
    renderCircle(point, 0.1f, color);

    //Arrow body
    renderLine(point, other, color);

    //Arrow head
    Vector2 dir = point.vectorTo(other).normalize();
    Vector2 rotated = dir.rotateDeg(90).scale(0.15f);
    dir = dir.scale(-0.3f);

    Point offset = other.translate(dir);
    Point left = offset.translate(rotated);
    Point right = offset.translate(rotated.scale(-1));

    other = other.translate(INV_OFFSET);
    left = left.translate(INV_OFFSET);
    right = right.translate(INV_OFFSET);

    SHAPE_RENDERER.setProjectionMatrix(CameraSystem.camera().combined);
    SHAPE_RENDERER.begin(ShapeRenderer.ShapeType.Filled);
    Gdx.gl.glEnable(GL20.GL_BLEND);
    SHAPE_RENDERER.setColor(color);
    SHAPE_RENDERER.triangle(other.x(), other.y(), left.x(), left.y(), right.x(), right.y());
    SHAPE_RENDERER.end();
  }

}
