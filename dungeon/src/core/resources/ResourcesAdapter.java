package core.resources;

import java.io.IOException;
import java.io.InputStream;

/**
 * Platform adapter interface for resource loading and access.
 *
 * <p>ResourcesAdapter abstracts resource access, allowing resources to be loaded from various
 * sources (classpath, file system, archive files, etc.) without coupling the engine to a specific
 * resource location strategy.
 *
 * <p>Key responsibilities:
 *
 * <ul>
 *   <li>Checking whether a resource exists at a given path
 *   <li>Opening input streams for reading resource content
 * </ul>
 *
 * <p>Implementations must handle path normalization and resolution according to their storage
 * backend.
 */
public interface ResourcesAdapter {

  /**
   * Checks whether a resource exists at the specified path.
   *
   * @param path the resource path to check
   * @return true if the resource exists and is accessible, false otherwise
   */
  boolean exists(String path);

  /**
   * Opens an input stream for reading the resource at the specified path.
   *
   * <p>The caller is responsible for closing the returned input stream.
   *
   * @param path the resource path to open
   * @return an InputStream for reading the resource content
   * @throws IOException if the resource does not exist, cannot be opened, or an I/O error occurs
   */
  InputStream open(String path) throws IOException;
}
