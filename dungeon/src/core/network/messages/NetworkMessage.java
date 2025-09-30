package core.network.messages;

import java.io.Serializable;

/**
 * Marker interface for all network messages.
 *
 * <p>All implementations must be Java-serializable for the current Netty prototype transport.
 */
public interface NetworkMessage extends Serializable {

  /**
   * Returns the schema version of this message.
   *
   * <p>Must match between client and server.
   *
   * <p>Increment when the wire format changes in a non-backwards-compatible way.
   *
   * @return the schema version
   */
  default int schemaVersion() {
    return 1;
  }
}
