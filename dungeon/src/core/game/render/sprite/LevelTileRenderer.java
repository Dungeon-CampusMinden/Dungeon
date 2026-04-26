package core.game.render.sprite;

import core.game.render.level.LevelEffectPipeline;
import core.game.render.level.LevelPassContext;
import core.level.Tile;
import core.level.elements.ILevel;
import core.level.utils.LevelElement;
import core.game.render.image.ImageAssets;
import core.utils.Time;
import de.gurkenlabs.litiengine.graphics.ImageRenderer;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * LevelTileRenderer is responsible for rendering the visible tiles of a level onto the screen.
 *
 * <p>It uses a tile image cache to optimize rendering performance by avoiding redundant image loading.
 *
 * <p>The renderer can operate in two modes: a direct rendering mode that draws tiles directly to the
 * Graphics2D context, and an effect pipeline mode that first renders the visible portion of the level
 * to an off-screen buffer, applies visual effects, and then draws the processed image to the screen.
 *
 * <p>The renderer determines which tiles are visible based on the provided CameraView and renders
 * them accordingly. If a tile has an associated texture, it will be drawn using that texture; otherwise.
 */
final class LevelTileRenderer {
  private final Map<String, BufferedImage> tileImageCache = new HashMap<>();

  void render(Graphics2D g, ILevel level, SpriteViewport view) {
    if (!LevelEffectPipeline.hasEnabledEffects()) {
      renderLevelTiles(g, level, view);
      return;
    }

    VisibleLevelBuffer levelBuffer = renderVisibleLevelToBuffer(level, view);
    if (levelBuffer == null) {
      return;
    }

    BufferedImage processed =
        LevelEffectPipeline.apply(levelBuffer.image(), levelBuffer.context(), Time.nowMs());
    g.drawImage(processed, levelBuffer.drawX(), levelBuffer.drawY(), null);
  }

  private void renderLevelTiles(Graphics2D g, ILevel level, SpriteViewport view) {
    final Tile[][] layout = level.layout();
    if (layout == null || layout.length == 0 || layout[0].length == 0) return;

    final int height = layout.length;
    final int width = layout[0].length;

    final int minX = clamp(view.minTileX(), 0, width - 1);
    final int maxX = clamp(view.maxTileX(), 0, width - 1);
    final int minY = clamp(view.minTileY(), 0, height - 1);
    final int maxY = clamp(view.maxTileY(), 0, height - 1);

    for (int y = minY; y <= maxY; y++) {
      for (int x = minX; x <= maxX; x++) {
        final Tile tile = layout[y][x];
        if (tile == null) continue;

        final int tilePx = view.tilePx();
        final int sx = x * tilePx;
        final int sy = (height - 1 - y) * tilePx;

        final LevelElement elem = tile.levelElement();
        BufferedImage img = imageForTile(tile);
        if (img != null) {
          drawTileImage(g, img, sx, sy, tilePx);
        } else {
          g.setColor(colorFor(elem));
          g.fillRect(sx, sy, tilePx, tilePx);
        }
      }
    }
  }

  private VisibleLevelBuffer renderVisibleLevelToBuffer(ILevel level, SpriteViewport view) {
    final Tile[][] layout = level.layout();
    if (layout == null || layout.length == 0 || layout[0].length == 0) {
      return null;
    }

    final int height = layout.length;
    final int width = layout[0].length;

    final int minX = clamp(view.minTileX(), 0, width - 1);
    final int maxX = clamp(view.maxTileX(), 0, width - 1);
    final int minY = clamp(view.minTileY(), 0, height - 1);
    final int maxY = clamp(view.maxTileY(), 0, height - 1);

    if (minX > maxX || minY > maxY) {
      return null;
    }

    final int tilePx = view.tilePx();
    final int bufferWidth = Math.max(1, (maxX - minX + 1) * tilePx);
    final int bufferHeight = Math.max(1, (maxY - minY + 1) * tilePx);

    BufferedImage buffer =
        new BufferedImage(bufferWidth, bufferHeight, BufferedImage.TYPE_INT_ARGB);
    Graphics2D bg = buffer.createGraphics();

    try {
      bg.setRenderingHint(
          RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

      for (int y = minY; y <= maxY; y++) {
        for (int x = minX; x <= maxX; x++) {
          final Tile tile = layout[y][x];
          if (tile == null) {
            continue;
          }

          final int sx = (x - minX) * tilePx;
          final int sy = (maxY - y) * tilePx;

          final LevelElement elem = tile.levelElement();
          BufferedImage img = imageForTile(tile);
          if (img != null) {
            drawTileImage(bg, img, sx, sy, tilePx);
          } else {
            bg.setColor(colorFor(elem));
            bg.fillRect(sx, sy, tilePx, tilePx);
          }
        }
      }
    } finally {
      bg.dispose();
    }

    int drawX = minX * tilePx;
    int drawY = (height - 1 - maxY) * tilePx;
    return new VisibleLevelBuffer(buffer, drawX, drawY, new LevelPassContext(minX, maxY, tilePx));
  }

  private void drawTileImage(Graphics2D g, BufferedImage img, int sx, int sy, int tilePx) {
    if (img.getWidth() <= 0 || img.getHeight() <= 0) return;
    double scaleX = tilePx / (double) img.getWidth();
    double scaleY = tilePx / (double) img.getHeight();
    ImageRenderer.renderScaled(g, img, sx, sy, scaleX, scaleY);
  }

  private BufferedImage imageForTile(Tile t) {
    if (t == null || t.texturePath() == null) return null;
    String raw = t.texturePath().pathString();
    if (raw == null || raw.isBlank()) return null;

    String key = ImageAssets.resolveImplicitFilePath(raw);

    if (tileImageCache.containsKey(key)) {
      return tileImageCache.get(key); // can be null -> cached miss
    }

    BufferedImage img = ImageAssets.get(raw);
    tileImageCache.put(key, img);
    return img;
  }

  private static int clamp(int v, int min, int max) {
    return Math.max(min, Math.min(max, v));
  }

  private static Color colorFor(LevelElement elem) {
    if (elem == null) return Color.DARK_GRAY;
    return switch (elem) {
      case FLOOR -> new Color(40, 40, 40);
      case WALL -> new Color(90, 90, 90);
      case HOLE -> new Color(10, 10, 10);
      default -> new Color(60, 60, 60);
    };
  }

  private record VisibleLevelBuffer(
      BufferedImage image, int drawX, int drawY, LevelPassContext context) {}
}
