package core.resources;

import core.platform.ResourcesAdapter;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * A ResourcesAdapter that composes multiple delegates, checking each in order until a resource is
 * found or all are exhausted.
 *
 * <p>CompositeResourcesAdapter allows flexible resource loading strategies by aggregating multiple
 * underlying adapters (e.g., classpath, filesystem, network). It checks for resource existence and
 * opens streams by delegating to each adapter in sequence, treating exceptions as "not found" and
 * continuing to the next.
 *
 * <p>Key features:
 * <ul>
 *   <li>Delegates resource existence checks and stream opening to multiple adapters
 *   <li>Handles exceptions gracefully, treating them as "not found" and continuing to the next
 *   <li>Provides meaningful error messages if all adapters fail to find the resource
 * </ul>
 *
 * <p>Null adapters are filtered out during construction. The order of delegates determines the
 * precedence of resource loading (first match wins).
 */
public final class CompositeResourcesAdapter implements ResourcesAdapter {
  private final List<ResourcesAdapter> delegates;

  /**
   * Constructs a CompositeResourcesAdapter that aggregates multiple {@link ResourcesAdapter}
   * instances. Each non-null adapter provided as a parameter is added to the internal delegate
   * list. Delegates are filtered to exclude null values and checked in the provided order when
   * performing resource operations.
   *
   * @param delegates an array of {@link ResourcesAdapter} instances to be composed. Null entries
   *                  are ignored.
   */
  public CompositeResourcesAdapter(ResourcesAdapter... delegates) {
    this.delegates = Arrays.stream(delegates).filter(Objects::nonNull).toList();
  }

  @Override
  public boolean exists(String path) {
    for (ResourcesAdapter d : delegates) {
      try {
        if (d.exists(path)) return true;
      } catch (Exception ignored) {
        // treat as not found
      }
    }
    return false;
  }

  @Override
  public InputStream open(String path) throws IOException {
    IOException last = null;

    for (ResourcesAdapter d : delegates) {
      try {
        if (!d.exists(path)) continue;
        return d.open(path);
      } catch (IOException e) {
        last = e;
      } catch (Exception ignored) {
        // ignore and continue
      }
    }

    if (last != null) throw last;
    throw new IOException("Resource not found: " + path);
  }
}
