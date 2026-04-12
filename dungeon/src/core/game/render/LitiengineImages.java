package core.game.render;

import core.platform.Platform;
import core.utils.logging.DungeonLogger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.*;

/**
 * Loads images through the engine-agnostic Platform.resources() abstraction.
 * Adds a small path-resolution layer to survive different workingDirs and
 * "assets/" prefix conventions.
 */
public final class LitiengineImages {
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(LitiengineImages.class);

  private static final Map<String, BufferedImage> CACHE = new HashMap<>();
  private static final Set<String> LOGGED = new HashSet<>();

  private LitiengineImages() {}

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
   * Matches the "folder name implies png" convention:
   * - "character/wizard/" -> "character/wizard/wizard.png"
   * - "character/wizard"  -> "character/wizard/wizard.png"
   * - "foo.png"           -> "foo.png"
   */
  static String resolveImplicitFilePath(String pathString) {
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
