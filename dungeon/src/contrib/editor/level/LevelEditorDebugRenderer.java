package contrib.editor.level;

import contrib.components.DecoComponent;
import core.Entity;
import core.Game;
import core.camera.CameraViewportState;
import core.components.DrawComponent;
import core.components.PlayerComponent;
import core.components.PositionComponent;
import core.game.render.image.ImageFrameResolver;
import core.game.render.sprite.effects.SpriteOutlineRenderer;
import core.level.DungeonLevel;
import core.level.Tile;
import core.utils.Point;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Optional;
import java.util.Set;

/**
 * This class is responsible for rendering debug information in the Level Editor.
 *
 * <p>It provides visualizations for level boundaries, tiles, and entities during a debugging
 * session. The main purpose of this renderer is to aid in development and debugging by providing
 * graphical insights into level geometry and entity placement.
 *
 * <p>The rendering includes:
 *
 * <ul>
 *   <li>Outlining the bounds of the dungeon level.
 *   <li>Debug representations for tiles within the dungeon.
 *   <li>Entity outlines based on their types (player, decoration, or generic).
 * </ul>
 *
 * <p>The rendering logic relies on a camera viewport for translating world coordinates into screen
 * coordinates and uses color coding to differentiate between debug elements.
 *
 * <p>Key rendering behaviors:
 * <li>Highlighting level bounds.
 * <li>Rendering outlines for tiles and debugging graphical properties.
 * <li>Rendering outlines or fallback rectangles for visible entities.
 *
 *     <p>Methods in this class are primarily invoked during debug mode to overlay helpful visual
 *     data over the game level.
 */
final class LevelEditorDebugRenderer {
  private static final Color LEVEL_BOUNDS_OUTLINE_COLOR = new Color(0, 255, 0, 77);

  private static final Color DEBUG_LEVEL_TILE_OUTLINE_COLOR = new Color(80, 140, 255, 150);
  private static final Color DEBUG_PLAYER_ENTITY_COLOR = Color.RED;
  private static final Color DEBUG_DECO_ENTITY_COLOR = Color.GREEN;
  private static final Color DEBUG_NORMAL_ENTITY_COLOR = Color.WHITE;

  private static final int DEBUG_ENTITY_INSET_PX = 2;

  void render(Graphics2D g, boolean layerDebugActive) {
    renderLevelBoundsOutline(g);

    if (layerDebugActive) {
      renderLayerDebug(g);
    }
  }

  private void renderLevelBoundsOutline(Graphics2D g) {
    CameraViewportState.Viewport view = CameraViewportState.get();
    if (view == null || view.tilePx() <= 0) {
      return;
    }

    currentDungeonLevel()
        .ifPresent(
            level -> {
              Tile[][] layout = level.layout();
              if (layout.length == 0 || layout[0].length == 0) {
                return;
              }

              int levelWidth = layout[0].length;
              int levelHeight = layout.length;

              drawWorldRectangleOutline(
                  g, 0f, 0f, levelWidth, levelHeight, LEVEL_BOUNDS_OUTLINE_COLOR);
            });
  }

  private void renderLayerDebug(Graphics2D g) {
    CameraViewportState.Viewport view = CameraViewportState.get();
    if (view == null || view.tilePx() <= 0) {
      return;
    }

    renderLayerDebugTiles(g);
    renderLayerDebugEntities(g, view);
  }

  private void renderLayerDebugTiles(Graphics2D g) {
    currentDungeonLevel()
        .ifPresent(
            level -> {
              Tile[][] layout = level.layout();
              for (int y = 0; y < layout.length; y++) {
                for (int x = 0; x < layout[y].length; x++) {
                  drawWorldRectangleOutline(g, x, y, 1.0f, 1.0f, DEBUG_LEVEL_TILE_OUTLINE_COLOR);
                }
              }
            });
  }

  private void renderLayerDebugEntities(Graphics2D g, CameraViewportState.Viewport view) {
    Game.levelEntities(Set.of(PositionComponent.class, DrawComponent.class))
        .forEach(
            entity -> {
              PositionComponent pc = entity.fetch(PositionComponent.class).orElse(null);
              DrawComponent dc = entity.fetch(DrawComponent.class).orElse(null);

              if (pc == null || dc == null || !dc.isVisible()) {
                return;
              }

              if (!tryDrawLayerDebugEntitySpriteOutline(g, entity, pc, dc, view)) {
                drawLayerDebugEntityFallbackRectangle(g, entity, pc, view);
              }
            });
  }

  private boolean tryDrawLayerDebugEntitySpriteOutline(
      Graphics2D g,
      Entity entity,
      PositionComponent pc,
      DrawComponent dc,
      CameraViewportState.Viewport view) {

    final core.utils.components.draw.animation.AnimationFrame frame;
    try {
      frame = dc.stateMachine().getFrame();
    } catch (Exception ignored) {
      return false;
    }

    BufferedImage sprite = ImageFrameResolver.toImage(frame);
    if (sprite == null || sprite.getWidth() <= 0 || sprite.getHeight() <= 0) {
      return false;
    }

    int tilePx = view.tilePx();

    int wPx = tilePx;
    int hPx = tilePx;

    try {
      float wWorld = dc.stateMachine().getWidth();
      float hWorld = dc.stateMachine().getHeight();

      if (wWorld > 0f) {
        wPx = Math.max(1, Math.round(wWorld * tilePx));
      }

      if (hWorld > 0f) {
        hPx = Math.max(1, Math.round(hWorld * tilePx));
      }
    } catch (Exception ignored) {
      // keep default tile-sized fallback dimensions
    }

    Point screenOrigin = CameraViewportState.worldToScreen(pc.position());

    int drawX = Math.round(screenOrigin.x() + (tilePx - wPx) / 2f);
    int drawY = Math.round(screenOrigin.y() + tilePx - hPx);

    SpriteOutlineRenderer.drawOutlinedSprite(
        g,
        sprite,
        drawX,
        drawY,
        wPx,
        hPx,
        debugEntityColor(entity),
        Math.max(1, DEBUG_ENTITY_INSET_PX));

    return true;
  }

  private void drawLayerDebugEntityFallbackRectangle(
      Graphics2D g, Entity entity, PositionComponent pc, CameraViewportState.Viewport view) {

    float insetWorld = DEBUG_ENTITY_INSET_PX / (float) view.tilePx();
    float sizeWorld = Math.max(0.05f, 1.0f - (2f * insetWorld));

    Point pos = pc.position();

    drawWorldRectangleOutline(
        g,
        pos.x() + insetWorld,
        pos.y() + insetWorld,
        sizeWorld,
        sizeWorld,
        debugEntityColor(entity));
  }

  private Color debugEntityColor(Entity entity) {
    if (entity.isPresent(PlayerComponent.class)) {
      return DEBUG_PLAYER_ENTITY_COLOR;
    }

    if (entity.isPresent(DecoComponent.class)) {
      return DEBUG_DECO_ENTITY_COLOR;
    }

    return DEBUG_NORMAL_ENTITY_COLOR;
  }

  private Optional<DungeonLevel> currentDungeonLevel() {
    return Game.currentLevel().filter(DungeonLevel.class::isInstance).map(DungeonLevel.class::cast);
  }

  private void drawWorldRectangleOutline(
      Graphics2D g, float worldX, float worldY, float worldWidth, float worldHeight, Color color) {
    CameraViewportState.Viewport view = CameraViewportState.get();
    if (view == null || view.tilePx() <= 0) {
      return;
    }

    Point screenTopLeft =
        CameraViewportState.worldToScreen(new Point(worldX, worldY + worldHeight - 1f));

    int px = Math.round(screenTopLeft.x());
    int py = Math.round(screenTopLeft.y());
    int pw = CameraViewportState.worldLengthToScreen(worldWidth);
    int ph = CameraViewportState.worldLengthToScreen(worldHeight);

    Color oldColor = g.getColor();
    g.setColor(color);
    g.drawRect(px, py, pw, ph);
    g.setColor(oldColor);
  }
}
