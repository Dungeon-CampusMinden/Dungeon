package core.network.messages;

import java.io.Serializable;

/**
 * Marker interface for all network messages.
 *
 * <p>Messages are encoded with protobuf in {@link core.network.codec.NetworkCodec}. The {@link
 * Serializable} marker remains for legacy compatibility during migration.
 */
public interface NetworkMessage extends Serializable {}
