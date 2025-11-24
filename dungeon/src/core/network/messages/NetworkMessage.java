package core.network.messages;

import java.io.Serializable;

/**
 * Marker interface for all network messages.
 *
 * <p>All implementations must be Java-serializable for the current Netty prototype transport.
 */
public interface NetworkMessage extends Serializable {}
