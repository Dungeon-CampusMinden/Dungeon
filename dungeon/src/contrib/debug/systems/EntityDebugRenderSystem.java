package contrib.debug.systems;

import contrib.components.AIComponent;
import contrib.components.CollideComponent;
import contrib.components.DecoComponent;
import contrib.components.HealthComponent;
import contrib.components.InventoryComponent;
import contrib.modules.interaction.InteractionComponent;
import contrib.utils.EntityUtils;
import core.Entity;
import core.System;
import core.components.DrawComponent;
import core.components.PlayerComponent;
import core.components.PositionComponent;
import core.components.SoundComponent;
import core.components.VelocityComponent;
import core.input.Keys;
import core.platform.client.ClientCursorAdapter;
import core.camera.CameraViewportState;
import core.utils.InputManager;
import core.utils.Point;
import core.utils.Vector2;
import core.utils.components.MissingComponentException;
import core.utils.components.draw.animation.Animation;
import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * The {@code EntityDebugRenderSystem} is responsible for rendering debug information
 * for various entities within the game world during runtime. This includes visualizing
 * their position, orientation, and relevant components such as collision bounds,
 * interaction ranges, and animations.
 *
 * <p>This system operates on the client-side and processes entities containing a
 * {@code PositionComponent}.
 */
public final class EntityDebugRenderSystem extends System {

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

  private static final ClientCursorAdapter CURSOR = new ClientCursorAdapter();

  public EntityDebugRenderSystem() {
    super(AuthoritativeSide.CLIENT, PositionComponent.class);
  }

  @Override
  public void execute() {}

  @Override
  public void render(float delta) {
    if (!DebugDrawSystem.isHudVisible()) {
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

    DebugDrawSystem.drawWorldCircleFill(
      position, POSITION_RADIUS, withAlpha(Color.ORANGE, alpha));

    if (!samePoint(position, centerPos)) {
      DebugDrawSystem.drawWorldCircleFill(
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
    DebugDrawSystem.drawWorldLine(position, end, Color.YELLOW);

    Point leftHead =
      new Point(
        end.x() - view.y() * ARROW_HEAD_SIZE,
        end.y() + view.x() * ARROW_HEAD_SIZE);

    Point rightHead =
      new Point(
        end.x() + view.y() * ARROW_HEAD_SIZE,
        end.y() - view.x() * ARROW_HEAD_SIZE);

    DebugDrawSystem.drawWorldLine(end, leftHead, Color.YELLOW);
    DebugDrawSystem.drawWorldLine(end, rightHead, Color.YELLOW);
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

    DebugDrawSystem.drawRectangleOutline(
      bottomLeft.x(), bottomLeft.y(), width, height, withAlpha(color, alpha));
  }

  private void drawInteractionRange(Entity entity, Point pos, float alpha) {
    InteractionComponent ic =
      entity
        .fetch(InteractionComponent.class)
        .orElseThrow(() -> MissingComponentException.build(entity, InteractionComponent.class));

    float radius = ic.interactions().interact().range();

    DebugDrawSystem.drawWorldCircleOutline(
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

    DebugDrawSystem.drawRectangleOutline(
      x, y, width, height, withAlpha(Color.GREEN, alpha));
  }

  private boolean isEntityHovered(Entity entity, PositionComponent pc) {
    int mouseX = CURSOR.screenX();
    int mouseY = CURSOR.screenY();

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
      .orElseGet(() -> pc.position().distance(CURSOR.world()) < HOVER_RADIUS);
  }

  private void drawEntityInfo(Entity entity, PositionComponent pc) {
    String text = buildInfoText(entity, pc);
    String[] lines = text.split("\\R");

    int longestLineLength = Arrays.stream(lines).mapToInt(String::length).max().orElse(0);
    int bgWidth = Math.max(160, longestLineLength * 7 + INFO_PADDING * 2);
    int bgHeight = lines.length * INFO_LINE_HEIGHT + INFO_PADDING * 2;

    Point anchor = CameraViewportState.worldToScreen(pc.position());
    Point topLeft =
      new Point(
        anchor.x() + INFO_OFFSET_X,
        anchor.y() - bgHeight - INFO_OFFSET_Y);

    DebugDrawSystem.drawScreenRectangle(
      topLeft,
      bgWidth,
      bgHeight,
      INFO_BACKGROUND,
      INFO_OUTLINE);

    float textX = topLeft.x() + INFO_PADDING;
    float textY = topLeft.y() + INFO_PADDING + 10;

    for (String line : lines) {
      DebugDrawSystem.drawText(line, new Point(textX, textY), Color.WHITE);
      textY += INFO_LINE_HEIGHT;
    }
  }

  private String buildInfoText(Entity entity, PositionComponent pc) {
    Point position = pc.position();

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

    entity
      .fetch(SoundComponent.class)
      .ifPresent(sc -> info.append("Sound Instances: ").append(sc.sounds().size()).append("\n"));

    entity
      .fetch(InventoryComponent.class)
      .ifPresent(
        ic ->
          info.append("Inventory: ")
            .append(Arrays.stream(ic.items()).filter(Objects::nonNull).count())
            .append("/")
            .append(ic.items().length)
            .append(" items\n"));

    entity
      .fetch(AIComponent.class)
      .ifPresent(
        ai -> info.append("AI State: ").append(ai.active() ? "Active" : "Inactive").append("\n"));

    entity
      .fetch(PlayerComponent.class)
      .ifPresent(
        player ->
          info.append("Player: ")
            .append(player.playerName())
            .append(player.isLocal() ? " (LOCAL)" : " (REMOTE)")
            .append("\n"));

    if (InputManager.isKeyPressed(Keys.SHIFT_LEFT) || InputManager.isKeyPressed(Keys.SHIFT_RIGHT)) {
      List<String> componentNames =
        entity
          .componentStream()
          .map(comp -> comp.getClass().getSimpleName())
          .sorted(String::compareToIgnoreCase)
          .toList();

      if (!componentNames.isEmpty()) {
        info.append("Components:\n");
        for (String componentName : componentNames) {
          info.append("  - ").append(componentName).append("\n");
        }
      }
    }

    return info.toString().trim();
  }

  private static boolean samePoint(Point a, Point b) {
    return Math.abs(a.x() - b.x()) < 0.0001f && Math.abs(a.y() - b.y()) < 0.0001f;
  }

  private static Color withAlpha(Color color, float alpha) {
    return new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.round(alpha * 255f));
  }
}
