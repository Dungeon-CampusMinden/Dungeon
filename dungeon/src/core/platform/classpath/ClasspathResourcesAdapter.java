package core.platform.classpath;

import core.platform.ResourcesAdapter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public final class ClasspathResourcesAdapter implements ResourcesAdapter {

  private static String normalize(String path) {
    if (path == null) return null;
    // ClassLoader resources should not start with "/"
    return path.startsWith("/") ? path.substring(1) : path;
  }

  @Override
  public boolean exists(String path) {
    String p = normalize(path);
    if (p == null || p.isBlank()) return false;
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    URL url = cl.getResource(p);
    return url != null;
  }

  @Override
  public InputStream open(String path) throws IOException {
    String p = normalize(path);
    if (p == null || p.isBlank()) throw new IOException("Invalid resource path: " + path);

    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    InputStream in = cl.getResourceAsStream(p);
    if (in == null) {
      throw new IOException("Resource not found on classpath: " + path);
    }
    return in;
  }
}
