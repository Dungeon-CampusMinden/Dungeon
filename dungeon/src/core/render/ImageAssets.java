package core.render;

import core.platform.Platform;
import core.utils.logging.DungeonLogger;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.*;

/**
 * Utility class for loading and caching image assets.
 *
 * <p>ImageAssets provides a centralized image loading system with intelligent path resolution
 * and caching. It handles the complexities of locating assets in different directory structures
 * that may result from various project configurations and build processes.
 *
 * <p>Key features:
 * <ul>
 *   <li>Loading images from the resource system with automatic caching
 *   <li>Intelligent path resolution with support for implicit file paths
 *   <li>Multiple candidate path generation for flexible asset locations
 *   <li>Graceful error handling with single-log-per-error to avoid spam
 *   <li>Support for common directory prefixes (assets/, dungeon/, etc.)
 * </ul>
 *
 * <p>Path resolution attempts multiple candidates automatically:
 * <ul>
 *   <li>Original normalized path
 *   <li>Paths with common prefixes stripped (assets/, dungeon/assets/, dungeon/)
 *   <li>Paths with common prefixes added (for different working directories)
 * </ul>
 *
 * <p>This class is not instantiable; all methods are static utilities.
 */
public final class ImageAssets {
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(ImageAssets.class);

  private static final Map<String, BufferedImage> CACHE = new HashMap<>();
  private static final Set<String> LOGGED = new HashSet<>();

  private ImageAssets() {}

  /**
   * Loads an image asset from the resource system, with caching and intelligent path resolution.
   *
   * <p>This method attempts to locate and load the image by:
   * <ol>
   *   <li>Resolving implicit file paths (e.g., "folder/image" → "folder/image.png")
   *   <li>Generating candidate paths to handle different directory structures
   *   <li>Checking the cache for previously loaded images
   *   <li>Trying each candidate path in order
   *   <li>Loading and caching the first successful image found
   * </ol>
   *
   * <p>Errors are logged at most once per candidate path to avoid spamming the log output during
   * repeated frames.
   *
   * <p>Null handling: Returns null if the input is null/blank or if the resource system is
   * unavailable. Also returns null if no matching image is found after trying all candidates.
   *
   * @param texturePathString the path to the image asset (e.g., "textures/player.png" or
   *     "textures/player")
   * @return the loaded BufferedImage, or null if not found or loading failed
   */
  public static BufferedImage get(final String texturePathString) {
    if (texturePathString == null || texturePathString.isBlank()) return null;
    if (Platform.resources() == null) return null;

    final String implicit = resolveImplicitFilePath(texturePathString);

    for (final String candidate : candidatePaths(implicit)) {
      if (candidate == null || candidate.isBlank()) continue;

      final BufferedImage cached = CACHE.get(candidate);
      if (cached != null) return cached;

      try {
        if (!Platform.resources().exists(candidate)) continue;

        try (InputStream in = Platform.resources().open(candidate)) {
          if (in == null) continue;
          final BufferedImage img = ImageIO.read(in);
          if (img == null) continue;

          CACHE.put(candidate, img);
          return img;
        }
      } catch (Exception e) {
        // log once per candidate to avoid spamming each frame
        if (LOGGED.add(candidate)) {
          LOGGER.warn("Failed to load image: " + candidate, e);
        }
      }
    }

    if (LOGGED.add(implicit)) {
      LOGGER.warn("Image not found (tried multiple prefixes): " + implicit);
    }
    return null;
  }

  private static List<String> candidatePaths(final String path) {
    final String p0 = normalize(path);

    final LinkedHashSet<String> out = new LinkedHashSet<>();
    out.add(p0);

    // Strip common prefixes (Gradle often exposes assets/ content at classpath root).
    if (p0.startsWith("assets/")) out.add(p0.substring("assets/".length()));
    if (p0.startsWith("dungeon/assets/")) out.add(p0.substring("dungeon/assets/".length()));
    if (p0.startsWith("dungeon/")) out.add(p0.substring("dungeon/".length()));

    // Also try adding prefixes (when running from repo root / different workingDir).
    if (!p0.startsWith("assets/")) out.add("assets/" + p0);
    if (!p0.startsWith("dungeon/assets/")) out.add("dungeon/assets/" + p0);
    if (!p0.startsWith("dungeon/")) out.add("dungeon/" + p0);

    return new ArrayList<>(out);
  }

  private static String normalize(String p) {
    if (p == null) return null;
    p = p.replace('\\', '/');
    while (p.startsWith("./")) p = p.substring(2);
    while (p.startsWith("/")) p = p.substring(1);
    return p;
  }


  /**
   * Resolves implicit file paths to explicit image file paths.
   *
   * <p>This method handles implicit image paths (directories or base names without extension)
   * by automatically appending the ".png" extension. Explicit paths (already containing a file
   * extension) are returned unchanged.
   *
   * <p>Transformation examples:
   * <ul>
   *   <li>"textures/player" → "textures/player/player.png"
   *   <li>"textures/player/" → "textures/player/player.png"
   *   <li>"textures/player.png" → "textures/player.png" (unchanged)
   *   <li>"textures/player.jpg" → "textures/player.jpg" (unchanged)
   * </ul>
   *
   * <p>Null or empty inputs are returned unchanged. The path is normalized before processing.
   *
   * @param pathString the implicit or explicit image path to resolve
   * @return the explicit image file path with extension, or null/empty if input is null/empty
   */
  public static String resolveImplicitFilePath(String pathString) {
    if (pathString == null || pathString.isEmpty()) return pathString;

    pathString = normalize(pathString);

    if (pathString.matches(".*\\.(png|jpg|jpeg)$")) {
      return pathString;
    }

    String dir = pathString.replaceAll("/$", "");
    String baseName = dir.substring(dir.lastIndexOf('/') + 1);
    return dir + "/" + baseName + ".png";
  }
}
