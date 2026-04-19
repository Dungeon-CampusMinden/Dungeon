package core.resources;

import core.platform.adapters.ResourcesAdapter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * A ResourcesAdapter implementation that loads resources from the Java classpath.
 *
 * <p>ClasspathResourcesAdapter provides access to resources packaged within the application
 * (typically in JAR files or on the build classpath). It uses the context ClassLoader to locate
 * and open resources by their path.
 *
 * <p>Key features:
 * <ul>
 *   <li>Checks for resource existence using ClassLoader resource lookup
 *   <li>Opens input streams for resource reading
 *   <li>Normalizes paths to remove leading slashes (ClassLoader requirement)
 *   <li>Provides meaningful error messages for missing resources
 * </ul>
 *
 * <p>Path normalization ensures compatibility with ClassLoader semantics: paths should not
 * start with "/". Null or blank paths are rejected.
 */
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
