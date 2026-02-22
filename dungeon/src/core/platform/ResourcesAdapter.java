package core.platform;

import java.io.IOException;
import java.io.InputStream;

/** Backend-specific access to game resources (assets). */
public interface ResourcesAdapter {
  /** @return true if the resource can be located. */
  boolean exists(String path);

  /**
   * Open a resource as stream.
   *
   * @throws IOException if the resource cannot be opened
   */
  InputStream open(String path) throws IOException;
}
