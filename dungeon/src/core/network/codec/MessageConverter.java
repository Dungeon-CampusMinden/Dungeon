package core.network.codec;

import com.google.protobuf.Message;
import com.google.protobuf.Parser;
import core.network.messages.NetworkMessage;

/**
 * Converts between a domain {@link NetworkMessage} and its protobuf representation.
 *
 * @param <D> the domain message type
 * @param <P> the protobuf message type
 */
public interface MessageConverter<D extends NetworkMessage, P extends Message> {

  /**
   * Converts a domain message to its protobuf form.
   *
   * @param message the domain message; must not be null
   * @return the protobuf representation; must not be null
   */
  P toProto(D message);

  /**
   * Converts a protobuf message to its domain form.
   *
   * @param proto the protobuf message; must not be null
   * @return the domain representation; must not be null
   */
  D fromProto(P proto);

  /**
   * Returns the domain message class this converter handles.
   *
   * @return the supported domain type; must not be null
   */
  Class<D> domainType();

  /**
   * Returns the protobuf message class this converter handles.
   *
   * @return the supported protobuf type; must not be null
   */
  Class<P> protoType();

  /**
   * Returns the protobuf parser used to deserialize this message type.
   *
   * @return the protobuf parser; must not be null
   */
  Parser<P> parser();

  /**
   * Returns the 1-byte wire type identifier.
   *
   * @return the wire type identifier
   */
  byte wireTypeId();
}
