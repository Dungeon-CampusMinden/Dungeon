package core.game.render.image;

import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.function.Function;

/**
 * Bounded, weakly keyed cache for derived image effect data.
 *
 * <p>Entries are grouped by the source image instance. The source image is held weakly, while
 * derived values are limited per source image and keyed by the current pixel signature plus the
 * effect parameters.
 *
 * <p>Reusing a {@link BufferedImage} instance with changed pixel data therefore produces a
 * different cache entry.
 *
 * @param <T> cached derived value type
 */
public final class ImageEffectCache<T> {
  private final int maxEntriesPerSource;
  private final Map<BufferedImage, LinkedHashMap<CacheKey, T>> entries = new WeakHashMap<>();

  /**
   * Creates a cache with a bounded number of derived values per source image.
   *
   * @param maxEntriesPerSource maximum cached variants per source image
   */
  public ImageEffectCache(final int maxEntriesPerSource) {
    if (maxEntriesPerSource <= 0) {
      throw new IllegalArgumentException("maxEntriesPerSource must be positive");
    }
    this.maxEntriesPerSource = maxEntriesPerSource;
  }

  /**
   * Returns the cached effect result for the source image and effect parameters, or computes and
   * stores a new result.
   *
   * @param source source image
   * @param effectKey key containing all effect parameters
   * @param builder function that creates the derived value
   * @return cached or newly computed derived value
   */
  public T getOrCompute(
      final BufferedImage source,
      final Object effectKey,
      final Function<BufferedImage, T> builder) {
    Objects.requireNonNull(source, "source");
    Objects.requireNonNull(effectKey, "effectKey");
    Objects.requireNonNull(builder, "builder");

    final CacheKey key = new CacheKey(ImageSignature.from(source), effectKey);

    synchronized (entries) {
      final Map<CacheKey, T> sourceEntries = entries.get(source);
      if (sourceEntries != null) {
        final T cached = sourceEntries.get(key);
        if (cached != null) {
          return cached;
        }
      }
    }

    final T computed = builder.apply(source);

    synchronized (entries) {
      final LinkedHashMap<CacheKey, T> sourceEntries =
          entries.computeIfAbsent(source, ignored -> newSourceEntries());
      final T cached = sourceEntries.get(key);
      if (cached != null) {
        return cached;
      }
      sourceEntries.put(key, computed);
      return computed;
    }
  }

  private LinkedHashMap<CacheKey, T> newSourceEntries() {
    return new LinkedHashMap<>(16, 0.75f, true) {
      @Override
      protected boolean removeEldestEntry(final Map.Entry<CacheKey, T> eldest) {
        return size() > maxEntriesPerSource;
      }
    };
  }

  private record CacheKey(ImageSignature sourceSignature, Object effectKey) {}

  private record ImageSignature(int width, int height, int type, long pixelsHash) {
    private static ImageSignature from(final BufferedImage source) {
      final int width = source.getWidth();
      final int height = source.getHeight();
      long hash = 0xcbf29ce484222325L;
      hash = mix(hash, width);
      hash = mix(hash, height);
      hash = mix(hash, source.getType());

      final int[] row = new int[width];
      for (int y = 0; y < height; y++) {
        source.getRGB(0, y, width, 1, row, 0, width);
        for (int x = 0; x < width; x++) {
          hash = mix(hash, row[x]);
        }
      }

      return new ImageSignature(width, height, source.getType(), hash);
    }

    private static long mix(final long hash, final int value) {
      return (hash ^ (value & 0xFFFFFFFFL)) * 0x100000001b3L;
    }
  }
}
