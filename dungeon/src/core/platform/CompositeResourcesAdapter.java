package core.platform;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Tries multiple resource backends in order (e.g. classpath first, filesystem fallback).
 */
public final class CompositeResourcesAdapter implements ResourcesAdapter {
  private final List<ResourcesAdapter> delegates;

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
