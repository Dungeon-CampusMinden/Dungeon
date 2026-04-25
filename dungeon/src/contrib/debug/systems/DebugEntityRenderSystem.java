package contrib.debug.systems;

import contrib.components.CollideComponent;
import contrib.components.DecoComponent;
import contrib.debug.draw.DebugDrawFacade;
import contrib.debug.draw.DebugEntityInfoFormatter;
import contrib.modules.interaction.InteractionComponent;
import contrib.utils.EntityUtils;
import core.Entity;
import core.System;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.input.Keys;
import core.camera.CameraViewportState;
import core.platform.Platform;
import core.platform.adapters.CursorAdapter;
import core.utils.InputManager;
import core.utils.Point;
import core.utils.Vector2;
import core.utils.components.MissingComponentException;
import core.utils.components.draw.animation.Animation;
import java.awt.Color;
import java.util.Arrays;

/**
 * The {@code DebugEntityRenderSystem} is responsible for rendering debug information
 * for various entities within the game world during runtime. This includes visualizing
 * their position, orientation, and relevant components such as collision bounds,
 * interaction ranges, and animations.
 *
 * <p>This system operates on the client-side and processes entities containing a
 * {@code PositionComponent}.
 */
public final class DebugEntityRenderSystem extends System {

  private static final float POSITION_RADIUS = 0.05f;
  private static final float ARROW_LENGTH = 0.5f;
  private static final float ARROW_HEAD_SIZE = 0.1f;
  private static final float HOVER_RADIUS = 0.5f;

  private static final int INFO_PADDING = 6;
  private static final int INFO_LINE_HEIGHT = 14;
  private static final int INFO_OFFSET_X = 12;
  private static final int INFO_OFFSET_Y = 12;
  private static final Color INFO_BACKGROUND = new Color(0, 0, 0, 170);
  private static final Color INFO_OUTLINE = new Color(255, 255, 255, 70);

  private final DebugEntityInfoFormatter infoFormatter = new DebugEntityInfoFormatter();

  /**
   * Constructs a new instance of the DebugEntityRenderSystem.
   *
   * <p>This system is responsible for rendering debugging information about entities,
   * such as their positions, view directions, interaction ranges, and other visual
   * overlays typically used for debugging purposes during development.
   */
  public DebugEntityRenderSystem() {
    super(AuthoritativeSide.CLIENT, PositionComponent.class);
  }

  @Override
  public void execute() {}

  @Override
  public void render(float delta) {
    if (!DebugDrawFacade.isHudVisible()) {
      return;
    }

    filteredEntityStream(PositionComponent.class).forEach(this::drawEntityDebug);
  }

  private void drawEntityDebug(Entity entity) {
    PositionComponent pc =
      entity
        .fetch(PositionComponent.class)
        .orElseThrow(() -> MissingComponentException.build(entity, PositionComponent.class));

    boolean isDeco = entity.isPresent(DecoComponent.class);
    float alpha = isDeco ? 0.4f : 1.0f;

    Point position = pc.position();
    Point centerPos = EntityUtils.getPosition(entity);
    Vector2 view = pc.viewDirection();

    DebugDrawFacade.drawWorldCircleFill(
      position, POSITION_RADIUS, withAlpha(Color.ORANGE, alpha));

    if (!samePoint(position, centerPos)) {
      DebugDrawFacade.drawWorldCircleFill(
        centerPos, POSITION_RADIUS, withAlpha(Color.BLUE, alpha));
    }

    if (!isDeco) {
      drawViewDirectionArrow(position, view);
    }

    if (entity.isPresent(DrawComponent.class)) {
      drawTextureBounds(entity, pc, alpha);
    }

    if (entity.isPresent(CollideComponent.class)) {
      drawCollideHitbox(entity, alpha);
    }

    if (entity.isPresent(InteractionComponent.class)) {
      drawInteractionRange(entity, centerPos, alpha);
    }

    if (!isDeco && isEntityHovered(entity, pc)) {
      drawEntityInfo(entity, pc);
    }
  }

  private void drawViewDirectionArrow(Point position, Vector2 view) {
    float endX = position.x() + view.x() * ARROW_LENGTH;
    float endY = position.y() + view.y() * ARROW_LENGTH;

    Point end = new Point(endX, endY);
    DebugDrawFacade.drawWorldLine(position, end, Color.YELLOW);

    Point leftHead =
      new Point(
        end.x() - view.y() * ARROW_HEAD_SIZE,
        end.y() + view.x() * ARROW_HEAD_SIZE);

    Point rightHead =
      new Point(
        end.x() + view.y() * ARROW_HEAD_SIZE,
        end.y() - view.x() * ARROW_HEAD_SIZE);

    DebugDrawFacade.drawWorldLine(end, leftHead, Color.YELLOW);
    DebugDrawFacade.drawWorldLine(end, rightHead, Color.YELLOW);
  }

  private void drawCollideHitbox(Entity entity, float alpha) {
    CollideComponent cc =
      entity
        .fetch(CollideComponent.class)
        .orElseThrow(() -> MissingComponentException.build(entity, CollideComponent.class));

    Point bottomLeft = cc.collider().absoluteBottomLeft();
    Point topRight = cc.collider().absoluteTopRight();

    float width = topRight.x() - bottomLeft.x();
    float height = topRight.y() - bottomLeft.y();

    Color color = cc.isSolid() ? Color.RED : Color.WHITE;

    DebugDrawFacade.drawRectangleOutline(
      bottomLeft.x(), bottomLeft.y(), width, height, withAlpha(color, alpha));
  }

  private void drawInteractionRange(Entity entity, Point pos, float alpha) {
    InteractionComponent ic =
      entity
        .fetch(InteractionComponent.class)
        .orElseThrow(() -> MissingComponentException.build(entity, InteractionComponent.class));

    float radius = ic.interactions().interact().range();

    DebugDrawFacade.drawWorldCircleOutline(
      pos, radius, withAlpha(Color.CYAN, alpha));
  }

  private void drawTextureBounds(Entity entity, PositionComponent pc, float alpha) {
    DrawComponent dc =
      entity
        .fetch(DrawComponent.class)
        .orElseThrow(() -> MissingComponentException.build(entity, DrawComponent.class));

    Animation animation = dc.currentAnimation();
    if (animation == null) {
      return;
    }

    float width = animation.getWidth() * pc.scale().x();
    float height = animation.getHeight() * pc.scale().y();

    float x = pc.position().x();
    float y = pc.position().y();

    if (animation.getConfig().centered()) {
      x -= width / 2f;
      y -= height / 2f;
    }

    DebugDrawFacade.drawRectangleOutline(
      x, y, width, height, withAlpha(Color.GREEN, alpha));
  }

  private boolean isEntityHovered(Entity entity, PositionComponent pc) {
    CursorAdapter cursor = Platform.cursor();
    int mouseX = cursor.screenX();
    int mouseY = cursor.screenY();

    return entity
      .fetch(DrawComponent.class)
      .map(
        dc -> {
          Animation animation = dc.currentAnimation();
          if (animation == null) {
            return false;
          }

          float widthWorld = animation.getWidth() * pc.scale().x();
          float heightWorld = animation.getHeight() * pc.scale().y();

          float x = pc.position().x();
          float y = pc.position().y();

          if (animation.getConfig().centered()) {
            x -= widthWorld / 2f;
            y -= heightWorld / 2f;
          }

          Point bottomLeftScreen = CameraViewportState.worldToScreen(new Point(x, y));

          int widthPx = CameraViewportState.worldLengthToScreen(widthWorld);
          int heightPx = CameraViewportState.worldLengthToScreen(heightWorld);

          int left = Math.round(bottomLeftScreen.x());
          int top = Math.round(bottomLeftScreen.y()) - heightPx;

          return left <= mouseX
            && mouseX <= left + widthPx
            && top <= mouseY
            && mouseY <= top + heightPx;
        })
      .orElseGet(() -> pc.position().distance(cursor.world()) < HOVER_RADIUS);
  }

  private void drawEntityInfo(Entity entity, PositionComponent pc) {
    String text = infoFormatter.format(entity, pc, isShiftPressed());
    String[] lines = text.split("\\R");

    int longestLineLength = Arrays.stream(lines).mapToInt(String::length).max().orElse(0);
    int bgWidth = Math.max(160, longestLineLength * 7 + INFO_PADDING * 2);
    int bgHeight = lines.length * INFO_LINE_HEIGHT + INFO_PADDING * 2;

    Point anchor = CameraViewportState.worldToScreen(pc.position());
    Point topLeft =
      new Point(
        anchor.x() + INFO_OFFSET_X,
        anchor.y() - bgHeight - INFO_OFFSET_Y);

    DebugDrawFacade.drawScreenRectangle(
      topLeft,
      bgWidth,
      bgHeight,
      INFO_BACKGROUND,
      INFO_OUTLINE);

    float textX = topLeft.x() + INFO_PADDING;
    float textY = topLeft.y() + INFO_PADDING + 10;

    for (String line : lines) {
      DebugDrawFacade.drawText(line, new Point(textX, textY), Color.WHITE);
      textY += INFO_LINE_HEIGHT;
    }
  }

  private static boolean samePoint(Point a, Point b) {
    return Math.abs(a.x() - b.x()) < 0.0001f && Math.abs(a.y() - b.y()) < 0.0001f;
  }

  private static boolean isShiftPressed() {
    return InputManager.isKeyPressed(Keys.SHIFT_LEFT)
      || InputManager.isKeyPressed(Keys.SHIFT_RIGHT);
  }

  private static Color withAlpha(Color color, float alpha) {
    return new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.round(alpha * 255f));
  }
}
