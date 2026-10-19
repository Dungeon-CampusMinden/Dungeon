package feature.puzzle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import engine.Game;
import engine.utils.components.draw.TextureMap;
import engine.utils.components.draw.animation.SpritesheetConfig;
import engine.utils.components.path.IPath;
import engine.utils.components.path.SimpleIPath;
import engine.utils.logging.DungeonLogger;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

/**
 * Generates the per-piece texture fragments of a {@link Puzzle} and registers them in the {@link
 * TextureMap} under deterministic virtual paths ({@code @gen/puzzle/<puzzleId>/<pieceIndex>.png}).
 *
 * <p>The same {@link Puzzle} instance produces the exact same set of textures on every machine
 * because the slicing is fully driven by the {@link Puzzle#seed() seed} + image dimensions. This
 * lets the multiplayer server reference a piece by its generated path while every client lazily
 * materializes the matching texture as soon as it needs to draw the piece.
 *
 * <p>Headless applications only read image dimensions to size world items; texture generation
 * remains client-side.
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

  static SpritesheetConfig pieceSpritesheetConfig(
      IPath imagePath, int pieceCount, long seed, int pieceIndex) {
    FileHandle source = sourceImage(imagePath);
    try (InputStream stream = source.read();
        ImageInputStream input = ImageIO.createImageInputStream(stream)) {
      if (input == null) {
        throw new IllegalStateException("Cannot read puzzle image dimensions: " + imagePath);
      }
      var readers = ImageIO.getImageReaders(input);
      if (!readers.hasNext()) {
        throw new IllegalArgumentException("Unsupported puzzle image: " + imagePath);
      }
      ImageReader reader = readers.next();
      try {
        reader.setInput(input, true, true);
        int width = reader.getWidth(0);
        int height = reader.getHeight(0);
        float[] polygon = PuzzleSlicer.slice(width, height, pieceCount, seed).get(pieceIndex);
        int[] bounds = PuzzleSlicer.boundingBox(polygon, width, height);
        return new SpritesheetConfig(
            0, 0, 1, 1, Math.max(1, bounds[2] - bounds[0]), Math.max(1, bounds[3] - bounds[1]));
      } finally {
        reader.dispose();
      }
    } catch (IOException e) {
      throw new IllegalStateException("Cannot read puzzle image dimensions: " + imagePath, e);
    }
  }

  private static FileHandle sourceImage(IPath imagePath) {
    FileHandle source = Gdx.files.internal(imagePath.pathString());
    return source.exists() ? source : Gdx.files.local(imagePath.pathString());
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

    FileHandle fh = sourceImage(puzzle.imagePath());
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
