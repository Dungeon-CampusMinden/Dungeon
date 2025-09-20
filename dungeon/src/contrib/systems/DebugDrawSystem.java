package contrib.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import contrib.components.AIComponent;
import contrib.components.CollideComponent;
import contrib.components.HealthComponent;
import contrib.components.InteractionComponent;
import core.Entity;
import core.System;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.systems.CameraSystem;
import core.systems.DrawSystem;
import core.utils.Point;
import core.utils.Vector2;
import core.utils.components.MissingComponentException;
import core.utils.components.draw.animation.Animation;
import java.util.List;

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

  private static final ShapeRenderer SHAPE_RENDERER = new ShapeRenderer();
  private static final Color BACKGROUND_COLOR =
      new Color(0f, 0f, 0f, 0.75f); // semi-transparent black

  private static final int CIRCLE_SEGMENTS = 60; // resolution of circles (higher = smoother)
  private final BitmapFont FONT = new BitmapFont();
  private boolean render = false;

  @Override
  public void execute() {
    if (!render) return;

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
    SHAPE_RENDERER.circle(position.x(), position.y(), 0.2f, CIRCLE_SEGMENTS);
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
    if (entity.isPresent(CollideComponent.class)) drawCollideHitbox(entity);
    if (entity.isPresent(VelocityComponent.class)) drawMoveHitbox(entity, pc);
    if (entity.isPresent(InteractionComponent.class)) drawInteractionRange(entity, pc);
    if (CameraSystem.isEntityHovered(entity)) drawEntityInfo(entity, pc);
  }

  /**
   * Draw a red rectangle around the hitbox of the entity.
   *
   * @param entity Entity to draw the rectangle for.
   */
  private void drawCollideHitbox(Entity entity) {
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
   * Draw a blue circle around the interaction range of the entity.
   *
   * @param entity Entity to draw the interaction range for.
   * @param pc PositionComponent of the entity.
   */
  private void drawInteractionRange(Entity entity, PositionComponent pc) {
    InteractionComponent ic =
        entity
            .fetch(InteractionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(entity, InteractionComponent.class));

    float radius = ic.radius();

    SHAPE_RENDERER.begin(ShapeRenderer.ShapeType.Line);
    SHAPE_RENDERER.setColor(Color.CYAN);
    SHAPE_RENDERER.circle(pc.position().x(), pc.position().y(), radius, CIRCLE_SEGMENTS);
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

  /**
   * Draw on entity hover relevant entity infos.
   *
   * <pre>
   *   * Entity ID
   *   * Position; View Direction
   *   * Current Velocity (if VelocityComponent is present)
   *   * curHealth/maxHealth (if HealthComponent is present)
   *   * Current Animation state (if DrawComponent is present)
   *   * List of all components attached to the entity
   * </pre>
   *
   * @param entity The entity to draw info for.
   * @param pc The PositionComponent of the entity.
   */
  private void drawEntityInfo(Entity entity, PositionComponent pc) {
    // In headless mode (e.g., tests) Gdx.gl may be null
    if (Gdx.gl == null) return;

    // Compute layout and render a semi-transparent background + white text near the entity
    drawInfoOverlay(buildInfoText(entity, pc), pc.position());
  }

  /**
   * Builds the multiline info text shown near a hovered entity.
   *
   * <p>Includes:
   *
   * <ul>
   *   <li>Entity name and ID
   *   <li>Position and view direction
   *   <li>Velocity (if present)
   *   <li>Health (if present)
   *   <li>Animation state (if present)
   *   <li>List of all components when holding Shift
   * </ul>
   *
   * @param entity The entity to build info for.
   * @param pc The PositionComponent of the entity.
   * @return The formatted info text.
   */
  private String buildInfoText(Entity entity, PositionComponent pc) {
    Point position = pc.position();

    // TODO: we should use a interface for components that want to add debug info lines e.g.
    // "implements DebugInfoProvider { String debugInfoLine(); }"
    StringBuilder info = new StringBuilder();
    info.append(entity.name()).append(" (").append(entity.id()).append(")\n");
    info.append("Position: (")
        .append(String.format("%.2f", position.x()))
        .append(", ")
        .append(String.format("%.2f", position.y()))
        .append("); ")
        .append(pc.viewDirection())
        .append("\n");

    entity
        .fetch(VelocityComponent.class)
        .ifPresent(
            vc -> {
              String velStr =
                  String.format("(%.2f, %.2f)", vc.currentVelocity().x(), vc.currentVelocity().y());
              info.append("Velocity: ").append(velStr).append("\n");
            });

    entity
        .fetch(HealthComponent.class)
        .ifPresent(
            hc ->
                info.append("Health: ")
                    .append(hc.currentHealthpoints())
                    .append("/")
                    .append(hc.maximalHealthpoints())
                    .append(hc.isDead() ? " (DEAD)" : "")
                    .append(hc.godMode() ? " (GOD)" : "")
                    .append("\n"));

    entity
        .fetch(DrawComponent.class)
        .ifPresent(
            dc ->
                info.append("Animation State: ")
                    .append(dc.currentStateName())
                    .append(" (")
                    .append(dc.currentState().getData())
                    .append(")\n"));

    // We should try to render the path for the current ai; this probably needs a PathAI to check
    // the instance here
    entity
        .fetch(AIComponent.class)
        .ifPresent(
            ai ->
                info.append("AI State: ").append(ai.active() ? "Active" : "Inactive").append("\n"));

    List<String> componentNames =
        entity
            .componentStream()
            .map(comp -> comp.getClass().getSimpleName())
            .sorted(String::compareToIgnoreCase)
            .toList();

    // If holding Shift, show all components; otherwise hint how to show them
    if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)
        || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)) {
      info.append(componentNames.size())
          .append(" component")
          .append(componentNames.size() == 1 ? "" : "s")
          .append("\n");
      componentNames.forEach(name -> info.append(" - ").append(name).append("\n"));
    } else {
      info.append("(Hold Shift to show all ")
          .append(componentNames.size())
          .append(" component")
          .append(componentNames.size() == 1 ? "" : "s")
          .append(")\n");
    }

    // remove last newline for a cleaner box bottom
    info.setLength(info.length() - 1);
    return info.toString();
  }

  /**
   * Renders the info overlay near the given world position.
   *
   * <p>Text is drawn in world space using DrawSystem's batch, but the font is scaled so that it
   * appears at a consistent pixel size regardless of camera zoom. A semi-transparent black
   * background is drawn behind the text using ShapeRenderer.
   *
   * @param text The multiline text to render.
   * @param worldPos The world position near which to render the text.
   */
  private void drawInfoOverlay(String text, Point worldPos) {
    // Ensure crisp text at sub-pixel world positions
    FONT.setUseIntegerPositions(false);

    // Compute pixels-per-world-unit to counteract camera scaling so the font renders at
    // consistent pixel size regardless of zoom.
    float zoom = CameraSystem.camera().zoom;
    float vpw = CameraSystem.camera().viewportWidth;
    float vph = CameraSystem.camera().viewportHeight;
    float pxPerWU_X = Gdx.graphics.getWidth() / (vpw * zoom);
    float pxPerWU_Y = Gdx.graphics.getHeight() / (vph * zoom);

    // Scale the font to cancel out world->screen scaling applied by the batch/camera.
    float scaleX = 1f / pxPerWU_X;
    float scaleY = 1f / pxPerWU_Y;
    FONT.getData().setScale(scaleX, scaleY);

    GlyphLayout layout = new GlyphLayout(FONT, text);

    // Layout dimensions are now in world units due to font scaling above.
    // Convert desired pixel paddings/offsets into world units using the scale.
    float padding = 4f * scaleX; // ~4px padding (using X as baseline for a uniform box)
    float offsetX = 8f * scaleX; // ~8px to the right of the entity
    float offsetY = 12f * scaleY; // ~12px above the entity

    float textX = worldPos.x() + offsetX;
    float textY = worldPos.y() + offsetY; // y is baseline for BitmapFont
    float bgX = textX - padding;
    float bgY = textY - layout.height - padding; // background below baseline
    float bgW = layout.width + 2f * padding;
    float bgH = layout.height + 2f * padding;

    // semi-transparent black box
    Gdx.gl.glEnable(GL20.GL_BLEND);
    Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    SHAPE_RENDERER.begin(ShapeRenderer.ShapeType.Filled);
    SHAPE_RENDERER.setColor(BACKGROUND_COLOR);
    SHAPE_RENDERER.rect(bgX, bgY, bgW, bgH);
    SHAPE_RENDERER.end();

    // Draw text on top using the shared batch
    Batch batch = DrawSystem.batch();
    boolean started = false;
    if (!batch.isDrawing()) {
      batch.setProjectionMatrix(CameraSystem.camera().combined);
      batch.begin();
      started = true;
    }
    FONT.setColor(Color.WHITE);
    FONT.draw(batch, layout, textX, textY);
    if (started) {
      batch.end();
    }
  }

  /** Whether this debug system is currently active and drawing the overlays. */
  public void toggleHUD() {
    this.render = !this.render;
  }

  @Override
  public void stop() {
    this.run = true; // This system can not be stopped.
  }

  @Override
  public void run() {
    this.run = true;
  }
}
