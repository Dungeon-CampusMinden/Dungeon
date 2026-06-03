package contrib.modules.puzzle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import core.Game;
import core.utils.components.draw.TextureMap;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import core.utils.logging.DungeonLogger;

/**
 * Generates the per-piece texture fragments of a {@link Puzzle} and registers them in the {@link
 * TextureMap} under deterministic virtual paths ({@code @gen/puzzle/<puzzleId>/<pieceIndex>.png}).
 *
 * <p>The same {@link Puzzle} instance produces the exact same set of textures on every machine
 * because the slicing is fully driven by the {@link Puzzle#seed() seed} + image dimensions. This
 * lets the multiplayer server reference a piece by its generated path while every client lazily
 * materializes the matching texture as soon as it needs to draw the piece.
 *
 * <p>This class is a no-op on a headless application: the server never needs the textures
 * themselves (it only needs the items to carry the {@code @gen/...} paths to the clients).
 */
public final class PuzzleTextureGenerator {

  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(PuzzleTextureGenerator.class);

  private PuzzleTextureGenerator() {}

  /**
   * Returns the virtual {@link TextureMap} path of a single puzzle piece.
   *
   * @param puzzleId parent puzzle id ({@link Puzzle#id()})
   * @param pieceIndex 0-based piece index
   * @return path under which the rendered piece texture is registered
   */
  public static String texturePath(String puzzleId, int pieceIndex) {
    return "@gen/puzzle/" + puzzleId + "/" + pieceIndex + ".png";
  }

  /**
   * Convenience overload of {@link #texturePath(String, int)} returning an {@link IPath}.
   *
   * @param puzzleId parent puzzle id
   * @param pieceIndex piece index
   * @return path wrapped in a {@link SimpleIPath}
   */
  public static IPath texturePathFor(String puzzleId, int pieceIndex) {
    return new SimpleIPath(texturePath(puzzleId, pieceIndex));
  }

  /**
   * Ensures every piece texture of the given puzzle is registered in the {@link TextureMap}.
   *
   * <p>If the textures are already present this call is a cheap no-op. Otherwise the source image
   * is loaded once, the polygons are (re)computed from {@link Puzzle#seed()} + image dimensions,
   * and one premultiplied-alpha texture is registered per piece.
   *
   * <p>On a headless application (typically the dedicated server) this method does nothing: the
   * server only needs the items to carry the {@code @gen/...} paths and lets clients materialize
   * the actual textures locally.
   *
   * @param puzzle the puzzle to (re)materialize textures for
   */
  public static void ensureRegistered(Puzzle puzzle) {
    if (puzzle == null) return;
    if (Game.isHeadless()) return;

    String firstPath = texturePath(puzzle.id(), 0);
    if (TextureMap.instance().containsKey(firstPath)
        && puzzle.polygons().size() == puzzle.pieceCount()) {
      return;
    }

    FileHandle fh = Gdx.files.internal(puzzle.imagePath().pathString());
    if (!fh.exists()) fh = Gdx.files.local(puzzle.imagePath().pathString());
    if (!fh.exists()) {
      LOGGER.warn(
          "PuzzleTextureGenerator: source image not found for puzzle '{}': {}",
          puzzle.id(),
          puzzle.imagePath());
      return;
    }

    Pixmap src;
    try {
      src = new Pixmap(fh);
    } catch (RuntimeException ex) {
      LOGGER.warn(
          "PuzzleTextureGenerator: failed to load source pixmap '{}': {}",
          puzzle.imagePath(),
          ex.getMessage());
      return;
    }

    try {
      int w = src.getWidth();
      int h = src.getHeight();
      if (puzzle.polygons().isEmpty() || puzzle.polygons().size() != puzzle.pieceCount()) {
        puzzle.regenerate(puzzle.seed(), w, h);
      }

      for (int i = 0; i < puzzle.pieceCount(); i++) {
        SimpleIPath path = new SimpleIPath(texturePath(puzzle.id(), i));
        if (TextureMap.instance().containsKey(path.pathString())) continue;

        float[] poly = puzzle.polygons().get(i);
        int[] bb = PuzzleSlicer.boundingBox(poly, w, h);
        int bbX = bb[0];
        int bbY = bb[1];
        int bbW = Math.max(1, bb[2] - bb[0]);
        int bbH = Math.max(1, bb[3] - bb[1]);

        Pixmap dst = new Pixmap(bbW, bbH, Pixmap.Format.RGBA8888);
        dst.setBlending(Pixmap.Blending.None);
        dst.setColor(0f, 0f, 0f, 0f);
        dst.fill();
        for (int y = 0; y < bbH; y++) {
          int sy = bbY + y;
          if (sy < 0 || sy >= h) continue;
          for (int x = 0; x < bbW; x++) {
            int sx = bbX + x;
            if (sx < 0 || sx >= w) continue;
            if (PuzzleSlicer.contains(poly, sx + 0.5f, sy + 0.5f)) {
              dst.drawPixel(x, y, src.getPixel(sx, sy));
            }
          }
        }
        // NOTE: TextureMap#putPixmap takes ownership of the passed pixmap and disposes it
        // internally after copying into a premultiplied-alpha texture, so we must NOT dispose
        // `dst` ourselves here (doing so would double-free and throw "Pixmap already disposed!").
        TextureMap.instance().putPixmap(path, dst, false);
      }
    } finally {
      src.dispose();
    }
  }

  /**
   * Removes every generated piece texture of the given puzzle id from the {@link TextureMap}, so
   * future {@link #ensureRegistered(Puzzle)} calls will rebuild them from scratch (useful e.g. when
   * the debug UI re-rolls the slicing seed).
   *
   * @param puzzleId puzzle id whose generated textures should be discarded
   * @param pieceCount number of pieces to discard
   */
  public static void unregister(String puzzleId, int pieceCount) {
    if (Game.isHeadless()) return;
    for (int i = 0; i < pieceCount; i++) {
      String key = texturePath(puzzleId, i);
      var existing = TextureMap.instance().remove(key);
      if (existing != null) {
        try {
          existing.dispose();
        } catch (RuntimeException ignored) {
          // best-effort cleanup
        }
      }
    }
  }
}
