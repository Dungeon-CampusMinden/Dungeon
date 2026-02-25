package core.network.codec;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import core.network.messages.NetworkMessage;
import io.netty.buffer.ByteBuf;
import java.io.IOException;
import java.util.Objects;

/**
 * Protocol Buffer based network codec.
 *
 * <p>Frames are encoded with a 1-byte message type header followed by protobuf payload bytes.
 */
public final class NetworkCodec {

  private NetworkCodec() {}

  /**
   * Serializes a given network message into a byte array with a type header.
   *
   * @param message the message to serialize
   * @return a byte array representing the serialized message
   * @throws IOException if serialization fails
   */
  public static byte[] serialize(NetworkMessage message) throws IOException {
    Objects.requireNonNull(message, "message");
    Message proto = ConverterRegistry.global().toProto(message);
    return serializeProto(proto);
  }

  /**
   * Deserializes a {@link NetworkMessage} from a {@link ByteBuf}.
   *
   * @param buf the {@link ByteBuf} containing the serialized message data
   * @return the deserialized message
   * @throws IOException if parsing fails
   */
  public static NetworkMessage deserialize(ByteBuf buf) throws IOException {
    if (buf.readableBytes() < 1) {
      throw new IOException("Empty buffer; missing message type header.");
    }
    byte typeId = buf.readByte();
    byte[] payload = new byte[buf.readableBytes()];
    buf.readBytes(payload);
    Message proto = parseProto(typeId, payload);
    return ConverterRegistry.global().fromProto(proto);
  }

  private static byte[] serializeProto(Message message) throws IOException {
    try {
      byte typeId = ConverterRegistry.global().typeId(message);
      byte[] protoBytes = message.toByteArray();
      byte[] result = new byte[1 + protoBytes.length];
      result[0] = typeId;
      System.arraycopy(protoBytes, 0, result, 1, protoBytes.length);
      return result;
    } catch (IllegalArgumentException e) {
      throw new IOException("Failed to serialize message.", e);
    }
  }

  private static Message parseProto(byte typeId, byte[] payload) throws IOException {
    try {
      return ConverterRegistry.global().parse(typeId, payload);
    } catch (InvalidProtocolBufferException | IllegalArgumentException e) {
      throw new IOException("Failed to parse message type " + typeId + ".", e);
    }
  }
}
