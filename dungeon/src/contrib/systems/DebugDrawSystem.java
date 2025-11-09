package contrib.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import contrib.components.*;
import contrib.utils.EntityUtils;
import core.Entity;
import core.Game;
import core.System;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.level.DungeonLevel;
import core.level.elements.ILevel;
import core.systems.CameraSystem;
import core.utils.FontHelper;
import core.utils.Point;
import core.utils.Vector2;
import core.utils.components.MissingComponentException;
import core.utils.components.draw.BlendUtil;
import core.utils.components.draw.ColorUtil;
import core.utils.components.draw.animation.Animation;
import java.util.List;
import java.util.Optional;

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

  private static final Batch UI_BATCH = new SpriteBatch();
  private static final OrthographicCamera UI_CAM = new OrthographicCamera();
  private static final ShapeRenderer SHAPE_RENDERER = new ShapeRenderer();
  private static final Color BACKGROUND_COLOR =
      new Color(0f, 0f, 0f, 0.75f); // semi-transparent black
  private static final Color NAMED_POINT_COLOR =
      withAlpha(Color.GREEN, 0.4f); // semi-transparent purple for named points
  private static final Color NAMED_POINT_HIGHLIGHT_COLOR =
      withAlpha(Color.YELLOW, 0.7f); // more opaque yellow for highlighted named points

  private static final int CIRCLE_SEGMENTS = 60; // resolution of circles (higher = smoother)
  private static final BitmapFont FONT = FontHelper.getDefaultFont();
  private boolean render = false;

  @Override
  public void execute() {
    UI_CAM.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

    if (!render) return;

    SHAPE_RENDERER.setProjectionMatrix(CameraSystem.camera().combined);
    filteredEntityStream(PositionComponent.class).forEach(this::drawPosition);

    if (!LevelEditorSystem.active()) {
      drawNamedPoints();
    }
  }

  private void drawPosition(Entity entity) {
    PositionComponent pc =
        entity
            .fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(entity, PositionComponent.class));
    Optional<DecoComponent> decoComponent = entity.fetch(DecoComponent.class);
    Point position = pc.position();
    Vector2 view = pc.viewDirection(); // normalized
    Point centerPos = EntityUtils.getPosition(entity);

    float alpha = decoComponent.isEmpty() ? 1.0f : 0.4f;

    // --- filled dot for position ---
    BlendUtil.setBlending();
    SHAPE_RENDERER.begin(ShapeRenderer.ShapeType.Filled);
    SHAPE_RENDERER.setColor(withAlpha(Color.ORANGE, alpha));
    SHAPE_RENDERER.circle(position.x(), position.y(), 0.05f, CIRCLE_SEGMENTS);
    SHAPE_RENDERER.end();

    if (centerPos != position) {
      // --- filled dot for center position ---
      SHAPE_RENDERER.begin(ShapeRenderer.ShapeType.Filled);
      SHAPE_RENDERER.setColor(withAlpha(Color.BLUE, alpha));
      SHAPE_RENDERER.circle(centerPos.x(), centerPos.y(), 0.05f, CIRCLE_SEGMENTS);
      SHAPE_RENDERER.end();
    }

    // Dont do this for deco entities
    if (decoComponent.isEmpty()) {
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
    }

    if (entity.isPresent(DrawComponent.class)) drawTextureSize(entity, pc, alpha);
    if (entity.isPresent(CollideComponent.class)) drawCollideHitbox(entity, alpha);
    if (entity.isPresent(InteractionComponent.class))
      drawInteractionRange(entity, EntityUtils.getPosition(entity), alpha);
    if (CameraSystem.isEntityHovered(entity) && decoComponent.isEmpty()) drawEntityInfo(entity, pc);
  }

  /** Draws named points from the current level. */
  public static void drawNamedPoints() {
    drawNamedPoints(null);
  }

  /**
   * Draws named points from the current level.
   *
   * @param highlightPoint The name of the point to highlight, or null for none.
   */
  public static void drawNamedPoints(String highlightPoint) {
    ILevel l = Game.currentLevel().orElse(null);
    if (l == null) return;
    DungeonLevel level = (DungeonLevel) l;
    level
        .namedPoints()
        .forEach(
            (name, point) -> {
              Color color =
                  name.equals(highlightPoint) ? NAMED_POINT_HIGHLIGHT_COLOR : NAMED_POINT_COLOR;
              // Draw a small purple square at the point location
              drawRectangleOutline(point.x(), point.y(), 1.0f, 1.0f, color);

              // Draw the name of the point above it
              drawTextInWorldCoordsCentered(FONT, name, point.translate(0.5f, 0.5f), color);
            });
  }

  /**
   * Draw a red rectangle around the hitbox of the entity.
   *
   * @param entity Entity to draw the rectangle for.
   * @param alpha Alpha transparency value.
   */
  private void drawCollideHitbox(Entity entity, float alpha) {
    CollideComponent cc =
        entity
            .fetch(CollideComponent.class)
            .orElseThrow(() -> MissingComponentException.build(entity, CollideComponent.class));

    Point bottomLeft = cc.collider().absoluteBottomLeft();
    Point topRight = cc.collider().absoluteTopRight();

    float width = topRight.x() - bottomLeft.x();
    float height = topRight.y() - bottomLeft.y();

    SHAPE_RENDERER.begin(ShapeRenderer.ShapeType.Line);
    SHAPE_RENDERER.setColor(withAlpha(Color.RED, alpha));
    SHAPE_RENDERER.rect(bottomLeft.x(), bottomLeft.y(), width, height);
    SHAPE_RENDERER.end();
  }

  /**
   * Draw a blue circle around the interaction range of the entity.
   *
   * @param entity Entity to draw the interaction range for.
   * @param pos the position of the entity.
   * @param alpha Alpha transparency value.
   */
  private void drawInteractionRange(Entity entity, Point pos, float alpha) {
    InteractionComponent ic =
        entity
            .fetch(InteractionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(entity, InteractionComponent.class));

    float radius = ic.radius();

    SHAPE_RENDERER.begin(ShapeRenderer.ShapeType.Line);
    SHAPE_RENDERER.setColor(withAlpha(Color.CYAN, alpha));
    SHAPE_RENDERER.circle(pos.x(), pos.y(), radius, CIRCLE_SEGMENTS);
    SHAPE_RENDERER.end();
  }

  /**
   * Draw a green rectangle around the texture of the entity.
   *
   * @param entity Entity to draw the rectangle for.
   * @param pc PositionComponent of the entity.
   * @param alpha Alpha transparency value.
   */
  private void drawTextureSize(Entity entity, PositionComponent pc, float alpha) {
    DrawComponent dc =
        entity
            .fetch(DrawComponent.class)
            .orElseThrow(() -> MissingComponentException.build(entity, DrawComponent.class));
    Animation a = dc.currentAnimation();
    Point position = pc.position();

    float width = a.getWidth() * pc.scale().x();
    float height = a.getHeight() * pc.scale().y();

    float x = position.x();
    float y = position.y();

    if (a.getConfig().centered()) {
      x -= width / 2f;
      y -= height / 2f;
    }

    SHAPE_RENDERER.begin(ShapeRenderer.ShapeType.Line);
    SHAPE_RENDERER.setColor(withAlpha(Color.GREEN, alpha));
    SHAPE_RENDERER.rect(x, y, width, height);
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
    Vector3 screenPos = CameraSystem.camera().project(new Vector3(worldPos.x(), worldPos.y(), 0));

    GlyphLayout layout = new GlyphLayout(FONT, text);

    // Positioning and padding
    float padding = 4f; // ~4px padding (using X as baseline for a uniform box)
    float offsetX = 8f; // ~8px to the right of the entity
    float offsetY = -12f; // ~12px above the entity

    float textX = screenPos.x + offsetX;
    float textY = screenPos.y + offsetY;
    float bgX = textX - padding;
    float bgY = textY - layout.height - padding; // background below baseline
    float bgW = layout.width + 2f * padding;
    float bgH = layout.height + 2f * padding;

    // semi-transparent black box
    BlendUtil.setBlending();
    SHAPE_RENDERER.setProjectionMatrix(UI_CAM.combined);
    SHAPE_RENDERER.begin(ShapeRenderer.ShapeType.Filled);
    SHAPE_RENDERER.setColor(BACKGROUND_COLOR);
    SHAPE_RENDERER.rect(bgX, bgY, bgW, bgH);
    SHAPE_RENDERER.end();
    SHAPE_RENDERER.setProjectionMatrix(CameraSystem.camera().combined);

    drawText(text, new Point(textX, textY));
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

  private static Color withAlpha(Color color, float alpha) {
    return ColorUtil.pmaColor(new Color(color.r, color.g, color.b, alpha));
  }

  /**
   * Draws the outline of a rectangle at the specified position with the given width, height, and
   * color.
   *
   * @param x the x-coordinate of the bottom-left corner of the rectangle
   * @param y the y-coordinate of the bottom-left corner of the rectangle
   * @param width the width of the rectangle
   * @param height the height of the rectangle
   * @param color the color of the rectangle outline
   */
  public static void drawRectangleOutline(
      float x, float y, float width, float height, Color color) {
    // Enable blending for transparency
    BlendUtil.setBlending();
    SHAPE_RENDERER.setProjectionMatrix(CameraSystem.camera().combined);
    SHAPE_RENDERER.begin(ShapeRenderer.ShapeType.Line);
    SHAPE_RENDERER.setColor(ColorUtil.pmaColor(color));
    SHAPE_RENDERER.rect(x, y, width, height);
    SHAPE_RENDERER.end();
  }

  /**
   * Draws text on the screen at the specified screen coordinates with the given font and color.
   *
   * @param font the {@link BitmapFont} to use for rendering the text
   * @param text the text string to draw
   * @param screen the screen coordinates where the text should be drawn
   * @param color the color of the text
   */
  public static void drawText(BitmapFont font, String text, Point screen, Color color) {
    UI_BATCH.setProjectionMatrix(UI_CAM.combined);
    UI_BATCH.begin();
    font.setColor(color);
    font.draw(UI_BATCH, text, screen.x(), screen.y());
    UI_BATCH.end();
  }

  /**
   * Draws text on the screen at the specified screen coordinates with the given font in white
   * color.
   *
   * @param font the {@link BitmapFont} to use for rendering the text
   * @param text the text string to draw
   * @param screen the screen coordinates where the text should be drawn
   */
  public static void drawText(BitmapFont font, String text, Point screen) {
    drawText(font, text, screen, Color.WHITE);
  }

  /**
   * Draws text on the screen at the specified screen coordinates with the default font and given
   * color.
   *
   * @param text the text string to draw
   * @param screen the screen coordinates where the text should be drawn
   * @param color the color of the text
   */
  public static void drawText(String text, Point screen, Color color) {
    drawText(FONT, text, screen, color);
  }

  /**
   * Draws text on the screen at the specified screen coordinates with the default font in white
   * color.
   *
   * @param text the text string to draw
   * @param screen the screen coordinates where the text should be drawn
   */
  public static void drawText(String text, Point screen) {
    drawText(FONT, text, screen, Color.WHITE);
  }

  /**
   * Draws text in world coordinates using the specified font and color.
   *
   * @param font the {@link BitmapFont} to use for rendering the text
   * @param text the text string to draw
   * @param world the world coordinates where the text should be drawn
   * @param color the color of the text
   */
  public static void drawTextInWorldCoords(BitmapFont font, String text, Point world, Color color) {
    Vector3 screen = CameraSystem.camera().project(new Vector3(world.x(), world.y(), 0));
    drawText(font, text, new Point(screen.x, screen.y), color);
  }

  /**
   * Draws text in world coordinates using the specified font in white color.
   *
   * @param text the text string to draw
   * @param world the world coordinates where the text should be drawn
   * @param color the color of the text
   */
  public static void drawTextInWorldCoords(String text, Point world, Color color) {
    drawTextInWorldCoords(FONT, text, world, color);
  }

  /**
   * Draws text in world coordinates using the default font in white color.
   *
   * @param text the text string to draw
   * @param world the world coordinates where the text should be drawn
   */
  public static void drawTextInWorldCoords(String text, Point world) {
    drawTextInWorldCoords(FONT, text, world, Color.WHITE);
  }

  /**
   * Draws text in world coordinates using the specified font and color.
   *
   * @param font the {@link BitmapFont} to use for rendering the text
   * @param text the text string to draw
   * @param world the world coordinates where the text should be drawn
   * @param color the color of the text
   */
  public static void drawTextInWorldCoordsCentered(
      BitmapFont font, String text, Point world, Color color) {
    Vector3 screen = CameraSystem.camera().project(new Vector3(world.x(), world.y(), 0));
    GlyphLayout layout = new GlyphLayout(font, text);
    float textX = screen.x - layout.width / 2f;
    float textY = screen.y + layout.height / 2f;
    drawText(font, text, new Point(textX, textY), color);
  }
}
